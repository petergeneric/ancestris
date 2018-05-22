package ancestris.modules.releve.merge;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Source;
import java.awt.Frame;
import java.util.ArrayList;
import org.openide.util.NbBundle;

/**
 *
 * @author michel
 */


public abstract class MergeTableAction {
    private static final String NEW_FAMILY = NbBundle.getMessage(MergeTableAction.class, "MergeTable.label.newFamily");
    private static final String NEW_INDI = NbBundle.getMessage(MergeTableAction.class, "MergeTable.label.newIndi");
    private static final String NEW_SOURCE = NbBundle.getMessage(MergeTableAction.class, "MergeTable.label.selectSource");

    private static final String entityToolTip = "<html>"+NbBundle.getMessage(MergePanel.class, "MergeTable.entityToolTip")+"</html>";
    private static final String sourceToolTip = "<html>"+ NbBundle.getMessage(MergeTableAction.class, "MergeTable.sourceToolTip")+ "</html>";

    abstract String getText() ;
    abstract void applyAction(Frame parent, int clickCount );
    abstract boolean isClickable();
    abstract String  getToolTipText();

    static class EntityAction extends MergeTableAction {
        protected Entity m_entity;
        protected String m_text;

        EntityAction(Entity entity, String label) {
            m_entity = entity;
            if( entity != null) {
                m_text = entity.getId();
            } else {
                m_text = label;
            }
        }

        EntityAction(String label) {
            m_entity = null;
            m_text = label;
        }

        @Override
        boolean isClickable() {
            return m_entity != null;
        }

        @Override
        String  getText() {
            return m_text;
        }

        @Override
        String  getToolTipText() {
            return entityToolTip;
        }

        @Override
        void applyAction(Frame parent, int clickCount ) {
            if (clickCount == 2) {
                SelectionManager.setRootEntity(m_entity);
            } else {
                SelectionManager.showEntity(m_entity);
            }
        }
    }

    static class SourceAction extends EntityAction {
        //private Source m_source;
        private final ProposalHelper m_proposalHelper;
        private final Gedcom m_gedcom;
        private final ArrayList<SourceUpdateListener> sourceUpdateListeners = new ArrayList<SourceUpdateListener>();

        SourceAction(ProposalHelper proposalHelper, Source source, Gedcom gedcom) {
            super(source, NEW_SOURCE);
            m_proposalHelper = proposalHelper;
            //m_source = source;
            m_gedcom = gedcom;
        }

        @Override
        boolean isClickable() {
            return true;
        }

        @Override
        String  getToolTipText() {
            return sourceToolTip;
        }

        @Override
        void applyAction(Frame parent, int clickCount ) {
            String sourceTitle = m_proposalHelper.getEventSourceTitle();
            if (m_entity != null ) {
                sourceTitle = ((Source)m_entity).getTitle();
            }
            Source source = RecordSourceConfigDialog.show(parent, m_proposalHelper.getRecord().getFileName(), sourceTitle, m_gedcom );
            if (source != null ) {
                updateSource(source);
            }
        }

        public void updateSource(Source source) {
            m_entity = source;
            m_text = source.getId();
            //m_recordHelper.getEventSourceTitle();

            // je mets à jour toutes les propositions car la source est commune à toutes les propositions
            for (SourceUpdateListener listener : sourceUpdateListeners) {
                listener.sourceUpdated(source);
            }
            // j'enregistre la paire fileName,source dans les preferences
            MergeOptionPanel.SourceModel.getModel().add(m_proposalHelper.getRecord().getFileName(), source.getTitle());
            MergeOptionPanel.SourceModel.getModel().savePreferences();
        }

        void addSourceListener (SourceUpdateListener listener ) {
            sourceUpdateListeners.add(listener);
        }

    }


    static interface SourceUpdateListener {
        void sourceUpdated(Source source) ;
    }




}
