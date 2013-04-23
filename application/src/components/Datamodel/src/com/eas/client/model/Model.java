/* Datamodel license.
 * Exclusive rights on this code in any form
 * are belong to it's author. This code was
 * developed for commercial purposes only. 
 * For any questions and any actions with this
 * code in any form you have to contact to it's
 * author.
 * All rights reserved.
 */
package com.eas.client.model;

import com.bearsoft.rowset.metadata.Field;
import com.bearsoft.rowset.metadata.Fields;
import com.bearsoft.rowset.metadata.Parameters;
import com.eas.client.Client;
import com.eas.client.model.visitors.ModelVisitor;
import com.eas.client.queries.Query;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.javascript.Scriptable;
import org.w3c.dom.Document;

/**
 * @param <E> Entity generic type.
 * @param <P> Parameters entity generic type.
 * @param <C> Client generic type.
 * @param <Q> Query generic type.
 * @author mg
 */
public abstract class Model<E extends Entity<?, Q, E>, P extends E, C extends Client, Q extends Query<C>> implements PropertyChangeListener {

    public static final long PARAMETERS_ENTITY_ID = -1L;
    public static final String PARAMETERS_SCRIPT_NAME = "params";
    public static final String DATASOURCE_METADATA_SCRIPT_NAME = "md";
    public static final String DATASOURCE_NAME_TAG_NAME = "Name";
    public static final String DATASOURCE_TITLE_TAG_NAME = "Title";
    public static final String DATASOURCE_BEFORE_CHANGE_EVENT_TAG_NAME = "onBeforeChange";
    public static final String DATASOURCE_AFTER_CHANGE_EVENT_TAG_NAME = "onAfterChange";
    public static final String DATASOURCE_BEFORE_SCROLL_EVENT_TAG_NAME = "onBeforeScroll";
    public static final String DATASOURCE_AFTER_SCROLL_EVENT_TAG_NAME = "onAfterScroll";
    public static final String DATASOURCE_BEFORE_INSERT_EVENT_TAG_NAME = "onBeforeInsert";
    public static final String DATASOURCE_AFTER_INSERT_EVENT_TAG_NAME = "onAfterInsert";
    public static final String DATASOURCE_BEFORE_DELETE_EVENT_TAG_NAME = "onBeforeDelete";
    public static final String DATASOURCE_AFTER_DELETE_EVENT_TAG_NAME = "onAfterDelete";
    public static final String DATASOURCE_AFTER_REQUERY_EVENT_TAG_NAME = "onRequeried";
    public static final String DATASOURCE_AFTER_FILTER_EVENT_TAG_NAME = "onFiltered";
    protected StoredQueryFactory queryFactory;
    protected C client = null;
    protected Set<Relation<E>> relations = new HashSet<>();
    protected Map<Long, E> entities = new HashMap<>();
    protected P parametersEntity;
    protected Parameters parameters = new Parameters();
    protected Scriptable scriptScope = null;
    protected boolean runtime = false;
    protected boolean commitable = true;
    protected int ajustingCounter = 0;
    protected GuiCallback guiCallback;
    protected ModelEditingSupport<E> editingSupport = new ModelEditingSupport<>();
    protected PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public Model<E, P, C, Q> copy() throws Exception {
        Model<E, P, C, Q> copied = getClass().newInstance();
        copied.setClient(client);
        for (E entity : entities.values()) {
            copied.addEntity((E) entity.copy());
        }
        for (Relation<E> relation : relations) {
            copied.addRelation(relation.copy());
        }
        if (parameters != null) {
            copied.setParameters(parameters.copy());
        }
        if (getParametersEntity() != null) {
            copied.setParametersEntity((P) getParametersEntity().copy());
        }
        copied.resolveReferences();
        return copied;
    }

    /**
     * Base datamodel constructor.
     */
    protected Model() {
        super();
    }

    /**
     * Constructor of datamodel. Used in designers.
     *
     * @param aClient C instance all queries to be sent to.
     * @see C
     */
    public Model(C aClient) {
        this();
        setClient(aClient);
    }

    public PropertyChangeSupport getChangeSupport() {
        return changeSupport;
    }

    public void setClient(C aValue) {
        client = aValue;
    }

    public void clearRelations() {
        if (relations != null) {
            relations.clear();
        }
    }

    public GuiCallback getGuiCallback() {
        return guiCallback;
    }

    public void setGuiCallback(GuiCallback aValue) {
        guiCallback = aValue;
        editingSupport.setGuiCallback(aValue);
    }

    /**
     * Finds entity by table name. Takes into account only table name, ignoring
     * schema, even it's exist.
     *
     * @param aTableName
     * @return Entity found;
     */
    public E getEntityByTableName(String aTableName) {
        if (aTableName != null && !aTableName.isEmpty() && entities != null) {
            for (E ent : entities.values()) {
                String tableName = ent.getTableName();
                if (tableName != null && !tableName.isEmpty()) {
                    if (tableName.toLowerCase().equals(aTableName.toLowerCase())) {
                        return ent;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds entity by full table name. Takes into account schema name.
     *
     * @param aTableName
     * @return Entity found;
     */
    public E getEntityByFullTableName(String aTableName) {
        if (aTableName != null && !aTableName.isEmpty() && entities != null) {
            for (E ent : entities.values()) {
                String tableName = ent.getTableName();
                if (tableName != null && !tableName.isEmpty()) {
                    if (ent.getTableSchemaName() != null && !ent.getTableSchemaName().isEmpty()) {
                        tableName = ent.getTableSchemaName() + "." + tableName;
                    }
                    if (tableName.toLowerCase().equals(aTableName.toLowerCase())) {
                        return ent;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds entity by name.
     *
     * @param aName
     * @return Entity found;
     */
    public E getEntityByName(String aName) {
        for (E appEntity : getEntities().values()) {
            if (appEntity.getName() != null && appEntity.getName().equalsIgnoreCase(aName)) {
                return appEntity;
            }
        }
        if (parametersEntity != null && parametersEntity.getName() != null && parametersEntity.getName().equalsIgnoreCase(aName)) {
            return parametersEntity;
        }
        return null;
    }

    public abstract E newGenericEntity();

    public abstract void accept(ModelVisitor<E> visitor);

    public abstract Document toXML();

    /**
     * Registers an
     * <code>DatamodelEditingValidator</code>. The validator is notified
     * whenever an datamodel edit action will occur.
     *
     * @param v an <code>DatamodelEditingValidator</code> object
     * @see #removeDatamodelEditingValidator
     */
    public synchronized void addEditingValidator(ModelEditingValidator<E> v) {
        editingSupport.addValidator(v);
    }

    /**
     * Removes an
     * <code>DatamodelEditingValidator</code>.
     *
     * @param v the <code>DatamodelEditingValidator</code> object to be removed
     * @see #addDatamodelEditingValidator
     */
    public synchronized void removeEditingValidator(ModelEditingValidator<E> v) {
        editingSupport.removeValidator(v);
    }

    public boolean checkRelationAddingValid(Relation<E> aRelation) {
        return editingSupport.checkRelationAddingValid(aRelation);
    }

    public boolean checkRelationRemovingValid(Relation<E> aRelation) {
        return editingSupport.checkRelationRemovingValid(aRelation);
    }

    public boolean checkEntityAddingValid(E aEntity) {
        return editingSupport.checkEntityAddingValid(aEntity);
    }

    public boolean checkEntityRemovingValid(E aEntity) {
        return editingSupport.checkEntityRemovingValid(aEntity);
    }

    public void addEditingListener(ModelEditingListener<E> l) {
        editingSupport.addListener(l);
    }

    public void removeEditingListener(ModelEditingListener<E> l) {
        editingSupport.removeListener(l);
    }

    private void fireEntityAdded(E ent) {
        editingSupport.fireEntityAdded(ent);
    }

    private void fireEntityRemoved(E ent) {
        editingSupport.fireEntityRemoved(ent);
    }

    public void fireRelationAdded(Relation<E> aRel) {
        editingSupport.fireRelationAdded(aRel);
    }

    public void fireRelationRemoved(Relation<E> aRel) {
        editingSupport.fireRelationRemoved(aRel);
    }

    public P getParametersEntity() {
        return parametersEntity;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void addRelation(Relation<E> aRel) {
        relations.add(aRel);
        E lEntity = aRel.getLeftEntity();
        E rEntity = aRel.getRightEntity();
        if (lEntity != null && rEntity != null) {
            lEntity.addOutRelation(aRel);
            rEntity.addInRelation(aRel);
        }
        fireRelationAdded(aRel);
    }

    public Set<Relation<E>> collectRelationsByEntity(E aEntity) {
        return aEntity.getInOutRelations();
    }

    public Scriptable getScriptScope() {
        return scriptScope;
    }

    public void setScriptScope(Scriptable aScriptScope) throws Exception {
        scriptScope = aScriptScope;
    }

    public int getAjustingCounter() {
        return ajustingCounter;
    }

    public boolean isAjusting() {
        return ajustingCounter > 0;
    }

    public void beginAjusting() {
        ajustingCounter++;
    }

    public void endAjusting() {
        ajustingCounter--;
        assert (ajustingCounter >= 0);
    }

    public C getClient() {
        return client;
    }

    public void removeRelation(Relation<E> aRelation) {
        relations.remove(aRelation);
        E lEntity = aRelation.getLeftEntity();
        E rEntity = aRelation.getRightEntity();
        if (lEntity != null && rEntity != null) {
            lEntity.removeOutRelation(aRelation);
            rEntity.removeInRelation(aRelation);
        }
        fireRelationRemoved(aRelation);
    }

    public void addEntity(E aEntity) {
        entities.put(aEntity.getEntityID(), aEntity);
        fireEntityAdded(aEntity);
    }

    public boolean removeEntity(E aEnt) {
        return aEnt != null && removeEntity(aEnt.getEntityID()) != null;
    }

    public E removeEntity(Long aEntId) {
        E lent = entities.get(aEntId);
        if (lent != null) {
            entities.remove(aEntId);
            fireEntityRemoved(lent);
        }
        return lent;
    }

    public void resolveReferences() {
        fixupReferences();
        for (Relation<E> rel : relations) {
            E lEntity = entities.get(rel.getLeftEntityId());
            if (lEntity == null && rel.getLeftEntityId() == PARAMETERS_ENTITY_ID) {
                lEntity = parametersEntity;
            }
            rel.setLEntity(lEntity);
            lEntity.addOutRelation(rel);

            E rEntity = entities.get(rel.getRightEntityId());
            if (rEntity == null && rel.getRightEntityId() == PARAMETERS_ENTITY_ID) {
                rEntity = parametersEntity;
            }
            rel.setREntity(rEntity);
            rEntity.addInRelation(rel);
        }
    }

    public Map<Long, E> getEntities() {
        return entities;
    }

    /**
     * Sane as getEntities, but returns also parameters entity if it exists.
     *
     * @return
     */
    public Map<Long, E> getAllEntities() {
        Map<Long, E> allEntities = new HashMap<>();
        allEntities.putAll(entities);
        if (parametersEntity != null) {
            allEntities.put(parametersEntity.getEntityID(), parametersEntity);
        }
        return allEntities;
    }

    public E getEntityById(Long aId) {
        if (aId != null && PARAMETERS_ENTITY_ID == aId) {
            return parametersEntity;
        } else {
            return entities.get(aId);
        }
    }

    public abstract void fixupReferences();

    public void setEntities(Map<Long, E> aValue) {
        entities = aValue;
    }

    public Set<Relation<E>> getRelations() {
        return relations;
    }

    public void setParameters(Parameters aParams) {
        parameters = aParams;
    }

    public void setParametersEntity(P aParamsEntity) {
        parametersEntity = aParamsEntity;
    }

    public void setRelations(Set<Relation<E>> aRelations) {
        relations = aRelations;
    }

    public boolean isParameterNameInRelations(E aEntity, Set<Relation<E>> aRelations, String aParameterName) {
        for (Relation<E> lrel : aRelations) {
            String leftParameter = lrel.getLeftParameter();
            if (aEntity != null && leftParameter != null
                    && leftParameter.equals(aParameterName)
                    && aEntity == lrel.getLeftEntity()) {
                return true;
            }

            String rightParameter = lrel.getRightParameter();
            if (aEntity != null && rightParameter != null
                    && rightParameter.equals(aParameterName)
                    && aEntity == lrel.getRightEntity()) {
                return true;
            }
        }
        return false;
    }

    public boolean isFieldNameInRelations(E aEntity, Set<Relation<E>> aRelations, String aFieldName) {
        for (Relation<E> lrel : aRelations) {
            String leftField = lrel.getLeftField();
            if (aEntity != null && leftField != null
                    && leftField.equals(aFieldName) && aEntity == lrel.getLeftEntity()) {
                return true;
            }

            String rightField = lrel.getRightField();
            if (aEntity != null && rightField != null
                    && rightField.equals(aFieldName) && aEntity == lrel.getRightEntity()) {
                return true;
            }
        }
        return false;
    }

    public void setRuntime(boolean aValue) throws Exception {
        boolean oldValue = runtime;
        runtime = aValue;
        changeSupport.firePropertyChange("runtime", oldValue, runtime);
    }

    public boolean isRuntime() {
        return runtime;
    }

    public boolean isCommitable() {
        return commitable;
    }

    public void setCommitable(boolean aValue) {
        boolean oldValue = commitable;
        commitable = aValue;
        changeSupport.firePropertyChange("commitable", oldValue, commitable);
    }

    public abstract boolean isTypeSupported(int type) throws Exception;

    public boolean isNamePresent(String aName, E toExclude, Field field2Exclude) {
        if (entities != null && aName != null) {
            for (E ent : entities.values()) {
                if (ent != null && ent != toExclude) {
                    String lName = ent.getName();
                    if (lName != null && aName.equals(lName)) {
                        return true;
                    }
                    Fields mayBeParams = ent.getFields();
                    if (mayBeParams != null && mayBeParams instanceof Parameters && mayBeParams.isNameAlreadyPresent(aName, field2Exclude)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt != null && evt.getPropertyName() != null) {
            try {
                if (!(evt.getSource() instanceof Entity<?, ?, ?>)
                        && !(evt.getSource() instanceof Model<?, ?, ?, ?>)) {
                    if (evt.getPropertyName().equals("scriptScope")) {
                        setScriptScope((Scriptable) evt.getNewValue());
                    }
                    if (evt.getPropertyName().equals("runtime")) {
                        setRuntime((Boolean) evt.getNewValue());
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}