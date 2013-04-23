/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.client.model.application;

import com.bearsoft.rowset.changes.Change;
import com.bearsoft.rowset.metadata.Field;
import com.bearsoft.rowset.metadata.Fields;
import com.bearsoft.rowset.metadata.Parameters;
import com.eas.client.model.Model;
import com.eas.client.model.ParametersRowset;
import com.eas.client.model.script.RowsetHostObject;
import com.eas.client.model.script.ScriptableRowset;
import com.eas.client.model.visitors.ApplicationModelVisitor;
import com.eas.client.model.visitors.ModelVisitor;
import com.eas.client.queries.SqlQuery;
import java.util.ArrayList;
import java.util.List;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 *
 * @author mg
 */
public class ApplicationDbParametersEntity extends ApplicationDbEntity implements ApplicationParametersEntity {

    protected List<Change> changeLog = new ArrayList<>();// dummy change log. No entries expected here

    public ApplicationDbParametersEntity(ApplicationDbModel aModel) {
        super();
        model = aModel;
        entityID = ApplicationModel.PARAMETERS_ENTITY_ID;
        executed = true;
    }

    @Override
    public List<Change> getChangeLog() throws Exception {
        return changeLog;
    }

    @Override
    public boolean isRowsetPresent() {
        return true;
    }

    @Override
    public Fields getFields() {
        return model.getParameters();
    }

    @Override
    protected boolean isTagValid(String aTagName) {
        return true;
    }

    @Override
    public ApplicationDbParametersEntity copy() throws Exception {
        ApplicationDbParametersEntity copied = new ApplicationDbParametersEntity(model);
        assign(copied);
        return copied;
    }

    @Override
    public void accept(ModelVisitor<ApplicationDbEntity> visitor) {
        if(visitor instanceof ApplicationModelVisitor<?>)
        {
            ((ApplicationModelVisitor<?>)visitor).visit(this);
        }
    }

    @Override
    public String getQueryId() {
        return null;
    }

    @Override
    public SqlQuery getQuery() {
        return null;
    }

    @Override
    public String getTableDbId() {
        return null;
    }

    @Override
    public void setTableDbId(String tableDbId) {
    }

    @Override
    public String getTableName() {
        return null;
    }

    @Override
    public void setTableName(String aTableName) {
    }

    @Override
    public Long getEntityID() {
        return ApplicationModel.PARAMETERS_ENTITY_ID;
    }

    @Override
    public Scriptable defineProperties() throws Exception {
        if (model.getScriptScope() != null && model.getScriptScope() instanceof ScriptableObject) {
            ScriptableObject scope = (ScriptableObject) model.getScriptScope();
            ScriptableRowset<ApplicationDbEntity> sRowset = new ScriptableRowset<>((ApplicationDbEntity)this);
            // global parameters names
            Fields md = sRowset.getFields();
            sRowset.createScriptableFields();
            if (md != null) {
                for (int i = 1; i <= md.getFieldsCount(); i++) {
                    Field field = md.get(i);
                    String fName = field.getName();
                    if (fName != null && !fName.isEmpty()) {
                        scope.defineProperty(fName, sRowset.getScriptableField(fName), ScriptableRowset.getValueScriptableFieldMethod, ScriptableRowset.setValueScriptableFieldMethod, 0);
                    }
                }
            }
            // predefined and user parameters names
            sRowsetWrap = new RowsetHostObject<>(sRowset, scope);
            // global parameters metadata
            scope.defineProperty(Model.DATASOURCE_METADATA_SCRIPT_NAME, sRowsetWrap.get(Model.DATASOURCE_METADATA_SCRIPT_NAME, sRowsetWrap), ScriptableObject.READONLY);
            // predefined
            scope.defineProperty(Model.PARAMETERS_SCRIPT_NAME, sRowsetWrap, ScriptableObject.READONLY);
            // user
            String dsName = getName();
            if (dsName != null && !dsName.isEmpty()) {
                scope.defineProperty(dsName, sRowsetWrap, ScriptableObject.READONLY);
            }
            return sRowsetWrap;
        }
        return null;
    }

    @Override
    public boolean execute() {
        if (rowset == null) {
            rowset = new ParametersRowset((Parameters) model.getParameters());
            rowset.addRowsetListener(this);
        }
        return rowset != null;
    }

    @Override
    protected boolean internalExecute(boolean refresh) {
        executing = false;
        executed = true;
        return true;
    }

    @Override
    protected void achieveOrRefreshRowset() throws Exception {
        // no op
    }

    @Override
    public void validateQuery() throws Exception {
        // no op
    }
}