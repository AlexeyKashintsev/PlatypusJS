/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.dbcontrols.grid;

import com.eas.client.model.ModelEntityRef;
import com.eas.controls.DesignInfo;
import com.eas.store.Serial;

/**
 *
 * @author mg
 */
public class DbGridRowsColumnsDesignInfo extends DesignInfo {

    public static final String FIXEDCOLUMNS = "fixedColumns";
    public static final String FIXEDROWS = "fixedRows";
    public static final String ROWSDATASOURCE = "rowsDatasource";
    public static final String ROWSHEADERTYPE = "rowsHeaderType";
    public static final String GENERALROWFUNCTION = "generalRowFunction";
    public static final int ROWS_HEADER_TYPE_NONE = 0;
    public static final int ROWS_HEADER_TYPE_USUAL = 1;
    public static final int ROWS_HEADER_TYPE_CHECKBOX = 2;
    public static final int ROWS_HEADER_TYPE_RADIOBUTTON = 3;
    //
    protected ModelEntityRef rowsDatasource = new ModelEntityRef();
    protected int fixedRows;
    protected int fixedColumns;
    protected int rowsHeaderType = ROWS_HEADER_TYPE_USUAL;
    protected String generalRowFunction;

    @Override
    public boolean isEqual(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DbGridRowsColumnsDesignInfo other = (DbGridRowsColumnsDesignInfo) obj;
        if (this.rowsDatasource != other.rowsDatasource && (this.rowsDatasource == null || !this.rowsDatasource.equals(other.rowsDatasource))) {
            return false;
        }
        if (this.fixedRows != other.fixedRows) {
            return false;
        }
        if (this.fixedColumns != other.fixedColumns) {
            return false;
        }
        if (this.rowsHeaderType != other.rowsHeaderType) {
            return false;
        }
        if ((this.generalRowFunction == null) ? (other.generalRowFunction != null) : !this.generalRowFunction.equals(other.generalRowFunction)) {
            return false;
        }
        return true;
    }

    @Serial
    public ModelEntityRef getRowsDatasource() {
        return rowsDatasource;
    }

    @Serial
    public void setRowsDatasource(ModelEntityRef aValue) {
        ModelEntityRef old = rowsDatasource;
        rowsDatasource = aValue;
        firePropertyChange(ROWSDATASOURCE, old, aValue);
    }

    @Serial
    public int getFixedRows() {
        return fixedRows;
    }

    @Serial
    public void setFixedRows(int aValue) {
        int old = fixedRows;
        fixedRows = aValue;
        firePropertyChange(FIXEDROWS, old, aValue);
    }

    @Serial
    public int getFixedColumns() {
        return fixedColumns;
    }

    @Serial
    public void setFixedColumns(int aValue) {
        int old = fixedColumns;
        fixedColumns = aValue;
        firePropertyChange(FIXEDCOLUMNS, old, aValue);
    }

    @Serial
    public int getRowsHeaderType() {
        return rowsHeaderType;
    }

    @Serial
    public void setRowsHeaderType(int aValue) {
        int old = rowsHeaderType;
        rowsHeaderType = aValue;
        firePropertyChange(ROWSHEADERTYPE, old, aValue);
    }

    @Serial
    public String getGeneralRowFunction() {
        return generalRowFunction;
    }

    @Serial
    public void setGeneralRowFunction(String aValue) {
        String oldValue = generalRowFunction;
        generalRowFunction = aValue;
        firePropertyChange(GENERALROWFUNCTION, oldValue, aValue);
    }

    protected void assign(DbGridRowsColumnsDesignInfo aInfo) {
        if (aInfo != null) {
            setRowsDatasource(aInfo.getRowsDatasource() != null ? aInfo.getRowsDatasource().copy() : null);
            setFixedRows(aInfo.getFixedRows());
            setFixedColumns(aInfo.getFixedColumns());
            setRowsHeaderType(aInfo.getRowsHeaderType());
            setGeneralRowFunction(aInfo.generalRowFunction != null ? new String(aInfo.generalRowFunction.toCharArray()) : null);
        }
    }

    @Override
    public void assign(DesignInfo aSource) {
        if (aSource != null && aSource instanceof DbGridRowsColumnsDesignInfo) {
            assign((DbGridRowsColumnsDesignInfo) aSource);
        }
    }
}