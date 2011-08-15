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

import genj.util.GridBagHelper;
import genj.util.Resources;
import genj.util.swing.EditorHyperlinkSupport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

/**
 * Select report from list, show details, show options
 */
class ReportDetail extends JPanel {

  private JLabel      lFile,lAuthor,lVersion;
  private JTextPane   tpInfo;
  private HTMLEditorKit editorKit = new HTMLEditorKit(ReportDetail.class);
  
  /** Constructor */
  public ReportDetail() {

    Resources res = Resources.get(this);
    
    GridBagHelper gh = new GridBagHelper(this);
    
    // ... Report's filename
    gh.setParameter(GridBagHelper.FILL_HORIZONTAL);
    gh.setInsets(new Insets(0, 0, 0, 5));

    lFile = new JLabel("");
    lFile.setForeground(Color.black);

    gh.add(new JLabel(res.getString("report.file")),2,0);
    gh.add(lFile,3,0,1,1,GridBagHelper.GROWFILL_HORIZONTAL);
    
    // ... Report's author

    lAuthor = new JLabel("");
    lAuthor.setForeground(Color.black);

    gh.add(new JLabel(res.getString("report.author")),2,1);
    gh.add(lAuthor,3,1,1,1,GridBagHelper.GROWFILL_HORIZONTAL);

    // ... Report's version
    lVersion = new JLabel();
    lVersion.setForeground(Color.black);

    gh.add(new JLabel(res.getString("report.version")),2,2);
    gh.add(lVersion,3,2);

    // ... Report's infos
    tpInfo = new JTextPane();
    tpInfo.setEditable(false);
    tpInfo.setEditorKit(editorKit);
    tpInfo.setFont(new JTextField().getFont()); //don't use standard clunky text area font
    tpInfo.addHyperlinkListener(new EditorHyperlinkSupport(tpInfo));
    tpInfo.setPreferredSize(new Dimension(256,256));
    JScrollPane spInfo = new JScrollPane(tpInfo);
    gh.add(spInfo,2,4,2,1,GridBagHelper.GROWFILL_BOTH);

    tpInfo.setText("Some very long info on report from file");

  }
  
  public void setReport(Report report) {
    // update info
    if (report == null) {
      lFile    .setText("");
      lAuthor  .setText("");
      lVersion .setText("");
      tpInfo   .setText("");
    } else {
      editorKit.setFrom(report.getClass());
      lFile    .setText(report.getFile().getName());
      lAuthor  .setText(report.getAuthor());
      lVersion .setText(getReportVersion(report));
      tpInfo   .setText(report.getInfo().replaceAll("\n", "<br>"));
      tpInfo   .setCaretPosition(0);
    }
  }
  
  /**
   * Returns the report version with last update date
   * @param report the report
   * @return version information
   */
  private String getReportVersion(Report report) {
    String version = report.getVersion();
    String update = report.getLastUpdate();
    if (update != null)
      version += " - " + Resources.get(this).getString("report.updated") + ": " + update;
    return version;
  }
    
}
