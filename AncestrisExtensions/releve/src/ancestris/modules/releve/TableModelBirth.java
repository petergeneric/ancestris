package ancestris.modules.releve;

import ancestris.modules.releve.model.*;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Michel
 */
public class TableModelBirth extends AbstractTableModel {

    final String columnName[] = {
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Id"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Name"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Sex"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Date"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Father"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Mother"),
        java.util.ResourceBundle.getBundle("ancestris/modules/releve/model/Bundle").getString("model.column.Picture")
    };
    final Class columnType[] = {Integer.class, String.class, FieldSex.class, FieldDate.class, String.class, String.class, FieldPicture.class};

    ModelAbstract dataModel;

    /**
     * Constructor
     * @param dataManager
     */
    public TableModelBirth(ModelAbstract dataModel) {
        this.dataModel = dataModel;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Implement AbstractTableModel methods
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public int getColumnCount() {
        return columnName.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnName[col];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return columnType[column];
    }

    /**
     * Cette methode est appelée par les tables qui utilisent ce modele
     * Elle retourne la valeur pour remplir la cellule (row, col)
     * ATTENTION : la date est retounée sous forme de propriete pour permettre
     * le tri par date en utilisant le comparateur de ProperyDate.
     * Par ailleurs l'affichage de la date doit être mis en en forme par le renderer
     * de la table ( jj/mm/aaa)
     *
     * @param row
     * @param col
     * @return
     */
    @Override
    public Object getValueAt(int row, int col) {
        Object value;
        switch (col) {
            case 0:
                value = new Integer(dataModel.getRecord(row).recordNo);
                break;
            case 1:
                value = dataModel.getRecord(row).getIndiLastName().toString() + " " + dataModel.getRecord(row).getIndiFirstName().toString();
                break;
            case 2:
                value = dataModel.getRecord(row).getIndiSex();
                break;
            case 3:
                value = dataModel.getRecord(row).getEventDateProperty();
                break;
            case 4:
                value = dataModel.getRecord(row).getIndiFatherLastName().toString() + " " + dataModel.getRecord(row).getIndiFatherFirstName().toString();
                break;
            case 5:
                value = dataModel.getRecord(row).getIndiMotherLastName().toString() + " " + dataModel.getRecord(row).getIndiMotherFirstName().toString();
                break;
            case 6:
                value = dataModel.getRecord(row).getFreeComment();
                break;
            default:
                value = "";
                break;
        }
        return value;
    }

    @Override
    public int getRowCount() {
        return dataModel.getRowCount();
    }

}
