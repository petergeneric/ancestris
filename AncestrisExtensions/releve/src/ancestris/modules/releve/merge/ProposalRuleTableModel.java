package ancestris.modules.releve.merge;

import javax.swing.table.TableModel;


public interface ProposalRuleTableModel extends TableModel {
    ProposalRule.CompareResult getCompareResult(int index);
    boolean isMergeChanged(int index);
}

