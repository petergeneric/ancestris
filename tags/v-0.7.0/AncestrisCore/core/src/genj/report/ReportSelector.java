/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2009 Nils Meier <nils@meiers.net>
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
package genj.report;

import genj.option.Option;
import genj.option.OptionsWidget;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ImageIcon;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 * Select report from list, show details, show options
 */
class ReportSelector extends JPanel {
  
  private final static ImageIcon
    imgReload= new ImageIcon(ReportView.class,"Reload"     );
  
  private ReportDetail detail = new ReportDetail();
  private ReportList list = new ReportList(ReportLoader.getInstance().getReports(), false);
  private OptionsWidget options = new OptionsWidget("");

  /** Constructor */
  public ReportSelector() {
    
    super(new BorderLayout());

    final Resources res = Resources.get(this);
    
    final JTabbedPane right = new JTabbedPane();
    right.add(res.getString("title"), detail);
    right.add(res.getString("report.options"), options);
    
    detail.setOpaque(false);
    
    detail.setPreferredSize(new Dimension(320,200));
    options.setPreferredSize(new Dimension(320,200));
    
    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.add(new JLabel(res.getString("report.reports")));
    JButton reload = new JButton(new ActionReload());
    reload.setFocusable(false);
    reload.setMargin(new Insets(0,0,0,0));
    top.add(reload);
    
    add(top, BorderLayout.NORTH);
    add(new JScrollPane(list), BorderLayout.WEST);
    add(right, BorderLayout.CENTER);

    list.setVisibleRowCount(8);
    list.setSelectionListener(new ReportSelectionListener() {
      public void valueChanged(Report report) {
        detail.setReport(report);
        if (report!=null) {
          right.setTitleAt(0, report.getName());
          options.setOptions(report.getOptions());
        } else {
          right.setTitleAt(0, res.getString("report.options"));
          options.setOptions(new ArrayList<Option>());          
        }
      }
    });
    
    if (list.getModel().getSize()>0)
      list.setSelectedIndex(0);
    
  }
  
  /**
   * select a report
   */
  public void select(Report report) {
    if (report!=null)
      list.setSelection(report);
  }
  

  /**
   * Action: RELOAD
   */
  private class ActionReload extends Action2 {
    protected ActionReload() {
      setImage(imgReload);
      setTip(Resources.get(this), "report.reload.tip");
      setEnabled(!ReportLoader.getInstance().isReportsInClasspath());
    }
    public void actionPerformed(ActionEvent event) {
      // .. do it (forced!);
      ReportLoader.clear();
      // .. get them
      Report reports[] = ReportLoader.getInstance().getReports();
      // .. update
      list.setReports(reports);
      // .. done
    }
  } //ActionReload


  /** Selected report */
  Report getReport() {
    
    // commit edits
    options.stopEditing();

    // selection
    return list.getSelection();
  }
}
