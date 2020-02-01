package ancestris.modules.releve.merge;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

////////////////////////////////////////////////////////////////////////////
// MergeRuleList
////////////////////////////////////////////////////////////////////////////
/**
 * liste a deux entrées ( index et RuleType)
 */
class ProposalRuleList extends AbstractTableModel implements ProposalRuleTableModel {

    private final List<ProposalRule> dataList = new ArrayList<ProposalRule>();
    private int nbEqual = 0;
    private int nbCompatibleChecked = 0;
    private int nbCompatibleNotChecked = 0;
    private int nbConflict = 0;
    private int nbMandatory = 0;
    private int nbNotApplicable = 0;
    private int nbTotal = 0;

    void add(ProposalRule proposalRule, boolean display) {
        add(proposalRule, display, 0);
    }

    /**
     *
     * @param rule
     * @param level
     * @param position
     * @return
     */
    private void add(ProposalRule rule, boolean display, int level) {
        if (rule.getSuperRule() != null) {
            if( rule.getSuperRule().getCompareResult() == ProposalRule.CompareResult.NOT_APPLICABLE) {
                // si la ligne de superRule est NOT_APPLICABLE
                rule.m_merge = false;
                rule.m_compareResult = ProposalRule.CompareResult.NOT_APPLICABLE;
            }
        }

        rule.m_merge_initial = rule.m_merge;

        if (display  &&  !dataList.contains(rule)) {
            dataList.add(rule);

            rule.m_label = ProposalRule.getRuleLabel(rule.getLabelResourceName());

            for(int i= 0 ; i < level ; i++) {
                rule.m_label  = "  "+ rule.m_label ;
            }
            level++;

            if (rule.getCompareResult() != null) {
                // j'incremente le score
                switch (rule.getCompareResult()) {
                    case EQUAL:
                        this.nbEqual++;
                        break;
                    case COMPATIBLE:
                        if(rule.getMerge() ) {
                            this.nbCompatibleChecked++;
                        } else {
                            this.nbCompatibleNotChecked++;
                        }
                        break;
                    case CONFLICT:
                        this.nbConflict++;
                        break;
                    case MANDATORY:
                        this.nbMandatory++;
                        break;
                    case NOT_APPLICABLE:
                    default:
                        nbNotApplicable++;
                        break;
                }
                this.nbTotal++;
            }
        }

        for(ProposalRule subRule : rule.getSubRules() ) {
            add(subRule, subRule.m_display, level);

        }

    }

    public int getNbEqual() {
        return nbEqual;
    }

    public int getNbCompatibleChecked() {
        return nbCompatibleChecked;
    }
    public int getNbCompatibleNotChecked() {
        return nbCompatibleNotChecked;
    }

    public int getNbConflict() {
        return nbConflict;
    }
    public int getNbTotal() {
        return nbTotal;
    }

    @Override
    public ProposalRule.CompareResult getCompareResult(int index) {
        if (dataList.get(index).getCompareResult() != null) {
            return dataList.get(index).getCompareResult();
        } else {
            return ProposalRule.CompareResult.NOT_APPLICABLE;
        }
    }

    @Override
    public boolean isMergeChanged(int index) {
        return dataList.get(index).isMergeChanged();
    }

    /**
     * coche ou décoche une ligne en fonction de son numéro d'ordre
     *
     * @param ruleNum
     * @param state
     */
    private void check(int ruleNum, boolean state) {
        ProposalRule rule = dataList.get(ruleNum);
        rule.setMerge(state);
        fireTableCellUpdated(ruleNum, 2);
        fireTableCellUpdated(ruleNum, 3);
        // je mets a jour les sous regles
        checkSubRule(rule, state);
        // je mets à jour la regle supérieure
        checkSuperRule(rule, state);
    }


    /**
     * coche ou décoche les lignes des sous règles
     *
     * @param ruleNum
     * @param state
     */
    private void checkSubRule(ProposalRule rule, boolean state) {
        for (ProposalRule subRule : rule.getSubRules()) {
            int index = dataList.indexOf(subRule);  // <=== 2018-11-25 - FIXME (FL) : why do we need to check it is in the list here when it is not verified durng copy !!!
                                                    //                                so remove this test. Apply change to all subRules, regardless.
            if (index != -1 || true) {
                if (state == true) {
                    // je restaure l'etat initial de la ligne fille
                    subRule.restoreInitialMerge();
                } else {
                    subRule.setMerge( false);
                }
                fireTableCellUpdated(dataList.indexOf(subRule), 2);
                fireTableCellUpdated(dataList.indexOf(subRule), 3);
            }
            // je mets a jour les sous regles (récursif)
            checkSubRule(subRule, state);
        }
    }


    /**
     * coche ou décoche la linge de la regle superieure
     *
     * @param ruleNum
     * @param state
     */
    private void checkSuperRule(ProposalRule rule, boolean state) {
        // je mets a jour la ligne parent
        // seulement si elle était décochée et que la fille vient d'etre cochee
        ProposalRule superRule = rule.getSuperRule();
        if( superRule != null ) {
            // je met à jour d'abord la ligne superieure (récursif)
            checkSuperRule(superRule, state);
            // je met à jour la ligne courante
            if (state == true ) {
                superRule.setMerge(true);
                int parentIndex = dataList.indexOf(superRule);
                if(parentIndex != -1 ) {
                    fireTableCellUpdated(parentIndex, 2);
                    fireTableCellUpdated(parentIndex, 3);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // fonctions de gestion de la JTable
    ///////////////////////////////////////////////////////////////////////////
    private final String[] columnNames = {
        "",
        NbBundle.getMessage(ProposalRuleList.class, "MergePanel.title.recordColumn"),
        "=>",
        NbBundle.getMessage(ProposalRuleList.class, "MergePanel.title.gedcomColumn"),
        NbBundle.getMessage(ProposalRuleList.class, "MergePanel.title.identifierColumn")
    };

    private final Class<?>[] columnClass = {
        String.class,
        Object.class,
        Boolean.class,
        Object.class,
        MergeTableAction.class
    };

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return dataList.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return columnClass[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        switch (col) {
            case 0:
                return dataList.get(row).getLabel();
            case 1:
                return dataList.get(row).getDisplayRecord();
            case 2:
                ProposalRule.CompareResult cr =  dataList.get(row).getCompareResult();
                if( cr == ProposalRule.CompareResult.NOT_APPLICABLE
                    || cr == ProposalRule.CompareResult.MANDATORY
                    || cr == ProposalRule.CompareResult.EQUAL)
                {
                    return null;
                } else {
                    return dataList.get(row).getMerge();
                }
            case 3:
                return dataList.get(row).getDisplayEntity();
            case 4:
                return dataList.get(row).getDisplayAction();
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int rule, int col) {
        switch (col) {
            case 2:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void setValueAt(Object value, int rule, int col) {
        switch (col) {
            case 2:
                check(rule, (Boolean) value);
                //fireTableCellUpdated(rule, col);
                break;
            default:
                break;
        }
    }

}

