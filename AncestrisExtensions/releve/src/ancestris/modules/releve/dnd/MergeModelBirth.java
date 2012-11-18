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
class MergeModelBirth extends MergeModel {

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
    protected MergeModelBirth(MergeRecord record, Gedcom gedcom) throws Exception {
        this.record = record;
        this.currentIndi = null;
        this.gedcom = gedcom;
        addRowIndi();
        addRowParents(null);
    }

    /**
     * le constucteur initialise les données du modele
     * en comparant les champs du releve et les
     *
     * @param indi
     * @param record
     */
    protected MergeModelBirth(MergeRecord record, Gedcom gedcom, Indi indi, Fam fam) throws Exception {
        this.record = record;
        this.currentIndi = indi;
        this.gedcom = gedcom;
        addRowIndi();
        addRowParents(fam);
    }
    
    /**
     * le constucteur initialise les données du modele
     * en comparant les champs du releve et les
     *
     * @param indi
     * @param record
     */
    protected MergeModelBirth(MergeRecord record, Gedcom gedcom, Indi father, Indi mother ) throws Exception {
        this.record = record;
        this.currentIndi = null;
        this.gedcom = gedcom;

        // j'affiche l'individu 
        addRowIndi();

        // j'affiche la famille de l'enfant
        addRow(RowType.IndiParentFamily, record, null);
        addRow(RowType.IndiParentMarriageDate, record.getIndiParentMarriageDate(), null);

        // j'affiche les parents
        addRowFather(father);
        addRowMother(mother);

//        // je coche IndiFamille si au moins un attribut des parents est coché
//        if (isChecked(RowType.IndiFatherLastName) || isChecked(RowType.IndiFatherFirstName)
//                || isChecked(RowType.IndiFatherBirthDate) || isChecked(RowType.IndiFatherDeathDate)
//                || isChecked(RowType.IndiFatherOccupation)
//                || isChecked(RowType.IndiMotherLastName) || isChecked(RowType.IndiMotherFirstName)
//                || isChecked(RowType.IndiMotherBirthDate) || isChecked(RowType.IndiMotherDeathDate)
//                || isChecked(RowType.IndiMotherOccupation) ) {
//            check(RowType.IndiParentFamily, true);
//        }
    }

    private void addRowIndi() throws Exception {
       if (currentIndi != null) {
            // j'affiche la source de la naissance
            Property sourceProperty = MergeQuery.findSource(record, gedcom, currentIndi.getProperty("BIRT"));
            addRow(RowType.EventSource, record.getEventSource(), MergeQuery.findSourceTitle(sourceProperty, gedcom), MergeQuery.findSource(record, gedcom));
            addRow(RowType.EventPage, record.getEventPage(),  MergeQuery.findSourcePage(record, sourceProperty, gedcom), null);

            // j'affiche un separateur
            addRowSeparator();

            // j'affiche le nom
            addRow(RowType.IndiLastName, record.getIndiLastName(), currentIndi.getLastName(), currentIndi);
            addRow(RowType.IndiFirstName, record.getIndiFirstName(), currentIndi.getFirstName());
            addRow(RowType.IndiSex, record.getIndiSexString(), currentIndi.getPropertyValue("SEX"));
            addRow(RowType.IndiBirthDate, record.getIndiBirthDate() , currentIndi.getBirthDate());
            addRow(RowType.IndiPlace, record.getIndiPlace(), currentIndi.getValue(new TagPath("INDI:BIRT:PLAC"), ""));
            addRow(RowType.EventComment, record.getEventComment(), currentIndi.getValue(new TagPath("INDI:BIRT:NOTE"), ""));
            // j'affiche un separateur
            addRowSeparator();

        } else {
            // selectedIndi est nul

            // j'affiche la source de la naissance
            addRow(RowType.EventSource, record.getEventSource(),"" , MergeQuery.findSource(record, gedcom) );
            addRow(RowType.EventPage,       record.getEventPage(), "", null);

            // j'affiche un separateur
            addRowSeparator();

            // j'affiche le nom
            addRow(RowType.IndiLastName, record.getIndiLastName(), "");
            addRow(RowType.IndiFirstName, record.getIndiFirstName(), "");
            addRow(RowType.IndiSex, record.getIndiSexString(), "");
            addRow(RowType.IndiBirthDate, record.getIndiBirthDate() , null);
            addRow(RowType.IndiPlace, record.getIndiPlace(), "");
            addRow(RowType.EventComment, record.getEventComment(), "");
            // j'affiche un separateur
            addRowSeparator();
        }
    }

    private void addRowParents( Fam fam) throws Exception {
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
//        // je coche IndiFamille si au moins un attribut des parents est coché
//        if (isChecked(RowType.IndiFatherLastName) || isChecked(RowType.IndiFatherFirstName)
//                || isChecked(RowType.IndiFatherBirthDate) || isChecked(RowType.IndiFatherDeathDate)
//                || isChecked(RowType.IndiFatherOccupation)
//                || isChecked(RowType.IndiMotherLastName) || isChecked(RowType.IndiMotherFirstName)
//                || isChecked(RowType.IndiMotherBirthDate) || isChecked(RowType.IndiMotherDeathDate)
//                || isChecked(RowType.IndiMotherOccupation)) {
//            check(RowType.IndiParentFamily, true);
//        }
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
        Property birthProperty = currentIndi.getProperty("BIRT");
        if (isChecked(RowType.IndiBirthDate) || isChecked(RowType.EventSource) || isChecked(RowType.EventComment)) {
            if (birthProperty == null) {
                birthProperty = currentIndi.addProperty("BIRT", "");
            }
        }

        // je copie la date de naissance du releve dans l'individu
        if (isChecked(RowType.IndiBirthDate)) {
            // j'ajoute (ou remplace) la date de la naissance
            PropertyDate propertyDate = (PropertyDate) birthProperty.getProperty("DATE");
            if (propertyDate == null) {
                propertyDate = (PropertyDate) birthProperty.addProperty("DATE", "");
            }
            propertyDate.setValue(record.getIndiBirthDate().getValue());
        }

        // je copie la source de la naissance du releve dans l'individu
        if (isChecked(RowType.EventSource)) {
            copySource((Source) getRow(RowType.EventSource).entityObject, birthProperty, record);
        }

        // je copie le lieu de la naissance .
        if (isChecked(RowType.IndiPlace)) {
            copyPlace(record.getIndiPlace(),  birthProperty);
        }

        // je copie le commentaire de la naissance .
        if (isChecked(RowType.EventComment)) {
            Property propertyNote = birthProperty.getProperty("NOTE");
            if (propertyNote == null) {
                // je cree une note .
                propertyNote = birthProperty.addProperty("NOTE", "");
            }

            // j'ajoute le commentaire de la naissance au debut de la note existante.
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

            // je copie la date de décès du pere
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

    }

    /**
     * retourne une chaine de caracteres contenant le nom, prénom et la date du relevé
     * Cette chaine sert de titre a la fenetre principale
     * @return
     */
    @Override
    protected String getTitle() {
        String message = record.getIndiFirstName() + " "+ record.getIndiLastName()+ " " + record.getEventDate().getDisplayValue();
        return MessageFormat.format(NbBundle.getMessage(MergeDialog.class, "MergeModel.title.birth"), message);
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
                summary = "Nouvel enfant" + " - ";
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
                summary = "Modifier "+ currentIndi.toString(true);
//                if ( currentIndi.equals(selectedEntity)) {
//                    summary += " "+ "(Selectionné)";
//                }
            }
        }
        return summary;
    }
}
