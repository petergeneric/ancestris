/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.editors.standard;

import ancestris.api.editor.Editor;
import ancestris.modules.editors.standard.tools.Utils;
import static ancestris.modules.editors.standard.tools.Utils.getImageFromFile;
import static ancestris.modules.editors.standard.tools.Utils.getResizedIcon;
import ancestris.util.TimingUtility;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Media;
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Repository;
import genj.gedcom.Source;
import genj.gedcom.Submitter;
import genj.io.InputSource;
import genj.view.ViewContext;
import java.awt.Component;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.openide.util.NbBundle;

/**
 * Displays a text and then the basic information on entities, without the ability to edit them
 *
 * Generic fields:
 *      Name
 *      Address
 *      Text
 *      Media
 *      Source
 * 
 * SUBM:
 *      Name +1 NAME <SUBMITTER_NAME> {1:1}
 *      Address +1 <<ADDRESS_STRUCTURE>>{0:1}
 *      Text +1 <<NOTE_STRUCTURE>>{0:M}
 *      Media +1 <<MULTIMEDIA_LINK>> {0:M}
 *      Source n/a
 * 
 * NOTE:
 *      Name n/a
 *      Address
 *      Text +1 [CONC|CONT] <SUBMITTER_TEXT> {0:M}
 *      Media n/a
 *      Source +1 <<SOURCE_CITATION>> {0:M}
 * 
 * OBJE:
 *      Name +2 TITL <DESCRIPTIVE_TITLE> {0:1}
 *      Address n/a
 *      Text +1 <<NOTE_STRUCTURE>> {0:M}
 *      Media +1 FILE <MULTIMEDIA_FILE_REFN> {1:M}
 *      Source +1 <<SOURCE_CITATION>> {0:M}
 * 
 * REPO:
 *      Name +1 NAME <NAME_OF_REPOSITORY> {1:1}
 *      Address +1 <<ADDRESS_STRUCTURE>> {0:1}
 *      Text +1 <<NOTE_STRUCTURE>> {0:M}
 *      Media n/a
 *      Source n/a
 * 
 * SOUR:
 *      Name +1 TITL <SOURCE_DESCRIPTIVE_TITLE> {0:1}
 *      Address n/a
 *      Text +1 TEXT <TEXT_FROM_SOURCE> {0:1}
 *           +2 [CONC|CONT] <TEXT_FROM_SOURCE> {0:M} 
 *      Media +1 <<MULTIMEDIA_LINK>> {0:M}
 *      Source +1 <<SOURCE_REPOSITORY_CITATION>> {0:M}
 * 
 * @author frederic
 */
public class BlankPanel extends Editor {

    private static final Logger LOG = Logger.getLogger("ancestris.editor.blank");
    
    private Context context;
    private Gedcom gedcom;
    private Entity entity;
    
    
    /**
     * Creates new form BlankJPanel
     */
    public BlankPanel() {
        initComponents();
    }

    @Override
    public ViewContext getContext() {
        return new ViewContext(context);
    }

    @Override
    public Component getEditorComponent() {
        return this;
    }

    @Override
    protected void setContextImpl(Context context) {
        LOG.finer(TimingUtility.getInstance().getTime() + ": setContextImpl().start");
        
        this.context = context;
        Entity localEntity = context.getEntity();
        if (localEntity != null) {
            this.entity = localEntity;
            this.gedcom = localEntity.getGedcom();
            
            String str = localEntity.getPropertyName() + " (" + localEntity.getId() + ")";
            entityName.setText("<html>"+NbBundle.getMessage(BlankPanel.class, "BlankPanel.entityName.text") + " <b>" + str +"</b></html>");
            
            if (entity instanceof Submitter) {
                Submitter subm = (Submitter) entity;
                displayName(subm.getName());
                displayAddress(subm.getAddress(), subm.getCity(), subm.getState(), subm.getPostcode(), subm.getCountry());
                displayNote(entity);
                displayMedia(entity);
                displaySource(null);
                        
            } else if (entity instanceof Note) {
                Note note = (Note) entity;
                displayName(null);
                displayAddress(null);
                displayNote(note.getValue());
                displayMedia((InputSource)null);
                displaySource(entity);
                
            } else if (entity instanceof Media) {
                Media media = (Media) entity;
                displayName(media.getTitle());
                displayAddress(null);
                displayNote(entity);
                displayMedia(media.getFile());
                displaySource(entity);
                
            } else if (entity instanceof Repository) {
                Repository repo = (Repository) entity;
                displayName(repo.getRepositoryName());
                Property address = repo.getProperty("ADDR");
                if (address != null) {
                    Property pCity = address.getProperty("CITY");
                    Property pState = address.getProperty("STAE");
                    Property pCode = address.getProperty("POST");
                    Property pCtry = address.getProperty("CTRY");
                    displayAddress(address.getValue(), 
                            pCity != null ? pCity.getValue() : "",
                            pState != null ? pState.getValue() : "",
                            pCode != null ? pCode.getValue() : "",
                            pCtry != null ? pCtry.getValue() : "");
                } else {
                    displayAddress(null);
                }
                displayNote(entity);
                displayMedia((InputSource) null);
                displaySource(null);
                
            } else if (entity instanceof Source) {
                Source source = (Source) entity;
                displayName(source.getTitle());
                displayAddress(null);
                displayNote(source.getText());
                displayMedia(entity);
                displayRepository(entity);
                
            }
            
            
        }

        LOG.finer(TimingUtility.getInstance().getTime() + ": setContextImpl().finish");
    }

    @Override
    public void commit() throws GedcomException {
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        entityName = new javax.swing.JLabel();
        description = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        nameText = new javax.swing.JTextField();
        addressLabel = new javax.swing.JLabel();
        addressText = new javax.swing.JTextField();
        cityText = new javax.swing.JTextField();
        countyText = new javax.swing.JTextField();
        codetext = new javax.swing.JTextField();
        countryText = new javax.swing.JTextField();
        textLabel = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        textArea = new ancestris.swing.UndoTextArea();
        mediaLabel = new javax.swing.JLabel();
        mediaImage = new javax.swing.JLabel();
        sourceLabel = new javax.swing.JLabel();
        sourceText = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(entityName, org.openide.util.NbBundle.getMessage(BlankPanel.class, "BlankPanel.entityName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(description, org.openide.util.NbBundle.getMessage(BlankPanel.class, "BlankPanel.description.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(BlankPanel.class, "BlankPanel.nameLabel.text")); // NOI18N

        nameText.setEditable(false);
        nameText.setText(org.openide.util.NbBundle.getMessage(BlankPanel.class, "BlankPanel.nameText.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addressLabel, org.openide.util.NbBundle.getMessage(BlankPanel.class, "BlankPanel.addressLabel.text")); // NOI18N

        addressText.setEditable(false);
        addressText.setText(org.openide.util.NbBundle.getMessage(BlankPanel.class, "BlankPanel.addressText.text")); // NOI18N

        cityText.setEditable(false);
        cityText.setText(org.openide.util.NbBundle.getMessage(BlankPanel.class, "BlankPanel.cityText.text")); // NOI18N

        countyText.setEditable(false);
        countyText.setText(org.openide.util.NbBundle.getMessage(BlankPanel.class, "BlankPanel.countyText.text")); // NOI18N

        codetext.setEditable(false);
        codetext.setText(org.openide.util.NbBundle.getMessage(BlankPanel.class, "BlankPanel.codetext.text")); // NOI18N

        countryText.setEditable(false);
        countryText.setText(org.openide.util.NbBundle.getMessage(BlankPanel.class, "BlankPanel.countryText.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(textLabel, org.openide.util.NbBundle.getMessage(BlankPanel.class, "BlankPanel.textLabel.text")); // NOI18N

        textArea.setEditable(false);
        textArea.setColumns(20);
        textArea.setLineWrap(true);
        textArea.setRows(6);
        textArea.setWrapStyleWord(true);
        scrollPane.setViewportView(textArea);

        org.openide.awt.Mnemonics.setLocalizedText(mediaLabel, org.openide.util.NbBundle.getMessage(BlankPanel.class, "BlankPanel.mediaLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(mediaImage, org.openide.util.NbBundle.getMessage(BlankPanel.class, "BlankPanel.mediaImage.text")); // NOI18N
        mediaImage.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        mediaImage.setPreferredSize(new java.awt.Dimension(4, 187));

        org.openide.awt.Mnemonics.setLocalizedText(sourceLabel, org.openide.util.NbBundle.getMessage(BlankPanel.class, "BlankPanel.sourceLabel.text")); // NOI18N

        sourceText.setEditable(false);
        sourceText.setText(org.openide.util.NbBundle.getMessage(BlankPanel.class, "BlankPanel.sourceText.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(entityName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(description, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addressLabel)
                            .addComponent(nameLabel)
                            .addComponent(textLabel)
                            .addComponent(mediaLabel)
                            .addComponent(sourceLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sourceText)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(codetext, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(countryText))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cityText)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(countyText))
                            .addComponent(nameText)
                            .addComponent(addressText)
                            .addComponent(scrollPane)
                            .addComponent(mediaImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(entityName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(description, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addressLabel)
                    .addComponent(addressText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cityText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(countyText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(codetext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(countryText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(textLabel)))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mediaImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mediaLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(sourceLabel)
                    .addComponent(sourceText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addressLabel;
    private javax.swing.JTextField addressText;
    private javax.swing.JTextField cityText;
    private javax.swing.JTextField codetext;
    private javax.swing.JTextField countryText;
    private javax.swing.JTextField countyText;
    private javax.swing.JLabel description;
    private javax.swing.JLabel entityName;
    private javax.swing.JLabel mediaImage;
    private javax.swing.JLabel mediaLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameText;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JLabel sourceLabel;
    private javax.swing.JTextField sourceText;
    private javax.swing.JTextArea textArea;
    private javax.swing.JLabel textLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public Entity getEditedEntity() {
        return entity;
    }
    
    private Property getXRefFromEntity(Entity entity, Object o) {
        Entity target = null;
        for (PropertyXRef xref : entity.getProperties(PropertyXRef.class)) {
            target = xref.getTargetEntity();
            if (target.getClass().equals(o)) {
                return target;
            }
        }
        return null;
    }

    private void displayName(String name) {
        nameLabel.setVisible(name != null);
        nameText.setVisible(name != null);
        nameText.setText(name != null ? name : "");
    }

    private void displayAddress(String str) {
        displayAddress(str, null, null, null, null);
    }

    private void displayAddress(String address, String city, String state, String postcode, String country) {
        addressLabel.setVisible(address != null);

        addressText.setVisible(address != null);
        cityText.setVisible(city != null);
        countyText.setVisible(state != null);
        codetext.setVisible(postcode != null);
        countryText.setVisible(country != null);
        
        addressText.setText(address != null ? address : "");
        cityText.setText(city != null ? city : "");
        countyText.setText(state != null ? state : "");
        codetext.setText(postcode != null ? postcode : "");
        countryText.setText(country != null ? country : "");
    }

    private void displayNote(Entity entity) {
        Note note = (Note) getXRefFromEntity(entity, Note.class);
        String text = "";
        if (note != null) {
            text = note.getValue().trim();
        }
        displayNote(text);
    }

    private void displayNote(String text) {
        boolean display = entity != null && !text.isEmpty();
        textLabel.setVisible(display);
        scrollPane.setVisible(display);
        textArea.setVisible(display);
        textArea.setText(text);
        
    }

    private void displayMedia(InputSource f) {
        if (f != null) {
             mediaImage.setIcon(getResizedIcon(new ImageIcon(getImageFromFile(f, getClass(), Utils.IMG_INVALID_PHOTO)), 238, 187));
        }
        mediaLabel.setVisible(entity != null && f != null);
        mediaImage.setVisible(entity != null && f != null);
    }

    private void displayMedia(Entity entity) {
        Media media = (Media) getXRefFromEntity(entity, Media.class);
        InputSource f = null;
        if (media != null) {
            f = media.getFile();
        }
        displayMedia(f);
    }

    private void displaySource(Entity entity) {
        Source source = null;
        if (entity != null) {
            source = (Source) getXRefFromEntity(entity, Source.class);
        }
        String title = "";
        if (source != null) {
            title = source.getTitle().trim();
        }
        sourceLabel.setText(source != null ? source.getPropertyName() : "Source");
        sourceLabel.setVisible(entity != null && !title.isEmpty());
        sourceText.setVisible(entity != null && !title.isEmpty());
        sourceText.setText(title);
        
    }

    private void displayRepository(Entity entity) {
        Repository repo = null;
        if (entity != null) {
            repo = (Repository) getXRefFromEntity(entity, Repository.class);
        }
        String title = "";
        if (repo != null) {
            title = repo.getRepositoryName().trim();
        }
        sourceLabel.setText(repo != null ? repo.getPropertyName() : "Repo");
        sourceLabel.setVisible(entity != null && !title.isEmpty());
        sourceText.setVisible(entity != null && !title.isEmpty());
        sourceText.setText(title);
    }

    
    
}
