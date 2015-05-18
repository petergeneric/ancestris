package ancestris.modules.releve.editor;

import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.FieldPicture;
import ancestris.modules.releve.model.FieldSimpleValue;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeListener;

/**
 * Remarque : remplace genj.edit.beans.SimpleValueBean a laquelle il manque
 * dans le constructeur "defaultFocus = tfield;"
 * @author Michel
 */
public class BeanFreeComment extends Bean {

    private JTextField editor = new JTextField();
    //private CompletionProvider completionProvider;

    public BeanFreeComment( ) {
        //this.completionProvider = completionProvider;
        setLayout(new java.awt.BorderLayout());
        
        //tfield = new TextFieldWidget("");
        JSpinner spinner = new JSpinner();
        spinner.setEditor(editor);
        spinner.setModel(new PictureNameModel(editor));
        setPreferredSize(new java.awt.Dimension(100, 20));
        add(spinner,BorderLayout.CENTER);
        editor.getDocument().addDocumentListener(changeSupport);
        JButton jButtonSaveComment = new JButton();
        setPreferredSize(new java.awt.Dimension(20, 20));

//        add(jButtonSaveComment, BorderLayout.EAST);
//        jButtonSaveComment.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                saveComment();
//            }
//        });
        defaultFocus = spinner;
        revalidate();
        repaint();
    }

    /**
     * Set context to edit
     */
    @Override
    public void setFieldImpl() {

        final FieldPicture property = (FieldPicture) getField();
        if (property == null) {
            editor.setText("");
        } else {
            String txt = property.toString();
            editor.setText(txt);
        }
        changeSupport.setChanged(false);

        // je configure le raccourci de la touche ESCAPE pour annuler la saisie en cours
        resetKeyboardActions();
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(escape, this);
        getActionMap().put(this, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // restaure la valeur
                editor.setText(property.toString());
            }
        });
    }

    @Override
    protected void replaceValueImpl(Field field) {
       final FieldSimpleValue name = (FieldSimpleValue) field;
        if (name == null) {
            editor.setText("");
        } else {
            editor.setText(name.toString());
        }
    }

    /**
     * Finish editing a property through proxy
     */
    @Override
    protected void commitImpl() {
        FieldSimpleValue p = (FieldSimpleValue) getField();

        String value = editor.getText().trim();
        editor.setText(value);
        p.setValue(value);
    }




    private class PictureNameModel implements SpinnerModel {
        JTextField editor ;


        PictureNameModel(JTextField editor) {
            this.editor = editor;
        }

        @Override
        public Object getValue() {
            return editor.getText();
        }

        @Override
        public void setValue(Object value) {
            editor.setText(value.toString());
        }

        @Override
        public Object getNextValue() {
            String value = editor.getText();
            int i;
            for (i = value.length() -1 ; i >= 0 &&  value.charAt(i) >= '0' && value.charAt(i) <= '9' ; i--) {

            }
            i++;
            // je cree le format pour préserver les zeros à gauche
            String format = String.format("%%s%%0%dd", value.length() - i);
            if ( i < value.length() ) {
                int num = new Integer(value.substring(i,value.length())).intValue() +1;
                value = String.format(format, value.substring(0,i), num);
            }
            return value;
        }

        @Override
        public Object getPreviousValue() {
            String value = editor.getText();
            int i;
            for (i = value.length() -1 ; i >= 0 &&  value.charAt(i) >= '0' && value.charAt(i) <= '9' ; i--) {

            }
            i++;
            // je cree le format pour préserver les zeros à gauche
            String format = String.format("%%s%%0%dd", value.length() - i);
            if ( i < value.length() ) {
                int num = new Integer(value.substring(i,value.length())).intValue() -1;
                if (num >=0) {
                    // je memorise la valeur si elle est positive
                    value = String.format(format, value.substring(0,i), num);
                }
            }
            return value;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }
    }    
}
