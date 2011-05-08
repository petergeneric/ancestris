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

import genj.util.swing.Action2;
import genj.util.swing.ButtonHelper;
import genj.util.Registry;
import genj.window.WindowManager;

import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EtchedBorder;

/**
 * A settings component 
 */
class SettingsWidget extends JPanel implements ItemListener {

  /** the registry we use */
  private EditDocsPanel mainPanel;
  private Registry registry;

  /** components */
  private ActionApply apply;

  private int FS = 10;

  /** settings */
  JCheckBox settingUsetabs;
  JCheckBox settingConfirm;
  JCheckBox settingAutoSave;

  JCheckBox settingSosa;
  JCheckBox settingDabo;
  JLabel labelSosaTag;
  JTextField settingSosaTag;

  JCheckBox settingDebug;


  /**
   * Constructor
   */
  protected SettingsWidget(EditDocsPanel panel, Registry registry) {

    this.mainPanel = panel;
    this.registry = registry;


    apply = new ActionApply();

    // Create tabbed pane
    JTabbedPane tabbedPane = new JTabbedPane();

    JPanel pSettings1 = createPanelEdition();
    tabbedPane.add(mainPanel.translate("Setting_Edition"), pSettings1);

    JPanel pSettings2 = createPanelSosa();
    tabbedPane.add(mainPanel.translate("Setting_Sosa"), pSettings2);

    JPanel pSettings3 = createPanelHelp();
    tabbedPane.add(mainPanel.translate("Setting_Help"), pSettings3);

    // Panel for Actions
    JPanel pActions = new JPanel();
    ButtonHelper bh = new ButtonHelper().setContainer(pActions);
    bh.create(apply);
    bh.create(new ActionClose());

    // Layout
    setLayout(new BorderLayout());
    add(tabbedPane,"North");
    add(pActions ,"South" );

    // enable buttons
    apply.setEnabled(true);

    // show
    tabbedPane.revalidate();
    tabbedPane.repaint();

    // done
  }


  /**
   * Applies the changes currently being done
   */
  private class ActionApply extends Action2 {
    protected ActionApply() { 
      setText(mainPanel.translate("Setting_OK"));
      setEnabled(false);
    }
    protected void execute() {
      registry.put("usetabs", settingUsetabs.isSelected() ? "1" : "0");
      registry.put("confirm", settingConfirm.isSelected() ? "1" : "0");
      registry.put("autosave", settingAutoSave.isSelected() ? "1" : "0");

      registry.put("sosa", settingSosa.isSelected() ? "1" : "0");
      registry.put("dabo", settingDabo.isSelected() ? "1" : "0");
      registry.put("sosatag", settingSosaTag.getText());

      registry.put("debug", settingDebug.isSelected() ? "1" : "0");
      Registry.persist(); // stores it
    }
  }

  /**
   * closes the settings
   */
  private class ActionClose extends Action2 {
    private ActionClose() {
      setText(mainPanel.translate("Setting_Close"));
    }
    protected void execute() {
      WindowManager.getInstance(getTarget()).close("docs.settings");
    }
  }

  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(450,250);
  }

  /**
   * Detect click on chek boxes
   */
  public void itemStateChanged(ItemEvent e) {

     if (e.getItemSelectable() == settingSosa) {
        settingDabo.setEnabled(settingSosa.isSelected());
        labelSosaTag.setEnabled(settingSosa.isSelected());
        settingSosaTag.setEnabled(settingSosa.isSelected());
        }
     }


  /**
   * Create panel tabs of different settings
   */
  public JPanel createPanelEdition() {

    JPanel pSettings = new JPanel(new GridBagLayout());
    pSettings.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;

    settingUsetabs = new JCheckBox(mainPanel.translate("Setting_DisplayTabs"));
    settingConfirm = new JCheckBox(mainPanel.translate("Setting_Confim"));
    settingAutoSave = new JCheckBox(mainPanel.translate("Setting_Autosave"));

    // read settings
    settingUsetabs.setSelected(registry.get("usetabs", "1").equals("1"));
    settingConfirm.setSelected(registry.get("confirm", "1").equals("1"));
    settingAutoSave.setSelected(registry.get("autosave", "1").equals("1"));

    // position elements
    c.insets = new Insets(10, 10, 10, 10);
    c.gridx = 0; c.gridy = 0; c.weightx = 1.0; c.gridwidth = 1;
    pSettings.add(settingUsetabs, c);
    c.gridx = 0; c.gridy = 1;
    pSettings.add(settingConfirm, c);
    c.gridx = 0; c.gridy = 2;
    pSettings.add(settingAutoSave, c);

    return pSettings;
    }


  public JPanel createPanelSosa() {

    JPanel pSettings = new JPanel(new GridBagLayout());
    pSettings.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;

    settingSosa = new JCheckBox(mainPanel.translate("Setting_SosaNb"));
    settingSosa.addItemListener(this);

    settingDabo = new JCheckBox(mainPanel.translate("Setting_Aboville"));

    JPanel groupSosaTag = new JPanel(new FlowLayout(FlowLayout.LEFT));
    labelSosaTag = new JLabel(mainPanel.translate("Setting_SosaTag"));
    settingSosaTag = new JTextField("", FS);
    groupSosaTag.add(labelSosaTag);
    groupSosaTag.add(settingSosaTag);

    // read settings
    settingSosa.setSelected(registry.get("sosa", "1").equals("1"));
    settingDabo.setSelected(registry.get("dabo", "1").equals("1"));
    settingSosaTag.setText(registry.get("sosatag", "_SOSA"));
    settingDabo.setEnabled(settingSosa.isSelected());
    labelSosaTag.setEnabled(settingSosa.isSelected());
    settingSosaTag.setEnabled(settingSosa.isSelected());

    // position elements
    c.insets = new Insets(10, 10, 10, 10);
    c.gridx = 0; c.gridy = 0; c.weightx = 1.0; c.gridwidth = 1;
    pSettings.add(settingSosa, c);
    c.gridx = 0; c.gridy = 1;
    pSettings.add(settingDabo, c);
    c.gridx = 0; c.gridy = 2;
    pSettings.add(groupSosaTag, c);

    return pSettings;
    }




  public JPanel createPanelHelp() {

    JPanel pSettings = new JPanel(new GridBagLayout());
    pSettings.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;

    settingDebug = new JCheckBox(mainPanel.translate("Setting_Debug"));

    // read settings
    settingDebug.setSelected(registry.get("debug", "0").equals("1"));

    // position elements
    c.insets = new Insets(10, 10, 10, 10);
    c.gridx = 0; c.gridy = 0; c.weightx = 1.0; c.gridwidth = 1;
    pSettings.add(settingDebug, c);

    return pSettings;
    }



} //SettingsWidget

