/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.client.model.gui.selectors;

import com.bearsoft.rowset.metadata.Field;
import com.bearsoft.rowset.metadata.Parameter;
import com.eas.client.model.*;
import com.eas.client.model.application.ApplicationEntity;
import com.eas.client.model.application.ApplicationParametersEntity;
import com.eas.client.model.gui.view.ModelSelectionListener;
import com.eas.client.model.query.QueryParametersEntity;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;

/**
 *
 * @author mg
 */
public class ModelElementRefSelectionValidator<E extends Entity<?, ?, E>> implements ModelSelectionListener<E> {

    protected ModelElementRef dmRef = null;
    protected Action okAction;
    protected int selectionSubject = ModelElementSelector.DATASOURCE_SELECTION_SUBJECT;
    protected ModelElementValidator validator = null;

    public ModelElementRefSelectionValidator(ModelElementRef aElementRef, Action aOkAction, int aSelectionSubject, ModelElementValidator aValidator) {
        super();
        dmRef = aElementRef;
        okAction = aOkAction;
        selectionSubject = aSelectionSubject;
        validator = aValidator;
    }

    protected E getEntityByField(Model<E, ?, ?, ?> aModel, Field aField) {
        try {
            for (E e : aModel.getAllEntities().values()) {
                if (e.getFields().toCollection().contains(aField)) {
                    return e;
                }
                if (e instanceof ApplicationEntity<?, ?, ?> && ((ApplicationEntity<?, ?, ?>) e).getQuery().getParameters().toCollection().contains(aField)) {
                    return e;
                }
            }
            return null;
        } catch (Exception ex) {
            Logger.getLogger(ModelElementRefSelectionValidator.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public void selectionChanged(List<SelectedParameter<E>> aParams, List<SelectedField<E>> aFields) {
        okAction.setEnabled(false);
        switch (selectionSubject) {
            case ModelElementSelector.PARAMETER_SELECTION_SUBJECT:
                if (aFields != null && !aFields.isEmpty() && aFields.get(0).field instanceof Parameter) {
                    okAction.setEnabled(true);
                    dmRef.setField(true);
                    dmRef.setField(aFields.get(0).field);
                    dmRef.setEntityId(aFields.get(0).entity.getEntityID());
                }
                break;
            case ModelElementSelector.FIELD_SELECTION_SUBJECT:
                if ((aFields != null && !aFields.isEmpty()) || (aParams != null && !aParams.isEmpty())) {
                    okAction.setEnabled(true);
                    dmRef.setField(aFields != null && !aFields.isEmpty());
                    dmRef.setField(dmRef.isField() ? aFields.get(0).field : aParams.get(0).parameter);
                    dmRef.setEntityId(dmRef.isField() ? aFields.get(0).entity.getEntityID() : aParams.get(0).entity.getEntityID());
                }
                break;
            case ModelElementSelector.STRICT_DATASOURCE_PARAMETER_SELECTION_SUBJECT:
                if (aParams != null && !aParams.isEmpty() && !(aParams.get(0).entity instanceof ApplicationParametersEntity) && !(aParams.get(0).entity instanceof QueryParametersEntity)) {
                    okAction.setEnabled(true);
                    dmRef.setField(false);
                    dmRef.setField(aParams.get(0).parameter);
                    dmRef.setEntityId(aParams.get(0).entity.getEntityID());
                }
                break;
            case ModelElementSelector.STRICT_DATASOURCE_FIELD_SELECTION_SUBJECT:
                if (aFields != null && !aFields.isEmpty() && !(aFields.get(0).field instanceof Parameter)) {
                    okAction.setEnabled(true);
                    dmRef.setField(true);
                    dmRef.setField(aFields.get(0).field);
                    dmRef.setEntityId(aFields.get(0).entity.getEntityID());
                }
                break;
        }
        if (validator != null) {
            okAction.setEnabled(validator.validateDatamodelElementSelection(dmRef));
        }
    }

    @Override
    public void selectionChanged(Collection<Relation<E>> oldSelected, Collection<Relation<E>> newSelected) {
    }

    @Override
    public void selectionChanged(Set<E> oldSelected, Set<E> newSelected) {
        okAction.setEnabled(false);
        if (newSelected != null && newSelected.size() == 1) {
            E entity = newSelected.iterator().next();
            switch (selectionSubject) {
                case ModelElementSelector.DATASOURCE_SELECTION_SUBJECT:
                    dmRef.setEntityId(entity.getEntityID());
                    dmRef.setField(false);
                    dmRef.setField(null);
                    okAction.setEnabled(true);
                    break;
            }
        }
    }
}