package ancestris.modules.releve.dnd;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import java.text.MessageFormat;
import org.openide.util.NbBundle;

/**
 *
 */
class MergeModelDeath extends MergeModel {

    private Indi currentIndi;
    private MergeRecord record;
    private Gedcom gedcom;
    
    /**
     * le constucteur initialise les données du modele
     * en comparant les champs du releve et les
     *
     * @param indi
     * @param record
     */
    protected MergeModelDeath(MergeRecord record, Gedcom gedcom) throws Exception {
        this.record = record;
        this.currentIndi = null;
        this.gedcom = gedcom;
        addRowIndi();
        addRowMarried(null);
        addRowParents(null);
    }

    /**
     * le constucteur initialise les données du modele
     * en comparant les champs du releve
     *
     * @param indi
     * @param record
     */
    protected MergeModelDeath(MergeRecord record, Gedcom gedcom, Indi indi, Fam parentfam) throws Exception {
        this.record = record;
        this.currentIndi = indi;
        this.gedcom = gedcom;
        addRowIndi();
        addRowMarried(null);
        addRowParents(parentfam);
    }

    /**
     * le constucteur initialise les données du modele
     * en comparant les champs du releve
     *
     * @param record
     * @param gedcom
     * @param indi
     * @param marriedFamily famille avec l'ex conjoint
     * @param parentFamily  famille parent de l'individu
     */
    protected MergeModelDeath(MergeRecord record, Gedcom gedcom, Indi indi, Fam marriedFamily, Fam parentFamily) throws Exception {
        this.record = record;
        this.currentIndi = indi;
        this.gedcom = gedcom;
        addRowIndi();
        addRowMarried(marriedFamily);
        addRowParents(parentFamily);

    }
    
    /**
     * le constucteur initialise les données du modele
     * en comparant les champs du releve et les
     *
     * @param indi
     * @param record
     */
    protected MergeModelDeath(MergeRecord record, Gedcom gedcom, Indi father, Indi mother ) throws Exception {
        this.record = record;
        this.currentIndi = null;
        this.gedcom = gedcom;

        // j'affiche l'individu 
        addRowIndi();
        // j'affiche l'ex conjoint
        addRowMarried(null);

        // j'affiche la famille de l'enfant
        addRowSeparator();
        addRow(RowType.IndiParentFamily, record, null);
        addRow(RowType.IndiParentMarriageDate, record.getIndiParentMarriageDate(), null);

        // j'affiche les parents
        addRowFather(father);
        addRowMother(mother);

    }

    private void addRowIndi() throws Exception {

       if (currentIndi != null) {
            // j'affiche la source de la naissance
            Property sourceProperty = MergeQuery.findSource(record, gedcom, currentIndi.getProperty("DEAT"));
            addRow(RowType.EventSource, record.getEventSource(), MergeQuery.findSourceTitle(sourceProperty, gedcom), MergeQuery.findSource(record, gedcom));
            addRow(RowType.EventPage, record.getEventPage(),  MergeQuery.findSourcePage(record, sourceProperty, gedcom), null);

            // j'affiche un separateur
            addRowSeparator();

            // j'affiche le nom
            addRow(RowType.IndiLastName, record.getIndiLastName(), currentIndi.getLastName(), currentIndi);
            addRow(RowType.IndiFirstName, record.getIndiFirstName(), currentIndi.getFirstName());
            addRow(RowType.IndiSex, record.getIndiSexString(), currentIndi.getPropertyValue("SEX"));
            addRow(RowType.IndiBirthDate, record.getIndiBirthDate() , currentIndi.getBirthDate());
            addRow(RowType.IndiDeathDate, record.getIndiDeathDate() , currentIndi.getDeathDate());
            addRow(RowType.IndiPlace, record.getIndiPlace(), currentIndi.getValue(new TagPath("INDI:DEAT:PLAC"), ""));
            addRow(RowType.EventComment, record.getEventComment(), currentIndi.getValue(new TagPath("INDI:DEAT:NOTE"), ""));

        } else {
            // selectedIndi est nul

            // j'affiche la source de la naissance
            addRow(RowType.EventSource, record.getEventSource(), "", MergeQuery.findSource(record, gedcom) );
            addRow(RowType.EventPage,       record.getEventPage(), "", null);

            // j'affiche un separateur
            addRowSeparator();

            // j'affiche le nom
            addRow(RowType.IndiLastName, record.getIndiLastName(), "");
            addRow(RowType.IndiFirstName, record.getIndiFirstName(), "");
            addRow(RowType.IndiSex, record.getIndiSexString(), "");
            addRow(RowType.IndiBirthDate, record.getIndiBirthDate() , null);
            addRow(RowType.IndiDeathDate, record.getIndiDeathDate() , null);
            addRow(RowType.IndiPlace, record.getIndiPlace(), "");
            addRow(RowType.EventComment, record.getEventComment(), "");
        }
    }

    private void addRowParents( Fam fam) throws Exception {
        addRowSeparator();
        addRow(RowType.IndiParentFamily, record, fam);
        if (fam != null) {
            addRow(RowType.IndiParentMarriageDate, record.getIndiParentMarriageDate(), fam.getMarriageDate());

            addRowFather(fam.getHusband());
            addRowMother(fam.getWife());
        } else {
            addRow(RowType.IndiParentMarriageDate, record.getIndiParentMarriageDate(), null);
            addRowFather(null);
            addRowMother(null);
        }
    }

    private void addRowFather( Indi father ) throws Exception {
        if (father != null) {
            addRow(RowType.IndiFatherLastName, record.getIndiFatherLastName(), father.getLastName(), father);
            addRow(RowType.IndiFatherFirstName, record.getIndiFatherFirstName(), father.getFirstName());
            addRow(RowType.IndiFatherBirthDate, record.getIndiFatherBirthDate(), father.getBirthDate());
            addRow(RowType.IndiFatherDeathDate, record.getIndiFatherDeathDate(), father.getDeathDate());
            addRow(RowType.IndiFatherOccupation, record.getIndiFatherOccupationWithDate(), MergeQuery.findOccupation(father, record.getEventDate()));

        } else {
            addRow(RowType.IndiFatherLastName, record.getIndiFatherLastName(), "");
            addRow(RowType.IndiFatherFirstName, record.getIndiFatherFirstName(), "");
            addRow(RowType.IndiFatherBirthDate, record.getIndiFatherBirthDate(), null);
            addRow(RowType.IndiFatherDeathDate, record.getIndiFatherDeathDate(), null);
            addRow(RowType.IndiFatherOccupation, record.getIndiFatherOccupationWithDate(), "");
        }
    }

    private void addRowMother( Indi mother ) throws Exception {
        if (mother != null) {
            addRow(RowType.IndiMotherLastName, record.getIndiMotherLastName(), mother.getLastName(), mother);
            addRow(RowType.IndiMotherFirstName, record.getIndiMotherFirstName(), mother.getFirstName());
            addRow(RowType.IndiMotherBirthDate, record.getIndiMotherBirthDate(), mother.getBirthDate());
            addRow(RowType.IndiMotherDeathDate, record.getIndiMotherDeathDate(), mother.getDeathDate());
            addRow(RowType.IndiMotherOccupation, record.getIndiMotherOccupationWithDate(), MergeQuery.findOccupation(mother, record.getEventDate()));
        } else {
            addRow(RowType.IndiMotherLastName, record.getIndiMotherLastName(), "");
            addRow(RowType.IndiMotherFirstName, record.getIndiMotherFirstName(), "");
            addRow(RowType.IndiMotherBirthDate, record.getIndiMotherBirthDate(), null);
            addRow(RowType.IndiMotherDeathDate, record.getIndiMotherDeathDate(), null);
            addRow(RowType.IndiMotherOccupation, record.getIndiMotherOccupationWithDate(), "");
        }
    }

    /**
     * affiche les informations sur l'ex conjoint
     * @param marriedFamily
     * @throws Exception
     */
    private void addRowMarried( Fam marriedFamily ) throws Exception {
        if (! record.getIndiMarriedLastName().isEmpty() || ! record.getIndiMarriedFirstName().isEmpty()) {
            // j'affiche un separateur
            addRowSeparator();
            addRow(RowType.IndiMarriedFamily, record, marriedFamily);

            if (marriedFamily != null) {
                addRow(RowType.IndiMarriedMarriageDate, record.getIndiMarriedMarriageDate(), marriedFamily.getMarriageDate());

                Indi married;
                if ( currentIndi.getSex() == PropertySex.MALE) {
                    married = marriedFamily.getWife();
                } else {
                    married = marriedFamily.getHusband();
                }
                addRow(RowType.IndiMarriedLastName, record.getIndiMarriedLastName(), married.getLastName(), married);
                addRow(RowType.IndiMarriedFirstName, record.getIndiMarriedFirstName(), married.getFirstName());
                addRow(RowType.IndiMarriedBirthDate, record.getIndiMarriedBirthDate(), married.getBirthDate());
                addRow(RowType.IndiMarriedDeathDate, record.getIndiMarriedDeathDate(), married.getDeathDate());
                addRow(RowType.IndiMarriedOccupation, record.getIndiMotherOccupationWithDate(), MergeQuery.findOccupation(married, record.getEventDate()));
            } else {
                addRow(RowType.IndiMarriedMarriageDate, record.getIndiMarriedMarriageDate(),null);
                addRow(RowType.IndiMarriedLastName, record.getIndiMarriedLastName(), "", null);
                addRow(RowType.IndiMarriedFirstName, record.getIndiMarriedFirstName(), "");
                addRow(RowType.IndiMarriedBirthDate, record.getIndiMarriedBirthDate(), null);
                addRow(RowType.IndiMarriedDeathDate, record.getIndiMarriedDeathDate(), null);
                addRow(RowType.IndiMarriedOccupation, record.getIndiMotherOccupationWithDate(), "");
            }
            
        }
    }

    /**
     * retoune l'individu selectionné
     * @return individu selectionné ou null si c'est un nouvel individu
     */
    @Override
    protected Entity getSelectedEntity() {
        return currentIndi;
    }

    /**
     * retoune le gedcom du modele
     * @return gedcom
     */
    @Override
    protected Gedcom getGedcom() {
        return gedcom;
    }

    /**
     * copie les données du relevé dans l'entité
     */
    @Override
    protected void copyRecordToEntity() throws Exception {
        if (currentIndi == null) {
            currentIndi = (Indi) gedcom.createEntity(Gedcom.INDI);
            currentIndi.setName(record.getIndiFirstName(), record.getIndiLastName());
            currentIndi.setSex(record.getIndiSex());
        } else {
            // je copie le nom du releve dans l'individu
            if (isChecked(RowType.IndiLastName)) {
                currentIndi.setName(currentIndi.getFirstName(), record.getIndiLastName());
            }

            // je copie le prénom du releve dans l'individu
            if (isChecked(RowType.IndiFirstName)) {
                currentIndi.setName(record.getIndiFirstName(), currentIndi.getLastName());
            }

            // je copie le sex du releve dans l'individu
            if (isChecked(RowType.IndiSex)) {
                currentIndi.setSex(record.getIndiSex());
            }
        }

        // je cree la propriete de naissance si elle n'existait pas
        if (isChecked(RowType.IndiBirthDate) || isChecked(RowType.EventSource) || isChecked(RowType.EventComment)) {
            Property birthProperty = currentIndi.getProperty("BIRT");
            if (birthProperty == null) {
                birthProperty = currentIndi.addProperty("BIRT", "");
            }

            // je copie la date de naissance du releve dans l'individu
            if (isChecked(RowType.IndiBirthDate)) {
                // j'ajoute (ou remplace) la date de la naissance (le lieu de naissance n'est pas connu)
                copyBirthDate(currentIndi,record.getIndiBirthDate(), "", record);
            }
        }

        
        // je cree la propriete de deces si elle n'existait pas
        if (isChecked(RowType.IndiDeathDate) || isChecked(RowType.EventSource) || isChecked(RowType.EventComment)) {
            Property deathProperty = currentIndi.getProperty("DEAT");
            if (deathProperty == null) {
                deathProperty = currentIndi.addProperty("DEAT", "");
            }

            // je copie la date de deces du releve dans l'individu
            if (isChecked(RowType.IndiDeathDate)) {
                // j'ajoute (ou remplace) la date de la naissance
                PropertyDate propertyDate = (PropertyDate) deathProperty.getProperty("DATE");
                if (propertyDate == null) {
                    propertyDate = (PropertyDate) deathProperty.addProperty("DATE", "");
                }
                propertyDate.setValue(record.getIndiDeathDate().getValue());
            }
            
            // je copie la source du deces du releve dans l'individu
            if (isChecked(RowType.EventSource)) {
                copySource((Source) getRow(RowType.EventSource).entityObject, deathProperty, record);
            }

            // je copie le lieu du deces
            if (isChecked(RowType.IndiPlace)) {
                copyPlace(record.getIndiPlace(), deathProperty);
            }

            // je copie le commentaire du deces
            if (isChecked(RowType.EventComment)) {
                Property propertyNote = deathProperty.getProperty("NOTE");
                if (propertyNote == null) {
                    // je cree une note .
                    propertyNote = deathProperty.addProperty("NOTE", "");
                }

                // j'ajoute le commentaire du deces au debut de la note existante.
                String value = propertyNote.getValue();
                String comment = record.getEventComment();
                if (!comment.isEmpty()) {
                    if (!value.isEmpty()) {
                        comment += "\n";
                    }
                    comment += value;
                    propertyNote.setValue(comment);
                }
            }
        }


        // je copie les données de l'ex conjoint
        if (isChecked(RowType.IndiMarriedFamily)) {
            Indi exSpouse = (Indi) getRow(RowType.IndiMarriedLastName).entityObject;
            if (exSpouse == null) {
                // je cree l'individu
                exSpouse = (Indi) gedcom.createEntity(Gedcom.INDI);
                exSpouse.setName(record.getIndiMarriedFirstName(), record.getIndiMarriedLastName());
                exSpouse.setSex(currentIndi.getSex()==PropertySex.MALE ? PropertySex.FEMALE : PropertySex.MALE);
            } else {
                // je copie le nom de l'ex conjoint
                if (isChecked(RowType.IndiMarriedLastName)) {
                    exSpouse.setName(exSpouse.getFirstName(), record.getIndiMarriedLastName());
                }

                // je copie le prénom de l'ex conjoint
                if (isChecked(RowType.IndiMarriedFirstName)) {
                    exSpouse.setName(record.getIndiMarriedFirstName(), exSpouse.getLastName());
                }
            }

            // je copie la date, le lieu et commentaire de naissance de l'ex conjoint
            if (isChecked(RowType.IndiMarriedBirthDate)) {
                copyBirthDate(exSpouse, record.getIndiMarriedBirthDate(), "", record);
            }

            // je copie la date, le lieu et commentaire de naissance de l'ex conjoint
            if (isChecked(RowType.IndiMarriedDeathDate)) {
                copyDeathDate(exSpouse, record.getIndiMarriedDeathDate(), "", record);
            }

            // je copie la profession de l'ex conjoint
            if (isChecked(RowType.IndiMarriedOccupation) && !record.getIndiMarriedOccupation().isEmpty()) {
                copyOccupation(exSpouse, record.getIndiMarriedOccupation(), record.getIndiMarriedResidence(), record);
            }

            // je copie la famille avec l'ex conjoint
            Fam family = (Fam) getRow(RowType.IndiMarriedFamily).entityObject;
            if (family == null) {
                // je cree la famille
                family = (Fam) gedcom.createEntity(Gedcom.FAM);
                // j'ajoute les epoux
                if ( currentIndi.getSex() == PropertySex.MALE) {
                    family.setHusband(currentIndi);
                    family.setWife(exSpouse);
                } else {
                    family.setHusband(exSpouse);
                    family.setWife(currentIndi);
                }
            }

           // je copie la date du mariage avec l'ex conjoint et une note indiquant l'origine de cette date
            if (isChecked(RowType.IndiMarriedMarriageDate)) {
                copyMarriageDate(family, record.getIndiMarriedMarriageDate(), record );
            }
        }

        
        // je copie les données des parents
        if (isChecked(RowType.IndiParentFamily)) {
            // je copie la famille des parents
            Fam family = (Fam) getRow(RowType.IndiParentFamily).entityObject;
            if (family == null) {
                // je cree la famille
                family = (Fam) gedcom.createEntity(Gedcom.FAM);
            }

            // j'ajoute l'enfant dans la famille si ce n'est pas déja le cas
            if (!currentIndi.isDescendantOf(family)) {
                family.addChild(currentIndi);
            }

            // je copie la date du mariage et une note indiquant l'origine de cette date
            if (isChecked(RowType.IndiParentMarriageDate)) {                
                copyMarriageDate(family, record.getIndiParentMarriageDate(), record );
            }

            // je copie le nom et le prenom du pere
            Indi father = family.getHusband();
            if (father == null) {
                // je cree le pere
                father = (Indi) gedcom.createEntity(Gedcom.INDI);
                father.setName(record.getIndiFatherFirstName(), record.getIndiFatherLastName());
                father.setSex(PropertySex.MALE);
                family.setHusband(father);
            } else {
                if (isChecked(RowType.IndiFatherFirstName)) {
                    father.setName(record.getIndiFatherFirstName(), father.getLastName());
                }
                if (isChecked(RowType.IndiFatherLastName)) {
                    father.setName(father.getFirstName(), record.getIndiFatherLastName());
                }
            }

            // je copie la date de naissance du pere
            if (isChecked(RowType.IndiFatherBirthDate)) {
                copyBirthDate(father, record.getIndiFatherBirthDate(), "", record);
            }

            //je copie la date de décès du pere
            if (isChecked(RowType.IndiFatherDeathDate)) {
                copyDeathDate(father, record.getIndiFatherDeathDate(), "", record);
            }

            // je copie la profession du pere
            if (isChecked(RowType.IndiFatherOccupation) && !record.getIndiFatherOccupation().isEmpty()) {
                copyOccupation(father, record.getIndiFatherOccupation(), record.getIndiFatherResidence(), record);
            }            

            // je copie le nom et le prenom de la mere
            Indi mother = family.getWife();
            if (mother == null) {
                // je cree la mere
                mother = (Indi) gedcom.createEntity(Gedcom.INDI);
                mother.setName(record.getIndiMotherFirstName(), record.getIndiMotherLastName());
                mother.setSex(PropertySex.FEMALE);
                family.setWife(mother);
            } else {
                if (isChecked(RowType.IndiMotherFirstName)) {
                    mother.setName(record.getIndiMotherFirstName(), mother.getLastName());
                }
                if (isChecked(RowType.IndiMotherLastName)) {
                    mother.setName(mother.getFirstName(), record.getIndiMotherLastName());
                }
            }

            // je copie la date de naissance de la mere
            if (isChecked(RowType.IndiMotherBirthDate)) {
                copyBirthDate(mother, record.getIndiMotherBirthDate(), "", record);
            }

            // je copie la date de décès de la mere
            if (isChecked(RowType.IndiMotherDeathDate)) {
                copyDeathDate(mother, record.getIndiMotherDeathDate(), "", record);
            }

            // je met à jour la profession de la mere
            if (isChecked(RowType.IndiMotherOccupation) && !record.getIndiMotherOccupation().isEmpty()) {
                copyOccupation(mother, record.getIndiMotherOccupation(), record.getIndiMotherResidence(), record);
            }
            
        }

        //TODo ajouter les données de l'ex conjoint

    }

    /**
     * retourne une chaine de caracteres contenant le nom, prénom et la date du relevé
     * Cette chaine sert de titre a la fenetre principale
     * @return
     */
    @Override
    protected String getTitle() {
        String message = record.getIndiFirstName() + " "+ record.getIndiLastName()+ " " + record.getEventDate().getDisplayValue();
        return MessageFormat.format(NbBundle.getMessage(MergeDialog.class, "MergeModel.title.death"), message);
    }

    /**
     * retourne un resumé du modele
     * Cette chaine sert de commentaire dans la liste des modeles
     * @return
     */
    @Override
    public String getSummary(Entity selectedEntity) {
        String summary;
        if ( currentIndi == null ) {
                summary = "Nouvel individu" + " - ";
                if (getRow(MergeModel.RowType.IndiParentFamily).entityObject != null) {
                    summary += getRow(MergeModel.RowType.IndiParentFamily).entityObject.toString(false);
                } else {
                    if (getRow(MergeModel.RowType.IndiFatherLastName).entityObject != null
                            || getRow(MergeModel.RowType.IndiMotherLastName).entityObject != null) {
                        summary += "Nouveau couple:" + " ";
                        if (getRow(MergeModel.RowType.IndiFatherLastName).entityObject != null) {
                            summary += getRow(MergeModel.RowType.IndiFatherLastName).entityObject.toString(true);
                        } else {
                            summary += "Nouveau père";
                        }
                        summary += " , ";
                        if (getRow(MergeModel.RowType.IndiMotherLastName).entityObject != null) {
                            summary += getRow(MergeModel.RowType.IndiMotherLastName).entityObject.toString(true);
                        } else {
                            summary += "Nouvelle mère";
                        }
                    } else {
                        if( isChecked(MergeModel.RowType.IndiParentFamily)) {
                            summary += "Nouveaux parents";
                        } else {
                            summary += "Sans parents";
                        }
                    }
                }
        } else {
            if (selectedEntity instanceof Fam) {
                summary = "Nouvel enfant de la famille sélectionnée";
            } else {
                summary = "Modifier " + currentIndi.toString(true) + ", ";

                if (getRow(MergeModel.RowType.IndiParentFamily).entityObject != null) {
                    summary += getRow(MergeModel.RowType.IndiParentFamily).entityObject.toString(false);
                } else {
                    if (getRow(MergeModel.RowType.IndiFatherLastName).entityObject != null
                            || getRow(MergeModel.RowType.IndiMotherLastName).entityObject != null) {
                        summary += "Nouveau couple:" + " ";
                        if (getRow(MergeModel.RowType.IndiFatherLastName).entityObject != null) {
                            summary += getRow(MergeModel.RowType.IndiFatherLastName).entityObject.toString(true);
                        } else {
                            summary += "Nouveau père";
                        }
                        summary += ", ";
                        if (getRow(MergeModel.RowType.IndiMotherLastName).entityObject != null) {
                            summary += getRow(MergeModel.RowType.IndiMotherLastName).entityObject.toString(true);
                        } else {
                            summary += "Nouvelle mère";
                        }
                    } else {
                        if( isChecked(MergeModel.RowType.IndiParentFamily)) {
                            summary += "Nouveaux parents";
                        } else {
                            summary += "Sans parents";
                        }
                    }
                }
            }
        }
        return summary;
    }

}
