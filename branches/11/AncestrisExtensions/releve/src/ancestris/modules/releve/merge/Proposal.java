package ancestris.modules.releve.merge;

import ancestris.modules.releve.merge.ProposalRule.CompareResult;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertySource;
import genj.gedcom.Source;
import org.openide.util.NbBundle;

/**
 *
 * @author michel
 */

public class Proposal implements java.lang.Comparable<Proposal> , MergeTableAction.SourceUpdateListener {

    private final ProposalRuleList m_displayRuleList;
    private ARuleEntity<?,?,?> m_ruleRoot;
    private final ProposalHelper m_helper;

    //birth
    public Proposal(ProposalHelper proposalHelper, Indi indi, Fam parentFamily, Indi father, Indi mother ) throws Exception {
        this(proposalHelper, indi, null, null, parentFamily, father, mother);
    }

    //death
    public Proposal(ProposalHelper proposalHelper, Indi indi, Fam marriedFamily, SpouseTag marriedTag, Fam parentFamily, Indi father, Indi mother ) throws Exception {
        this(proposalHelper, proposalHelper.getRecord().getIndi(), indi, marriedFamily, marriedTag, parentFamily, father, mother);
    }

    //proposal Indi
    public Proposal(ProposalHelper proposalHelper, MergeRecord.RecordParticipant participant,
            Indi indi, Fam marriedFamily, SpouseTag marriedTag, Fam parentFamily, Indi father, Indi mother ) throws Exception {
        this(proposalHelper, indi);

        if (indi != null && parentFamily == null) {
            parentFamily = indi.getFamilyWhereBiologicalChild();
        }

        if (parentFamily != null) {
            if( father == null) {
                father = parentFamily.getHusband();
            }
            if( mother == null) {
                mother = parentFamily.getWife();
            }
        }

        m_ruleRoot = new RuleIndi(null, participant, indi, marriedFamily, marriedTag, parentFamily, father, mother);
        if( participant.getParticipantType() == MergeRecord.MergeParticipantType.participant1) {
            // j'affiche l'evenement principal et la source uniquement dans la proposition de du participant1
            initDisplayMainEvent();
        }
        initDisplay(m_ruleRoot, true);
    }


    //Famille
    public Proposal(ProposalHelper proposalHelper, Fam family,
            Indi husband, Fam husbandParentFamily, Indi husbandFather, Indi husbandMother,
            Indi wife, Fam wifeParentFamily, Indi wifeFather, Indi wifeMother) throws Exception {
        this(proposalHelper, family);

        if( family != null) {
            if( husband == null) {
                husband = family.getHusband();
            }
            if( wife == null) {
                wife = family.getWife();
            }
        }

        if (husband != null) {
            if( husbandParentFamily == null) {
                husbandParentFamily = husband.getFamilyWhereBiologicalChild();
            }
        }
        if (husbandParentFamily != null) {
            if( husbandFather == null) {
                husbandFather = husbandParentFamily.getHusband();
            }
            if( husbandMother == null) {
                husbandMother = husbandParentFamily.getWife();
            }
        }

        if (wife != null) {
            if( wifeParentFamily == null) {
                wifeParentFamily = wife.getFamilyWhereBiologicalChild();
            }
        }

        if (wifeParentFamily != null) {
            if( wifeFather == null ) {
                wifeFather = wifeParentFamily.getHusband();
            }
            if( wifeMother == null) {
                wifeMother = wifeParentFamily.getWife();
            }
        }

        m_ruleRoot = new RuleFamily(proposalHelper.getRecord().getFamily(), family,
                husband, null, husbandParentFamily, husbandFather, husbandMother,
                wife, null, wifeParentFamily, wifeFather, wifeMother);

        initDisplayMainEvent( );

        // j'affiche la famille et le mariage
        initDisplay(m_ruleRoot, true);

    }

    private void initDisplayMainEvent ( ) {
        if (m_helper.getRecord().isInsinuation()) {

            // j'affiche l'insinuation
            RuleEvent ruleInsinuation = new RuleEvent(m_ruleRoot,
                    MergeRecord.EventTypeTag.EVEN,
                    m_helper.getRecord().getInsinuationType(),
                    m_helper.getRecord().getInsinuationDate(),
                    m_helper.getRecord().getEventResidence(),
                    true  // evenement principal
            );

            // j'affiche la source
            initDisplay(ruleInsinuation.findSubRule(RuleSource.class), true);
            // separateur
            initDisplay(new RuleSeparator(m_ruleRoot), true);
            // j'affiche l'insinuation
            initDisplay(ruleInsinuation, true);
            // separateur
            initDisplay(new RuleSeparator(m_ruleRoot), true);
            // evenement insinué
            initDisplay(new RuleEvent(m_ruleRoot,
                    m_helper.getRecord().getEventTypeTag(),
                    m_helper.getRecord().getEventType(),
                    m_helper.getRecord().getEventDate(),
                    null, // je ne connais pas le lieu de l'evenement insinué
                    false // ce n'est pas l'evenement principal
                 ),
              true);

        } else {

            // evenement principal
            if (m_helper.getRecord().getEventTypeTag() != MergeRecord.EventTypeTag.BIRT
                    && m_helper.getRecord().getEventTypeTag() != MergeRecord.EventTypeTag.DEAT
                    && m_helper.getRecord().getEventTypeTag() != MergeRecord.EventTypeTag.MARR
                    ) {
                // je cree l'evenement principal autre que BIRT, DEAT ou MARR
                RuleEvent ruleEvent = new RuleEvent(m_ruleRoot,
                        m_helper.getRecord().getEventTypeTag(),
                        m_helper.getRecord().getEventType(),
                        m_helper.getRecord().getEventDate(),
                        m_helper.getRecord().getEventResidence(),
                        true);

                // j'affiche la source
                initDisplay(m_ruleRoot.findSubRule(RuleSource.class), true);
                // separateur
                initDisplay(new RuleSeparator(m_ruleRoot), true);
                // j'affiche l'evenement principal
                initDisplay(ruleEvent, true);
            } else {
                // l'evenement principal BIRT, DEAT ou MARR est affiché par m_ruleRoot
                // pour occuper moins de place dans la fenetre
                // j'affiche la source
                initDisplay(m_ruleRoot.findSubRule(RuleSource.class), true);
            }
        }
        // separateur
        initDisplay(new RuleSeparator(m_ruleRoot), true);
    }

    // common constructor
    protected Proposal(ProposalHelper helper, Entity proposedEntity) {
        m_helper = helper;
        m_displayRuleList = new ProposalRuleList();
        initListener();
    }

    private void initListener() {
        m_helper.getSourceAction().addSourceListener(this);
    }

    private void initDisplay(ProposalRule rule, boolean display) {
        rule.m_display = display;
        m_displayRuleList.add(rule, display);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Accessors
    ///////////////////////////////////////////////////////////////////////////

    ProposalRuleList getDisplayRuleList () {
        return m_displayRuleList;
    }

    boolean isSelectedEntityProposed() {
        return m_ruleRoot.getEntity() == m_helper.getSelectedEntity();
    }

    Entity getMainEntity() {
        return m_ruleRoot.getEntity();
    }

    Property getMainEvent() {
        Property eventProperty = null;

        ProposalRule commentRule = m_ruleRoot.findSubRule(RuleComment.class);
        if (commentRule != null) {
            eventProperty = ((RuleComment) commentRule).getSuperRule().getEventProperty();
        }
        return eventProperty;
    }


    protected String getEventComment() {
        return m_helper.getEventComment();
    }


//s

    public MergeInfo getMergeInfo() {
        MergeInfo info = new MergeInfo();
        m_ruleRoot.getInfo(info);
        return info;
    }


    public String getSummary(final boolean html) {

        MergeInfo.InfoFormatter formatter = new MergeInfo.InfoFormatter() {
            @Override
            public String getSeparator() {
                if( html ) {
                    return "</li><li>";
                } else {
                    return ", ";
                }
            }

            @Override
            public Object format(Object arg) {
                StringBuilder stringArgs = new StringBuilder();
                if ( arg == null) {
                    stringArgs.append("");
                } else if ( arg instanceof ProposalRule) {
                    ProposalRule rule = (ProposalRule) arg;
                    MergeInfo info = new MergeInfo();
                    rule.getInfo(info);
                    stringArgs.append( info.toString(this) );
                } else if ( arg instanceof Indi) {
                    // prénom, NOM, date de naissance (id)
                    Indi indi = (Indi) arg;
                    stringArgs.append(indi.getFirstName()).append(" ").append(indi.getLastName());
                    if (indi.getBirthDate() != null && indi.getBirthDate().isValid()) {
                        stringArgs.append(" °");
                        if (indi.getBirthDate().getFormat().getPrefix1Name() != null) {
                            stringArgs.append(indi.getBirthDate().getFormat().getPrefix1Name()).append(" ");
                        }
                        stringArgs.append(indi.getBirthDate().getStart().getYear());
                    }
                    stringArgs.append(" (").append(indi.getId()).append(")");

                } else if ( arg instanceof Fam) {
                    stringArgs.append( ((Fam)  arg).getDisplayValue() );
                    stringArgs.append("(").append(((Fam)arg).getId()).append(")");
                } else if (arg instanceof Entity) {
                    stringArgs.append( ((Entity)  arg).getId() );
                } else {
                    stringArgs.append( arg.toString() );
                }
                return stringArgs.toString().trim();
            }

        };

        if( html ) {
            StringBuilder summary = new StringBuilder();
            summary.append( "<html>") ;
            if ( m_displayRuleList.getNbConflict() >0 && m_helper.getSelectedEntity() != null) {
                summary.append("<font color=\"red\">")
                    .append(NbBundle.getMessage(Proposal.class, "Summary.conflict1", m_helper.getSelectedEntity().toString() ))
                    .append("<br>")
                    .append(NbBundle.getMessage(Proposal.class, "Summary.conflict2"))
                    .append("</font>")
                    .append("<br><br>");
            }

            Entity proposedEntity = getMainEntity();
            if( proposedEntity != null ) {
                summary.append(NbBundle.getMessage(Proposal.class, "Summary.fit1", proposedEntity.toString() ));
            } else {
                summary.append(NbBundle.getMessage(Proposal.class, "Summary.fit2"));
            }


            summary.append("<ul>");
            if (m_displayRuleList.getNbEqual() >0) {
                summary.append("<li>")
                        .append(NbBundle.getMessage(Proposal.class, "Summary.nbEqual", m_displayRuleList.getNbEqual()) )
                        .append("</li>");
            }
            if (m_displayRuleList.getNbCompatibleChecked() >0) {
                summary.append("<li>")
                        .append(NbBundle.getMessage(Proposal.class, "Summary.nbCompatible1", m_displayRuleList.getNbCompatibleChecked()) )
                        .append("</li>");
            }
            if (m_displayRuleList.getNbCompatibleNotChecked() >0) {
                summary.append("<li>")
                        .append(NbBundle.getMessage(Proposal.class, "Summary.nbCompatible2", m_displayRuleList.getNbCompatibleNotChecked() ))
                        .append("</li>");
            }
            if (m_displayRuleList.getNbConflict() >0) {
                summary.append("<li><font color=\"red\">")  //
                        .append(NbBundle.getMessage(Proposal.class, "Summary.nbConflict" , m_displayRuleList.getNbConflict()) )
                        .append("</li></font>");  //</font>
            }
            summary.append( "</ul>");

            summary.append (NbBundle.getMessage(ProposalRule.class, "Summary.copy"));
            summary.append( "<ul><li>" );
            summary.append( getMergeInfo().toString(formatter) );
            summary.append( "</li></ul>" );
            summary.append( "</html>") ;
            return summary.toString();
        } else {
            return getMergeInfo().toString(formatter) ;
        }

    }


    ///////////////////////////////////////////////////////////////////////////
    // methods
    ///////////////////////////////////////////////////////////////////////////

    public boolean equalAs(Proposal that) {
        return this.m_ruleRoot.equalAs(that.m_ruleRoot);
    }

    void copyRecordToEntity() throws Exception {
        m_ruleRoot.copyRecordToEntity();
    }

    /**
     * compare le nombre de champs egaux de la proposition avec celui d'une autre proposition
     * pour savoir quel est la proposition correspondant le mieux au relevé
     *
     * @param object
     * @return
     */
    @Override
    public int compareTo(Proposal that) {
        int nombre1 = that.getDisplayRuleList().getNbEqual();
        int nombre2 = this.getDisplayRuleList().getNbEqual();
        if (nombre1 > nombre2) {
            return 1;
        } else if (nombre1 == nombre2) {
            // compare le nombre de  champs plus precis
            nombre1 = that.getDisplayRuleList().getNbCompatibleNotChecked();
            nombre2 = this.getDisplayRuleList().getNbCompatibleNotChecked();
            if (nombre1 > nombre2) {
                return 1;
            } else if (nombre1 == nombre2) {
                nombre1 = that.getDisplayRuleList().getNbCompatibleChecked();
                nombre2 = this.getDisplayRuleList().getNbCompatibleChecked();
                if (nombre1 > nombre2) {
                    return 1;
                } else if (nombre1 == nombre2) {
                    return 0;
                } else {
                    return -1;
                }
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    @Override
    public void sourceUpdated(Source source) {
        RuleSource ruleSource =(RuleSource) m_ruleRoot.findSubRule(RuleSource.class);
        ruleSource.sourceUpdated(source);
        //m_displayRuleList.fireTableDataChanged();
        m_displayRuleList.fireTableRowsUpdated(0, 0);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Abstract rules
    ///////////////////////////////////////////////////////////////////////////

    abstract class ARule<P extends ProposalRule > extends ProposalRule {
        private P m_SuperRule;
        ARule(P superRule) {
            m_SuperRule = superRule;
            initSuper();
        }

        private void initSuper() {
            if (m_SuperRule != null) {
                m_SuperRule.getSubRules().add(this);
            }
        }

        @Override
        public final P getSuperRule() {
            return  m_SuperRule;
        }
    }

    /**
     * Modele de regle pour comparer une propriete du releve avec une propiete du gedcom
     * @param <P>  regle parent
     * @param <V>  type des proprietes
     */
    abstract class ARuleValue<P extends ProposalRule, V> extends ARule<P> {
        protected V m_recordValue;
        protected V m_entityValue;

        ARuleValue(P superRule) {
            super(superRule);
        }

        @Override
        public Object getDisplayRecord() {
            return m_recordValue;
        }

        @Override
        public Object getDisplayEntity() {
            return m_entityValue;
        }
    }

    /**
     * Modele de regle pour comparer une Entite du releve avec une Entite du gedcom
     * @param <P> regle parent
     * @param <R> entite du releve
     * @param <E> entite du gedcom
     */
    abstract class ARuleEntity<P extends ProposalRule, R extends MergeRecord.RecordEntity, E extends Entity>  extends ARule<P> {
        private final R m_recordEntity;
        private E m_entity;
        private MergeTableAction m_action;

        ARuleEntity(P superRule, R recordEntity,  E entity) {
            super(superRule);
            m_recordEntity = recordEntity;
            m_entity = entity;
            m_action = null;
        }

        public final R getRecordEntity() {
            return m_recordEntity;
        }

        public final E getEntity() {
            return m_entity;
        }

        public void setEntity(E entity) {
            m_entity = entity;
        }

        @Override
        public boolean equalAs(ProposalRule that) {
            boolean result;
            if (that instanceof ARuleEntity) {
                Entity entity1 = this.getEntity();
                Entity entity2 = ((ARuleEntity) that).getEntity();
                if (entity1 == null) {
                    result = entity1 == entity2;
                } else {
                    if (entity2 == null) {
                        result = false;
                    } else {
                        result = entity1.getId().equals(entity2.getId());
                    }
                }
                if( result == true) {
                    // en cas d'égalité je compare les sous regles
                    result =  super.equalAs(that);
                }
            } else {
                result = false;
            }

            return result;
        }

        public void createDisplayAction(CompareResult compareResult) {
            if( compareResult != CompareResult.NOT_APPLICABLE) {
                m_action = new MergeTableAction.EntityAction(getEntity(), getRuleLabel(getLabelResourceName()+".New"));
            }
        }

        @Override
        public MergeTableAction getDisplayAction() {
            return m_action;
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Rules
    ///////////////////////////////////////////////////////////////////////////

    class RuleComment extends ARuleValue<RuleEvent,String> {
        private Property m_propertyNote ;

        RuleComment(RuleEvent superRule) {
            super(superRule);
            this.m_recordValue = m_helper.getEventComment();
            Property eventProperty = getSuperRule().getEventProperty();
            this.m_entityValue = eventProperty==null ? "" : eventProperty.getPropertyValue("NOTE");

            if (m_recordValue.isEmpty()) {
                m_merge = false;
                m_compareResult = CompareResult.NOT_APPLICABLE;
            } else {
                if (m_entityValue.isEmpty()) {
                    m_merge = !m_recordValue.equals(m_entityValue);
                    m_compareResult = m_merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
                } else {
                    // m_merge actif si le commentaire existant dans l'entité ne contient pas deja le commentaire du relevé.
                    m_merge = !m_entityValue.contains(m_recordValue);
                    m_compareResult = !m_recordValue.equals(m_entityValue) ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
                }
            }
        }

        @Override
        public void copyRecordToEntity() throws Exception {
            if (isChecked()) {
                // commentaire complet de l'evenement
                String comment = m_helper.getEventComment();
                if (!comment.isEmpty()) {
                    Property birthProperty = getSuperRule().getEventProperty();
                    m_propertyNote = birthProperty.getProperty("NOTE");
                    if (m_propertyNote == null) {
                        // je cree une note .
                        m_propertyNote = birthProperty.addProperty("NOTE", "");
                    }
                    // j'ajoute le commentaire de la naissance au debut de la note existante.
                    String value = m_propertyNote.getValue();
                    if (!value.isEmpty()) {
                        comment += "\n";
                    }
                    comment += value;
                    m_propertyNote.setValue(comment);
                }
            }
        }

        @Override
        public String getLabelResourceName() {
            return "Proposal.EventComment" ;
        }
    }


    class RuleDate extends ARuleValue<RuleEvent, PropertyDate> {

        RuleDate(RuleEvent superRule, PropertyDate recordDate) {
            super(superRule);
            // je copie la valeur car la date peut être modifiee
            m_recordValue = new PropertyDate();
            PropertyDate tempDate = recordDate;
            m_recordValue.setValue(tempDate.getFormat(),
                    tempDate.getStart(),
                    tempDate.getEnd(),
                    tempDate.getPhrase());

            this.m_entityValue = getSuperRule().getEventProperty() == null ?  null : (PropertyDate) getSuperRule().getEventProperty().getProperty("DATE", false) ;

            // je compare les valeurs par defaut du releve et de l'entite
            if (!m_recordValue.isComparable()) {
                m_merge = false;
                m_compareResult = CompareResult.NOT_APPLICABLE;
            } else if (m_entityValue == null || !m_entityValue.isComparable()) {
                // j'active Merge seulement si la date du releve est comparable
                m_merge = m_recordValue.isComparable();
                m_compareResult = m_merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
            } else {
                if (m_recordValue.getValue().equals(m_entityValue.getValue())) {
                    // les valeurs sont egales, pas besoin de merger
                    m_merge = false;
                    m_compareResult = CompareResult.EQUAL;
                } else {
                    PropertyDate bestDate = MergeQuery.getMostAccurateDate(m_recordValue, m_entityValue);
                    if (bestDate == null) {
                        m_merge = false;
                        m_compareResult = CompareResult.CONFLICT;
                    } else if (bestDate == m_entityValue) {
                        m_merge = false;
                        m_compareResult = MergeQuery.isCompatible(m_recordValue, m_entityValue) ? CompareResult.COMPATIBLE : CompareResult.CONFLICT;
                    } else {
                        // je propose une date plus precise que celle du releve
                        m_recordValue.setValue(bestDate.getFormat(), bestDate.getStart(), bestDate.getEnd(), bestDate.getPhrase());
                        m_merge = true;
                        m_compareResult = CompareResult.COMPATIBLE;
                    }
                }
            }
        }

        @Override
        public void copyRecordToEntity() throws Exception {
            if (isChecked()) {
                Property eventProperty = getSuperRule().getEventProperty();
                // je copie la date du releve dans l'individu
                PropertyDate propertyDate = (PropertyDate) eventProperty.getProperty("DATE", false);
                if (propertyDate == null) {
                    propertyDate = (PropertyDate) eventProperty.addProperty("DATE", "");
                }
                // je copie la date du relevé
                propertyDate.setValue(m_recordValue.getValue());
            }
        }

        @Override
        public String getLabelResourceName() {
            switch( getSuperRule().getEventTag() ) {
                case BIRT:
                    return "Proposal.BirthDate" ;
                case DEAT:
                    return "Proposal.DeathDate" ;
                case MARR:
                    return "Proposal.MarriageDate" ;
                default:
                    return "Proposal.EventInsinuationDate" ;
            }
        }
    }


    class RuleEvent extends ARuleValue<ARuleEntity<?,?,?>, String> {

        private final MergeRecord.EventTypeTag m_eventTag;
        private PropertyEvent m_eventProperty;

        /**
         * niassanceevenTypeLabel@param superRule
         * @param recordValue
         * @param eventTag
         * @param recordEventDate
         * @param recordEventResidence
         * @param mainEvent
         */
        RuleEvent(ARuleEntity<?,?,?> superRule,
                MergeRecord.EventTypeTag eventTag,
                String evenTypeLabel,
                PropertyDate recordEventDate,
                MergeRecord.RecordResidence recordEventResidence,
                boolean mainEvent) {
            super(superRule);
            m_eventTag = eventTag ;

            // je renseigne m_eventProperty
            Entity proposedEntity = superRule.getEntity();
            if (proposedEntity != null) {
                if (m_eventTag== MergeRecord.EventTypeTag.BIRT || m_eventTag== MergeRecord.EventTypeTag.DEAT  ) {
                    // je cherche l'evenement sans controler la date car il ne peut y avoir qu'un seul evenement BIRT ou DEAT
                     m_eventProperty = (PropertyEvent) proposedEntity.getProperty(m_helper.getEventTag(m_eventTag));
                } else {
                    // je cherche l'evenement en fonction de la date car il peut exister plusieurs évènements avec le même tag

                    String tag = m_helper.getEventTag(eventTag);
                    if (recordEventDate.isComparable()) {
                        for (Property iterationEvent : proposedEntity.getProperties(tag)) {

                            if (evenTypeLabel != null && evenTypeLabel.isEmpty()) {
                                String entityEventType = iterationEvent.getPropertyValue("TYPE").trim();
                                if (!evenTypeLabel.equalsIgnoreCase(entityEventType)) {
                                    continue;
                                }
                            }
                            // je recherche les dates meme si elles ne sont pas valides
                            for (Property iterationProperty : iterationEvent.getProperties("DATE", false)) {
                                PropertyDate iterationDate = (PropertyDate) iterationProperty;
                                if (MergeQuery.isCompatible(recordEventDate, iterationDate)) {
                                    m_eventProperty = (PropertyEvent) iterationEvent;
                                    break;
                                }
                            }
                            if (m_eventProperty != null) {
                                break;
                            }
                        }
                    }
                }
            } else {
                m_eventProperty = null;
            }

            // je renseigne m_recordValue et m_entityValue
            m_recordValue = evenTypeLabel;
            if (m_eventProperty != null) {
                if( eventTag == MergeRecord.EventTypeTag.EVEN) {
                    m_entityValue = m_eventProperty.getPropertyValue("TYPE");
                } else {
                    m_entityValue = m_helper.getTagName(m_eventProperty);
                }
            } else {
                m_entityValue = "";
            }

            // je renseigne m_merge et m_compareResult
            if (mainEvent) {
                m_merge = true;
                m_compareResult = CompareResult.MANDATORY;
            } else {
                if (m_recordValue != null) {
                    if (m_recordValue.isEmpty()) {
                        m_merge = false;
                        m_compareResult = CompareResult.NOT_APPLICABLE;
                    } else {
                        m_merge = !m_recordValue.equals(m_entityValue);
                        m_compareResult = m_merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
                    }
                } else {
                    m_merge = true;
                    m_compareResult = CompareResult.COMPATIBLE;
                }
            }

            // j'ajoute les sous regles
            RuleDate ruleDate = new RuleDate(this, recordEventDate);
            addSubRule(ruleDate, true);
            boolean mergeFalse = ruleDate.getMerge() == false && ruleDate.getCompareResult() != CompareResult.EQUAL;

            if( recordEventResidence != null) {
                RulePlace rulePlace = new RulePlace(this, recordEventResidence);
                addSubRule(rulePlace, true);
                mergeFalse &= rulePlace.getDisplayEntity() != null && !rulePlace.getDisplayEntity().toString().isEmpty();
            }
            if( mainEvent) {
                addSubRule(new RuleComment(this), true);
                addSubRule(new RuleSource(this), true);
            }

            if (mergeFalse) {
                // le lieu, le commentaire et la source ne sont pas modifiés par defaut si la date du releve n'est pas plus precise
                // et si le lieu du releve est deja renseigné
                for (int index = 0; index < this.getSubRules().size(); index++) {
                    this.getSubRules().get(index).setMerge(false);
                }
            }
        }

        @Override
        public void copyRecordToEntity() throws Exception {
            if (isChecked() ) {
                if (getEventProperty() == null) {
                    // je cree la propriete de l'evenement si elle n'existait pas
                    RuleDate ruleDate = (RuleDate) findSubRule(RuleDate.class);
                    int position = m_helper.getPropertyBestPosition(getSuperRule().getEntity(), ruleDate.m_recordValue);
                    m_eventProperty = (PropertyEvent) getSuperRule().getEntity().addProperty(m_helper.getEventTag(m_eventTag), "", position);
                }
                // je copie le type d'évènement
                if (m_eventTag ==  MergeRecord.EventTypeTag.EVEN) {
                    // j'ajoute la propriete TYPE si c'est un tag EVEN (par besoin pour WILL, MARR, etc.)
                    Property propertyType = m_eventProperty.getProperty("TYPE");
                    if (propertyType == null) {
                        propertyType = m_eventProperty.addProperty("TYPE", "");
                    }
                    propertyType.setValue(m_recordValue);
                }

                // je copie les sous données
                super.copyRecordToEntity();

                // je copie le commentaire de reference s'il n'y a pas de commentaire principal
                if( findSubRule(RuleComment.class) == null) {
                    for (ProposalRule rule : getSubRules()) {
                        if (rule.isChecked()) {
                            if (rule instanceof RuleDate) {
                                m_helper.copyReferenceNote(m_eventProperty, "DATE");
                                break;
                            }
                            if (rule instanceof RulePlace) {
                                m_helper.copyReferenceNote(m_eventProperty, "PLAC");
                                break;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public String getLabelResourceName() {
            if (m_helper.getRecord().isInsinuation() && findSubRule(RuleComment.class) == null) {
                // evenement insinué
                return "Proposal.EventInsinuated";
            } else {
                // evenement principal
                return "Proposal.EventType";
            }
        }

        public PropertyEvent getEventProperty() {
            return m_eventProperty;
        }

        public final MergeRecord.EventTypeTag getEventTag() {
            return m_eventTag;
        }

        @Override
        public String toString() {
            return m_eventTag.name() + " " + m_recordValue;
        }
    }


    class RuleFamily extends ARuleEntity<RuleIndi, MergeRecord.RecordFamily, Fam> {
        private final RuleIndi m_ruleHusband;
        private final RuleIndi m_ruleWife;

        // famille principale (superRule = null)
        RuleFamily(MergeRecord.RecordMarriageFamily recordFamily, Fam family,
                Indi husband,  Fam husbandMarriedFamily, Fam husbandParentFamily, Indi husbandFather, Indi husbandMother,
                Indi wife, Fam wifeMarriedFamily, Fam wifeParentFamily, Indi wifeFather, Indi wifeMother) throws Exception {
            super(null, recordFamily, family);

            m_merge = true;
            m_compareResult = CompareResult.MANDATORY;
            createDisplayAction(m_compareResult);

            if( m_helper.getRecord().getEventTypeTag() == MergeRecord.EventTypeTag.MARR) {
                // c'est l'evenement principal
                addSubRule( new RuleEvent(this, MergeRecord.EventTypeTag.MARR, null,
                        m_helper.getRecord().getEventDate(),
                        m_helper.getRecord().getEventResidence(), true ), false);
            } else {
                m_compareResult = CompareResult.COMPATIBLE;
                addSubRule(new RuleEvent(this, MergeRecord.EventTypeTag.MARR, null,
                        m_helper.getRecord().calculateMariageDateFromMarc(m_helper.getRecord().getEventDate()),
                        null, false), false);
            }

            m_ruleHusband = new RuleIndi(this, m_helper.getRecord().getIndi(), husband, husbandMarriedFamily, SpouseTag.HUSB, husbandParentFamily, husbandFather, husbandMother);
            addSubRule(m_ruleHusband, true);

            addSubRule(new RuleSeparator(this) , true);
            m_ruleWife = new RuleIndi(this, m_helper.getRecord().getWife(), wife, wifeMarriedFamily, SpouseTag.WIFE, wifeParentFamily, wifeFather, wifeMother);
            addSubRule(m_ruleWife, true);
        }

        // famille Parent (superRule != null)
        RuleFamily(RuleIndi superRule, MergeRecord.RecordParentFamily recordParentFamily, Fam parentFamily,
                Indi indiFather, Indi indiMother) throws Exception {
            super(superRule, recordParentFamily, parentFamily);

            if (parentFamily != null) {
                m_merge = true;
                m_compareResult = CompareResult.COMPATIBLE;
            } else {
                if (( recordParentFamily.getHusband().getLastName().isEmpty() && recordParentFamily.getWife().getLastName().isEmpty()) ) {
                    // je ne propose pas la creation d'un nouvelle famille si le nom du pere et de la mere sont vides.
                    m_merge = false;
                    m_compareResult = CompareResult.NOT_APPLICABLE;
                } else {
                    m_merge = true;
                    m_compareResult = CompareResult.COMPATIBLE;
                }
            }
            createDisplayAction(m_compareResult);

            //mariage
            addSubRule( new RuleEvent(this, MergeRecord.EventTypeTag.MARR,  null, recordParentFamily.getMarriageDate(), null, false), false);
            // parents
            m_ruleHusband = new RuleIndi(this, recordParentFamily.getHusband(), indiFather);
            addSubRule(m_ruleHusband, true);
            m_ruleWife = new RuleIndi(this, recordParentFamily.getWife(), indiMother);
            addSubRule(m_ruleWife, true);
        }

        @Override
        public void copyRecordToEntity() throws Exception {
            if(isChecked()) {
                // je cree la famille
                Fam family = getEntity();
                if (family== null) {
                    family = m_helper.createFam();
                    setEntity(family );
                }

                // je cree les mariés et leurs parents
                super.copyRecordToEntity();

                if (getSuperRule() != null) {
                    // j'ajoute l'enfant dans la famille si ce n'est pas déja le cas
                    Indi indi = getSuperRule().getEntity();
                    if (!indi.isDescendantOf(family)) {
                        family.addChild(indi);
                    }
                }

                // je relie les mariés au mariage
                Indi husband = m_ruleHusband.getEntity();
                if (husband != null && family.getHusband() != husband) {
                    family.setHusband(husband);
                }
                Indi wife = m_ruleWife.getEntity();
                if (wife != null && family.getWife() != wife) {
                    family.setWife(wife);
                }
            }
        }

        /**
         * Marriage evenement principal
         *      prénom NOM °nnnn (I01) x prénom NOM °nnnn (I02) (F001)
         *      prénom NOM °nnnn (I01) x prénom NOM °nnnn (I02) (nouvelle famille)
         *      nouvel époux x prénom NOM °nnnn (I02) (nouvelle famille)
         *      prénom NOM °nnnn (I01) x nouvelle épouse (nouvelle famille) ,
         *      nouvel époux x nouvelle épouse (F001),
         *      nouvel époux x nouvelle épouse (nouvelle famille),
         *
         * Mariage des parents
         *      Parents: prénom NOM °nnnn (I01) x prénom NOM °nnnn (I02) (F004)
         *      Parents: prénom NOM °nnnn (I01) x prénom NOM °nnnn (I02) (Nouvelle famille)
         *      Parents: nouveau père  x prénom NOM °nnnn (I02) (F004)
         *      Parents: prénom NOM °nnnn (I01) x nouvelle mère
         *
         * @param info
         */
        @Override
        public void getInfo(MergeInfo info) {
            if(isChecked()) {
                Fam family = getEntity();
                m_ruleHusband.getEntity();
                m_ruleWife.getEntity();
                if( getSuperRule() == null ) {
                    // famille de l'individu principal
                    info.add("%s x %s %s %s %s %s %s" ,
                            m_ruleHusband, m_ruleWife, family,
                            m_ruleHusband.findSubRule(RuleFamily.class),
                            m_ruleHusband.findSubRule(RuleFamilyMarried.class),
                            m_ruleWife.findSubRule(RuleFamily.class),
                            m_ruleWife.findSubRule(RuleFamilyMarried.class)
                         );
                } else {
                    // famille des parents
                    info.addSeparator();
                    //info.add("%s: %s x %s %s" , getRuleLabel(getLabelResourceName()), m_ruleHusband, m_ruleWife, family);
                    if( m_ruleHusband.isChecked() &&  m_ruleWife.isChecked()) {
                        info.add("%s: %s x %s %s" , getRuleLabel(getLabelResourceName()), m_ruleHusband, m_ruleWife, family);
                    } else {
                        info.add("%s: %s %s %s" , getRuleLabel(getLabelResourceName()), m_ruleHusband, m_ruleWife, family);
                    }
                }
            }
        }

        @Override
        public String getLabelResourceName() {
            if( getSuperRule() == null ) {
                // famille de l'individu principal
                return "Proposal.Family";
            } else {
                // famille des parents
                if( getSuperRule().getSuperRule() == null ) {
                    return "Proposal.Parents";
                } else {
                    if( getSuperRule().getRecordEntity().getSex() == PropertySex.MALE) {
                        return "Proposal.HusbandParents";
                    } else {
                        return "Proposal.WifeParents";
                    }
                }
            }
        }
    }


    class RuleFamilyMarried extends ARuleEntity<ARuleEntity<?,MergeRecord.RecordIndi, Indi> , MergeRecord.RecordMarriedFamily, Fam> {
        private final RuleIndi m_indiMarriedRule;
        private final SpouseTag m_marriedTag;

        RuleFamilyMarried(ARuleEntity<?,MergeRecord.RecordIndi, Indi> superRule,
                MergeRecord.RecordMarriedFamily recordFamily, Indi indiMarried,
                Fam marriedFamily, SpouseTag marriedTag ) throws Exception {
            super(superRule, recordFamily, marriedFamily );

            m_marriedTag = marriedTag;
            if( indiMarried == null) {
                if( getEntity() != null) {
                    if( marriedTag == SpouseTag.HUSB) {
                        indiMarried = getEntity().getWife();
                    } else {
                        indiMarried = getEntity().getHusband();
                    }
                }
            }

            if (getEntity() != null) {
                m_merge = true;
                m_compareResult = CompareResult.EQUAL;
            } else {
                if( recordFamily.getMarried().getLastName().isEmpty() && recordFamily.getMarried().getFirstName().isEmpty()) {
                    m_merge = false;
                    m_compareResult = CompareResult.NOT_APPLICABLE;

                } else {
                    m_merge = true;
                    m_compareResult = CompareResult.COMPATIBLE;
                }
            }

            createDisplayAction(m_compareResult);

            addSubRule( new RuleEvent(this, MergeRecord.EventTypeTag.MARR,  null, recordFamily.getMarriageDate(), null, false), false);
            addSubRule(m_indiMarriedRule = new RuleIndi(this, recordFamily.getMarried(), indiMarried), true);

        }

        @Override
        public void copyRecordToEntity() throws Exception {
            if(isChecked()) {
                if (getEntity() == null) {
                    setEntity(m_helper.createFam() );
                }

                // je copie et cree l'epoux
                super.copyRecordToEntity();

                // je relie l'époux au mariage
                Indi indi = getSuperRule().getEntity();
                if( indi != null ) {
                    if( m_marriedTag == SpouseTag.HUSB) {
                       getEntity().setHusband(indi);
                    } else {
                       getEntity().setWife(indi);
                    }
                }

                // je relie l'autre conjoint au mariage
                Indi indiMarried = m_indiMarriedRule.getEntity();
                if( indiMarried != null) {
                    if( m_marriedTag != SpouseTag.HUSB) {
                       getEntity().setHusband(indiMarried);
                    } else {
                       getEntity().setWife(indiMarried);
                    }
                }
            }
        }

        @Override
        public void getInfo(MergeInfo info) {
            if(isChecked()) {
                info.addSeparator();
                info.add("%s: %s %s", getRuleLabel(getLabelResourceName()), findSubRule(RuleIndi.class), getEntity());
            }
        }

        @Override
        public final String getLabelResourceName() {
            MergeRecord.EventTypeTag eventTag = m_helper.getRecord().getEventTypeTag();

            if (eventTag == MergeRecord.EventTypeTag.MARR
                 || eventTag == MergeRecord.EventTypeTag.MARB
                 || eventTag == MergeRecord.EventTypeTag.MARC
                 || eventTag == MergeRecord.EventTypeTag.MARL
               ) {
                return "Proposal.ExFamily";
            } else {
                return "Proposal.Family";
            }
        }
    }


    class RuleFirstName extends ARuleValue<ARuleEntity<?,MergeRecord.RecordIndi, Indi> , String> {

        RuleFirstName(ARuleEntity<?,MergeRecord.RecordIndi, Indi> superRule) {
            super(superRule);
            m_recordValue = superRule.getRecordEntity().getFirstName();
            m_entityValue = superRule.getEntity() == null ? "" : superRule.getEntity().getFirstName();


            if (m_recordValue.isEmpty()) {
                m_merge = false;
                m_compareResult = CompareResult.NOT_APPLICABLE;
            } else {
                if (m_entityValue.isEmpty()) {
                    m_merge = !m_recordValue.equals(m_entityValue);
                    m_compareResult = m_merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
                } else {
                    if (MergeQuery.isSameFirstName(m_recordValue.trim(), m_entityValue.trim())) {
                        // les noms sont semblables
                        m_merge = false;
                        m_compareResult = !m_recordValue.equals(m_entityValue) ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
                    } else {
                        m_merge = false;
                        m_compareResult = CompareResult.CONFLICT;
                    }
                }
            }
        }

        @Override
        public void copyRecordToEntity() throws Exception {
            if (isChecked()) {
               getSuperRule().getEntity().setName(getSuperRule().getRecordEntity().getFirstName(), getSuperRule().getEntity().getLastName());
            }
        }

        @Override
        public String getLabelResourceName() {
            return "Proposal.FirstName";
        }
    }


    class RuleIndi extends ARuleEntity<ProposalRule, MergeRecord.RecordIndi, Indi>   {

        // individu principal
        RuleIndi(ProposalRule superRule, MergeRecord.RecordParticipant participant, Indi indi,
                Fam marriedFamily, SpouseTag marriedTag,
                Fam parentFamily, Indi father, Indi mother ) throws Exception {
            super(superRule, participant, indi);
            m_merge = true;

            if(superRule == null && participant.getParticipantType() == MergeRecord.MergeParticipantType.participant1) {
                // c'est l'individu principal
                m_compareResult = CompareResult.MANDATORY;
            } else {
                m_compareResult = CompareResult.COMPATIBLE;
            }

            createDisplayAction(m_compareResult);

            addSubRule(new RuleLastName(this) , true);
            addSubRule(new RuleFirstName(this), true);
            addSubRule(new RuleSex(this), true);
            boolean birthMainEvent = (superRule == null  && m_helper.getRecord().getEventTypeTag() == MergeRecord.EventTypeTag.BIRT);
            // Naissance
             addSubRule(new RuleEvent(this, MergeRecord.EventTypeTag.BIRT, null, participant.getBirthDate(), participant.getBirthResidence(), birthMainEvent), false);

            if( ! birthMainEvent ) {

                // profession
                addSubRule(new RuleOccupation(this), true);

                // Deces
                boolean deathMainEvent = (superRule == null  && m_helper.getRecord().getEventTypeTag() == MergeRecord.EventTypeTag.DEAT);
                addSubRule(new RuleEvent(this, MergeRecord.EventTypeTag.DEAT,  null, participant.getDeathDate(), participant.getDeathResidence(), deathMainEvent), false);

                // Married family
                if( marriedFamily != null || ! participant.getMarriedFamily().getMarried().getFirstName().isEmpty() || ! participant.getMarriedFamily().getMarried().getLastName().isEmpty() ) {
                    addSubRule( new RuleSeparator(this), true );
                    addSubRule(new RuleFamilyMarried(this, participant.getMarriedFamily(), null, marriedFamily, marriedTag ), true);
                }
            }

            // parents
            addSubRule(new RuleSeparator(this) , true);
            addSubRule(new RuleFamily(this, participant.getParentFamily(), parentFamily, father, mother), true);

        }

        // indiMarried
        RuleIndi(RuleFamilyMarried superRule, MergeRecord.RecordMarried participant,  Indi indiMarried) throws Exception {
            super(superRule, participant, indiMarried);
            m_merge = true;
            m_compareResult = CompareResult.COMPATIBLE;
            createDisplayAction(m_compareResult);
            addSubRule(new RuleLastName(this) , true);
            addSubRule(new RuleFirstName(this), true);
            addSubRule(new RuleSex(this), true);
            addSubRule(new RuleEvent(this, MergeRecord.EventTypeTag.BIRT,  null, participant.getBirthDate(), null, false), false);
            addSubRule(new RuleEvent(this, MergeRecord.EventTypeTag.DEAT,  null, participant.getDeathDate(), null, false), false);
            addSubRule(new RuleOccupation(this), true);
        }

        // indiParent
        RuleIndi(RuleFamily superRule, MergeRecord.RecordParent recordParent, Indi indiParent) throws Exception {
            super(superRule, recordParent, indiParent);
            if( recordParent.getFirstName().isEmpty() && recordParent.getLastName().isEmpty() && indiParent == null ) {
                // si le nom du parent est inconnu, je ne permets pas de copier dans le gedcom
                m_merge = false;
                m_compareResult = CompareResult.NOT_APPLICABLE;
            } else {
                m_merge = true;
                m_compareResult = CompareResult.COMPATIBLE;
            }

            createDisplayAction(m_compareResult);

            addSubRule(new RuleLastName(this), true);
            addSubRule(new RuleFirstName(this), true);
            addSubRule(new RuleEvent(this, MergeRecord.EventTypeTag.BIRT, null, getRecordEntity().getBirthDate(), null, false), false);
            addSubRule(new RuleEvent(this, MergeRecord.EventTypeTag.DEAT, null, getRecordEntity().getDeathDate(), null, false), false);
            addSubRule(new RuleOccupation(this), true);
        }

        @Override
        public void copyRecordToEntity() throws Exception {
            if( isChecked()) {
                Indi indi = getEntity();
                if (indi == null) {
                    indi = m_helper.createIndi();
                    indi.setName(getRecordEntity().getFirstName(), getRecordEntity().getLastName());
                    indi.setSex(getRecordEntity().getSex());
                    setEntity(indi);
                }
                super.copyRecordToEntity();
            }
        }

        @Override
        public void getInfo(MergeInfo info) {
            if (isChecked()) {
                if ( getSuperRule() == null ) {
                    info.add("%s%s%s" ,
                            getEntity() != null ? getEntity() : getRuleLabel(getLabelResourceName()+".New"),
                            findSubRule(RuleFamilyMarried.class),
                            findSubRule(RuleFamily.class)
                         );
                } else {
                    if (getEntity() != null) {
                        info.add("%s", getEntity());
                    } else {
                        info.add(getRuleLabel(getLabelResourceName()+".New"));
                    }
                }
            }
        }

        @Override
        public final String getLabelResourceName() {
            if (getRecordEntity() instanceof MergeRecord.RecordParent) {
                if (getRecordEntity().getSex() == PropertySex.MALE) {
                    return "Proposal.Father";
                } else {
                    return "Proposal.Mother";
                }
            } else if (getRecordEntity() instanceof MergeRecord.RecordMarried) {
                MergeRecord.EventTypeTag eventTag = m_helper.getRecord().getEventTypeTag();
                if (eventTag == MergeRecord.EventTypeTag.MARR
                        || eventTag == MergeRecord.EventTypeTag.MARB
                        || eventTag == MergeRecord.EventTypeTag.MARC
                        || eventTag == MergeRecord.EventTypeTag.MARL) {
                    return "Proposal.ExSpouse";
                } else {
                    return "Proposal.Spouse";
                }
            } else {
                MergeRecord.EventTypeTag eventTag = m_helper.getRecord().getEventTypeTag();
                switch (eventTag) {
                    case MARR:
                    case MARB:
                    case MARC:
                    case MARL:
                        if (getRecordEntity().getSex() == PropertySex.MALE) {
                            return "Proposal.Husband";
                        } else {
                            return "Proposal.Wife";
                        }
                    case DEAT:
                        return "Proposal.Deceased";
                    case BIRT:
                        return "Proposal.Child";
                    default:
                        return "Proposal.Individual";
                }
            }
        }
    }


    class RuleLastName extends ARuleValue<ARuleEntity<?, MergeRecord.RecordIndi, Indi> , String>  {

        RuleLastName(ARuleEntity<?,MergeRecord.RecordIndi, Indi> superRule) {
            super(superRule);
            m_recordValue = superRule.getRecordEntity().getLastName();
            m_entityValue = superRule.getEntity() == null ? "" : superRule.getEntity().getLastName();

            if (m_recordValue.isEmpty()) {
                m_merge = false;
                m_compareResult = CompareResult.NOT_APPLICABLE;
            } else {
                if (m_entityValue.isEmpty()) {
                    m_merge = !m_recordValue.equals(m_entityValue);
                    m_compareResult = m_merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
                } else {
                    if (MergeQuery.isSameLastName(m_recordValue.trim(), m_entityValue.trim())) {
                        // les noms sont semblables
                        m_merge = false;
                        m_compareResult = !m_recordValue.equals(m_entityValue) ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
                    } else {
                        m_merge = false;
                        m_compareResult = CompareResult.CONFLICT;
                    }
                }
            }
        }

        @Override
        public void copyRecordToEntity() throws Exception {
            if (isChecked()) {
               getSuperRule().getEntity().setName(getSuperRule().getEntity().getFirstName(), getSuperRule().getRecordEntity().getLastName());
            }
        }

        @Override
        public String getLabelResourceName() {
            return "Proposal.LastName";
        }
    }



    class RuleOccupation extends ARuleValue<ARuleEntity<?,MergeRecord.RecordIndi, Indi> , String >  {

        RuleOccupation(ARuleEntity<?,MergeRecord.RecordIndi, Indi> superRule) {
            super(superRule);
            m_recordValue = superRule.getRecordEntity().getOccupationWithDate();
            m_entityValue = superRule.getEntity() ==null ? null : MergeQuery.findOccupation(superRule.getEntity(), m_helper.getRecord().getEventDate() );

            if (m_recordValue.isEmpty()) {
                m_merge = false;
                m_compareResult = CompareResult.NOT_APPLICABLE;
            } else {
                m_merge = !m_recordValue.equals(m_entityValue);
                m_compareResult = m_merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
            }
        }

        @Override
        public void copyRecordToEntity() throws Exception {
            if (isChecked()) {
                String occupation = getSuperRule().getRecordEntity().getOccupation();
                Indi indi = getSuperRule().getEntity();
                PropertyDate occupationDate = m_helper.getRecord().getEventDate();
                MergeRecord.RecordResidence residence = getSuperRule().getRecordEntity().getResidence();
                // je cherche si l'individu a deja un tag OCCU a la meme date
                Property occupationProperty = null;
                // j'ajoute la profession ou la residence
                if (!occupation.isEmpty()) {
                    occupationProperty = indi.addProperty("OCCU", "", m_helper.getPropertyBestPosition(indi, occupationDate));
                    occupationProperty.setValue(occupation);
                } else if ( !residence.isEmpty()) {
                    occupationProperty = indi.addProperty("RESI", "", m_helper.getPropertyBestPosition(indi, occupationDate));
                }

                if (occupationProperty != null) {
                    // j'ajoute la date
                    PropertyDate date = (PropertyDate) occupationProperty.addProperty("DATE", "");
                    date.setValue(occupationDate.getValue());

                    // j'ajoute le lieu
                    if (!residence.isEmpty()) {
                        m_helper.copyPlace(residence, occupationProperty);
                    }

                    // j'ajoute une note indiquant la reference
                    m_helper.copyReferenceNote(occupationProperty, "DATE");
                }
            }
        }

        @Override
        public String getLabelResourceName() {
            return "Proposal.Occupation";
        }
    }

    class RulePage extends ARuleValue<RuleSource , String> {
        private Property m_pageProperty;

        RulePage(RuleSource superRule, String recordValue) {
            super(superRule);
            this.m_recordValue = recordValue;
             m_pageProperty = null;

            if (recordValue.isEmpty()) {
                this.m_entityValue = "";
                m_merge = false;
                m_compareResult = CompareResult.NOT_APPLICABLE;
            } else {
                PropertySource sourcePropertie = getSuperRule().getPropertySourceXRef();
                if( sourcePropertie!= null) {
                    // je verifie si une propriete PAGE contient le meme numero de page et la meme cote
                    for (Property pageProperty : sourcePropertie.getProperties("PAGE")) {
                        String eventCote = m_helper.getRecord().getEventCote();
                        String eventPage = m_helper.getRecord().getEventPage();

                        if ((!eventCote.isEmpty() && pageProperty.getValue().contains(eventCote))
                             || (!eventPage.isEmpty() && pageProperty.getValue().contains(eventPage))) {
                            m_pageProperty = pageProperty;
                            break;
                        }
                    }
                }

                if( m_pageProperty != null) {
                    m_entityValue = m_pageProperty.getValue();
                } else {
                    m_entityValue = "";
                }
                m_merge = !recordValue.equals(m_entityValue);
                m_compareResult = m_merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
            }
        }

        void sourceUpdated () {
            if (m_recordValue.isEmpty()) {
                this.m_entityValue = "";
                m_merge = false;
                m_compareResult = CompareResult.NOT_APPLICABLE;
            } else {
                PropertySource sourcePropertie = getSuperRule().getPropertySourceXRef();
                if( sourcePropertie!= null) {
                    // je verifie si la page contient le meme numero de page et la meme cote
                    for (Property pageProperty : sourcePropertie.getProperties("PAGE")) {
                        String eventCote = m_helper.getRecord().getEventCote();
                        String eventPage = m_helper.getRecord().getEventPage();

                        if ((!eventCote.isEmpty() && pageProperty.getValue().contains(eventCote))
                             || (!eventPage.isEmpty() && pageProperty.getValue().contains(eventPage))) {
                            m_pageProperty = pageProperty;
                            break;
                        }
                    }
                }

                if( m_pageProperty != null) {
                    m_entityValue = m_pageProperty.getValue();
                } else {
                    m_entityValue = "";
                }
                m_merge = !m_recordValue.equals(m_entityValue);
                m_compareResult = m_merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
            }
        }

        @Override
        public void copyRecordToEntity() throws Exception {
            if (isChecked() ) {
                if( getSuperRule().getPropertySourceXRef() != null) {
                    // je cree une nouvelle propriété PAGE
                    getSuperRule().getPropertySourceXRef().addProperty("PAGE", m_helper.getRecord().makeEventPage());
                }
            }
        }

        @Override
        public String getLabelResourceName() {
            return "Proposal.EventPage";
        }
    }

    class RulePlace extends ARuleValue<RuleEvent , String> {
        private final MergeRecord.RecordResidence m_recordResidence;

        RulePlace(RuleEvent superRule, MergeRecord.RecordResidence recordResidence) {
            super(superRule);

            m_recordResidence = recordResidence;

            this.m_recordValue = appendValue(m_recordResidence.getAddress(), m_recordResidence.getPlace());
            String entityAddress, entityPlace;
            if( getSuperRule().getEventProperty() == null) {
                entityAddress= "";
                entityPlace = "";
            } else  {
                entityAddress= getSuperRule().getEventProperty().getPropertyValue("ADDR");
                entityPlace= getSuperRule().getEventProperty().getPropertyValue("PLAC");
            }
            this.m_entityValue = appendValue(entityAddress, entityPlace);
            if (m_recordResidence.isEmpty() ) {
                m_merge = false;
                m_compareResult = CompareResult.NOT_APPLICABLE;
            } else {
                m_merge = !m_recordResidence.getPlace().equals(entityPlace) || !m_recordResidence.getAddress().equals(entityAddress);
                m_compareResult = m_merge ? CompareResult.COMPATIBLE : CompareResult.EQUAL;
            }
        }

        @Override
        public void copyRecordToEntity() throws Exception {
            if (isChecked()) {
                String place = m_recordResidence.getPlace();
                String address = m_recordResidence.getAddress();
                Property eventProperty = getSuperRule().getEventProperty();

                PropertyPlace propertyPlace = (PropertyPlace) eventProperty.getProperty("PLAC");
                if (propertyPlace == null) {
                    // je cree le lieu .
                    propertyPlace = (PropertyPlace) eventProperty.addProperty("PLAC", "");
                }
                propertyPlace.setValue(place);

                m_helper.copyPlaceCoordinates(propertyPlace);

                // j'ajoute l'adresse
                if (!address.isEmpty()) {
                    Property propertyAddress = eventProperty.getProperty("ADDR");
                    if (propertyAddress == null) {
                        // je cree l'adresse
                        propertyAddress = eventProperty.addProperty("ADDR", "");
                    }
                    propertyAddress.setValue(address);
                }
            }
        }

        @Override
        public String getLabelResourceName() {
            switch(getSuperRule().getEventTag() ) {
                case BIRT:
                    return "Proposal.BirthPlace" ;
                case DEAT:
                    return "Proposal.DeathPlace" ;
                default:
                    return "Proposal.EventInsinuationPlace" ;
            }
        }
    }


    class RuleSeparator extends ARule<ProposalRule> {

        RuleSeparator(ProposalRule superRule) {
            super(superRule);
            m_merge = false;
            m_compareResult = CompareResult.NOT_APPLICABLE;
        }

    }


    class RuleSex extends ARuleValue<ARuleEntity<?,MergeRecord.RecordIndi, Indi> , String>  {
        RuleSex(ARuleEntity<?,MergeRecord.RecordIndi, Indi> superRule) {
            super(superRule);
            m_recordValue = PropertySex.getLabelForSex(superRule.getRecordEntity().getSex());
            // affiche "" si m_entityValue = UNKNOWN et si proposedEntity = null
            m_entityValue = superRule.getEntity() == null ? "" : PropertySex.getLabelForSex(superRule.getEntity() == null ? PropertySex.UNKNOWN : superRule.getEntity().getSex());

            Indi indi = getSuperRule().getEntity();
            int recordValue = getSuperRule().getRecordEntity().getSex();
            int entityValue = indi == null ? PropertySex.UNKNOWN : indi.getSex();

            if (recordValue == PropertySex.UNKNOWN) {
                if (entityValue == PropertySex.UNKNOWN) {
                    if (indi == null) {
                        m_merge = false;
                        m_compareResult = CompareResult.NOT_APPLICABLE;
                    } else {
                        m_merge = false;
                        m_compareResult = CompareResult.EQUAL;
                    }
                } else {
                    m_merge = false;
                    m_compareResult = CompareResult.COMPATIBLE;
                }
            } else {
                if (entityValue == PropertySex.UNKNOWN) {
                    m_merge = true;
                    m_compareResult = CompareResult.COMPATIBLE;
                } else {
                    // dans tous les cas merge=false par défaut
                    // car si ce sont les mêmes valeurs , il n'y a pas besoin de copier
                    //  si ce ne sont pas les mêmes valeurs , je laisse l'utilisateur décider de copier
                    m_merge = false;
                    m_compareResult = recordValue != entityValue ? CompareResult.CONFLICT : CompareResult.EQUAL;
                }
            }
        }

        @Override
        public void copyRecordToEntity()  {
            if (isChecked()) {
                getSuperRule().getEntity().setSex(getSuperRule().getRecordEntity().getSex());
            }
        }

        @Override
        public String getLabelResourceName() {
            return "Proposal.Sex";
        }
    }


    class RuleSource extends ARuleValue<RuleEvent , String>  {

        private PropertySource m_propertySource;
        private Source m_Source;
        private final RulePage m_rulePage;

        RuleSource(RuleEvent superRule) {
            super(superRule);

            String recordSourceTitle = m_helper.getEventSourceTitle();

            // je cherche la source et la page dans la propriété de l'entité
            m_propertySource = m_helper.findPropertySource(recordSourceTitle, getSuperRule().getEventProperty());

            this.m_recordValue = m_helper.getEventSourceTitle();
            if (m_propertySource != null) {
                // la source existe dans l'entité
                this.m_entityValue = recordSourceTitle ;
                m_Source = (Source) m_propertySource.getTargetEntity();
                m_merge = false;
                m_compareResult = CompareResult.EQUAL;
            } else {
                // la source n'existe pas dans l'entité

                // je cherche la source dans le gedcom
                m_Source = m_helper.findSource(recordSourceTitle);

                if (m_Source != null) {
                    // la source indiquée dans le releve existe dans le gedcom
                    // je propose de l'ajouter
                    this.m_entityValue = m_Source.getTitle();
                    m_merge = true;
                    m_compareResult = CompareResult.COMPATIBLE;
                } else {
                    // la source indiquée dans le releve n'existe pas dans le gedcom
                    this.m_entityValue = null;
                    m_merge = false;
                    m_compareResult = CompareResult.NOT_APPLICABLE;
                }
            }

            // page
            addSubRule(m_rulePage = new RulePage(this, m_helper.getRecord().makeEventPage()), true);
        }


        public void sourceUpdated(Source source) {
            m_Source = source;
            m_propertySource = m_helper.findPropertySource(source.getTitle(), getSuperRule().getEventProperty());
            m_entityValue = source.getTitle();
            m_merge = false;
            m_compareResult = CompareResult.EQUAL;

            // je met à jour la page
            m_rulePage.sourceUpdated();
        }

        @Override
        public void copyRecordToEntity() throws Exception {
            if( isChecked()) {
                Property eventProperty = getSuperRule().getEventProperty();
                if (m_Source != null) {
                    // je verifie si la source est déjà associée à la naissance
                    boolean found = false;
                    // je copie la source du releve dans l'evenement
                    Property[] sourceProperties = eventProperty.getProperties("SOUR", false);
                    for (Property sourcePropertie : sourceProperties) {
                        Source eventSource = (Source) ((PropertySource) sourcePropertie).getTargetEntity();
                        if (m_Source.compareTo(eventSource) == 0) {
                            found = true;
                            // je memorise le lien vers la source pour ajouter la page
                            m_propertySource = (PropertySource) sourcePropertie;
                            break;
                        }
                    }
                    if (found == false) {
                        try {
                            // je relie la reference de la source du releve à la propriété de naissance
                            m_propertySource = (PropertySource) eventProperty.addProperty("SOUR", "@" + m_Source.getId() + "@");
                            m_propertySource.link();
                        } catch (GedcomException ex) {
                            throw new Exception(String.format("Link source=%s error=%s ", m_Source.getTitle(), ex.getMessage()));
                        }
                    }
                } else {
                    // je cree une nouvelle source avec le titre
                    Source newSource = m_helper.createSource();
                    try {
                        // je relie la source du releve à l'entité
                        m_propertySource = (PropertySource) eventProperty.addProperty("SOUR", "@" + newSource.getId() + "@");
                        m_propertySource.link();
                    } catch (GedcomException ex) {
                        throw new Exception(String.format("Link source=%s error=%s ", m_Source == null ? m_Source : m_Source.getTitle(), ex.getMessage()));
                    }
                }
            }

            // je copie les sous regles
            super.copyRecordToEntity();
        }

        @Override
        public MergeTableAction getDisplayAction() {
            return m_helper.getSourceAction();
        }

        @Override
        public String getLabelResourceName() {
            return "Proposal.EventSource";
        }

        PropertySource getPropertySourceXRef() {
            return m_propertySource;
        }

    }


    ///////////////////////////////////////////////////////////////////////////
    // utilitaires
    ///////////////////////////////////////////////////////////////////////////

    /**
     * concatene plusieurs commentaires dans une chaine séparés par une
     * virgule
     */
    static public String appendValue(String value, String... otherValues) {
        int fieldSize = value.length();
        StringBuilder sb = new StringBuilder();
        sb.append(value.trim());
        for (String otherValue : otherValues) {
            // j'ajoute les valeurs supplémentaires séparées par des virgules
            if (!otherValue.trim().isEmpty()) {
                // je concatène les valeurs en inserant une virgule
                // si la valeur précedente n'est pas vide
                // et si la valeur suivante n'est pas vide non plus
                if (fieldSize > 0) {
                    sb.append(", ");
                }
//                sb.append(otherValue.trim());
                sb.append(otherValue);
                fieldSize += otherValue.length();
            }
        }
        return sb.toString();
    }
}
