package ancestris.modules.releve.editor;

import ancestris.modules.releve.ReleveTopComponent;
import ancestris.modules.releve.model.Record.RecordType;
import ancestris.modules.releve.model.Record.FieldType;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.ArrayList;
import javax.swing.KeyStroke;
import org.openide.util.NbPreferences;

/**
 *
 * @author Michel
 */
public class EditorBeanGroup {

    public static enum GroupId {
        general,
        indi,
        indiMarried,
        indiFather,
        indiMother,
        wife,
        wifeMarried,
        wifeFather,
        wifeMother,
        witness1,
        witness2,
        witness3,
        witness4,
        generalComment
    }

    private GroupId groupId ;
    private boolean visible;
    private  KeyStroke keystroke;
    private String title;
    private ArrayList<EditorBeanField> fields = null;

    private final static HashMap<RecordType,ArrayList<EditorBeanGroup>> groupArray = new HashMap<RecordType,ArrayList<EditorBeanGroup>>(4);
    private final static HashMap<RecordType,HashMap<GroupId,EditorBeanGroup>> groupMap = new HashMap<RecordType,HashMap<GroupId,EditorBeanGroup>>(4);


    final static KeyStroke ks1 = KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.ALT_DOWN_MASK);
    final static KeyStroke ks2 = KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.ALT_DOWN_MASK);
    final static KeyStroke ks3 = KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.ALT_DOWN_MASK);
    final static KeyStroke ks4 = KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.ALT_DOWN_MASK);
    final static KeyStroke ks5 = KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.ALT_DOWN_MASK);
    final static KeyStroke ks6 = KeyStroke.getKeyStroke(KeyEvent.VK_6, InputEvent.ALT_DOWN_MASK);
    final static KeyStroke ks7 = KeyStroke.getKeyStroke(KeyEvent.VK_7, InputEvent.ALT_DOWN_MASK);
    final static KeyStroke ks8 = KeyStroke.getKeyStroke(KeyEvent.VK_8, InputEvent.ALT_DOWN_MASK);
    final static KeyStroke ks9 = KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.ALT_DOWN_MASK);

    /**
     * visibilité par défaut des groupes et des champs
     */
    static {
        groupMap.put(RecordType.BIRTH,   new HashMap<GroupId,EditorBeanGroup>());
        groupMap.put(RecordType.MARRIAGE,new HashMap<GroupId,EditorBeanGroup>());
        groupMap.put(RecordType.DEATH,   new HashMap<GroupId,EditorBeanGroup>());
        groupMap.put(RecordType.MISC,    new HashMap<GroupId,EditorBeanGroup>());

        groupArray.put(RecordType.BIRTH,   new ArrayList<EditorBeanGroup>());
        groupArray.put(RecordType.MARRIAGE,new ArrayList<EditorBeanGroup>());
        groupArray.put(RecordType.DEATH,   new ArrayList<EditorBeanGroup>());
        groupArray.put(RecordType.MISC,    new ArrayList<EditorBeanGroup>());

        //   groupe               birthTitle   marriageTitle deathTitle  miscTitle        keyStroke
	init(GroupId.general,     "Birth",     "Marriage",   "Death",    "Misc" ,         ks1 );
        init(GroupId.indi,        "Child",     "Husband",    "Deceased", "Participant1" , ks2 );
        init(GroupId.indiMarried, "Married",   "ExWife",     "Married",  "Married" ,      null );
        init(GroupId.indiFather,  "Father",    "IndiFather", "Father",   "Father" ,       ks3 );
        init(GroupId.indiMother,  "Mother",    "IndiMother", "Mother",   "Mother" ,       ks4);
        init(GroupId.wife,        "Wife",      "Wife",       "Wife",     "Participant2" , ks5 );
        init(GroupId.wifeMarried, "Married",   "ExHusband",  "",         "Married" ,      null);
        init(GroupId.wifeFather,  "WifeFather","WifeFather", "Father",   "Father" ,       ks6 );
        init(GroupId.wifeMother,  "WifeMother","WifeMother", "Mother",   "Mother" ,       ks7 );
        init(GroupId.witness1,    "GodFather", "Witness1",   "Witness1", "Witness1" ,     ks8 );
        init(GroupId.witness2,    "GodMother", "Witness2",   "Witness2", "Witness2" ,     null);
        init(GroupId.witness3,    "Witness3",  "Witness3",   "Witness3", "Witness3" ,     null);
        init(GroupId.witness4,    "Witness4",  "Witness4",   "Witness4", "Witness4" ,     null);
        init(GroupId.generalComment, "GeneralComment",  "GeneralComment",   "GeneralComment", "GeneralComment" ,     ks9);

        // j'intialise avec les valeurs par defaut
        EditorBeanField.init();
        // je charge les preferences
        loadPreferences();
    }


    /**
     *
     */
    static public void init(GroupId id, String birthTitle, String marriageTitle,
            String deathTitle, String miscTitle,  KeyStroke ks  ) {
            
            String title ="";
            if ( birthTitle!= null && !birthTitle.isEmpty() ) {
                title = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.row."+birthTitle);
            }
            EditorBeanGroup group;
            group = new EditorBeanGroup( id, title, ks );
            groupMap.get(RecordType.BIRTH).put(id, group);
            groupArray.get(RecordType.BIRTH).add( group);

            if ( marriageTitle!= null && !marriageTitle.isEmpty() ) {
                title = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.row."+marriageTitle);
            } else {
                title = "";
            }
            group = new EditorBeanGroup( id, title, ks );
            groupMap.get(RecordType.MARRIAGE).put(id, group);
            groupArray.get(RecordType.MARRIAGE).add( group);

            if ( deathTitle!= null && !deathTitle.isEmpty() ) {
                title = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.row."+deathTitle);
            } else {
                title = "";
            }
            group = new EditorBeanGroup( id, title, ks );
            groupMap.get(RecordType.DEATH).put(id, group);
            groupArray.get(RecordType.DEATH).add( group);

            if ( miscTitle!= null && !miscTitle.isEmpty() ) {
                title = java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.row."+miscTitle);
            } else {
                title = "";
            }
            group = new EditorBeanGroup( id, title, ks );
            groupMap.get(RecordType.MISC).put(id, group);
            groupArray.get(RecordType.MISC).add( group);
    }

    static public void addField(RecordType recordType, GroupId groupId, EditorBeanField field) {
        EditorBeanGroup group = groupMap.get(recordType).get(groupId);
        group.add(field);
    }


    static public Collection<EditorBeanGroup> getGroups(RecordType recordType) {
        return groupArray.get(recordType);
    }

    /**
     * retourne le groupe dans lequel est le champ d'un type de releve
     * @param recordType
     * @param fieldType
     * @return 
     */
    static public EditorBeanGroup getGroup(RecordType recordType, FieldType fieldType) {
        ArrayList<EditorBeanGroup> beanGroups = groupArray.get(recordType);
        for( EditorBeanGroup beanGroup : beanGroups ) {
            for( EditorBeanField beanField:  beanGroup.getFields() ) {
                if( beanField.getFieldType() == fieldType) {
                    return beanGroup;
                }
            }
        }
        return null;
    }

    static public void loadPreferences() {
        for (RecordType recordType : RecordType.values()) {
            for (EditorBeanGroup group : EditorBeanGroup.getGroups(recordType)) {
                for (EditorBeanField field : group.getFields()) {
                    String preferenceKey = "Editor." + group.groupId.name() + "." + field.getFieldType().name() + "." + recordType.name();
                    String defaultValue = field.isUsed() + ";" + field.isVisible();
                    String preferenceValue = NbPreferences.forModule(ReleveTopComponent.class).get(preferenceKey, defaultValue);
                    try {
                        StringTokenizer tokens = new StringTokenizer(preferenceValue, ";");
                        if (tokens.countTokens() == 2) {
                            // set used
                            // je n'utilise pas la sauvegarde de field.isUsed(), seulement la sauvegarde de field.isVisible()
                            //field.setUsed(Boolean.valueOf(tokens.nextToken()));
                            tokens.nextToken();
                            // set visibled 
                            field.setVisible(Boolean.valueOf(tokens.nextToken()));
                        }

                    } catch (Throwable t) {
                        // ignore
                    }
                }
            }
        }
    }

    static public void savePreferences() {
        for (RecordType recordType : RecordType.values()) {
            for (EditorBeanGroup group : EditorBeanGroup.getGroups(recordType)) {
                for (EditorBeanField field : group.getFields()) {
                    String preferenceKey = "Editor."+group.groupId.name()+"."+field.getFieldType().name()+"."+recordType.name();
                    String preferenceValue = field.isUsed()+";"+field.isVisible();
                    NbPreferences.forModule(EditorBeanGroup.class).put( preferenceKey, preferenceValue);
                }
            }
        }
    }

    /**
     * instance 
     */

    public EditorBeanGroup(GroupId groupId, String title, KeyStroke ks) {
        this.groupId = groupId;
        //this.recordType =recordType;
        this.visible = false; // sera mis a jour lors de l'ajout des champs
        this.title = title;
        this.keystroke = ks;

        fields=new ArrayList<EditorBeanField>();
    }

    private void add(EditorBeanField field) {
        fields.add(field);
        visible = false;
        for (Iterator<EditorBeanField> fieldIter = fields.iterator(); fieldIter.hasNext() && ! visible; ) {
           visible |= fieldIter.next().isVisible();
        }
    }

    public GroupId getGroupId() {
        return groupId;
    }

    public boolean isVisible() {
        return visible;
    }

    public String getTitle() {
        return title;
    }

    public Collection<EditorBeanField> getFields() {
        return fields;
    }

    /**
     * @return the keystroke
     */
    public KeyStroke getKeystroke() {
        return keystroke;
    }   

}
