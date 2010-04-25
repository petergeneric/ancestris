/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genjreports.docs;

import genj.gedcom.Gedcom;
import genj.gedcom.Entity;
import genj.gedcom.Indi;
import genj.gedcom.Fam;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.util.swing.Action2;
import genj.util.swing.ButtonHelper;
import genj.util.Registry;
import genj.util.swing.*;
import genj.window.WindowManager;
import genj.gedcom.time.PointInTime;
import genj.util.WordBuffer;


import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;




public class SearchWidget extends JPanel {

    /** the registry we use */
    private EditDocsPanel mainPanel;
    private Registry registry;
    private Gedcom gedcom;

    private Property selectedProperty;

    private JTable table;
    private MyTableModel model;
    private SortableTableModel sorter;
    private int FS = 10;


    public SearchWidget(EditDocsPanel panel, Registry registry, Gedcom gedcom) {
        super();

        this.mainPanel = panel;
        this.registry = registry;
        this.gedcom = gedcom;

        // Set layout
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Create table
        model = new MyTableModel();
        sorter = new SortableTableModel(model);
        table = new JTable(sorter) {
           protected JTableHeader createDefaultTableHeader() {
              return new JTableHeader(columnModel) {
                  public String getToolTipText(MouseEvent e) {
                     return mainPanel.translate("Search_sorttip");
                     }
                 };
              }
            //Implement table cell tool tips.
           public String getToolTipText(MouseEvent e) {
              java.awt.Point p = e.getPoint();
              int rowIndex = rowAtPoint(p);
              int colIndex = columnAtPoint(p);
              if (colIndex != 9) {
                 Object value = getValueAt(rowIndex, colIndex);
                 if (value != null) return value.toString();
                 else return "";
                 }
              else return "";
              }
           };
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent event) {
                        selectedProperty = getRowProperty(table.getSelectedRow());
                        }
                    }
                );
        setColumnLayout(getSavedLayout());
        sorter.setTableHeader(table.getTableHeader());
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);


        // Panel for Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setMaximumSize(new Dimension(300, 16));
        ButtonHelper bh = new ButtonHelper().setContainer(actions);
        bh.create(new ActionOK());
        bh.create(new ActionClose());
        add(actions);

    }



    /** 
     * Get row entity pointer upon selection.
     */
    private Property getRowProperty(int i) {

       if (i<0) return null;
       boolean isIndi = false;

       String tag = (String)sorter.getValueAt(i,6);
       if      (tag.equals((new TagPath("INDI:BIRT")).getName())) { tag = "BIRT"; isIndi = true;  }
       else if (tag.equals((new TagPath("FAM:MARR")).getName()))  { tag = "MARR"; isIndi = false; }
       else if (tag.equals((new TagPath("INDI:DEAT")).getName())) { tag = "DEAT"; isIndi = true;  };

       Entity ent = null;
       Object value1 = sorter.getValueAt(i,7);
       Object value2 = sorter.getValueAt(i,8);
       if (isIndi && value1 != null) {
          ent = (Entity) value1;
          }
       if (!isIndi && value1 != null && value2 != null) {
          Indi husb = (Indi)value1;
          Indi wife = (Indi)value2;
          ent = HelperDocs.getFamily(husb, wife);
          }
       if (ent == null) return null;
       if (tag == null) return ent;
       Property prop = ent.getPropertyByPath(ent.getTag()+":"+tag);
       return prop;
    }

  /**
   * Return column layout - a string that can be used to return column widths and sorting
   */
  public String getColumnLayout() {

    // e.g. 4, 40, 60, 70, 48, 0, -1, 1, 1 
    // for a table with 4 columns and two sort directives

    SortableTableModel model = (SortableTableModel)table.getModel();
    TableColumnModel columns = table.getColumnModel();
    List directives = model.getDirectives();

    WordBuffer result = new WordBuffer(",");
    result.append(columns.getColumnCount());

    for (int c=0; c<columns.getColumnCount(); c++) 
      result.append(columns.getColumn(c).getWidth());

    for (int d=0;d<directives.size();d++) {
      SortableTableModel.Directive dir = (SortableTableModel.Directive)directives.get(d);
      result.append(dir.getColumn());
      result.append(dir.getDirection());
    }

    return result.toString();
  }

  /**
   * Set column layout
   */
  public void setColumnLayout(String layout) {

    SortableTableModel model = (SortableTableModel)table.getModel();
    TableColumnModel columns = table.getColumnModel();

    try {
      StringTokenizer tokens = new StringTokenizer(layout, ",");
      int n = Integer.parseInt(tokens.nextToken());
      if (n!=model.getColumnCount())
        return;

      for (int i=0;i<n;i++) {
        TableColumn col = columns.getColumn(i);
        int w = Integer.parseInt(tokens.nextToken());
        col.setWidth(w);
        col.setPreferredWidth(w);
      }

      model.cancelSorting();
      while (tokens.hasMoreTokens()) {
        int c = Integer.parseInt(tokens.nextToken());
        int d = Integer.parseInt(tokens.nextToken());
        model.setSortingStatus(c, d);
      }

    } catch (Throwable t) {
      // ignore
    }
  }

  /**
   * Return saved layout
   */
  public String getSavedLayout() {
    return registry.get("docs.search.layout", "");
  }

  /**
   * Saves layout
   */
  public void saveLayout() {
    registry.put("docs.search.layout", getColumnLayout());
    return;
  }

    /**
     * MODEL Class
     */
    class MyTableModel extends AbstractTableModel {

        private int NBCOL = 10;

        private String[] columnNames = {
                                         " " + mainPanel.translate("Search_repo"),
                                         " " + mainPanel.translate("Search_sour"),
                                         " " + mainPanel.translate("Search_page"),
                                         " " + mainPanel.translate("Search_ctry"),
                                         " " + mainPanel.translate("Search_city"),
                                         " " + mainPanel.translate("Search_date"),
                                         " " + mainPanel.translate("Search_even"),
                                         " " + mainPanel.translate("Search_name"),
                                         " " + mainPanel.translate("Search_spouse"),
                                         " " + mainPanel.translate("Search_id")
                                        };

        private Object[][] data;

        public MyTableModel() {
            data = getDataFromGedcom();
            return;
            }

        public int getColumnCount() {
            return columnNames.length;
            }
        public int getRowCount() {
            return data.length;
            }
        public String getColumnName(int col) {
            return columnNames[col];
            }
        public Object getValueAt(int row, int col) {
            return data[row][col];
            }

        private Object[][] getDataFromGedcom() {

           // Get data
           List<Object[]> list = new ArrayList();
           list.addAll(getRows(gedcom.getEntities(Gedcom.INDI, "INDI:NAME")));
           list.addAll(getRows(gedcom.getEntities(Gedcom.FAM, "FAM")));

           // Return data
           Object[][] data = new Object[list.size()][NBCOL];
           int i = 0;
           for (Iterator it3 = list.iterator(); it3.hasNext();) {
              Object[] r = (Object[]) it3.next();
              data[i] = r;
              i++;
              }
           return data;
           }

        private List getRows(Entity[] entities) {
           List<Object[]> tmpList = new ArrayList();

           for (int e = 0 ; e < entities.length ; e++) {
              Entity entity = (Entity) entities[e];
              List<Property> props = getEventProperties(entity);

              // loop
              for (Iterator it2 = props.iterator(); it2.hasNext();) {
                 Property p = (Property) it2.next();
                 Object[] row = new Object[NBCOL];
                 for (int i = 0 ; i < NBCOL ; i++) row[i] = null;

                 // basic stuff depending on indi or fam
                 row[9] = getId(entity);

                 if (entity instanceof Indi) {
                    row[7] = (Property)entity;
                    }

                 if (entity instanceof Fam) {
                    Fam fam = (Fam)entity;
                    Indi wife = fam.getWife();
                    if (wife != null) row[8] = (Property)wife;
                    Indi husband = fam.getHusband();
                    if (husband != null) row[7] = (Property)husband;
                    }

                 // get tag
                 row[6] = p.getPropertyName();

                 // date of event
                 String tag = p.getTag();
                 row[5] = p.getPropertyByPath(tag+":DATE");

                 // place of event
                 Property pPlace = p.getPropertyByPath(tag+":PLAC");
                 if (pPlace != null && pPlace instanceof PropertyPlace) {
                    row[4] = ((PropertyPlace)pPlace).getCity();
                    int nbJur = ((PropertyPlace)pPlace).getJurisdictions().length - 1;
                    if (nbJur < 0) nbJur = 0;
                    row[3] = ((PropertyPlace)pPlace).getJurisdiction(nbJur);
                    }

                 // source of related stuff
                 Property pSource = p.getPropertyByPath(tag+":SOUR");
                 if (pSource != null) {
                    Property pPage = pSource.getPropertyByPath("SOUR:PAGE");
                    if (pPage != null) row[2] = pPage;
                    if (pSource instanceof PropertyXRef) {
                       Entity ent = ((PropertyXRef)pSource).getTargetEntity();
                       if (ent != null) {
                          row[1] = ent;
                          Property repo = ent.getPropertyByPath("SOUR:REPO");
                          if (repo != null && repo instanceof PropertyXRef) {
                             Entity ent2 = ((PropertyXRef)repo).getTargetEntity();
                             if (ent2 != null) row[0] = ent2;
                             }
                          }
                       }
                    }

                 // add row
                 tmpList.add(row);
                 }
              }
           return tmpList;
           } // getRows

        private List<Property> getEventProperties(Property prop) {
           List<Property> props = new ArrayList();
           props.addAll(Arrays.asList(prop.getProperties("BIRT")));
           props.addAll(Arrays.asList(prop.getProperties("MARR")));
           props.addAll(Arrays.asList(prop.getProperties("DEAT")));
           return props;
           }

        private Integer getId(Entity ent) {
           Integer n = 0;
           if (ent == null) return n;
           n = HelperDocs.extractNumber(ent.getId());
           return n;
           }

    }




  /**
   * opens the document in the document window
   */
  private class ActionOK extends Action2 {
    private ActionOK() {
      setText("Afficher");
    }
    protected void execute() {
      if (selectedProperty == null) return;
      mainPanel.setPanel(selectedProperty);
    }
  }

  /**
   * closes the window
   */
  private class ActionClose extends Action2 {
    private ActionClose() {
      setText("Fermer");
    }
    protected void execute() {
      saveLayout();
      WindowManager.getInstance(getTarget()).close("docs.search");
    }
  }


} //SettingsWidget

