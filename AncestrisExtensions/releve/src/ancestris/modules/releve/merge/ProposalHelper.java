package ancestris.modules.releve.merge;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertyLatitude;
import genj.gedcom.PropertyLongitude;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySource;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import genj.util.ReferenceSet;
import java.text.MessageFormat;
import java.util.Set;

/**
 *
 * @author michel
 */


public class ProposalHelper {
    private final Entity m_selectedEntity;
    private final Gedcom m_gedcom;
    private final MergeTableAction.SourceAction m_sourceAction;
    private final MergeRecord m_record;
    private final boolean showFrenchCalendarDate = true;
    private String eventSourceTitle;


    public ProposalHelper(MergeRecord record, Entity selectedEntity, Gedcom gedcom) {
        m_selectedEntity = selectedEntity;
        m_gedcom = gedcom;
        m_record = record;
        m_sourceAction = new MergeTableAction.SourceAction(this, findSource(getEventSourceTitle()), m_gedcom );
    }

    Fam createFam() throws GedcomException {
        return (Fam) m_gedcom.createEntity(Gedcom.FAM);
    }

    Indi createIndi() throws GedcomException {
        return (Indi) m_gedcom.createEntity(Gedcom.INDI);
    }

    Source createSource() throws GedcomException {
        Source newSource = (Source) m_gedcom.createEntity(Gedcom.SOUR);
        newSource.addProperty("TITL", getEventSourceTitle());
        return (Source) m_gedcom.createEntity(Gedcom.SOUR);
    }

    String getTagName( PropertyEvent eventProperty) {
        return Gedcom.getName(eventProperty.getTag());
    }


    final Source findSource(String sourceTitle) {
        Source result = null;
        if( sourceTitle != null && !sourceTitle.isEmpty() ) {
            Entity[] sources = m_gedcom.getEntities("SOUR", "SOUR:TITL");
            for (Entity source : sources) {
                if (((Source) source).getTitle().equals(sourceTitle)) {
                    result = (Source) source;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * verifie si une source est citée dans un evenement avec la même page
     * @param source
     * @param eventProperty
     * @return
     */
    PropertySource findPropertySource(String recordSourceTitle, PropertyEvent entityEventProperty) {
        String entityEventPage = null;
        PropertySource result = null;

        if (recordSourceTitle != null && !recordSourceTitle.isEmpty() && entityEventProperty != null) {
            Property[] sourceProperties = entityEventProperty.getProperties("SOUR", false);
            for (Property sourcePropertie : sourceProperties) {
                // remarque : verification de classe PropertySource avant de faire le cast en PropertySource pour eliminer
                // les cas anormaux , par exemple une source "multiline"
                if (sourcePropertie instanceof PropertySource) {
                    Source foundSource = (Source) ((PropertySource) sourcePropertie).getTargetEntity();
                    if (foundSource != null && recordSourceTitle.compareTo(foundSource.getTitle()) == 0) {
                        result = (PropertySource) sourcePropertie;
                        // je verifie si elle contient le meme numero de page ou la meme cote
                        for (Property pageProperty : sourcePropertie.getProperties("PAGE")) {
                            if ((!m_record.getEventCote().isEmpty() && pageProperty.getValue().contains(m_record.getEventCote()))
                                    || (!m_record.getEventPage().isEmpty() && pageProperty.getValue().contains(m_record.getEventPage()))) {
                                entityEventPage = pageProperty.getValue();
                                break;
                            }
                        }
                    }
                }
                if (result != null && entityEventPage != null) {
                    break;
                }
            }

        }
        return result;

    }

    /**
     * crée une association entre associatedProperty et l'entité sélectionné
     * dans le modele
     *
     * @param associatedProperty1
     * @throws Exception
     */
    protected void copyAssociation(Property associatedProperty1, Entity entity2) throws Exception {
        PropertyXRef asso = (PropertyXRef) entity2.addProperty("ASSO", '@' + associatedProperty1.getEntity().getId() + '@');
        TagPath anchor = associatedProperty1.getPath(true);

        asso.addProperty("RELA", anchor == null ? "Présent" : "Présent" + '@' + anchor.toString());

        // je cree le lien à l'autre extermite de l'association
        try {
            asso.link();
        } catch (GedcomException e) {
            entity2.delProperty(asso);
            throw e;
        }
    }


    /**
     * ajoute un lieu a une propriete
     *
     * @param residence          juridictions du lieu à ajouter
     * @param address          adresse
     * @param eventProperty  propriete dans laquelle est ajoutée le lieu
     */
    protected void copyPlace(MergeRecord.RecordResidence residence, Property eventProperty) {
        copyPlace(residence.getPlace(), residence.getAddress(), eventProperty);
    }
    /**
     * ajoute un lieu a une propriete
     *
     * @param residence          juridictions du lieu à ajouter
     * @param address          adresse
     * @param eventProperty  propriete dans laquelle est ajoutée le lieu
     */
    protected void copyPlace(String place, String address, Property eventProperty) {
        PropertyPlace propertyPlace = (PropertyPlace) eventProperty.getProperty("PLAC");
        if (propertyPlace == null) {
            // je cree le lieu .
            propertyPlace = (PropertyPlace) eventProperty.addProperty("PLAC", "");
        }
        // je copie les juridictions
        propertyPlace.setValue(place);

        if (!place.isEmpty()) {
            //je copie les coordonnées s'il existe un lieu avec les mêmes juridicitions dans le m_gedcom
            ReferenceSet<String, Property>  gedcomPlaces = m_gedcom.getReferenceSet("PLAC");
            // je recherche les lieux avec la même juridiction
            Set<Property> similarPlaces = gedcomPlaces.getReferences(place);
            for(Property similarPlace : similarPlaces  )  {
                // je vérifie si les coordonnées sont renseignées
                if( ((PropertyPlace)similarPlace).getMap() != null) {
                    PropertyPlace similarPropertyPlace = (PropertyPlace)similarPlace;
                    PropertyLatitude latitude = similarPropertyPlace.getLatitude(true);
                    PropertyLongitude longitude = similarPropertyPlace.getLongitude(true);
                    if( latitude != null && longitude != null) {
                        // je copie les coordonnées
                        propertyPlace.setCoordinates(latitude.getValue(), longitude.getValue());
                        break;
                    }
                }
            }
        }

       // j'ajoute l'adresse
       if( !address.isEmpty() ) {
            Property propertyAddress = eventProperty.getProperty("ADDR");
            if (propertyAddress == null) {
                // je cree l'adresse
                propertyAddress = eventProperty.addProperty("ADDR", "");
            }
            propertyAddress.setValue(address);
        }
    }

    void copyPlaceCoordinates(PropertyPlace propertyPlace) {
        String placeValue = propertyPlace.getValue();

        if (!placeValue.isEmpty()) {
            //je copie les coordonnées s'il existe un lieu avec les mêmes juridicitions dans le m_gedcom
            ReferenceSet<String, Property> gedcomPlaces = m_gedcom.getReferenceSet("PLAC");
            // je recherche les lieux avec la même juridiction
            Set<Property> similarPlaces = gedcomPlaces.getReferences(placeValue);
            for (Property similarPlace : similarPlaces) {
                // je vérifie si les coordonnées sont renseignées
                if (((PropertyPlace) similarPlace).getMap() != null) {
                    PropertyPlace similarPropertyPlace = (PropertyPlace) similarPlace;
                    PropertyLatitude latitude = similarPropertyPlace.getLatitude(true);
                    PropertyLongitude longitude = similarPropertyPlace.getLongitude(true);
                    if (latitude != null && longitude != null) {
                        // je copie les coordonnées
                        propertyPlace.setCoordinates(latitude.getValue(), longitude.getValue());
                        break;
                    }
                }
            }
        }
    }

    /**
     * ajoute une NOTE a un evenement La note contient contenant les
     * informations de reference du relevé a une propriete.
     *
     * @param eventProperty
     * @param record
     */
    protected void copyReferenceNote(Property eventProperty, String subTagName) {
        String noteText = getReferenceNote(eventProperty, subTagName);

        // je recherche une NOTE deja presente dans la propriete qui
        // contiendrait deja le texte à ajouter
        Property[] notes = eventProperty.getProperties("NOTE");
        boolean found = false;
        for (Property note : notes) {
            if (note.getValue().contains(noteText)) {
                found = true;
                break;
            }
        }

        // J'ajoute le commentaire si aucune note existe deja avec le texte
        if (!found) {
            Property propertyNote = eventProperty.getProperty("NOTE");
            if (propertyNote == null) {
                // je cree une note .
                propertyNote = eventProperty.addProperty("NOTE", "");
            }

            // j'ajoute le commentaire a la fin de la note existante.
            String value = propertyNote.getValue();
            if (!noteText.isEmpty()) {
                if (!value.isEmpty()) {
                    value += "\n";
                }
                value += noteText;
                propertyNote.setValue(value);
            }
        }
    }

    /**
     * ajoute une NOTE a un evenement La note contient contenant les
     * informations de reference du relevé a une propriete.
     *
     * @param eventProperty
     * @param record
     */
    protected String getReferenceNote(Property eventProperty, String subTageName) {
        String noteText;

        String eventTagName = eventProperty.getTag();
        //String subTageName = subProperty.getTag();
        String propertyValue;
        if("DATE".equals(subTageName)) {
            PropertyDate propertyDate = (PropertyDate) eventProperty.getProperty(subTageName, false); // j'accepte les DATE ouPLAC invalides pour eviter null pointerexception
            propertyValue = propertyDate.getDisplayValue();
            if (eventTagName.equals("BIRT")) {
                noteText = "Date de naissance {0} déduite de";
            } else if (eventTagName.equals("DEAT")) {
                noteText = "Date de décès {0} déduite de";
            } else if (eventTagName.equals("MARR")) {
                noteText = "Date de mariage {0} déduite de";
            } else if (eventTagName.equals("OCCU")) {
                noteText = "Profession indiquée dans";
            } else if (eventTagName.equals("RESI")) {
                noteText = "Domicile indiqué dans";
            } else {
                noteText = "Information indiquée dans";
            }
        } else if("PLAC".equals(subTageName)) {
            String  place =    eventProperty.getPropertyValue("PLAC");
            String  address =  eventProperty.getPropertyValue("ADDR");
            propertyValue = MergeRecord.appendValue( address, place);

            if (eventTagName.equals("BIRT")) {
                noteText = "Lieu de naissance {0} indiqué dans";
            } else if (eventTagName.equals("DEAT")) {
                noteText = "Lieu de décès {0} indiqué dans";
            } else if (eventTagName.equals("MARR")) {
                noteText = "Lieu de mariage {0} indiqué dans";
            } else if (eventTagName.equals("OCCU")) {
                noteText = "Profession {0} indiquée dans";
            } else if (eventTagName.equals("RESI")) {
                noteText = "Domicile {0} indiqué dans";
            } else {
                noteText = "Lieu {0} indiquée dans";
            }
        } else {
            Property property = eventProperty.getProperty(subTageName);
            propertyValue = property.getValue();
            noteText = "Information "+ subTageName + " {0} indiquée dans";
        }

        switch (m_record.getRecordType()) {
            case BIRTH:
                noteText = MessageFormat.format(noteText + " " + "l''acte de naissance de {1} {2} le {3} ({4})",
                        propertyValue,
                        m_record.getIndi().getFirstName(),
                        m_record.getIndi().getLastName(),
                        m_record.getEventDateDDMMYYYY(showFrenchCalendarDate),
                        m_record.getEventPlaceCityName());
                break;
            case MARRIAGE:
                noteText = MessageFormat.format(noteText + " " + "l''acte de mariage de {1} {2} et {3} {4} le {5} ({6})",
                        propertyValue,
                        m_record.getIndi().getFirstName(),
                        m_record.getIndi().getLastName(),
                        m_record.getWife().getFirstName(),
                        m_record.getWife().getLastName(),
                        m_record.getEventDateDDMMYYYY(showFrenchCalendarDate),
                        m_record.getEventPlaceCityName());
                break;
            case DEATH:
                noteText = MessageFormat.format(noteText + " " + "l''acte de décès de {1} {2} le {3} ({4})",
                        propertyValue,
                        m_record.getIndi().getFirstName(),
                        m_record.getIndi().getLastName(),
                        m_record.getEventDateDDMMYYYY(showFrenchCalendarDate),
                        m_record.getEventPlaceCityName());
                break;
            default:
                String eventType = m_record.isInsinuation() ? m_record.getInsinuationType() : m_record.getEventType();
                switch (m_record.getEventTypeTag()) {
                    case WILL:
                        noteText = MessageFormat.format(noteText + " " + "l''acte de {1} de {2} {3} le {4} ({5})",
                                propertyValue,
                                eventType,
                                m_record.getIndi().getFirstName(),
                                m_record.getIndi().getLastName(),
                                m_record.getEventDateDDMMYYYY(showFrenchCalendarDate),
                                m_record.getEventPlaceCityName() + (m_record.getNotary().isEmpty() ? "" : ", " + m_record.getNotary()));
                        break;
                    case MARB:
                        noteText = MessageFormat.format(noteText + " " + "l''acte de bans de mariage entre {2} {3} et {4} {5} le {6} ({7})",
                                propertyValue,
                                eventType,
                                m_record.getIndi().getFirstName(),
                                m_record.getIndi().getLastName(),
                                m_record.getWife().getFirstName(),
                                m_record.getWife().getLastName(),
                                m_record.getEventDateDDMMYYYY(showFrenchCalendarDate),
                                m_record.getEventPlaceCityName() + (m_record.getNotary().isEmpty() ? "" : ", " + m_record.getNotary()));
                        break;
                    case MARC:
                        noteText = MessageFormat.format(noteText + " " + "l''acte de contrat de mariage entre {2} {3} et {4} {5} le {6} ({7})",
                                propertyValue,
                                eventType,
                                m_record.getIndi().getFirstName(),
                                m_record.getIndi().getLastName(),
                                m_record.getWife().getFirstName(),
                                m_record.getWife().getLastName(),
                                m_record.getEventDateDDMMYYYY(showFrenchCalendarDate),
                                m_record.getEventPlaceCityName() + (m_record.getNotary().isEmpty() ? "" : ", " + m_record.getNotary()));
                        break;
                    case MARL:
                        noteText = MessageFormat.format(noteText + " " + "l''acte de certificat de mariage entre {2} {3} et {4} {5} le {6} ({7})",
                                propertyValue,
                                eventType,
                                m_record.getIndi().getFirstName(),
                                m_record.getIndi().getLastName(),
                                m_record.getWife().getFirstName(),
                                m_record.getWife().getLastName(),
                                m_record.getEventDateDDMMYYYY(showFrenchCalendarDate),
                                m_record.getEventPlaceCityName() + (m_record.getNotary().isEmpty() ? "" : ", " + m_record.getNotary()));
                        break;
                    default:
                        noteText = MessageFormat.format(noteText + " " + "l''acte ''{1}'' entre {2} {3} et {4} {5} le {6} ({7})",
                                propertyValue,
                                eventType,
                                m_record.getIndi().getFirstName(),
                                m_record.getIndi().getLastName(),
                                m_record.getWife().getFirstName(),
                                m_record.getWife().getLastName(),
                                m_record.getEventDateDDMMYYYY(showFrenchCalendarDate),
                                m_record.getEventPlaceCityName() + (m_record.getNotary().isEmpty() ? "" : ", " + m_record.getNotary()));
                        break;
                }
                break;
        }

        return noteText;
    }

    String getEventComment() {
        return m_record.getEventComment(showFrenchCalendarDate);
    }

    final String getEventSourceTitle() {
        if (eventSourceTitle == null) {
            if (m_record.getFileName() != null) {
                eventSourceTitle = MergeOptionPanel.SourceModel.getModel().getSource(m_record.getFileName());
            } else {
                eventSourceTitle = "";
            }
        }
        return eventSourceTitle;
    }

    /**
     * retourne la position du propriété - apres BIRT - avant DEATH
     *
     * @param entity
     * @param propertyDate
     */
    protected int getPropertyBestPosition(Entity entity, PropertyDate propertyDate) {
        int resultPosition = -1;
        if ( entity != null && propertyDate != null) {
            int birthPosition;
            int position = 0;
            for (Property child : entity.getProperties()) {
                if (child.getTag().equals("BIRT")) {
                    birthPosition = position;
                    resultPosition = birthPosition + 1;
                } else if (child.getTag().equals("DEAT")) {
                    // rien à faire
                } else {
                    Property[] childDates = child.getProperties("DATE");
                    if (childDates.length > 0) {
                        try {
                            if (!MergeQuery.isRecordBeforeThanDate(propertyDate, (PropertyDate) childDates[0], 0, 0)) {
                                resultPosition = position + 1;
                            }
                        } catch (GedcomException ex) {
                            // rien a faire
                        }
                    }
                }

                position++;
            }
        }

        return resultPosition;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Accessors
    ///////////////////////////////////////////////////////////////////////////

    MergeRecord getRecord() {
        return m_record;
    }

    Entity getSelectedEntity() {
        return m_selectedEntity;
    }

    public String getEventTag(MergeRecord.EventTypeTag typeTag) {
        String tag;
        switch (typeTag) {
            case BIRT:
                tag = "BIRT";
                break;
            case DEAT:
                tag = "DEAT";
                break;
            case MARR:
                tag = "MARR";
                break;
            case MARB:
                tag = "MARB";
                break;
            case MARC:
                tag = "MARC";
                break;
            case MARL:
                tag = "MARL";
                break;
            case WILL:
                tag = "WILL";
                break;
            default:
                tag = "EVEN";
                break;
        }
        return tag;
    }

    MergeTableAction.SourceAction getSourceAction () {
        return m_sourceAction;
    }

}
