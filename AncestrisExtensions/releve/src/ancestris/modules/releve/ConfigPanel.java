/*
 * ConfigPanel.java
 *
 * Created on 1 avr. 2012, 10:25:12
 */

package ancestris.modules.releve;

import ancestris.app.TreeTopComponent;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.GedcomDirectory;
import ancestris.modules.releve.dnd.TreeViewDropTarget;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.ModelBirth;
import ancestris.modules.releve.model.ModelDeath;
import ancestris.modules.releve.model.ModelMisc;
import ancestris.modules.releve.model.ModelMarriage;
import genj.app.GedcomFileListener;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.tree.TreeView;
import genj.util.ChangeSupport;
import java.awt.Toolkit;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.netbeans.api.javahelp.Help;
import org.openide.util.HelpCtx;

/**
 *
 * @author Michel
 */
public class ConfigPanel extends javax.swing.JPanel implements TableModelListener, GedcomFileListener {

    protected ChangeSupport changeSupport = new ChangeSupport(this);
    DataManager dataManager;
    Gedcom gedcomCompletion = null;
    ReleveTopComponent topComponent ;

    /** Creates new form ConfigPanel */
    public ConfigPanel() {
        initComponents();
        birthNumber.setText("0");
        marriageNumber.setText("0");
        deathNumber.setText("0");
        miscNumber.setText("0");

        copyFreeComment.setSelected( Boolean.parseBoolean(NbPreferences.forModule(ReleveTopComponent.class).get("DefaultFreeCommentEnabled", "true")));

        // pour mettre à jour "isDirty"
        cityNameEntry.getDocument().addDocumentListener(changeSupport);
        cityCodeEntry.getDocument().addDocumentListener(changeSupport);
        stateEntry.getDocument().addDocumentListener(changeSupport);
        countyNameEntry.getDocument().addDocumentListener(changeSupport);
        countryEntry.getDocument().addDocumentListener(changeSupport);

        changeSupport.setChanged(false);
    }

    public void setModel(DataManager dataManager) {
        this.dataManager = dataManager;
        dataManager.getModel(DataManager.ModelType.birth).addTableModelListener(this);
        dataManager.getModel(DataManager.ModelType.marriage).addTableModelListener(this);
        dataManager.getModel(DataManager.ModelType.death).addTableModelListener(this);
        dataManager.getModel(DataManager.ModelType.misc).addTableModelListener(this);
    }

    public void setTopComponent (ReleveTopComponent topComponent) {
        this.topComponent = topComponent;

         Context context = GedcomDirectory.getInstance().getLastContext();
            if (context != null && context.getGedcom() != null) {
                gedcomCompletion = context.getGedcom();
                dataManager.addGedcomCompletion(gedcomCompletion);

                for (TreeTopComponent tc : AncestrisPlugin.lookupAll(TreeTopComponent.class)) {
                    if( tc.getContext().getGedcom().equals(gedcomCompletion)) {
                        TreeView view= (TreeView)tc.getView();
                        TreeViewDropTarget viewDropTarget = new TreeViewDropTarget();
                        viewDropTarget.createDropTarget(view);
                        //view.setTransferHandler(new RecordTransferHandle());
                    }
                }
            }
    }

    /**
     * enregistre les parametres de configuration
     * Cette methode est appelee par le TopCompoant avant la fermeture du composant
     */
    public void componentClosed() {
        NbPreferences.forModule(ReleveTopComponent.class).put("DefaultFreeCommentEnabled", String.valueOf(copyFreeComment.isSelected()));
    }


    public boolean getCopyFreeComment() {
        return copyFreeComment.isSelected();
    }

    public String getCityName() {
        return cityNameEntry.getText();
    }

    public String getCityCode() {
        return cityCodeEntry.getText();
    }

    public String getCountyName() {
        return countyNameEntry.getText();
    }

    public String getStateName() {
        return stateEntry.getText();
    }

    public String getCountryName() {
        return countryEntry.getText();
    }

    /**
     * retourne le lieu au format "cityName,cityCode,countyName,stateName,countryName"
     * @return
     */
    String getPlace() {
        if ( getCityName().isEmpty() && getCityCode().isEmpty() && getCountyName().isEmpty()
                && getStateName().isEmpty() && getCountryName().isEmpty()) {
            return "";
        } else {
            return getCityName()+ ","+getCityCode()+ ","+getCountyName()+ ","+getStateName()+ ","+getCountryName();
        }
    }

    /**
     * memorise la commune du releve
     * Cette methode est appelée quand on charge un fichier de relevé.
     * @param value
     */
    public void setPlace(String value) {
        String[] juridictions =  value.split(",");
        if (juridictions.length > 0 ) {
            cityNameEntry.setText(juridictions[0]);
        } else {
            cityNameEntry.setText("");
        }
        if (juridictions.length > 1 ) {
            cityCodeEntry.setText(juridictions[1]);
        } else {
            cityCodeEntry.setText("");
        }
        if (juridictions.length > 2 ) {
            countyNameEntry.setText(juridictions[2]);
        } else {
            countyNameEntry.setText("");
        }
        if (juridictions.length > 3 ) {
            stateEntry.setText(juridictions[3]);
        } else {
            stateEntry.setText("");
        }
        if (juridictions.length > 4 ) {
            countryEntry.setText(juridictions[4]);
        } else {
            countryEntry.setText("");
        }
    }

    public boolean isDirty() {
        return changeSupport.hasChanged();
    }

    public void resetDirty() {
        changeSupport.setChanged(false);
    }

    public boolean getDuplicateControl() {
        return jCheckBoxDuplicateRecord.isSelected();
    }

    public boolean getNewValueControlEnabled() {
        return jCheckBoxNewValueControl.isSelected();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Implement TableModelListener
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Mise a jour des statistiques
     * met a jour le nombre de releves chaque fois qu'un releve est ajouté ou supprimé
     * dans un modele.
     * @param e
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        if (e.getType() == TableModelEvent.INSERT || e.getType() == TableModelEvent.DELETE || e.getType() == TableModelEvent.ALL_COLUMNS) {
            TableModel model = (TableModel) e.getSource();
            if (model instanceof ModelBirth) {
                birthNumber.setText(String.valueOf(model.getRowCount()));
            } else if (model instanceof ModelMarriage) {
                marriageNumber.setText(String.valueOf(model.getRowCount()));
            } else if (model instanceof ModelDeath) {
                deathNumber.setText(String.valueOf(model.getRowCount()));
            } else if (model instanceof ModelMisc) {
                miscNumber.setText(String.valueOf(model.getRowCount()));
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        jTextArea1 = new javax.swing.JTextArea();
        jPanelCommand = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jButtonNew = new javax.swing.JButton();
        jButtonOpen = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButtonImport = new javax.swing.JButton();
        jButtonExport = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButtonHelp = new javax.swing.JButton();
        jButtonDemo = new javax.swing.JButton();
        placePanel = new javax.swing.JPanel();
        cityNameLabel = new javax.swing.JLabel();
        cityNameEntry = new javax.swing.JTextField();
        cityCodeLabel = new javax.swing.JLabel();
        cityCodeEntry = new javax.swing.JTextField();
        countyNameLabel = new javax.swing.JLabel();
        countyNameEntry = new javax.swing.JTextField();
        stateLabel = new javax.swing.JLabel();
        stateEntry = new javax.swing.JTextField();
        countryLabel = new javax.swing.JLabel();
        countryEntry = new javax.swing.JTextField();
        statisticPanel = new javax.swing.JPanel();
        birthLabel = new javax.swing.JLabel();
        marriageLabel = new javax.swing.JLabel();
        deathLabel = new javax.swing.JLabel();
        miscLabel = new javax.swing.JLabel();
        birthNumber = new javax.swing.JTextField();
        marriageNumber = new javax.swing.JTextField();
        deathNumber = new javax.swing.JTextField();
        miscNumber = new javax.swing.JTextField();
        OptionsPanel = new javax.swing.JPanel();
        jCheckBoxDuplicateRecord = new javax.swing.JCheckBox();
        jCheckBoxNewValueControl = new javax.swing.JCheckBox();
        jCheckBoxGedcomCompletion = new javax.swing.JCheckBox();
        copyFreeComment = new javax.swing.JCheckBox();
        fillerPanelHorizontal = new javax.swing.JPanel();
        fillerPanelVertical = new javax.swing.JPanel();

        setForeground(new java.awt.Color(200, 45, 45));
        setFocusTraversalPolicyProvider(true);
        setLayout(new java.awt.BorderLayout());

        jPanel2.setForeground(new java.awt.Color(200, 45, 45));
        jPanel2.setFocusTraversalPolicyProvider(true);
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jTextArea1.setBackground(javax.swing.UIManager.getDefaults().getColor("Button.background"));
        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setRows(3);
        jTextArea1.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.jTextArea1.text")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setAutoscrolls(false);
        jTextArea1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jTextArea1.setFocusable(false);
        jTextArea1.setMinimumSize(new java.awt.Dimension(200, 22));
        jTextArea1.setPreferredSize(new java.awt.Dimension(460, 58));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(jTextArea1, gridBagConstraints);

        jPanelCommand.setAutoscrolls(true);
        jPanelCommand.setPreferredSize(new java.awt.Dimension(400, 39));
        jPanelCommand.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jToolBar1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        jButtonNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/NewFile.png"))); // NOI18N
        jButtonNew.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.jButtonNew.text")); // NOI18N
        jButtonNew.setToolTipText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.jButtonNew.toolTipText")); // NOI18N
        jButtonNew.setFocusable(false);
        jButtonNew.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonNew.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNewActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonNew);

        jButtonOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/OpenFile.png"))); // NOI18N
        jButtonOpen.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.jButtonOpen.text")); // NOI18N
        jButtonOpen.setToolTipText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.jButtonOpen.toolTipText")); // NOI18N
        jButtonOpen.setFocusable(false);
        jButtonOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonOpen.setPreferredSize(new java.awt.Dimension(23, 23));
        jButtonOpen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonOpen);

        jButtonSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/SaveFile.png"))); // NOI18N
        jButtonSave.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.jButtonSave.text")); // NOI18N
        jButtonSave.setToolTipText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.jButtonSave.toolTipText")); // NOI18N
        jButtonSave.setFocusable(false);
        jButtonSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonSave);
        jToolBar1.add(jSeparator1);

        jButtonImport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/ImportFile16.png"))); // NOI18N
        jButtonImport.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.jButtonImport.text")); // NOI18N
        jButtonImport.setToolTipText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.jButtonImport.toolTipText")); // NOI18N
        jButtonImport.setFocusable(false);
        jButtonImport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonImport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonImportActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonImport);

        jButtonExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/ExportFile16.png"))); // NOI18N
        jButtonExport.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.jButtonExport.text")); // NOI18N
        jButtonExport.setToolTipText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.jButtonExport.toolTipText")); // NOI18N
        jButtonExport.setFocusable(false);
        jButtonExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonExport.setPreferredSize(new java.awt.Dimension(23, 23));
        jButtonExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExportActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonExport);
        jToolBar1.add(jSeparator2);

        jButtonHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/information.png"))); // NOI18N
        jButtonHelp.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.jButtonHelp.text_1")); // NOI18N
        jButtonHelp.setToolTipText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.jButtonHelp.toolTipText")); // NOI18N
        jButtonHelp.setFocusable(false);
        jButtonHelp.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jButtonHelp.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jButtonHelp.setPreferredSize(new java.awt.Dimension(30, 25));
        jButtonHelp.setRequestFocusEnabled(false);
        jButtonHelp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHelpActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonHelp);

        jPanelCommand.add(jToolBar1);

        jButtonDemo.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.jButtonDemo.text")); // NOI18N
        jButtonDemo.setToolTipText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.jButtonDemo.toolTipText")); // NOI18N
        jButtonDemo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDemoActionPerformed(evt);
            }
        });
        jPanelCommand.add(jButtonDemo);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(jPanelCommand, gridBagConstraints);

        placePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.placePanel.border.title"))); // NOI18N
        placePanel.setPreferredSize(new java.awt.Dimension(460, 150));
        placePanel.setRequestFocusEnabled(false);
        placePanel.setLayout(new java.awt.GridBagLayout());

        cityNameLabel.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.cityNameLabel.text")); // NOI18N
        cityNameLabel.setFocusable(false);
        cityNameLabel.setPreferredSize(null);
        cityNameLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        placePanel.add(cityNameLabel, gridBagConstraints);

        cityNameEntry.setMinimumSize(new java.awt.Dimension(50, 20));
        cityNameEntry.setName("cityNameEntry"); // NOI18N
        cityNameEntry.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        placePanel.add(cityNameEntry, gridBagConstraints);

        cityCodeLabel.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.cityCodeLabel.text")); // NOI18N
        cityCodeLabel.setFocusable(false);
        cityCodeLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        placePanel.add(cityCodeLabel, gridBagConstraints);

        cityCodeEntry.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.cityCodeEntry.text")); // NOI18N
        cityCodeEntry.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        placePanel.add(cityCodeEntry, gridBagConstraints);

        countyNameLabel.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.countyNameLabel.text")); // NOI18N
        countyNameLabel.setFocusable(false);
        countyNameLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        placePanel.add(countyNameLabel, gridBagConstraints);

        countyNameEntry.setMinimumSize(new java.awt.Dimension(50, 20));
        countyNameEntry.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.ipady = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        placePanel.add(countyNameEntry, gridBagConstraints);

        stateLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        stateLabel.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.stateLabel.text")); // NOI18N
        stateLabel.setFocusable(false);
        stateLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        stateLabel.setPreferredSize(null);
        stateLabel.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        placePanel.add(stateLabel, gridBagConstraints);

        stateEntry.setMinimumSize(new java.awt.Dimension(50, 20));
        stateEntry.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        placePanel.add(stateEntry, gridBagConstraints);

        countryLabel.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.countryLabel.text")); // NOI18N
        countryLabel.setFocusable(false);
        countryLabel.setPreferredSize(null);
        countryLabel.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        placePanel.add(countryLabel, gridBagConstraints);
        countryLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.countryLabel.AccessibleContext.accessibleName")); // NOI18N

        countryEntry.setMinimumSize(new java.awt.Dimension(50, 20));
        countryEntry.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        placePanel.add(countryEntry, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(placePanel, gridBagConstraints);

        statisticPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.statisticPanel.border.title"))); // NOI18N
        statisticPanel.setPreferredSize(new java.awt.Dimension(460, 80));
        statisticPanel.setRequestFocusEnabled(false);
        statisticPanel.setLayout(new java.awt.GridLayout(2, 4, 2, 2));

        birthLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        birthLabel.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.birthLabel.text")); // NOI18N
        birthLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        statisticPanel.add(birthLabel);

        marriageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        marriageLabel.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.marriageLabel.text")); // NOI18N
        marriageLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        statisticPanel.add(marriageLabel);

        deathLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        deathLabel.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.deathLabel.text")); // NOI18N
        deathLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        statisticPanel.add(deathLabel);

        miscLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        miscLabel.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.miscLabel.text")); // NOI18N
        miscLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        statisticPanel.add(miscLabel);

        birthNumber.setEditable(false);
        birthNumber.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        birthNumber.setText("0");
        birthNumber.setBorder(null);
        birthNumber.setPreferredSize(null);
        statisticPanel.add(birthNumber);

        marriageNumber.setEditable(false);
        marriageNumber.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        marriageNumber.setText("0");
        marriageNumber.setBorder(null);
        marriageNumber.setPreferredSize(null);
        statisticPanel.add(marriageNumber);

        deathNumber.setEditable(false);
        deathNumber.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        deathNumber.setText("0");
        deathNumber.setBorder(null);
        deathNumber.setPreferredSize(null);
        statisticPanel.add(deathNumber);

        miscNumber.setEditable(false);
        miscNumber.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        miscNumber.setText("0");
        miscNumber.setBorder(null);
        miscNumber.setPreferredSize(null);
        miscNumber.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miscNumberActionPerformed(evt);
            }
        });
        statisticPanel.add(miscNumber);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(statisticPanel, gridBagConstraints);

        OptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.OptionsPanel.border.title"))); // NOI18N
        OptionsPanel.setPreferredSize(new java.awt.Dimension(460, 119));
        OptionsPanel.setLayout(new java.awt.GridBagLayout());

        jCheckBoxDuplicateRecord.setSelected(true);
        jCheckBoxDuplicateRecord.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.jCheckBoxDuplicateRecord.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        OptionsPanel.add(jCheckBoxDuplicateRecord, gridBagConstraints);

        jCheckBoxNewValueControl.setSelected(true);
        jCheckBoxNewValueControl.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.jCheckBoxNewValueControl.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        OptionsPanel.add(jCheckBoxNewValueControl, gridBagConstraints);
        jCheckBoxNewValueControl.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.jCheckBox1.AccessibleContext.accessibleName")); // NOI18N

        jCheckBoxGedcomCompletion.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.jCheckBoxGedcomCompletion.text")); // NOI18N
        jCheckBoxGedcomCompletion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxGedcomCompletionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        OptionsPanel.add(jCheckBoxGedcomCompletion, gridBagConstraints);

        copyFreeComment.setSelected(true);
        copyFreeComment.setText(org.openide.util.NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.copyFreeComment.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        OptionsPanel.add(copyFreeComment, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(OptionsPanel, gridBagConstraints);

        fillerPanelHorizontal.setEnabled(false);
        fillerPanelHorizontal.setFocusable(false);
        fillerPanelHorizontal.setRequestFocusEnabled(false);
        fillerPanelHorizontal.setVerifyInputWhenFocusTarget(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(fillerPanelHorizontal, gridBagConstraints);

        fillerPanelVertical.setEnabled(false);
        fillerPanelVertical.setFocusable(false);
        fillerPanelVertical.setRequestFocusEnabled(false);
        fillerPanelVertical.setVerifyInputWhenFocusTarget(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(fillerPanelVertical, gridBagConstraints);

        jScrollPane1.setViewportView(jPanel2);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * affiche la page d'aide
     * @param evt
     */
    private void jButtonHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHelpActionPerformed
        String id = "Releve.about";
        Help help = Lookup.getDefault().lookup(Help.class);
        if (help != null && help.isValidID(id, true).booleanValue()) {
            help.showHelp(new HelpCtx(id));
        }
    }//GEN-LAST:event_jButtonHelpActionPerformed

    private void jCheckBoxGedcomCompletionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxGedcomCompletionActionPerformed
        if (jCheckBoxGedcomCompletion.isSelected()) {
            // je commence par supprimer la completion avec le gedcom precedent
            if (gedcomCompletion != null) {
                dataManager.removeGedcomCompletion(gedcomCompletion);
                gedcomCompletion = null;
            }

            Context context = GedcomDirectory.getInstance().getLastContext();
            if (context != null && context.getGedcom() != null) {
                gedcomCompletion = context.getGedcom();
                dataManager.addGedcomCompletion(gedcomCompletion);


                for (TreeTopComponent tc : AncestrisPlugin.lookupAll(TreeTopComponent.class)) {
                    if( tc.getContext().getGedcom().equals(gedcomCompletion)) {
                        TreeView view= (TreeView)tc.getView();
                        TreeViewDropTarget viewDropTarget = new TreeViewDropTarget();
                        viewDropTarget.createDropTarget(view);
                    }
                }
            } else {
                // pas de gedcom courant
                jCheckBoxGedcomCompletion.setSelected(false);
                Toolkit.getDefaultToolkit().beep();
            }



        } else {
            dataManager.removeGedcomCompletion(gedcomCompletion);
            gedcomCompletion = null;
        }
    }//GEN-LAST:event_jCheckBoxGedcomCompletionActionPerformed

    /**
     * charge le fichier de démo
     * @param evt
     */
    private void jButtonDemoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDemoActionPerformed
        topComponent.loadFileDemo();
    }//GEN-LAST:event_jButtonDemoActionPerformed

    /**
     * crée un nouveau relevé
     * @param evt
     */
    private void jButtonNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNewActionPerformed
        topComponent.createFile();
    }//GEN-LAST:event_jButtonNewActionPerformed

    /**
     * ouvre un fichier de relevé
     * @param evt
     */
    private void jButtonOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenActionPerformed
        topComponent.loadFile();
    }//GEN-LAST:event_jButtonOpenActionPerformed

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
        topComponent.saveFile();
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonImportActionPerformed
        topComponent.importFile();
    }//GEN-LAST:event_jButtonImportActionPerformed

    private void jButtonExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExportActionPerformed
        topComponent.exportFile();
    }//GEN-LAST:event_jButtonExportActionPerformed

    private void miscNumberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miscNumberActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_miscNumberActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel OptionsPanel;
    private javax.swing.JLabel birthLabel;
    private javax.swing.JTextField birthNumber;
    private javax.swing.JTextField cityCodeEntry;
    private javax.swing.JLabel cityCodeLabel;
    private javax.swing.JTextField cityNameEntry;
    private javax.swing.JLabel cityNameLabel;
    private javax.swing.JCheckBox copyFreeComment;
    private javax.swing.JTextField countryEntry;
    private javax.swing.JLabel countryLabel;
    private javax.swing.JTextField countyNameEntry;
    private javax.swing.JLabel countyNameLabel;
    private javax.swing.JLabel deathLabel;
    private javax.swing.JTextField deathNumber;
    private javax.swing.JPanel fillerPanelHorizontal;
    private javax.swing.JPanel fillerPanelVertical;
    private javax.swing.JButton jButtonDemo;
    private javax.swing.JButton jButtonExport;
    private javax.swing.JButton jButtonHelp;
    private javax.swing.JButton jButtonImport;
    private javax.swing.JButton jButtonNew;
    private javax.swing.JButton jButtonOpen;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JCheckBox jCheckBoxDuplicateRecord;
    private javax.swing.JCheckBox jCheckBoxGedcomCompletion;
    private javax.swing.JCheckBox jCheckBoxNewValueControl;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelCommand;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel marriageLabel;
    private javax.swing.JTextField marriageNumber;
    private javax.swing.JLabel miscLabel;
    private javax.swing.JTextField miscNumber;
    private javax.swing.JPanel placePanel;
    private javax.swing.JTextField stateEntry;
    private javax.swing.JLabel stateLabel;
    private javax.swing.JPanel statisticPanel;
    // End of variables declaration//GEN-END:variables

    

    ///////////////////////////////////////////////////////////////////////////
    // Implement GedcomFileListener
    ///////////////////////////////////////////////////////////////////////////

    /**
     * desactive la completion avec gedcom si le fichier gedcom utilisé
     * est fermé par l'utilisateur
     * @param gedcom
     */
    @Override
    public void gedcomClosed(Gedcom gedcom) {
        if (gedcom.equals(gedcomCompletion)) {
            dataManager.removeGedcomCompletion(gedcomCompletion);
            gedcomCompletion = null;
            jCheckBoxGedcomCompletion.setSelected(false);
        }
    }

    @Override
    public void commitRequested(Context context) {
        //rien à faire
    }

    @Override
    public void gedcomOpened(Gedcom gedcom) {
        // rien à faire
    }

}
