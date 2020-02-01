package ancestris.modules.releve.merge;

import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;

////////////////////////////////////////////////////////////////////////////
// MergeRule
////////////////////////////////////////////////////////////////////////////

abstract class ProposalRule {
    protected String m_label;
    protected boolean m_merge;
    protected boolean m_merge_initial;
    protected CompareResult m_compareResult;
    private final ArrayList<ProposalRule> m_subRules;
    protected boolean      m_display;

    ProposalRule( ) {
        m_subRules = new ArrayList<ProposalRule>();
    }

    final ProposalRule addSubRule(ProposalRule rule, boolean display) {
        rule.m_display = display;
        return rule;
    }

    abstract public ProposalRule getSuperRule();

    public final List<ProposalRule> getSubRules() {
        return m_subRules;
    }

    //TODO caster la valeur retournée avec un template
    public final ProposalRule findSubRule(Class<?> ruleClass) {
        return findSubRule(this, ruleClass);
    }

    private ProposalRule findSubRule(ProposalRule rule, Class<?> ruleClass) {
        ProposalRule result = null;
        for( ProposalRule subRule : rule.getSubRules() ) {

            if( ruleClass.isInstance(subRule) ) {
                return subRule;
            }
            result = findSubRule(subRule, ruleClass ); // recursif
            if( result != null) {
                return result;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return m_label + " " + getDisplayRecord() + " " + getDisplayEntity();
    }

    public Object getDisplayRecord() {
        return null;
    };

    public Object getDisplayEntity() {
        return null;
    };

    public MergeTableAction getDisplayAction() {
        return null;
    };

    public boolean equalAs(ProposalRule that) {
            int thisSize = this.getSubRules().size();
            int thatSize = that.getSubRules().size();

            // je compare les sous regles
            for (int i = 0 ; i < thisSize && i < thatSize; i++ ) {
                ProposalRule thisSub = this.getSubRules().get(i);
                ProposalRule thatSub = that.getSubRules().get(i);

                if ( ! thisSub.equalAs(thatSub) ) {
                    return false;
                }
            }
            return true ;
    }

    public void copyRecordToEntity() throws Exception {
        for (ProposalRule rule : getSubRules()) {
            rule.copyRecordToEntity();
        }
    }

    public void getInfo(MergeInfo info) {
        for (ProposalRule rule : getSubRules()) {
            rule.getInfo(info);
        }
    }

    public boolean getMerge() {
        return m_merge;
    }

    /**
     * retourne true si la rèle ou une des sous regles a m_merge=true
     * @return
     */
    public boolean isChecked() {
        boolean checked = m_merge;

        for (ProposalRule rule : getSubRules()) {
            checked |= rule.isChecked();
        }
        return checked;
    }

    public void setMerge(boolean merge) {
        this.m_merge = merge;
    }

    public boolean isMergeChanged() {
        return m_merge != m_merge_initial;
    }

    public CompareResult getCompareResult() {
        return m_compareResult;
    }

    public String getLabel() {
        return m_label;
    }
    public String getLabelResourceName() {
        return "";
    }

    public void restoreInitialMerge() {
        m_merge = m_merge_initial;
    }

    static protected String getRuleLabel(String resourceName) {
        if (resourceName.isEmpty()) {
            return "";
        } else {
            return NbBundle.getMessage(ProposalRule.class, resourceName);
        }
    }

    static protected enum CompareResult {
        EQUAL,
        COMPATIBLE,
        CONFLICT,
        MANDATORY,
        NOT_APPLICABLE,
    }

}

