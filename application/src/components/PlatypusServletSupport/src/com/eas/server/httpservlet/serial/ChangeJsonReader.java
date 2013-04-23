/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.server.httpservlet.serial;

import com.bearsoft.rowset.Converter;
import com.bearsoft.rowset.RowsetConverter;
import com.bearsoft.rowset.changes.Change;
import com.bearsoft.rowset.changes.ChangeVisitor;
import com.bearsoft.rowset.changes.Command;
import com.bearsoft.rowset.changes.Delete;
import com.bearsoft.rowset.changes.EntitiesHost;
import com.bearsoft.rowset.changes.Insert;
import com.bearsoft.rowset.changes.Update;
import com.bearsoft.rowset.metadata.Field;
import com.eas.script.ScriptUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;

/**
 *
 * @author mg
 */
public class ChangeJsonReader implements ChangeVisitor {

    private static final String CHANGE_DATA_NAME = "data";
    private static final String CHANGE_KEYS_NAME = "keys";
    private static final String CHANGE_PARAMETERS_NAME = "parameters";
    protected static Converter converter = new RowsetConverter();
    protected Scriptable sChange;
    protected String entityId;
    protected EntitiesHost fieldsResolver;

    public ChangeJsonReader(Scriptable aSChange, String aEntityId, EntitiesHost aFieldsResolver) throws Exception {
        super();
        sChange = aSChange;
        entityId = aEntityId;
        fieldsResolver = aFieldsResolver;
    }

    protected Change.Value[] parseObjectProperties(Object oData) throws Exception {
        List<Change.Value> data = new ArrayList<>();
        if (oData instanceof Scriptable) {
            Scriptable sValue = (Scriptable) oData;
            Object[] valueIds = sValue.getIds();
            for (int j = 0; j < valueIds.length; j++) {
                Object oValueName = valueIds[j];
                if (oValueName instanceof String) {
                    String sValueName = (String) oValueName;
                    Object oValueValue = sValue.get(sValueName, ScriptUtils.getScope());
                    Field field = fieldsResolver.resolveField(entityId, sValueName);
                    if (field != null) {
                        Object convertedValueValue = converter.convert2RowsetCompatible(oValueValue, field.getTypeInfo());
                        data.add(new Change.Value(sValueName, convertedValueValue, field.getTypeInfo()));
                    } else {
                        Logger.getLogger(ChangeJsonReader.class.getName()).log(Level.WARNING, String.format("Couldn't resolve entity property name: %s.%s", entityId, sValueName));
                    }
                } else {
                    Logger.getLogger(ChangeJsonReader.class.getName()).log(Level.WARNING, "Value name must be a string.");
                }
            }
        }
        return data.toArray(new Change.Value[]{});
    }

    @Override
    public void visit(Insert aChange) throws Exception {
        Object oData = sChange.get(CHANGE_DATA_NAME, ScriptUtils.getScope());
        aChange.data = parseObjectProperties(oData);
    }

    @Override
    public void visit(Update aChange) throws Exception {
        Object oData = sChange.get(CHANGE_DATA_NAME, ScriptUtils.getScope());
        aChange.data = parseObjectProperties(oData);
        Object oKeys = sChange.get(CHANGE_KEYS_NAME, ScriptUtils.getScope());
        aChange.keys = parseObjectProperties(oKeys);
    }

    @Override
    public void visit(Delete aChange) throws Exception {
        Object oKeys = sChange.get(CHANGE_KEYS_NAME, ScriptUtils.getScope());
        aChange.keys = parseObjectProperties(oKeys);
    }

    @Override
    public void visit(Command aChange) throws Exception {
        Object parameters = sChange.get(CHANGE_PARAMETERS_NAME, ScriptUtils.getScope());
        aChange.parameters = parseObjectProperties(parameters);
    }

    public static List<Change> parse(String aJsonText, EntitiesHost aFieldsResolver) throws Exception {
        List<Change> changes = new ArrayList<>();
        Object sChanges = ScriptUtils.parseJson(aJsonText);
        if (sChanges instanceof NativeArray) {
            NativeArray aChanges = (NativeArray) sChanges;
            for (int i = 0; i < aChanges.getLength(); i++) {
                Object oChange = aChanges.get(i);
                if (oChange instanceof Scriptable) {
                    Scriptable sChange = (Scriptable) oChange;
                    Object oKind = sChange.get("kind", ScriptUtils.getScope());
                    Object oEntityId = sChange.get("entity", ScriptUtils.getScope());
                    if (oKind instanceof String && oEntityId instanceof String) {
                        String sKind = (String) oKind;
                        String sEntityId = (String) oEntityId;
                        Change change = null;
                        switch (sKind) {
                            case "insert":
                                change = new Insert(sEntityId);
                                break;
                            case "update":
                                change = new Update(sEntityId);
                                break;
                            case "delete":
                                change = new Delete(sEntityId);
                                break;
                            case "command":
                                change = new Command(sEntityId);
                                break;
                        }
                        if (change != null) {
                            ChangeJsonReader reader = new ChangeJsonReader(sChange, sEntityId, aFieldsResolver);
                            change.accept(reader);
                            changes.add(change);
                        } else {
                            Logger.getLogger(ChangeJsonReader.class.getName()).log(Level.SEVERE, String.format("Unknown type of change occured %s.", sKind));
                        }
                    } else {
                        Logger.getLogger(ChangeJsonReader.class.getName()).log(Level.SEVERE, "Kind of change and target entity id must present.");
                    }
                } else {
                    Logger.getLogger(ChangeJsonReader.class.getName()).log(Level.SEVERE, "Every change must be an object.");
                }
            }
        } else {
            Logger.getLogger(ChangeJsonReader.class.getName()).log(Level.SEVERE, "Changes must be an array.");
        }
        return changes;
    }
}