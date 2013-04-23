/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.dbcontrols.grid;

import com.bearsoft.gui.grid.header.GridColumnsGroup;
import com.eas.client.model.ModelElementRef;
import com.eas.client.model.ModelEntityRef;
import com.eas.controls.DesignInfo;
import com.eas.dbcontrols.DbControlDesignInfo;
import com.eas.dbcontrols.DbControlsUtils;
import com.eas.dbcontrols.ScalarDbControl;
import com.eas.dbcontrols.grid.rt.DummyCellEditor;
import com.eas.dbcontrols.visitors.DbSwingFactory;
import com.eas.design.Designable;
import com.eas.design.Undesignable;
import com.eas.gui.CascadedStyle;
import com.eas.store.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author mg
 */
public class DbGridColumn extends DesignInfo implements PropertiesSimpleFactory {

    public static final String BACKGROUND = "background";
    public static final String CELLDESIGNINFO = "cellDesignInfo";
    public static final String CELLFUNCTION = "cellFunction";
    public static final String CELLSDATASOURCE = "cellsDatasource";
    public static final String CHILDREN = "children";
    public static final String COLUMNSDATASOURCE = "columnsDatasource";
    public static final String COLUMNSDISPLAYFIELD = "columnsDisplayField";
    public static final String CONTROLINFO = "controlInfo";
    public static final String DATAMODELELEMENT = "datamodelElement";
    public static final String EDITABLE = "editable";
    public static final String ENABLED = "enabled";
    public static final String HEADERFONT = "headerFont";
    public static final String NAME = "name";
    public static final String PARENT = "parent";
    public static final String PLAIN = "plain";
    public static final String VEER = "veer";
    public static final String READONLY = "readonly";
    public static final String SELECTFUNCTION = "selectFunction";
    public static final String SELECTONLY = "selectOnly";
    public static final String STYLE = "style";
    public static final String SUBSTITUTE = "substitute";
    public static final String TITLE = "title";
    public static final String VISIBLE = "visible";
    public static final String WIDTH = "width";
    protected DbGridColumn parent = null;
    // flag indicating whethier column is plain or fliped
    protected boolean plain = true;
    // plain columns
    protected ModelElementRef datamodelElement = null;
    protected DbControlDesignInfo controlInfo = null;
    protected int width = 50;
    protected boolean readonly = false;
    protected boolean enabled = true;
    protected boolean visible = true;
    protected String title = null;
    protected String name = null;
    // fliped columns
    protected ModelEntityRef columnsDatasource = new ModelEntityRef();
    protected ModelEntityRef cellsDatasource = new ModelEntityRef();
    protected ModelElementRef columnsDisplayField = new ModelElementRef();
    protected DbGridCellDesignInfo cellDesignInfo = new DbGridCellDesignInfo();
    // flag indicating whethier this column should remain in place or be substituted by it's flipping ds
    protected boolean substitute = false;
    protected String selectFunction = null;
    protected boolean selectOnly = false;
    protected String cellFunction = null;
    // runtime needed temporary data
    public int rtMinimumWidth = -1;
    public int rtWidth = -1;
    public int rtChildrenWidth = -1;
    public int rtHeight = -1;
    public int rtPaddedHeight = -1;
    public int rtChildrenHeight = -1;
    public int rtPaddingHeight = 0;
    public Rectangle rtCell = null;
    protected CascadedStyle style = new CascadedStyle();
    protected List<DbGridColumn> children = new ArrayList<>();

    public DbGridColumn() {
        super();
    }

    public DbGridColumn(DbGridColumn aParent) {
        this();
        setParent(aParent);
    }

    public void setParent(DbGridColumn aValue) {
        DbGridColumn old = parent;
        parent = aValue;
        if (parent != null && style != null) {
            style.setParent(parent.getHeaderStyle());
        }
        firePropertyChange(PARENT, old, aValue);
    }

    @Undesignable
    @Serial
    public CascadedStyle getHeaderStyle() {
        return style;
    }

    @Serial
    public void setHeaderStyle(CascadedStyle aValue) {
        CascadedStyle old = style;
        style = aValue;
        firePropertyChange(STYLE, old, aValue);
    }

    public boolean containsInChildren(DbGridColumn aChild) {
        return children != null && children.indexOf(aChild) != -1;
    }

    public TableCellRenderer createCellRenderer() throws Exception {
        if (controlInfo != null) {
            DbSwingFactory factory = new DbSwingFactory();
            controlInfo.accept(factory);
            assert factory.getComp() instanceof ScalarDbControl;
            ScalarDbControl control = (ScalarDbControl) factory.getComp();
            control.setBorderless(true);
            control.setStandalone(false);
            return control;
        }
        return new DefaultTableCellRenderer();
    }

    public TableCellEditor createCellEditor() throws Exception {
        if (!isReadonly() && isEnabled() && controlInfo != null) {
            DbSwingFactory factory = new DbSwingFactory();
            controlInfo.accept(factory);
            assert factory.getComp() instanceof ScalarDbControl;
            ScalarDbControl control = (ScalarDbControl) factory.getComp();
            control.setSelectOnly(isSelectOnly());
            control.setStandalone(false);
            control.setBorderless(true);
            return control;
        } else {
            return new DummyCellEditor();
        }
    }

    @Undesignable
    public DbGridColumn getParent() {
        return parent;
    }

    public void lightAssign(DbGridColumn aSource) {
        if (aSource != null) {
            if (aSource.getDatamodelElement() != null) {
                setDatamodelElement(aSource.getDatamodelElement().copy());
            } else {
                setDatamodelElement(null);
            }
            if (aSource.getControlInfo() != null) {
                setControlInfo((DbControlDesignInfo) aSource.getControlInfo().copy());
            } else {
                setControlInfo(null);
            }

            if (aSource.getCellDesignInfo() != null) {
                setCellDesignInfo((DbGridCellDesignInfo) aSource.getCellDesignInfo().copy());
            } else {
                setCellDesignInfo(null);
            }
            setColumnsDatasource(aSource.getColumnsDatasource() != null ? aSource.getColumnsDatasource().copy() : null);
            setColumnsDisplayField(aSource.getColumnsDisplayField() != null ? aSource.getColumnsDisplayField().copy() : null);
            setCellsDatasource(aSource.getCellsDatasource() != null ? aSource.getCellsDatasource().copy() : null);

            setEnabled(aSource.isEnabled());
            setWidth(aSource.getWidth());
            setReadonly(aSource.isReadonly());
            setVisible(aSource.isVisible());
            setPlain(aSource.isPlain());
            setSubstitute(aSource.isSubstitute());
            setSelectOnly(aSource.isSelectOnly());
            if (aSource.getTitle() != null) {
                setTitle(new String(aSource.getTitle().toCharArray()));
            } else {
                setTitle(null);
            }
            if (aSource.getName() != null) {
                setName(new String(aSource.getName().toCharArray()));
            } else {
                setName(null);
            }
            if (aSource.getSelectFunction() != null) {
                setSelectFunction(new String(aSource.getSelectFunction().toCharArray()));
            } else {
                setSelectFunction(null);
            }
            if (aSource.getCellFunction() != null) {
                setCellFunction(new String(aSource.getCellFunction().toCharArray()));
            } else {
                setCellFunction(null);
            }
            if (aSource.getHeaderStyle() != null) {
                setHeaderStyle(aSource.getHeaderStyle().copy());
            } else {
                setHeaderStyle(null);
            }
        } else {
            setDatamodelElement(null);
            setControlInfo(null);
            setWidth(50);
            setReadonly(false);
            setEnabled(true);
            setVisible(true);
            setPlain(true);
            setSubstitute(false);
            setTitle(null);
            setName(null);
            setSelectFunction(null);
            setSelectOnly(false);
            setCellFunction(null);
            setHeaderStyle(null);

            setCellDesignInfo(null);
            setColumnsDatasource(null);
            setColumnsDisplayField(null);
            setCellsDatasource(null);
        }
    }

    public boolean lightIsEqual(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DbGridColumn other = (DbGridColumn) obj;

        if (this.datamodelElement != other.datamodelElement && (this.datamodelElement == null || !this.datamodelElement.equals(other.datamodelElement))) {
            return false;
        }
        if (this.controlInfo != other.controlInfo && (this.controlInfo == null || !this.controlInfo.isEqual(other.controlInfo))) {
            return false;
        }
        if (this.width != other.width) {
            return false;
        }
        if (this.readonly != other.readonly) {
            return false;
        }
        if (this.enabled != other.enabled) {
            return false;
        }
        if (this.visible != other.visible) {
            return false;
        }
        if (this.plain != other.plain) {
            return false;
        }
        if (this.substitute != other.substitute) {
            return false;
        }
        if (this.selectOnly != other.selectOnly) {
            return false;
        }
        if ((this.title == null) ? (other.title != null) : !this.title.equals(other.title)) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.selectFunction == null) ? (other.selectFunction != null) : !this.selectFunction.equals(other.selectFunction)) {
            return false;
        }
        if ((this.cellFunction == null) ? (other.cellFunction != null) : !this.cellFunction.equals(other.cellFunction)) {
            return false;
        }
        if (this.cellDesignInfo != other.cellDesignInfo && (this.cellDesignInfo == null || !this.cellDesignInfo.isEqual(other.cellDesignInfo))) {
            return false;
        }
        if (this.columnsDisplayField != other.columnsDisplayField && (this.columnsDisplayField == null || !this.columnsDisplayField.equals(other.columnsDisplayField))) {
            return false;
        }
        if (this.columnsDatasource != other.columnsDatasource && (this.columnsDatasource == null || !this.columnsDatasource.equals(other.columnsDatasource))) {
            return false;
        }
        if (this.cellsDatasource != other.cellsDatasource && (this.cellsDatasource == null || !this.cellsDatasource.equals(other.cellsDatasource))) {
            return false;
        }
        if (this.style != other.style && (this.style == null || !this.style.isEqual(other.style))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isEqual(Object obj) {
        if (!lightIsEqual(obj)) {
            return false;
        }
        final DbGridColumn other = (DbGridColumn) obj;
        if (this.children != other.children && (this.children == null || !isColumnsEquals(this.children, other.children))) {
            return false;
        }
        return true;
    }

    public static boolean isColumnsEquals(List<DbGridColumn> first, List<DbGridColumn> second) {
        if (first != second) {
            if (first != null && second != null) {
                if (first.size() == second.size()) {
                    for (int i = 0; i < first.size(); i++) {
                        DbGridColumn fColumn = first.get(i);
                        DbGridColumn sColumn = second.get(i);
                        if (fColumn != sColumn) {
                            if (fColumn != null && sColumn != null) {
                                if (!fColumn.isEqual(sColumn)) {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        }
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    protected void assign(DbGridColumn aSource) {
        lightAssign(aSource);
        List<DbGridColumn> old = children;
        children = new ArrayList<>();
        if (aSource != null && aSource.getChildren() != null) {
            List<DbGridColumn> lchildren = aSource.getChildren();
            for (int i = 0; i < lchildren.size(); i++) {
                if (lchildren.get(i) != null) {
                    DbGridColumn newCol = lchildren.get(i).copy();
                    children.add(newCol);
                    newCol.setParent(this);
                } else {
                    children.add(null);
                }
            }
            assert children.size() == lchildren.size();
        }
        firePropertyChange(CHILDREN, old, children);
    }

    @Override
    public void assign(DesignInfo aValue) {
        if (aValue != null && aValue instanceof DbGridColumn) {
            assign((DbGridColumn) aValue);
        }
    }

    public DbGridColumn lightCopy() {
        DbGridColumn col = new DbGridColumn();
        col.lightAssign(this);
        return col;
    }

    @Override
    public DbGridColumn copy() {
        DbGridColumn col = new DbGridColumn();
        col.assign(this);
        return col;
    }

    @Designable(category = "header")
    public java.awt.Font getFont() {
        return style != null ? DbControlsUtils.toNativeFont(style.getFont()) : null;
    }

    public void setFont(java.awt.Font aValue) {
        java.awt.Font oldValue = getFont();
        if (oldValue != aValue) {
            if (style != null) {
                style.setFont(DbControlsUtils.toFont(aValue));
                firePropertyChange(HEADERFONT, oldValue, aValue);
            }
        }
    }

    @Serial
    public int getWidth() {
        return width;
    }

    @Serial
    public void setWidth(int aValue) {
        int old = width;
        width = aValue;
        firePropertyChange(WIDTH, old, aValue);
    }

    @Undesignable
    @SerialCollection(elementType = DbGridColumn.class, elementTagName = "column", deserializeAs = ArrayList.class)
    public List<DbGridColumn> getChildren() {
        return children;
    }

    @SerialCollection(elementType = DbGridColumn.class, elementTagName = "column", deserializeAs = ArrayList.class)
    public void setChildren(List<DbGridColumn> aValue) {
        List<DbGridColumn> old = children;
        children = aValue;
        if (children != null) {
            for (DbGridColumn lcol : children) {
                if (lcol != null) {
                    lcol.setParent(this);
                }
            }
        } else {
            children = new ArrayList<>();
        }
        firePropertyChange(CHILDREN, old, children);
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    @Undesignable
    @Serial
    public String getSelectFunction() {
        return selectFunction;
    }

    @Serial
    public void setSelectFunction(String aValue) {
        String old = selectFunction;
        selectFunction = aValue;
        firePropertyChange(SELECTFUNCTION, old, aValue);
    }

    @Undesignable
    public String getOnSelect() {
        return selectFunction;
    }

    public void setOnSelect(String aValue) {
        selectFunction = aValue;
    }

    @Serial
    public boolean isSelectOnly() {
        return selectOnly;
    }

    @Serial
    public void setSelectOnly(boolean aValue) {
        boolean old = selectOnly;
        selectOnly = aValue;
        firePropertyChange(SELECTONLY, old, aValue);
    }

    @Undesignable
    @Serial
    public String getCellFunction() {
        return cellFunction;
    }

    @Serial
    public void setCellFunction(String aValue) {
        String old = cellFunction;
        cellFunction = aValue;
        firePropertyChange(CELLFUNCTION, old, aValue);
    }

    @Undesignable
    public String getOnRender() {
        return cellFunction;
    }

    public void setOnRender(String aValue) {
        cellFunction = aValue;
    }

    @Undesignable
    @Serial
    public boolean isPlain() {
        return plain;
    }

    @Serial
    public void setPlain(boolean aValue) {
        boolean old = plain;
        plain = aValue;
        firePropertyChange(PLAIN, old, aValue);
    }

    public boolean isVeer() {
        return !isPlain();
    }

    public void setVeer(boolean aValue) {
        boolean old = !plain;
        plain = !aValue;
        firePropertyChange(VEER, old, !plain);
    }

    @Designable(category="veer")
    @Serial
    public boolean isSubstitute() {
        return substitute;
    }

    @Serial
    public void setSubstitute(boolean aValue) {
        boolean old = substitute;
        substitute = aValue;
        firePropertyChange(SUBSTITUTE, old, aValue);
    }

    @Serial
    public boolean isReadonly() {
        return readonly;
    }

    @Serial
    public void setReadonly(boolean aValue) {
        boolean old = readonly;
        readonly = aValue;
        firePropertyChange(READONLY, old, aValue);
    }

    @Undesignable
    public boolean isEditable() {
        return !readonly;
    }

    public void setEditable(boolean aValue) {
        boolean old = !readonly;
        readonly = !aValue;
        firePropertyChange(EDITABLE, old, aValue);
    }

    @Serial
    public boolean isEnabled() {
        return enabled;
    }

    @Serial
    public void setEnabled(boolean aValue) {
        boolean old = enabled;
        enabled = aValue;
        firePropertyChange(ENABLED, old, aValue);
    }

    @Serial
    public boolean isVisible() {
        return visible;
    }

    @Serial
    public void setVisible(boolean aValue) {
        boolean old = visible;
        visible = aValue;
        firePropertyChange(VISIBLE, old, aValue);
    }

    @Serial
    public String getTitle() {
        return title;
    }

    @Serial
    public void setTitle(String aValue) {
        String old = title;
        title = aValue;
        firePropertyChange(TITLE, old, aValue);
    }

    @Serial
    public String getName() {
        return name;
    }

    @Serial
    public void setName(String aValue) {
        String old = name;
        name = aValue;
        firePropertyChange(NAME, old, aValue);
    }

    @Designable(category = "header")
    public Color getBackground() {
        return style.getBackground();
    }

    public void setBackground(Color aValue) {
        Color old = getBackground();
        style.setBackground(aValue);
        firePropertyChange(BACKGROUND, old, aValue);
    }

    @Undesignable
    @Serial
    public DbGridCellDesignInfo getCellDesignInfo() {
        return cellDesignInfo;
    }

    @Serial
    public void setCellDesignInfo(DbGridCellDesignInfo aValue) {
        DbGridCellDesignInfo old = cellDesignInfo;
        cellDesignInfo = aValue;
        firePropertyChange(CELLDESIGNINFO, old, aValue);
    }

    @Designable(displayName = "field", category = "model")
    @Serial
    public ModelElementRef getDatamodelElement() {
        return datamodelElement;
    }

    @Serial
    public void setDatamodelElement(ModelElementRef aValue) {
        ModelElementRef old = datamodelElement;
        datamodelElement = aValue;
        firePropertyChange(DATAMODELELEMENT, old, aValue);
    }

    @Undesignable
    @ClassedSerial(propertyClassHint = "classHint")
    public DbControlDesignInfo getControlInfo() {
        return controlInfo;
    }

    @ClassedSerial(propertyClassHint = "classHint")
    public void setControlInfo(DbControlDesignInfo aValue) {
        DbControlDesignInfo old = controlInfo;
        controlInfo = aValue;
        firePropertyChange(CONTROLINFO, old, aValue);
    }

    @Designable(category="veer")
    public ModelElementRef getRowsKeyField() {
        return cellDesignInfo != null ? cellDesignInfo.getRowsKeyField() : null;
    }

    public void setRowsKeyField(ModelElementRef aValue) {
        if (cellDesignInfo != null) {
            cellDesignInfo.setRowsKeyField(aValue);
        }
    }

    @Designable(category="veer")
    public ModelElementRef getColumnsKeyField() {
        return cellDesignInfo != null ? cellDesignInfo.getColumnsKeyField() : null;
    }

    public void setColumnsKeyField(ModelElementRef aValue) {
        if (cellDesignInfo != null) {
            cellDesignInfo.setColumnsKeyField(aValue);
        }
    }

    @Designable(category="veer")
    public ModelElementRef getCellValueField() {
        return cellDesignInfo != null ? cellDesignInfo.getCellValueField() : null;
    }

    public void setCellValueField(ModelElementRef aValue) {
        if (cellDesignInfo != null) {
            cellDesignInfo.setCellValueField(aValue);
        }
    }

    @Designable(displayName = "columnsEntity", category = "veer")
    @Serial
    public ModelEntityRef getColumnsDatasource() {
        return columnsDatasource;
    }

    @Serial
    public void setColumnsDatasource(ModelEntityRef aValue) {
        ModelElementRef old = columnsDatasource;
        columnsDatasource = aValue;
        firePropertyChange(COLUMNSDATASOURCE, old, aValue);
    }

    @Designable(category = "veer")
    @Serial
    public ModelElementRef getColumnsDisplayField() {
        return columnsDisplayField;
    }

    @Serial
    public void setColumnsDisplayField(ModelElementRef aValue) {
        ModelElementRef old = columnsDisplayField;
        columnsDisplayField = aValue;
        firePropertyChange(COLUMNSDISPLAYFIELD, old, aValue);
    }

    @Designable(displayName="cellsEntity", category = "veer")
    @Serial
    public ModelEntityRef getCellsDatasource() {
        return cellsDatasource;
    }

    @Serial
    public void setCellsDatasource(ModelEntityRef aValue) {
        ModelElementRef old = cellsDatasource;
        cellsDatasource = aValue;
        firePropertyChange(CELLSDATASOURCE, old, aValue);
    }

    public void removeChild(DbGridColumn aCol) {
        if (children != null) {
            children.remove(aCol);
        }
    }

    public void addChild(DbGridColumn aCol) {
        if (children == null) {
            children = new ArrayList<>();
        }
        if (!children.contains(aCol)) {
            children.add(aCol);
        }
    }

    public void addChild(int atIndex, DbGridColumn aCol) {
        if (children == null) {
            children = new ArrayList<>();
        }
        if (!children.contains(aCol)
                && atIndex >= 0 && atIndex <= children.size()) {
            children.add(atIndex, aCol);
        }
    }

    @Override
    public Object createPropertyObjectInstance(String aSimpleClassName) {
        return DbControlsUtils.createDesignInfoBySimpleClassName(aSimpleClassName);
    }

    public void initializeGridColumnsGroup(GridColumnsGroup group) {
        group.setStyle(getHeaderStyle());
        group.setEditable(isEditable());
        group.setEnabled(isEnabled());
        group.setMaxWidth(Integer.MAX_VALUE);
        group.setMinWidth(5);
        group.setName(getName());
        group.setReadonly(isReadonly());
        group.setSelectOnly(isSelectOnly());
        group.setTitle(getTitle());
        group.setVisible(isVisible());
        group.setWidth(getWidth());
        group.setSubstitute(isSubstitute());
    }
}