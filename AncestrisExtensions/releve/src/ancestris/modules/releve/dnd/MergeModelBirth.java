package ancestris.modules.releve.dnd;

import ancestris.modules.releve.model.RecordBirth;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertySource;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import genj.gedcom.UnitOfWork;
import genj.gedcom.time.PointInTime;
import javax.swing.JOptionPane;
import org.openide.util.NbBundle;

/**
 *
 */
class MergeModelBirth extends MergeModel {

    Indi selectedIndi;
    RecordBirth record;
    Gedcom gedcom;

    /**
     * le constucteur initialise les données du modele
     * en comparant les champs du releve et les
     *
     * @param indi
     * @param record
     */
    MergeModelBirth(RecordBirth record, Gedcom gedcom) {
        this.record = record;
        this.selectedIndi = null;
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
    MergeModelBirth(RecordBirth record, Gedcom gedcom, Indi indi, Fam fam) {
        this.record = record;
        this.selectedIndi = indi;
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
    MergeModelBirth(RecordBirth record, Gedcom gedcom, Indi father, Indi mother ) {
        this.record = record;
        this.selectedIndi = null;
        this.gedcom = gedcom;
        addRowIndi();

        // j'affiche la famille de l'enfant
        addRow(RowType.IndiFamily, null);
        // j'affiche les parents
        addRow(RowType.IndiFatherLastName, record.getIndiFatherLastName().toString(), father.getLastName(), father);
        addRow(RowType.IndiFatherFirstName, record.getIndiFatherFirstName().toString(), father.getFirstName());
        addRow(RowType.IndiFatherBirthDate, getIndiFatherBirthDate(record), father.getBirthDate());
        addRow(RowType.IndiFatherDeathDate, getIndiFatherDeathDate(record), father.getDeathDate());
        addRow(RowType.IndiFatherOccupation, record.getIndiFatherOccupation().toString(), father.getValue(new TagPath("INDI:OCCU"), ""));
        addRow(RowType.IndiFatherComment, record.getIndiFatherComment().toString(), father.getValue(new TagPath("INDI:NOTE"), ""));

        addRow(RowType.IndiMotherLastName, record.getIndiMotherLastName().toString(), mother.getLastName(), mother);
        addRow(RowType.IndiMotherFirstName, record.getIndiMotherFirstName().toString(), mother.getFirstName());
        addRow(RowType.IndiMotherBirthDate, getIndiMotherBirthDate(record), mother.getBirthDate());
        addRow(RowType.IndiMotherDeathDate, getIndiMotherDeathDate(record), mother.getDeathDate());
        addRow(RowType.IndiMotherOccupation, record.getIndiMotherOccupation().toString(), mother.getValue(new TagPath("INDI:OCCU"), ""));
        addRow(RowType.IndiMotherComment, record.getIndiMotherComment().toString(), mother.getValue(new TagPath("INDI:NOTE"), ""));

        // je coche IndiFamille si au moins un attribut des parents est coché
        if (isChecked(RowType.IndiFatherLastName) || isChecked(RowType.IndiFatherFirstName)
                || isChecked(RowType.IndiFatherBirthDate) || isChecked(RowType.IndiFatherDeathDate)
                || isChecked(RowType.IndiFatherOccupation) || isChecked(RowType.IndiFatherComment)
                || isChecked(RowType.IndiMotherLastName) || isChecked(RowType.IndiMotherFirstName)
                || isChecked(RowType.IndiMotherBirthDate) || isChecked(RowType.IndiMotherDeathDate)
                || isChecked(RowType.IndiMotherOccupation) || isChecked(RowType.IndiMotherComment)) {
            check(RowType.IndiFamily, true);

        }
    }

    private void addRowIndi() {
       if (selectedIndi != null) {
            // j'affiche la source de la naissance
            Property birthProperty = selectedIndi.getProperty("BIRT");
            if (birthProperty != null) {
                Source[] entitySources;
                Property[] sourceProperties;
                // je recupere les sources correspond au releve
                Source[] recordSources = findSources(record, gedcom);
                // je copie les sources de l'entité
                sourceProperties = birthProperty.getProperties("SOUR", false);
                entitySources = new Source[sourceProperties.length];
                for (int i = 0; i < sourceProperties.length; i++) {
                    entitySources[i] = (Source) ((PropertySource) sourceProperties[i]).getTargetEntity();
                }
                addRow(RowType.Source, recordSources, entitySources);
            } else {
                Source[] recordSources = findSources(record, gedcom);
                addRow(RowType.Source, recordSources, new Source[0]);
            }
            // j'affiche un separateur
            addRowSeparator();

            // j'affiche le nom
            addRow(RowType.IndiLastName, record.getIndiLastName().toString(), selectedIndi.getLastName(), selectedIndi);

            // j'affiche le prénom
            addRow(RowType.IndiFirstName, record.getIndiFirstName().toString(), selectedIndi.getFirstName());

            // j'affiche le sexe
            addRow(RowType.IndiSex, record.getIndiSex().toString(), selectedIndi.getPropertyValue("SEX"));

            // j'affiche la date de naissance
            PropertyDate recordBirthDate = record.getIndiBirthDate().isEmpty() ? record.getEventDateField() : record.getIndiBirthDate().getPropertyDate();
            addRow(RowType.IndiBirthDate, recordBirthDate.getDisplayValue() , selectedIndi.getBirthAsString());

            // j'affiche le lieu de la naissance
            addRow(RowType.IndiPlace, record.getEventPlace().getValue(), selectedIndi.getValue(new TagPath("INDI:BIRT:PLAC"), ""));

            // j'affiche le commentaire de la naissance
            addRow(RowType.IndiComment, makeIndiComment(), selectedIndi.getValue(new TagPath("INDI:BIRT:NOTE"), ""));

        } else {
            // indi est nul

            // j'affiche la source de la naissance
            Source[] recordSources = findSources(record, gedcom);
            Source[] entitySources = new Source[0];
            addRow(RowType.Source, recordSources, entitySources);

            // j'affiche un separateur
            addRowSeparator();

            // j'affiche le nom
            addRow(RowType.IndiLastName, record.getIndiLastName().toString(), "");

            // j'affiche le prénom
            addRow(RowType.IndiFirstName, record.getIndiFirstName().toString(), "");

            // j'affiche le sexe
            addRow(RowType.IndiSex, record.getIndiSex().toString(), "");

            // j'affiche la date de naissance
            PropertyDate recordBirthDate = record.getIndiBirthDate().isEmpty() ? record.getEventDateField() : record.getIndiBirthDate().getPropertyDate();
            addRow(RowType.IndiBirthDate, recordBirthDate.getDisplayValue() , "");

            // j'affiche le lieu de naissance
            addRow(RowType.IndiPlace, record.getEventPlace().getValue(), "");

            // j'affiche le commentaire de la naissance
            addRow(RowType.IndiComment, makeIndiComment(), "");

        }
    }

    private void addRowParents( Fam fam) {
        if (fam != null) {
            addRow(RowType.IndiFamily, fam);
            addRow(RowType.IndiFatherLastName, record.getIndiFatherLastName().toString(), fam.getHusband().getLastName(), fam.getHusband());
            addRow(RowType.IndiFatherFirstName, record.getIndiFatherFirstName().toString(), fam.getHusband().getFirstName());
            addRow(RowType.IndiFatherBirthDate, getIndiFatherBirthDate(record), fam.getHusband().getBirthDate());
            addRow(RowType.IndiFatherDeathDate, getIndiFatherDeathDate(record), fam.getHusband().getDeathDate());
            addRow(RowType.IndiFatherOccupation, record.getIndiFatherOccupation().toString(), fam.getHusband().getValue(new TagPath("INDI:OCCU"), ""));
            addRow(RowType.IndiFatherComment, record.getIndiFatherComment().toString(), fam.getHusband().getValue(new TagPath("INDI:NOTE"), ""));

            addRow(RowType.IndiMotherLastName, record.getIndiMotherLastName().toString(), fam.getWife().getLastName(), fam.getWife());
            addRow(RowType.IndiMotherFirstName, record.getIndiMotherFirstName().toString(), fam.getWife().getFirstName());
            addRow(RowType.IndiMotherBirthDate, getIndiMotherBirthDate(record), fam.getWife().getBirthDate());
            addRow(RowType.IndiMotherDeathDate, getIndiMotherDeathDate(record), fam.getWife().getDeathDate());
            addRow(RowType.IndiMotherOccupation, record.getIndiMotherOccupation().toString(), fam.getWife().getValue(new TagPath("INDI:OCCU"), ""));
            addRow(RowType.IndiMotherComment, record.getIndiMotherComment().toString(), fam.getWife().getValue(new TagPath("INDI:NOTE"), ""));
        } else {
            addRow(RowType.IndiFamily, fam);
            addRow(RowType.IndiFatherLastName, record.getIndiFatherLastName().toString(), "");
            addRow(RowType.IndiFatherFirstName, record.getIndiFatherFirstName().toString(), "");
            addRow(RowType.IndiFatherBirthDate, getIndiFatherBirthDate(record), null);
            addRow(RowType.IndiFatherDeathDate, getIndiFatherDeathDate(record), null);
            addRow(RowType.IndiFatherOccupation, record.getIndiFatherOccupation().toString(), "");
            addRow(RowType.IndiFatherComment, record.getIndiFatherComment().toString(), "");

            addRow(RowType.IndiMotherLastName, record.getIndiMotherLastName().toString(), "");
            addRow(RowType.IndiMotherFirstName, record.getIndiMotherFirstName().toString(), "");
            addRow(RowType.IndiMotherBirthDate, getIndiMotherBirthDate(record), null);
            addRow(RowType.IndiMotherDeathDate, getIndiMotherDeathDate(record), null);
            addRow(RowType.IndiMotherOccupation, record.getIndiMotherOccupation().toString(), "");
            addRow(RowType.IndiMotherComment, record.getIndiMotherComment().toString(), "");
        }
        // je coche IndiFamille si au moins un attribut des parents est coché
        if (isChecked(RowType.IndiFatherLastName) || isChecked(RowType.IndiFatherFirstName)
                || isChecked(RowType.IndiFatherBirthDate) || isChecked(RowType.IndiFatherDeathDate)
                || isChecked(RowType.IndiFatherOccupation) || isChecked(RowType.IndiFatherComment)
                || isChecked(RowType.IndiMotherLastName) || isChecked(RowType.IndiMotherFirstName)
                || isChecked(RowType.IndiMotherBirthDate) || isChecked(RowType.IndiMotherDeathDate)
                || isChecked(RowType.IndiMotherOccupation) || isChecked(RowType.IndiMotherComment)) {
            check(RowType.IndiFamily, true);

        }

    }

    private String makeIndiComment() {
        String comment = appendValue(record.getIndiComment().toString(),
                record.getGeneralComment().toString());

        String godFather = appendValue(
                record.getWitness1FirstName().toString() + " " + record.getWitness1LastName().toString(),
                record.getWitness1Occupation().toString(),
                record.getWitness1Comment().toString());
        if (!godFather.isEmpty()) {
            if (!comment.isEmpty()) {
                comment += "\n";
            }
            comment += "Parrain" + ": " + godFather;
        }
        String godMother = appendValue(
                record.getWitness2FirstName().toString() + " " + record.getWitness2LastName().toString(),
                record.getWitness2Occupation().toString(),
                record.getWitness2Comment().toString());
        if (!godMother.isEmpty()) {
            if (!comment.isEmpty()) {
                comment += "\n";
            }
            comment += "Marraine" + ": " + godMother;
        }

        String witness = appendValue(
                record.getWitness3FirstName().toString() + " " + record.getWitness3LastName().toString(),
                record.getWitness3Occupation().toString(),
                record.getWitness3Comment().toString(),
                record.getWitness4FirstName().toString() + " " + record.getWitness4LastName().toString(),
                record.getWitness4Occupation().toString(),
                record.getWitness4Comment().toString());
        if (!witness.isEmpty()) {
            if (!comment.isEmpty()) {
                comment += "\n";
            }
            comment += "Témoin(s)" + ": " + witness;
        }
        return comment;
    }

    /**
     * retoune l'entité selectionnée
     * @return
     */
    @Override
    protected Entity getSelectedEntity() {
        return selectedIndi;
    }

    /**
     * copie les données du relevé dans l'entité
     */
    @Override
    protected void copyRecordToEntity() {
        gedcom.doMuteUnitOfWork(new UnitOfWork() {

            @Override
            public void perform(Gedcom gedcom) {
                try {
                    if (selectedIndi == null) {
                        selectedIndi = (Indi) gedcom.createEntity(Gedcom.INDI);
                    }
                    // je copie le nom du releve dans l'individu
                    if (isChecked(RowType.IndiLastName)) {
                        selectedIndi.setName(selectedIndi.getFirstName(), record.getIndiLastName().toString());
                    }
                    
                    // je copie le prénom du releve dans l'individu
                    if (isChecked(RowType.IndiFirstName)) {
                        selectedIndi.setName(record.getIndiFirstName().toString(), selectedIndi.getLastName());
                    }

                    // je copie le sex du releve dans l'individu
                    if (isChecked(RowType.IndiSex)) {
                        selectedIndi.setSex(record.getIndiSex().getSex());
                    }

                    Property birthProperty = selectedIndi.getProperty("BIRT");
                    if (isChecked(RowType.IndiBirthDate) || isChecked(RowType.Source) || isChecked(RowType.IndiComment)) {
                        if (birthProperty == null) {
                            birthProperty = selectedIndi.addProperty("BIRT", "");
                        }
                    }
                    
                    // je copie la date de naissance du releve dans l'individu
                    if (isChecked(RowType.IndiBirthDate)) {
                        // j'ajoute (ou remplace ) la date de la naissance
                        PropertyDate propertyDate = (PropertyDate) birthProperty.getProperty("DATE");
                        if (propertyDate == null) {
                            propertyDate = (PropertyDate) birthProperty.addProperty("DATE", "");
                        }
                        propertyDate.setValue(record.getEventDateField().getValue());
                    }

                    // je copie la source de la naissance du releve dans l'individu
                    if (isChecked(RowType.Source)) {
                        Source recordSource = (Source) getRow(RowType.Source).recordValue;
                        if (gedcom.contains(recordSource)) {
                            // je verifie si la source est déjà associée à la naissance
                            boolean found = false;
                            for (Source entitySource : (Source[]) getRow(RowType.Source).entityChoice) {
                                if (recordSource.equals(entitySource)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (found == false) {
                                try {
                                    // je relie la reference de la source du releve à la propriété de naissance
                                    PropertyXRef sourcexref = (PropertyXRef) birthProperty.addProperty("SOUR", "@"+recordSource.getId()+"@");
                                    sourcexref.link();
                                } catch (GedcomException ex) {
                                    throw new Exception(String.format("Link indi=%s with source=%s error=% ", selectedIndi.getName(), recordSource.getTitle(), ex.getMessage()));
                                }
                            }
                        } else {
                            // je cree une nouvelle source et je la relie à l'entité
                            Source newSource = (Source) gedcom.createEntity(Gedcom.SOUR);
                            newSource.addProperty("TITL", recordSource.getTitle());
                            try {
                                // je relie la source du releve à l'entité
                                PropertyXRef sourcexref = (PropertyXRef) birthProperty.addProperty("SOUR", "@"+newSource.getId()+"@");
                                sourcexref.link();
                            } catch (GedcomException ex) {
                                throw new Exception(String.format("Link indi=%s with source=%s error=% ", selectedIndi.getName(), recordSource.getTitle(), ex.getMessage()));
                            }
                        }
                    }

                    // je copie le lieu de la naissance .
                    if (isChecked(RowType.IndiPlace)) {
                        Property propertyPlace = birthProperty.getProperty("PLAC");
                        if (propertyPlace == null) {
                            // je cree le lieu .
                            propertyPlace = birthProperty.addProperty("PLAC","");
                        }
                        propertyPlace.setValue(getRow(RowType.IndiPlace).recordValue.toString());
                    }

                    // je copie le commentaire de la naissance .
                    if (isChecked(RowType.IndiComment)) {
                        Property propertyNote = birthProperty.getProperty("NOTE");
                        if (propertyNote == null) {
                            // je cree une note .
                            propertyNote = birthProperty.addProperty("NOTE", "");
                        }

                        // j'ajoute le commentaire de la naissance a la fin de la note existante.
                        //  composé de :
                        //     commentaire de la naissance,
                        //     commentaire genéral
                        //     parrain, marraine, et témoins
                        String value = propertyNote.getValue();
                        String comment = getRow(RowType.IndiComment).recordValue.toString();
                        if ( !comment.isEmpty()) {
                            if ( !value.isEmpty()) {
                                value += "\n";
                            }
                            value += comment;
                        }                        
                        propertyNote.setValue(value);                        
                    }

                    // je copie les données des parents
                    if (isChecked(RowType.IndiFamily)) {
                        // je mets a jour la famille des parents
                        Fam family = (Fam) dataMap.get(RowType.IndiFamily).entityValue;
                        if (family == null) {
                            // je cree la famille
                            family = (Fam) gedcom.createEntity(Gedcom.FAM);
                        }

                        // j'ajoute l'enfant dans la famille si ce n'est pas déja le cas
                        if ( !selectedIndi.isDescendantOf(family)) {
                            family.addChild(selectedIndi);
                        }

                        // je mets a jour la date du mariage si celle du releve est plus précise
                        // que celle deja presente dans l'entité
                        if ( record.getEventDateField().getStart().getYear() != PointInTime.UNKNOWN) {
                            PropertyDate marriageDate = family.getMarriageDate(true);
                            PropertyDate maxDate = new PropertyDate();
                            maxDate.setValue(String.format("BEF %d", record.getEventDateField().getStart().getYear()));
                            if ( isBestBirthDate(maxDate, marriageDate, null)) {
                                marriageDate.setValue(maxDate.getValue());
                            }
                        }

                        // je mets à jour le nom et le prenom du pere
                        Indi father = family.getHusband();
                        if (father == null) {
                            // je cree le pere
                            father = (Indi) gedcom.createEntity(Gedcom.INDI);
                            father.setName(record.getIndiFatherFirstName().toString(), record.getIndiFatherLastName().toString());
                            father.setSex(PropertySex.MALE);
                            family.setHusband(father);
                        } else {
                            if (isChecked(RowType.IndiFatherFirstName)) {
                                father.setName(record.getIndiFatherFirstName().toString(), father.getLastName());
                            }
                            if (isChecked(RowType.IndiFatherLastName)) {
                                father.setName(father.getFirstName(), record.getIndiFatherLastName().toString());
                            }
                        }

                        // je mets a jour la date de naissance du pere
                        if (isChecked(RowType.IndiFatherBirthDate)) {
                            father.getBirthDate(true).setValue(((PropertyDate) getRow(RowType.IndiFatherBirthDate).recordValue).getValue());
                        }

                        //je mets a jour la date de décès du pere
                        if (isChecked(RowType.IndiFatherDeathDate)) {
                            father.getDeathDate(true).setValue(((PropertyDate) getRow(RowType.IndiFatherDeathDate).recordValue).getValue());
                        }

                        // je mets à jour la profession du pere
                        if (isChecked(RowType.IndiFatherOccupation) && !record.getIndiFatherOccupation().isEmpty()) {
                            addOccupation(father, record.getIndiFatherOccupation().toString(),
                                    selectedIndi.getBirthDate(),
                                    record);
                        }
                        
                        // je mets à jour le commentaire du pere
                        if (isChecked(RowType.IndiFatherComment)) {
                            Property propertyNote = father.getProperty("NOTE");
                            if (propertyNote == null) {
                                // je cree une note .
                                propertyNote = father.addProperty("NOTE", "");
                            }
                            if ( !record.getIndiFatherComment().isEmpty()) {
                                String value = propertyNote.getValue();

                                if ( !value.isEmpty()) {
                                    value += "\n";
                                }
                                value += record.getIndiFatherComment();
                                propertyNote.setValue(value);
                            }
                        }

                        // je mets à jour le nom et le prenom de la mere
                        Indi mother = family.getWife();
                        if (mother == null) {
                            // je cree la mere
                            mother = (Indi) gedcom.createEntity(Gedcom.INDI);
                            mother.setName(record.getIndiMotherFirstName().toString(), record.getIndiMotherLastName().toString());
                            mother.setSex(PropertySex.FEMALE);
                            family.setWife(mother);
                        } else {
                            if (isChecked(RowType.IndiMotherFirstName)) {
                                mother.setName(record.getIndiMotherFirstName().toString(), mother.getLastName());
                            }
                            if (isChecked(RowType.IndiMotherLastName)) {
                                mother.setName(mother.getFirstName(), record.getIndiMotherLastName().toString());
                            }
                        }

                        // je mets a jour la date de naissance de la mere
                        if (isChecked(RowType.IndiMotherBirthDate)) {
                            mother.getBirthDate(true).setValue(((PropertyDate) getRow(RowType.IndiMotherBirthDate).recordValue).getValue());
                        }

                        // je mets a jour la date de décès de la mere
                        if (isChecked(RowType.IndiMotherDeathDate)) {
                            mother.getDeathDate(true).setValue(((PropertyDate) getRow(RowType.IndiMotherDeathDate).recordValue).getValue());
                        }

                        // je met à jour la profession de la mere
                        if (isChecked(RowType.IndiMotherOccupation) && !record.getIndiMotherOccupation().isEmpty()) {
                            addOccupation(mother, record.getIndiMotherOccupation().toString(), 
                                    selectedIndi.getBirthDate(),
                                    record);
                        }

                        //je mets à jour le commentaire de la mere
                        if (isChecked(RowType.IndiMotherComment)) {
                            Property propertyNote = mother.getProperty("NOTE");
                            if (propertyNote == null) {
                                // je cree une note .
                                propertyNote = mother.addProperty("NOTE", "");
                            }
                            if ( !record.getIndiMotherComment().isEmpty()) {
                                String value = propertyNote.getValue();
                                if ( !value.isEmpty()) {
                                    value += "\n";
                                }
                                value += record.getIndiMotherComment();
                                propertyNote.setValue(value);
                            }
                        }
                    }
                } catch (Exception ex1) {
                    ex1.printStackTrace();
                    JOptionPane.showMessageDialog(null, ex1.getMessage(), getTitle(), JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    
    /**
     * retourne une chaine de caracteres contenant le nom, prénom et la date du relevé
     * Cette chaine sert de titre a la fenetre principale
     * @return
     */
    @Override
    protected String getTitle() {
        String message = record.getIndiFirstName() + " "+ record.getIndiLastName()+ " " + record.getEventDateString();
        return String.format(NbBundle.getMessage(MergeDialog.class, "MergeModel.title.birth"), message);
    }

    


}
