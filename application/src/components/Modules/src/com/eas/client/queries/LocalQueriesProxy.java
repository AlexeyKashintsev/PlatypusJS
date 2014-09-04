/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.client.queries;

import com.bearsoft.rowset.metadata.DataTypeInfo;
import com.bearsoft.rowset.metadata.Field;
import com.bearsoft.rowset.metadata.Fields;
import com.bearsoft.rowset.metadata.ForeignKeySpec;
import com.bearsoft.rowset.metadata.Parameter;
import com.bearsoft.rowset.metadata.Parameters;
import com.eas.client.AppElementFiles;
import com.eas.client.DbClient;
import com.eas.client.cache.ActualCacheEntry;
import com.eas.client.cache.ApplicationSourceIndexer;
import com.eas.client.cache.PlatypusFiles;
import com.eas.client.scripts.ScriptedResource;
import com.eas.script.ScriptUtils;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.nashorn.api.scripting.JSObject;

/**
 *
 * @author mg
 */
public class LocalQueriesProxy implements QueriesProxy<SqlQuery> {

    protected Map<String, ActualCacheEntry<SqlQuery>> entries = new ConcurrentHashMap<>();
    protected ApplicationSourceIndexer indexer;
    protected StoredQueryFactory factory;
    protected DbClient core;

    public LocalQueriesProxy(DbClient aCore, String aAppPathName) throws Exception {
        this(aCore, new ApplicationSourceIndexer(aAppPathName));
    }

    public LocalQueriesProxy(DbClient aCore, ApplicationSourceIndexer aIndexer) throws Exception {
        super();
        indexer = aIndexer;
        factory = new StoredQueryFactory(aCore, this, indexer);
        core = aCore;
    }

    @Override
    public SqlQuery getQuery(String aName, Consumer<SqlQuery> onSuccess, Consumer<Exception> onFailure) throws Exception {
        Callable<SqlQuery> doWork = () -> {
            SqlQuery query;
            Date cachedTimeStamp = null;
            ActualCacheEntry<SqlQuery> entry = entries.get(aName);
            if (entry != null) {
                cachedTimeStamp = entry.getTimeStamp();
            }
            AppElementFiles files = indexer.nameToFiles(aName);
            Date filesTimeStamp = files.getLastModified();
            if (cachedTimeStamp == null || filesTimeStamp.after(cachedTimeStamp)) {
                if (files.hasExtension(PlatypusFiles.JAVASCRIPT_EXTENSION)) {
                    query = queryFromModule(aName);
                } else if (files.hasExtension(PlatypusFiles.SQL_EXTENSION)) {
                    query = factory.loadQuery(aName);
                } else {
                    throw new IllegalStateException("Queries can be constructed with only platypus queries or platypus modules application elements.");
                }
                entries.put(aName, new ActualCacheEntry<>(query, filesTimeStamp));
            } else {
                assert entry != null;
                query = entry.getValue();
            }
            return query;
        };
        if (onSuccess != null) {
            try {
                SqlQuery query = doWork.call();
                try {
                    onSuccess.accept(query);
                } catch (Exception ex) {
                    Logger.getLogger(LocalQueriesProxy.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (Exception ex) {
                if (onFailure != null) {
                    onFailure.accept(ex);
                }
            }
            return null;
        } else {
            return doWork.call();
        }
    }

    private void readScriptFields(String aQueryId, JSObject sSchema, Fields fields) {
        Object oLength = sSchema.getMember("length");
        if (oLength instanceof Number) {
            int length = ((Number) oLength).intValue();
            for (int i = 0; i < length; i++) {
                Object oElement = sSchema.getSlot(i);
                if (oElement instanceof JSObject) {
                    JSObject sElement = (JSObject) oElement;
                    Object oFieldName = ScriptUtils.toJava(sElement.hasMember("name") ? sElement.getMember("name") : null);
                    if (oFieldName instanceof String && !((String) oFieldName).isEmpty()) {
                        String sFieldName = (String) oFieldName;
                        Field field = fields instanceof Parameters ? new Parameter() : new Field();
                        field.setTypeInfo(DataTypeInfo.OTHER);
                        fields.add(field);
                        field.setName(sFieldName);
                        field.setOriginalName(sFieldName);
                        Object oEntity = ScriptUtils.toJava(sElement.hasMember("entity") ? sElement.getMember("entity") : null);
                        if (oEntity instanceof String && !((String) oEntity).isEmpty()) {
                            field.setTableName((String) oEntity);
                        } else {
                            field.setTableName(aQueryId);
                        }
                        Object oDescription = ScriptUtils.toJava(sElement.hasMember("description") ? sElement.getMember("description") : null);
                        if (oDescription instanceof String && !((String) oDescription).isEmpty()) {
                            field.setDescription((String) oDescription);
                        }
                        Object oType = sElement.getMember("type");
                        if (oType instanceof JSObject && ((JSObject) oType).isFunction()) {
                            Object ofName = ScriptUtils.toJava(((JSObject) oType).getMember("name"));
                            if (ofName instanceof String) {
                                String fName = (String) ofName;
                                if (String.class.getSimpleName().equals(fName)) {
                                    field.setTypeInfo(DataTypeInfo.VARCHAR.copy());
                                } else if (Number.class.getSimpleName().equals(fName)) {
                                    field.setTypeInfo(DataTypeInfo.DECIMAL.copy());
                                } else if (Boolean.class.getSimpleName().equals(fName)) {
                                    field.setTypeInfo(DataTypeInfo.BOOLEAN.copy());
                                } else if (Date.class.getSimpleName().equals(fName)) {
                                    field.setTypeInfo(DataTypeInfo.TIMESTAMP.copy());
                                }
                            }
                        }
                        Object oRequired = ScriptUtils.toJava(sElement.hasMember("required") ? sElement.getMember("required") : null);
                        if (oRequired instanceof Boolean) {
                            boolean bRequired = (Boolean) oRequired;
                            field.setNullable(!bRequired);
                        }
                        Object oKey = ScriptUtils.toJava(sElement.hasMember("key") ? sElement.getMember("key") : null);
                        if (oKey instanceof Boolean) {
                            boolean bKey = (Boolean) oKey;
                            field.setPk(bKey);
                            field.setNullable(false);
                        }
                        Object oRef = sElement.hasMember("ref") ? sElement.getMember("ref") : null;
                        if (oRef instanceof JSObject) {
                            JSObject sRef = (JSObject) oRef;
                            Object oProperty = ScriptUtils.toJava(sRef.hasMember("property") ? sRef.getMember("property") : null);
                            if (oProperty instanceof String) {
                                String sProperty = (String) oProperty;
                                if (!sProperty.isEmpty()) {
                                    Object oRefEntity = sRef.hasMember("entity") ? sRef.getMember("entity") : null;
                                    String sRefEntity;
                                    if (oRefEntity instanceof String && !((String) oRefEntity).isEmpty()) {
                                        sRefEntity = (String) oRefEntity;
                                    } else {
                                        sRefEntity = aQueryId;
                                    }
                                    field.setFk(new ForeignKeySpec(null, aQueryId, field.getName(), null, ForeignKeySpec.ForeignKeyRule.CASCADE, ForeignKeySpec.ForeignKeyRule.CASCADE, false, null, sRefEntity, sProperty, null));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected JSObject createModule(String aModuleName) throws Exception {
        ScriptedResource.executeScriptResource(aModuleName);
        return ScriptUtils.createModule(aModuleName);
    }

    protected SqlQuery queryFromModule(String aModuleName) throws Exception {
        SqlQuery query = new JsQuery(core, aModuleName);
        JSObject schemaContainer = createModule(aModuleName);
        if (schemaContainer != null) {
            Fields fields = new Fields();
            query.setFields(fields);
            Object oSchema = schemaContainer.hasMember("schema") ? schemaContainer.getMember("schema") : null;
            if (oSchema instanceof JSObject) {
                readScriptFields(aModuleName, (JSObject) oSchema, fields);
                Parameters params;
                Object oParams = schemaContainer.hasMember("params") ? schemaContainer.getMember("params") : null;
                if (oParams instanceof JSObject) {
                    params = new Parameters();
                    readScriptFields(aModuleName, (JSObject) oParams, params);
                    params.toCollection().stream().forEach((p) -> {
                        query.putParameter(p.getName(), p.getTypeInfo(), null);
                    });
                }
                return query;
            } else {
                throw new IllegalStateException(" datasource module: " + aModuleName + " doesn't contain a schema");
            }
        } else {
            throw new IllegalStateException(" datasource module: " + aModuleName + " is not found");
        }
    }
}