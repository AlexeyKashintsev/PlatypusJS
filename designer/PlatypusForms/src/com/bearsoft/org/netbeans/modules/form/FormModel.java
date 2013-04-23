/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package com.bearsoft.org.netbeans.modules.form;

import com.bearsoft.org.netbeans.modules.form.assistant.AssistantModel;
import com.bearsoft.org.netbeans.modules.form.bound.RADColumnView;
import com.bearsoft.org.netbeans.modules.form.bound.RADModelGridColumn;
import com.bearsoft.org.netbeans.modules.form.layoutsupport.LayoutConstraints;
import com.bearsoft.org.netbeans.modules.form.layoutsupport.LayoutSupportDelegate;
import com.bearsoft.org.netbeans.modules.form.layoutsupport.LayoutSupportManager;
import com.bearsoft.org.netbeans.modules.form.layoutsupport.delegates.MarginLayoutSupport;
import com.eas.dbcontrols.DbControlPanel;
import java.awt.Container;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import org.openide.ErrorManager;
import org.openide.awt.UndoRedo;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * Holds all data of a form.
 *
 * @author Tran Duc Trung, Tomas Pavek
 */
public class FormModel {
    // name of the form is name of the DataObject

    private PlatypusFormDataObject dataObject;
    private AssistantModel assistantModel = new AssistantModel();
    private String formName;
    private boolean readOnly = false;

    public FormModel(PlatypusFormDataObject aDataObject) {
        dataObject = aDataObject;
        setName(aDataObject.getName());
        setReadOnly(aDataObject.isReadOnly());
    }

    public PlatypusFormDataObject getDataObject() {
        return dataObject;
    }

    public AssistantModel getAssistantModel() {
        return assistantModel;
    }

    public String findFreeComponentName(Class<?> compClass) {
        return findFreeComponentName(FormUtils.getPlatypusControlClass(compClass).getSimpleName());
    }

    public String findFreeComponentName(String baseName) {
        baseName = baseName.substring(0, 1).toLowerCase() + baseName.substring(1);
        RADComponent<?> comp = getRADComponent(baseName);
        int counter = 0;
        String generatedName = baseName;
        while (comp != null) {
            counter++;
            generatedName = baseName + counter;
            comp = getRADComponent(generatedName);
        }
        return generatedName;
    }
    // the class on which the form is based (which is extended in the java file)
    private Class<?> formBaseClass;
    // the top radcomponent of the form (null if form is based on Object)
    private RADVisualContainer<?> topRADComponent;
    // other components - out of the main hierarchy under topRADComponent
    private List<RADComponent<?>> otherComponents = new ArrayList<>(10);
    // holds both topRADComponent and otherComponents
    private ModelContainer modelContainer;
    private Map<String, RADComponent<?>> namesToComponents = new HashMap<>();
    private boolean formLoaded = false;
    private UndoRedo.Manager undoRedoManager;
    private boolean undoRedoRecording = false;
    private CompoundEdit compoundEdit;
    private boolean undoCompoundEdit = false;
    private FormEvents formEvents;
    // list of listeners registered on FormModel
    private List<FormModelListener> listeners;
    private List<FormModelEvent> eventList;
    private boolean firing;
    private RADComponentCreator metaCreator;
    private FormSettings settings = new FormSettings();
    private boolean freeDesignDefaultLayout = false;

    /**
     * This methods sets the form base class (which is in fact the superclass of
     * the form class in source java file). It is used for initializing the top
     * meta component, and is also presented as the top component in designer
     * and inspector.
     *
     * @param formClass form base class.
     * @throws java.lang.Exception if anything goes wrong.
     */
    public void setFormBaseClass(Class<? extends Container> formClass) throws Exception {
        if (formBaseClass != null) {
            throw new IllegalStateException("Form type already initialized."); // NOI18N
        }
        if (FormUtils.isVisualizableClass(formClass) && FormUtils.isContainer(formClass)) {
            RADVisualContainer<?> topComp = new RADVisualFormContainer();
            topRADComponent = topComp;
            topComp.initialize(this);
            topComp.initInstance(formClass);
            ((RADVisualFormContainer) topComp).setLayoutSupportDelegate(new MarginLayoutSupport());
            topComp.setInModel(true);
            formBaseClass = formClass;
        }
    }

    public Class<?> getFormBaseClass() {
        return formBaseClass;
    }

    final void setName(String name) {
        formName = name;
    }

    final void setReadOnly(boolean aValue) {
        readOnly = aValue;
    }

    // -----------
    // getters
    public final String getName() {
        return formName;
    }

    public final boolean isReadOnly() {
        return readOnly;
    }

    public final boolean isFormLoaded() {
        return formLoaded;
    }

    public final RADVisualContainer<?> getTopRADComponent() {
        return topRADComponent;
    }

    public ModelContainer getModelContainer() {
        if (modelContainer == null) {
            modelContainer = new ModelContainer();
        }
        return modelContainer;
    }

    public Collection<RADComponent<?>> getOtherComponents() {
        return Collections.unmodifiableCollection(otherComponents);
    }

    public final RADComponent<?> getRADComponent(String aName) {
        return namesToComponents.get(aName);
    }

    /**
     * Returns list of all components in the model. A new List instance is
     * created. The order of the components is random.
     *
     * @return list of components in the model.
     */
    public java.util.List<RADComponent<?>> getComponentList() {
        return new ArrayList<>(namesToComponents.values());
    }

    /**
     * Returns list of all components in the model. A new instance of list is
     * created and the components are added to the list in the traversal order
     * (used e.g. by code generator or persistence manager).
     *
     * @return list of components in the model.
     */
    public java.util.List<RADComponent<?>> getOrderedComponentList() {
        java.util.List<RADComponent<?>> list = new ArrayList<>(namesToComponents.size());
        collectRadComponents(getModelContainer(), list);
        return list;
    }

    /**
     * Returns an unmodifiable collection of all components in the model in
     * random order.
     *
     * @return list of components in the model.
     */
    public Collection<RADComponent<?>> getAllComponents() {
        return Collections.unmodifiableCollection(namesToComponents.values());
    }

    public List<RADComponent<?>> getNonVisualComponents() {
        List<RADComponent<?>> list = new ArrayList<>(otherComponents.size());
        for (RADComponent<?> radComp : otherComponents) {
            if (!(radComp instanceof RADVisualComponent<?>)) {
                list.add(radComp);
            }
        }
        return list;
    }

    public List<RADComponent<?>> getVisualComponents() {
        List<RADComponent<?>> list = new ArrayList<>(namesToComponents.size());
        for (Map.Entry<String, RADComponent<?>> e : namesToComponents.entrySet()) {
            RADComponent<?> radComp = e.getValue();
            if (radComp instanceof RADVisualComponent<?>) {
                list.add(radComp);
            }
        }
        return list;
    }

    public FormEvents getFormEvents() {
        if (formEvents == null) {
            formEvents = new FormEvents(this);
        }
        return formEvents;
    }

    private static void collectRadComponents(ComponentContainer cont,
            java.util.List<RADComponent<?>> list) {
        RADComponent<?>[] comps = cont.getSubBeans();
        for (int i = 0; i < comps.length; i++) {
            RADComponent<?> comp = comps[i];
            list.add(comp);
            if (comp instanceof ComponentContainer) {
                collectRadComponents((ComponentContainer) comp, list);
            }
        }
    }

    public FormSettings getSettings() {
        return settings;
    }

    // -----------
    // adding/deleting components, setting layout, etc
    /**
     * @return RADComponentCreator responsible for creating new components and
     * adding them to the model.
     */
    public RADComponentCreator getComponentCreator() {
        if (metaCreator == null) {
            metaCreator = new RADComponentCreator(this);
        }
        return metaCreator;
    }

    /**
     * Adds a new component to given (non-visual) container in the model. If the
     * container is not specified, the component is added to the "other
     * components".
     *
     * @param radComp component to add.
     * @param parentContainer parent of the added component.
     * @param newlyAdded is newly added?
     */
    public void addComponent(RADComponent<?> radComp,
            ComponentContainer parentContainer,
            boolean newlyAdded) {
        if (newlyAdded || !radComp.isInModel()) {
            setInModelRecursively(radComp, true);
            newlyAdded = true;
        }
        if (parentContainer != null) {
            radComp.setParent(parentContainer);
            parentContainer.add(radComp);
        } else {
            radComp.setParent(null);
            otherComponents.add(radComp);
        }
        fireComponentAdded(radComp, newlyAdded);
    }

    /**
     * Adds a new visual component to given container managed by the layout
     * support.
     *
     * @param radComp component to add.
     * @param parentContainer parent of the added component.
     * @param aConstraints layout constraints.
     * @param newlyAdded is newly added?
     */
    public void addVisualComponent(RADVisualComponent<?> radComp,
            RADVisualContainer<?> parentContainer,
            int aIndex,
            LayoutConstraints<?> aConstraints,
            boolean newlyAdded) {
        LayoutSupportManager layoutSupport = parentContainer.getLayoutSupport();
        if (layoutSupport != null) {
            RADVisualComponent<?>[] compArray = new RADVisualComponent<?>[]{radComp};
            //LayoutConstraints<?> c = aConstraints instanceof LayoutConstraints<?>
            //        ? (LayoutConstraints<?>) aConstraints : null;
            LayoutConstraints<?>[] constrArray = new LayoutConstraints<?>[]{aConstraints};

            //int index = constraints instanceof Integer ? ((Integer) constraints).intValue() : -1;
            // constraints here may be of type Integer.
            // It comes from layout support delegates to
            // force us place a component in predefined position
            // like in toolbars while dragging new components in them.

            // component needs to be "in model" (have code expression) before added to layout
            if (newlyAdded || !radComp.isInModel()) {
                setInModelRecursively(radComp, true);
                newlyAdded = true;
            }
            if (newlyAdded) {
                try {
                    layoutSupport.acceptNewComponents(compArray, constrArray, aIndex);
                } catch (RuntimeException ex) {
                    // LayoutSupportDelegate may not accept the component
                    if (newlyAdded) {
                        setInModelRecursively(radComp, false);
                    }
                    throw ex;
                }
            }
            parentContainer.add(radComp, aIndex);
            layoutSupport.addComponents(compArray, constrArray, aIndex);
        } else {
            if (newlyAdded || !radComp.isInModel()) {
                setInModelRecursively(radComp, true);
                newlyAdded = true;
            }
            if (!newlyAdded) {
                radComp.resetConstraintsProperties();
            }
            parentContainer.add(radComp);
        }
        fireComponentAdded(radComp, newlyAdded);
    }

    void setContainerLayoutImpl(RADVisualContainer<?> radCont,
            LayoutSupportDelegate layoutDelegate)
            throws Exception {
        LayoutSupportManager currentLS = radCont.getLayoutSupport();
        LayoutSupportDelegate currentDel =
                currentLS != null ? currentLS.getLayoutDelegate() : null;

        if (currentLS == null) { // switching to layout support
            radCont.checkLayoutSupport();
        }
        try {
            radCont.setLayoutSupportDelegate(layoutDelegate);
        } catch (Exception ex) {
            throw ex;
        }

        fireContainerLayoutExchanged(radCont, currentDel, layoutDelegate);
    }

    public void setContainerLayout(RADVisualContainer<?> radCont,
            LayoutSupportDelegate layoutDelegate)
            throws Exception {
        setContainerLayoutImpl(radCont, layoutDelegate);
    }

    public void removeComponent(RADComponent<?> radComp, boolean fromModel) {
        removeComponentImpl(radComp, fromModel);
    }

    void removeComponentImpl(RADComponent<?> radComp, boolean fromModel) {
        if (fromModel && formEvents != null) {
            removeEventHandlersRecursively(radComp);
        }
        if (fromModel) {
            setInModelRecursively(radComp, false);
        }
        ComponentContainer parent = radComp.getParent();
        if (parent != null)// parented components
        {
            int index = parent.getIndexOf(radComp);
            parent.remove(radComp);
            fireComponentRemoved(radComp, parent, index, fromModel);
        } else {// parentless components
            int index = modelContainer.getIndexOf(radComp);
            modelContainer.remove(radComp);
            fireComponentRemoved(radComp, modelContainer, index, fromModel);
        }
    }

    public void updateMapping(RADComponent<?> radComp, boolean register) {
        if (register) {
            namesToComponents.put(radComp.getName(), radComp);
        } else {
            namesToComponents.remove(radComp.getName());
        }
    }

    // removes all event handlers attached to given component and all
    // its subcomponents
    private void removeEventHandlersRecursively(RADComponent<?> comp) {
        if (comp instanceof ComponentContainer) {
            RADComponent<?>[] subcomps = ((ComponentContainer) comp).getSubBeans();
            for (int i = 0; i < subcomps.length; i++) {
                removeEventHandlersRecursively(subcomps[i]);
            }
        }

        Event[] events = comp.getKnownEvents();
        for (int i = 0; i < events.length; i++) {
            if (events[i].hasEventHandlers()) {
                getFormEvents().detachEvent(events[i]);
            }
        }
    }

    static void setInModelRecursively(RADComponent<?> radComp, boolean inModel) {
        if (radComp instanceof ComponentContainer) {
            RADComponent<?>[] comps = ((ComponentContainer) radComp).getSubBeans();
            for (int i = 0; i < comps.length; i++) {
                setInModelRecursively(comps[i], inModel);
            }
        }
        radComp.setInModel(inModel);
    }

    // ----------
    // undo and redo
    public void setUndoRedoRecording(boolean record) {
        t("turning undo/redo recording " + (record ? "on" : "off")); // NOI18N
        undoRedoRecording = record;
    }

    public boolean isUndoRedoRecording() {
        return undoRedoRecording;
    }

    private void startCompoundEdit() {
        if (compoundEdit == null) {
            t("starting compound edit"); // NOI18N
            compoundEdit = new CompoundEdit();
        }
    }
    private static boolean formModifiedLogged = false;

    public CompoundEdit endCompoundEdit(boolean commit) {
        if (compoundEdit != null) {
            t("ending compound edit: " + commit); // NOI18N
            compoundEdit.end();
            if (commit && undoRedoRecording && compoundEdit.isSignificant()) {
                if (!formModifiedLogged) {
                    Logger logger = Logger.getLogger("org.netbeans.ui.metrics.form"); // NOI18N
                    LogRecord rec = new LogRecord(Level.INFO, "USG_FORM_MODIFIED"); // NOI18N
                    rec.setLoggerName(logger.getName());
                    logger.log(rec);
                    formModifiedLogged = true;
                }
                getUndoRedoManager().undoableEditHappened(
                        new UndoableEditEvent(this, compoundEdit));
            }
            CompoundEdit edit = compoundEdit;
            compoundEdit = null;
            return edit;
        }
        return null;
    }

    public void forceUndoOfCompoundEdit() {
        if (compoundEdit != null) {
            undoCompoundEdit = true;
        }
    }

    public boolean isCompoundEditInProgress() {
        return compoundEdit != null; // && compoundEdit.isInProgress();
    }

    public void addUndoableEdit(UndoableEdit edit) {
        t("adding undoable edit"); // NOI18N
        if (!isCompoundEditInProgress()) {
            startCompoundEdit();
        }
        compoundEdit.addEdit(edit);
    }

    public UndoRedo.Manager getUndoRedoManager() {
        return undoRedoManager;
    }

    public void setColumnViewImpl(RADModelGridColumn aColumn, RADColumnView<? super DbControlPanel> aView) {
        if (aColumn.getViewControl() != null) {
            RADColumnView<? super DbControlPanel> oldView = aColumn.getViewControl();
            aColumn.setViewControl(aView);
            fireColumnViewExchanged(aColumn, oldView, aView);
        }
    }

    // [Undo manager performing undo/redo in AWT event thread should not be
    //  probably implemented here - in FormModel - but seperately.]
    class UndoRedoManager extends UndoRedo.Manager {

        private Mutex.ExceptionAction<Object> runUndo = new Mutex.ExceptionAction<Object>() {
            @Override
            public Object run() throws Exception {
                superUndo();
                return null;
            }
        };
        private Mutex.ExceptionAction<Object> runRedo = new Mutex.ExceptionAction<Object>() {
            @Override
            public Object run() throws Exception {
                superRedo();
                return null;
            }
        };

        public void superUndo() throws CannotUndoException {
            super.undo();
            dataObject.getLookup().lookup(PlatypusFormSupport.class).notifyModified();
        }

        public void superRedo() throws CannotRedoException {
            super.redo();
            dataObject.getLookup().lookup(PlatypusFormSupport.class).notifyModified();
        }

        @Override
        public void undo() throws CannotUndoException {
            if (java.awt.EventQueue.isDispatchThread()) {
                superUndo();
            } else {
                try {
                    Mutex.EVENT.readAccess(runUndo);
                } catch (MutexException ex) {
                    Exception e = ex.getException();
                    if (e instanceof CannotUndoException) {
                        throw (CannotUndoException) e;
                    } else // should not happen, ignore
                    {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            }
        }

        @Override
        public void redo() throws CannotRedoException {
            if (java.awt.EventQueue.isDispatchThread()) {
                superRedo();
            } else {
                try {
                    Mutex.EVENT.readAccess(runRedo);
                } catch (MutexException ex) {
                    Exception e = ex.getException();
                    if (e instanceof CannotRedoException) {
                        throw (CannotRedoException) e;
                    } else // should not happen, ignore
                    {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            }
        }
    }

    // ----------
    // listeners registration, firing methods
    public synchronized void addFormModelListener(FormModelListener l) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(l);
    }

    public synchronized void removeFormModelListener(FormModelListener l) {
        if (listeners != null) {
            listeners.remove(l);
        }
    }

    /**
     * Fires an event informing about that the form has been just loaded.
     */
    public void fireFormLoaded() {
        t("firing form loaded"); // NOI18N
        formLoaded = true;
        if (!readOnly) { // NOI18N
            undoRedoManager = new UndoRedoManager();
            undoRedoManager.setLimit(50);
            setUndoRedoRecording(true);
        }
        sendEventLater(new FormModelEvent(this, FormModelEvent.FORM_LOADED));
    }

    /**
     * Fires an event informing about that the form is just about to be saved.
     */
    public void fireFormToBeSaved() {
        t("firing form to be saved"); // NOI18N

        sendEventImmediately(
                new FormModelEvent(this, FormModelEvent.FORM_TO_BE_SAVED));
    }

    /**
     * Fires an event informing about that the form is just about to be closed.
     */
    public void fireFormToBeClosed() {
        t("firing form to be closed"); // NOI18N

        if (undoRedoManager != null) {
            undoRedoManager.discardAllEdits();
        }

        sendEventImmediately(
                new FormModelEvent(this, FormModelEvent.FORM_TO_BE_CLOSED));
    }

    /**
     * Fires an event informing about changing layout manager of a container. An
     * undoable edit is created and registered automatically.
     *
     * @param radCont container whose layout has been changed.
     * @param oldLayout old layout.
     * @param newLayout new layout.
     * @return event that has been fired.
     */
    public FormModelEvent fireContainerLayoutExchanged(
            RADVisualContainer<?> radCont,
            LayoutSupportDelegate oldLayout,
            LayoutSupportDelegate newLayout) {
        t("firing container layout exchange, container: " // NOI18N
                + (radCont != null ? radCont.getName() : "null")); // NOI18N

        FormModelEvent ev = new FormModelEvent(this, FormModelEvent.CONTAINER_LAYOUT_EXCHANGED);
        ev.setLayout(radCont, oldLayout, newLayout);
        sendEvent(ev);

        if (undoRedoRecording && radCont != null && oldLayout != newLayout) {
            addUndoableEdit(ev.getUndoableEdit());
        }

        return ev;
    }

    /**
     * Fires an event informing about changing a property of container layout.
     * An undoable edit is created and registered automatically.
     *
     * @param radCont container whose layout has been changed.
     * @param propName name of the layout property.
     * @param oldValue old value of the property.
     * @param newValue new value of the property.
     * @return event that has been fired.
     */
    public FormModelEvent fireContainerLayoutChanged(
            RADVisualContainer<?> radCont,
            String propName,
            Object oldValue,
            Object newValue) {
        t("firing container layout change, container: " // NOI18N
                + (radCont != null ? radCont.getName() : "null") // NOI18N
                + ", property: " + propName); // NOI18N

        FormModelEvent ev = new FormModelEvent(this, FormModelEvent.CONTAINER_LAYOUT_CHANGED);
        ev.setComponentAndContainer(radCont, radCont);
        ev.setProperty(propName, oldValue, newValue);
        sendEvent(ev);

        if (undoRedoRecording
                && radCont != null && (propName == null || oldValue != newValue)) {
            addUndoableEdit(ev.getUndoableEdit());
        }

        return ev;
    }

    /**
     * Fires an event informing about changing a property of component layout
     * constraints. An undoable edit is created and registered automatically.
     *
     * @param radComp component whose layout property has been changed.
     * @param propName name of the layout property.
     * @param oldValue old value of the property.
     * @param newValue new value of the property.
     * @return event that has been fired.
     */
    public FormModelEvent fireComponentLayoutChanged(
            RADVisualComponent<?> radComp,
            String propName,
            Object oldValue,
            Object newValue) {
        t("firing component layout change: " // NOI18N
                + (radComp != null ? radComp.getName() : "null")); // NOI18N

        FormModelEvent ev = new FormModelEvent(this, FormModelEvent.COMPONENT_LAYOUT_CHANGED);
        ev.setComponentAndContainer(radComp, null);
        ev.setProperty(propName, oldValue, newValue);
        sendEvent(ev);

        if (undoRedoRecording
                && radComp != null && propName != null && oldValue != newValue) {
            addUndoableEdit(ev.getUndoableEdit());
        }

        return ev;
    }

    /**
     * Fires an event informing about changing view of a column. An undoable
     * edit is created and registered automatically.
     *
     * @param aRadColumn column whose view has been changed.
     * @param oldView old view.
     * @param newView new view.
     * @return event that has been fired.
     */
    public FormModelEvent fireColumnViewExchanged(
            RADModelGridColumn aRadColumn,
            RADColumnView<? super DbControlPanel> oldView,
            RADColumnView<? super DbControlPanel> newView) {
        t("firing column view exchange, column: " // NOI18N
                + (aRadColumn != null ? aRadColumn.getName() : "null")); // NOI18N

        FormModelEvent ev = new FormModelEvent(this, FormModelEvent.COLUMN_VIEW_EXCHANGED);
        ev.setColumnView(aRadColumn, oldView, newView);
        sendEvent(ev);

        if (undoRedoRecording && aRadColumn != null && oldView != newView) {
            addUndoableEdit(ev.getUndoableEdit());
        }
        return ev;
    }

    /**
     * Fires an event informing about adding a component to the form. An
     * undoable edit is created and registered automatically.
     *
     * @param radComp component that has been added.
     * @param addedNew is newly added?
     * @return event that has been fired.
     */
    public FormModelEvent fireComponentAdded(RADComponent<?> radComp,
            boolean addedNew) {
        t("firing component added: " // NOI18N
                + (radComp != null ? radComp.getName() : "null")); // NOI18N

        FormModelEvent ev = new FormModelEvent(this, FormModelEvent.COMPONENT_ADDED);
        ev.setAddData(radComp, radComp.getParent(), addedNew);
        sendEvent(ev);

        if (undoRedoRecording && radComp != null) {
            addUndoableEdit(ev.getUndoableEdit());
        }

        return ev;
    }

    /**
     * Fires an event informing about removing a component from the form. An
     * undoable edit is created and registered automatically.
     *
     * @param radComp component that has been removed.
     * @param radCont container from which the component was removed.
     * @param index index of the component in the container.
     * @param removedFromModel determines whether the component has been removed
     * from the model.
     * @return event that has been fired.
     */
    public FormModelEvent fireComponentRemoved(RADComponent<?> radComp,
            ComponentContainer radCont,
            int index,
            boolean removedFromModel) {
        t("firing component removed: " // NOI18N
                + (radComp != null ? radComp.getName() : "null")); // NOI18N

        FormModelEvent ev = new FormModelEvent(this, FormModelEvent.COMPONENT_REMOVED);
        ev.setRemoveData(radComp, radCont, index, removedFromModel);
        sendEvent(ev);

        if (undoRedoRecording && radComp != null && radCont != null) {
            addUndoableEdit(ev.getUndoableEdit());
        }

        return ev;
    }

    /**
     * Fires an event informing about reordering components in a container. An
     * undoable edit is created and registered automatically.
     *
     * @param radCont container whose subcomponents has been reordered.
     * @param perm permutation describing the change in order.
     * @return event that has been fired.
     */
    public FormModelEvent fireComponentsReordered(ComponentContainer radCont,
            int[] perm) {
        t("firing components reorder in container: " // NOI18N
                + (radCont instanceof RADComponent
                ? ((RADComponent) radCont).getName() : "<top>")); // NOI18N

        FormModelEvent ev = new FormModelEvent(this, FormModelEvent.COMPONENTS_REORDERED);
        ev.setComponentAndContainer(null, radCont);
        ev.setReordering(perm);
        sendEvent(ev);

        if (undoRedoRecording && radCont != null) {
            addUndoableEdit(ev.getUndoableEdit());
        }

        return ev;
    }

    /**
     * Fires an event informing about changing a property of a component. An
     * undoable edit is created and registered automatically.
     *
     * @param radComp component whose property has been changed.
     * @param propName name of the changed property.
     * @param oldValue old value of the property.
     * @param newValue new value of the property.
     * @return event that has been fired.
     */
    public FormModelEvent fireComponentPropertyChanged(RADComponent<?> radComp,
            String propName,
            Object oldValue,
            Object newValue) {
        t("firing component property change, component: " // NOI18N
                + (radComp != null ? radComp.getName() : "<null component>") // NOI18N
                + ", property: " + propName); // NOI18N

        FormModelEvent ev = new FormModelEvent(this, FormModelEvent.COMPONENT_PROPERTY_CHANGED);
        ev.setComponentAndContainer(radComp, null);
        ev.setProperty(propName, oldValue, newValue);
        sendEvent(ev);

        if (undoRedoRecording
                && radComp != null && propName != null && oldValue != newValue) {
            addUndoableEdit(ev.getUndoableEdit());
        }

        return ev;
    }

    /**
     * Fires an event informing about changing a synthetic property of a
     * component. An undoable edit is created and registered automatically.
     *
     * @param radComp component whose synthetic property has been changed.
     * @param propName name of the synthetic property that has been changed.
     * @param oldValue old value of the property.
     * @param newValue new value of the property.
     * @return event that has been fired.
     */
    public FormModelEvent fireSyntheticPropertyChanged(RADComponent<?> radComp,
            String propName,
            Object oldValue,
            Object newValue) {
        t("firing synthetic property change, component: " // NOI18N
                + (radComp != null ? radComp.getName() : "null") // NOI18N
                + ", property: " + propName); // NOI18N

        FormModelEvent ev = new FormModelEvent(this, FormModelEvent.SYNTHETIC_PROPERTY_CHANGED);
        ev.setComponentAndContainer(radComp, null);
        ev.setProperty(propName, oldValue, newValue);
        sendEvent(ev);

        if (undoRedoRecording && propName != null && oldValue != newValue) {
            addUndoableEdit(ev.getUndoableEdit());
        }

        return ev;
    }

    /**
     * Fires an event informing about attaching a new event to an event handler
     * (createdNew parameter indicates whether the event handler was created
     * first). An undoable edit is created and registered automatically.
     *
     * @param event event for which the handler was created.
     * @param handler name of the event handler.
     * @param bodyText body of the event handler.
     * @param createdNew newly created event handler?
     * @return event that has been fired.
     */
    public FormModelEvent fireEventHandlerAdded(Event event,
            String handler,
            String bodyText,
            String annotationText,
            boolean createdNew) {
        t("event handler added: " + handler); // NOI18N

        FormModelEvent ev = new FormModelEvent(this, FormModelEvent.EVENT_HANDLER_ADDED);
        ev.setEvent(event, handler, bodyText, annotationText, createdNew);
        sendEvent(ev);

        if (undoRedoRecording && event != null && handler != null) {
            addUndoableEdit(ev.getUndoableEdit());
        }

        return ev;
    }

    /**
     * Fires an event informing about detaching an event from event handler
     * (handlerDeleted parameter indicates whether the handler was deleted as
     * the last event was detached). An undoable edit is created and registered
     * automatically.
     *
     * @param event event for which the handler was removed.
     * @param handler removed event handler.
     * @param handlerDeleted was deleted?
     * @return event that has been fired.
     */
    public FormModelEvent fireEventHandlerRemoved(Event event,
            String handler,
            boolean handlerDeleted) {
        t("firing event handler removed: " + handler); // NOI18N

        FormModelEvent ev = new FormModelEvent(this, FormModelEvent.EVENT_HANDLER_REMOVED);
        ev.setEvent(event, handler, null, null, handlerDeleted);
        sendEvent(ev);

        if (undoRedoRecording && event != null && handler != null) {
            addUndoableEdit(ev.getUndoableEdit());
        }

        return ev;
    }

    /**
     * Fires an event informing about renaming an event handler. An undoable
     * edit is created and registered automatically.
     *
     * @param oldHandlerName old name of the event handler.
     * @param newHandlerName new name of the event handler.
     * @return event that has been fired.
     */
    public FormModelEvent fireEventHandlerRenamed(String oldHandlerName,
            String newHandlerName) {
        t("event handler renamed: " + oldHandlerName + " to " + newHandlerName); // NOI18N

        FormModelEvent ev = new FormModelEvent(this, FormModelEvent.EVENT_HANDLER_RENAMED);
        ev.setEvent(oldHandlerName, newHandlerName);
        sendEvent(ev);

        if (undoRedoRecording && oldHandlerName != null && newHandlerName != null) {
            addUndoableEdit(ev.getUndoableEdit());
        }

        return ev;
    }

    /**
     * Fires an event informing about general form change.
     *
     * @param immediately determines whether the change should be fire
     * immediately.
     * @return event that has been fired.
     */
    public FormModelEvent fireFormChanged(boolean immediately) {
        t("firing form change"); // NOI18N

        FormModelEvent ev = new FormModelEvent(this, FormModelEvent.OTHER_CHANGE);
        if (immediately) {
            sendEventImmediately(ev);
        } else {
            sendEvent(ev);
        }

        return ev;
    }

    // ---------
    // firing methods for batch event processing
    private void sendEvent(FormModelEvent ev) {
        if (formLoaded) {
            if (eventList != null || ev.isModifying()) {
                sendEventLater(ev);
            } else {
                sendEventImmediately(ev);
            }
        } else {
            fireEvents(ev);
        }
    }

    private synchronized void sendEventLater(FormModelEvent ev) {
        // works properly only if called from AWT event dispatch thread
        if (!java.awt.EventQueue.isDispatchThread()) {
            sendEventImmediately(ev);
            return;
        }

        if (eventList == null) {
            eventList = new ArrayList<>();
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    firePendingEvents();
                }
            });
        }
        eventList.add(ev);
    }

    private synchronized void sendEventImmediately(FormModelEvent ev) {
        if (eventList == null) {
            eventList = new ArrayList<>();
        }
        eventList.add(ev);
        firePendingEvents();
    }

    private void firePendingEvents() {
        List<FormModelEvent> list = pickUpEvents();
        if (list != null && !list.isEmpty()) {
            FormModelEvent[] events = new FormModelEvent[list.size()];
            list.toArray(events);
            fireEventBatch(events);
        }
    }

    private synchronized List<FormModelEvent> pickUpEvents() {
        List<FormModelEvent> list = eventList;
        eventList = null;
        return list;
    }

    boolean hasPendingEvents() {
        return eventList != null;
    }

    /**
     * This method fires events collected from all changes done during the last
     * round of AWT event queue. After all fired successfully (no error
     * occurred), all the changes are placed as one UndoableEdit into undo/redo
     * queue. When the fired events are being processed, some more changes may
     * happen (they are included in the same UndoableEdit). These changes are
     * typically fired immediately causing this method is re-entered while
     * previous firing is not finished yet. Additionally - for robustness, if
     * some unhandled error happens before or during firing the events, all the
     * changes done so far are undone: If an operation failed before firing, the
     * undoCompoundEdit field is set and then no events are fired at all (the
     * changes were defective), and the changes done before the failure are
     * undone. All the changes are undone also if the failure happens during
     * processing the events (e.g. the layout can't be built).
     */
    private void fireEventBatch(FormModelEvent... events) {
        if (!firing) {
            boolean firingFailed = false;
            try {
                firing = true;
                if (!undoCompoundEdit) {
                    firingFailed = true;
                    fireEvents(events);
                    firingFailed = false;
                }
            } finally {
                firing = false;
                boolean revert = undoCompoundEdit || firingFailed;
                undoCompoundEdit = false;
                CompoundEdit edit = endCompoundEdit(!revert);
                if (edit != null && revert) {
                    edit.undo();
                }
            }
        } else { // re-entrant call
            fireEvents(events);
        }
    }

    void fireEvents(FormModelEvent... events) {
        java.util.List<FormModelListener> targets = new ArrayList<>();
        synchronized (this) {
            if (listeners == null || listeners.isEmpty()) {
                return;
            }
            targets.addAll(listeners);
        }
        for (int i = 0; i < targets.size(); i++) {
            FormModelListener l = targets.get(i);
            l.formChanged(events);
        }
    }

    public boolean isFreeDesignDefaultLayout() {
        return freeDesignDefaultLayout;
    }

    void setFreeDesignDefaultLayout(boolean aValue) {
        freeDesignDefaultLayout = aValue;
    }

    // ---------------
    // ModelContainer inner class
    public final class ModelContainer implements ComponentContainer {

        @Override
        public RADComponent<?>[] getSubBeans() {
            int n = otherComponents.size();
            if (topRADComponent != null) {
                n++;
            }
            RADComponent<?>[] comps = new RADComponent<?>[n];
            otherComponents.toArray(comps);
            if (topRADComponent != null) {
                comps[n - 1] = topRADComponent;
            }
            return comps;
        }

        @Override
        public void initSubComponents(RADComponent<?>[] initComponents) {
            otherComponents.clear();
            for (int i = 0; i < initComponents.length; i++) {
                if (initComponents[i] != topRADComponent) {
                    add(initComponents[i]);
                }
            }
        }

        @Override
        public void reorderSubComponents(int[] perm) {
            RADComponent<?>[] components = new RADComponent<?>[otherComponents.size()];
            for (int i = 0; i < perm.length; i++) {
                components[perm[i]] = otherComponents.get(i);
            }
            otherComponents.clear();
            otherComponents.addAll(Arrays.asList(components));
        }

        @Override
        public void add(RADComponent<?> comp) {
            comp.setParent(null);
            otherComponents.add(comp);
        }

        @Override
        public void remove(RADComponent<?> comp) {
            if (otherComponents.remove(comp)) {
                comp.setParent(null);
            }
        }

        @Override
        public int getIndexOf(RADComponent<?> comp) {
            int index = otherComponents.indexOf(comp);
            if (index < 0 && comp == topRADComponent) {
                index = otherComponents.size();
            }
            return index;
        }
    }
    // ---------------
    /**
     * For debugging purposes only.
     */
    static private int traceCount = 0;
    /**
     * For debugging purposes only.
     */
    static private final boolean TRACE = false;

    /**
     * For debugging purposes only.
     */
    static void t(String str) {
        if (TRACE) {
            if (str != null) {
                System.out.println("FormModel " + (++traceCount) + ": " + str); // NOI18N
            } else {
                System.out.println(""); // NOI18N
            }
        }
    }
}