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

import com.bearsoft.org.netbeans.modules.form.Event;
import com.bearsoft.org.netbeans.modules.form.bound.RADColumnView;
import com.bearsoft.org.netbeans.modules.form.bound.RADModelGrid;
import com.bearsoft.org.netbeans.modules.form.bound.RADModelGridColumn;
import com.bearsoft.org.netbeans.modules.form.bound.RADModelMap;
import com.bearsoft.org.netbeans.modules.form.bound.RADModelMapLayer;
import com.bearsoft.org.netbeans.modules.form.bound.RADModelScalarComponent;
import com.bearsoft.org.netbeans.modules.form.editors.NbBorder;
import com.bearsoft.org.netbeans.modules.form.layoutsupport.*;
import com.bearsoft.org.netbeans.modules.form.layoutsupport.delegates.AbsoluteLayoutSupport;
import com.bearsoft.org.netbeans.modules.form.layoutsupport.delegates.MarginLayoutSupport;
import com.eas.client.geo.RowsetFeatureDescriptor;
import com.eas.controls.HtmlContentEditorKit;
import com.eas.dbcontrols.DbControl;
import com.eas.dbcontrols.DbControlPanel;
import com.eas.dbcontrols.ScalarDbControl;
import com.eas.dbcontrols.grid.DbGrid;
import com.eas.dbcontrols.grid.DbGridColumn;
import com.eas.dbcontrols.image.DbImage;
import com.eas.dbcontrols.map.DbMap;
import com.eas.dbcontrols.text.DbText;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.Border;
import org.openide.*;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * This class represents an access point for adding new components to FormModel.
 * Its responsibility is to create new meta components (from provided bean
 * classes) and add them to the FormModel. In some cases, no new component is
 * created, just modified (e.g. when a border is applied). This class is
 * intended to process user actions, so all errors are caught and reported here.
 *
 * @author Tomas Pavek
 */
public class RADComponentCreator {

    private enum TargetType {

        LAYOUT, BORDER, VISUAL, OTHER
    }

    private enum ComponentType {

        NON_VISUAL, VISUAL
    }

    private static class TargetInfo {

        private TargetType targetType; // the way of adding/applying to the target component
        private ComponentType componentType; // type of radcomponent to be added/applied
        private RADComponent<?> targetComponent; // actual target component (after adjustments)
    }
    private static final FormProperty.Filter COPIED_PROPERTY_FILTER = new FormProperty.Filter() {
        @Override
        public boolean accept(FormProperty<?> property) {
            // don't copy name property
            return !property.isDefaultValue() && !"name".equals(property.getName());
        }
    };
    private FormModel formModel;
    private RADVisualComponent<?> preRadComp;

    RADComponentCreator(FormModel model) {
        formModel = model;
    }

    /**
     * Creates and adds a new radcomponent to FormModel. The new component is
     * added to target component (if it is ComponentContainer).
     *
     * @param classSource ClassSource describing the component class
     * @param aConstraints constraints object (for visual components only)
     * @param targetComp component into which the new component is added
     * @return the radcomponent if it was successfully created and added (all
     * errors are reported immediately)
     */
    public RADComponent<?> createComponent(String classSource,
            RADComponent<?> targetComp,
            LayoutConstraints<?> aConstraints) {
        return createComponent(classSource, targetComp, aConstraints, true);
    }

    RADComponent<?> createComponent(String classSource,
            RADComponent<?> targetComp,
            LayoutConstraints<?> aConstraints,
            boolean exactTargetMatch) {
        Class<?> compClass = prepareClass(classSource);
        if (compClass != null) {
            RADComponent<?> radComp = createAndAddComponent(compClass, targetComp, aConstraints, exactTargetMatch);
            return radComp;
        } else {
            return null; // class loading failed
        }
    }

    /**
     * Creates a copy of a radcomponent and adds it to FormModel. The new
     * component is added or applied to the specified target component.
     *
     * @param sourceComp component to be copied
     * @param targetComp target component (where the new component is added)
     * @return the component if it was successfully created and added (all
     * errors are reported immediately)
     */
    public RADComponent<?> copyComponent(final RADComponent<?> sourceComp,
            final RADComponent<?> targetComp) {
        final TargetInfo target = getTargetInfo(sourceComp.getBeanClass(), targetComp,
                false, false);
        if (target == null) {
            return null;
        }

        try { // Look&Feel UI defaults remapping needed
            return FormLAF.<RADComponent<?>>executeWithLookAndFeel(formModel,
                    new Mutex.ExceptionAction<RADComponent<?>>() {
                        @Override
                        public RADComponent<?> run() throws Exception {
                            return copyComponent2(sourceComp, null, target);
                        }
                    });
        } catch (Exception ex) { // should not happen
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }
    }

    public boolean moveComponent(RADComponent<?> radComp, RADComponent<?> targetComp) throws Exception {
        TargetInfo target = getTargetInfo(radComp.getBeanClass(), targetComp, false, false);
        if (target != null) {
            formModel.removeComponent(radComp, false);
            if (targetComp instanceof RADVisualContainer<?>) {
                RADVisualContainer<?> targetCont = (RADVisualContainer<?>) targetComp;
                if (radComp instanceof RADVisualComponent<?> && targetCont.getLayoutSupport() != null && targetCont.getLayoutSupport().getLayoutDelegate() != null) {
                    RADVisualComponent<?> radCont = (RADVisualComponent<?>) radComp;
                    LayoutSupportDelegate lsd = targetCont.getLayoutSupport().getLayoutDelegate();
                    LayoutConstraints<?>[] newConstraints = new LayoutConstraints<?>[]{null};
                    lsd.convertConstraints(new LayoutConstraints<?>[]{null}, newConstraints, new Component[]{radCont.getBeanInstance()});
                    LayoutConstraints constraints = newConstraints[0];
                    radCont.setLayoutConstraints(lsd.getClass(), constraints);
                }
            }
            return copyComponent2(radComp, radComp, target) != null;
        } else {
            return false;
        }
    }

    public boolean addComponents(Collection<? extends RADComponent<?>> components, RADComponent<?> targetComp) throws Exception {
        for (RADComponent<?> radComp : components) {
            TargetInfo target = getTargetInfo(radComp.getBeanClass(), targetComp, false, false);
            if (target == null) {
                return false;
            }
            copyComponent2(radComp, radComp, target);
        }
        return true;
    }

    public static boolean canAddComponent(Class<?> beanClass, RADComponent<?> targetComp) {
        TargetInfo target = getTargetInfo(beanClass, targetComp, false, false);
        return target != null
                && (target.targetType == TargetType.OTHER
                || target.targetType == TargetType.VISUAL);
    }

    public static boolean canApplyComponent(Class<?> beanClass, RADComponent<?> targetComp) {
        TargetInfo target = getTargetInfo(beanClass, targetComp, false, false);
        return target != null
                && (target.targetType == TargetType.BORDER
                || target.targetType == TargetType.LAYOUT);
    }

    // --------
    // Visual component can be precreated before added to form to provide for
    // better visual feedback when being added. The precreated component may
    // end up as added or canceled. If it is added to the form (by the user),
    // addPrecreatedComponent methods gets called. If adding is canceled for
    // whatever reason, releasePrecreatedComponent is called.
    public RADVisualComponent<?> precreateVisualComponent(final String classSource) {
        final Class<?> compClass = prepareClass(classSource);

        // no preview component if this is a window, applet, or not visual
        if (compClass == null
                || java.awt.Window.class.isAssignableFrom(compClass)
                || java.applet.Applet.class.isAssignableFrom(compClass)
                // JPopupMenu can't be used as a visual component (added to a container)
                || javax.swing.JPopupMenu.class.isAssignableFrom(compClass)
                || !FormUtils.isVisualizableClass(compClass)) {
            return null;
        }

        if (preRadComp != null) {
            releasePrecreatedComponent();
        }

        try { // Look&Feel UI defaults remapping needed
            FormLAF.<RADVisualComponent<?>>executeWithLookAndFeel(formModel,
                    new Mutex.ExceptionAction<RADVisualComponent<?>>() {
                        @Override
                        public RADVisualComponent<?> run() throws Exception {
                            preRadComp = createVisualComponent(compClass);
                            return preRadComp;
                        }
                    });
            return preRadComp;
        } catch (Exception ex) { // should not happen
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }
    }

    public RADVisualComponent<?> getPrecreatedRADComponent() {
        return preRadComp;
    }

    static boolean shouldBeLayoutContainer(RADComponent<?> radComp) {
        return radComp instanceof RADVisualContainer
                && ((RADVisualContainer<?>) radComp).getLayoutSupport() == null;
    }

    public boolean addPrecreatedComponent(RADComponent<?> targetComp, int aIndex,
            LayoutConstraints<?> aConstraints) throws Exception {
        if (preRadComp != null) {
            TargetInfo target = getTargetInfo(preRadComp.getBeanClass(), targetComp, true, true);
            if (target != null
                    && (target.targetType == TargetType.VISUAL
                    || target.targetType == TargetType.OTHER)) {
                addVisualComponent2(preRadComp, target.targetComponent, aIndex, aConstraints, true);
            }
            releasePrecreatedComponent();
            return true;
        } else {
            return false;
        }
    }

    void releasePrecreatedComponent() {
        if (preRadComp != null) {
            preRadComp = null;
        }
    }

    // --------
    private RADComponent<?> createAndAddComponent(final Class<?> compClass,
            final RADComponent<?> targetComp,
            final LayoutConstraints<?> aConstraints,
            boolean exactTargetMatch) {
        // check adding form class to itself

        final TargetInfo target = getTargetInfo(compClass, targetComp,
                !exactTargetMatch, !exactTargetMatch);
        if (target == null) {
            return null;
        }

        try { // Look&Feel UI defaults remapping needed
            return FormLAF.<RADComponent<?>>executeWithLookAndFeel(formModel,
                    new Mutex.ExceptionAction<RADComponent<?>>() {
                        @Override
                        public RADComponent<?> run() throws Exception {
                            return createAndAddComponent2(compClass, target, aConstraints);
                        }
                    });
        } catch (Exception ex) { // should not happen
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }
    }

    private RADComponent<?> createAndAddComponent2(Class<?> compClass,
            TargetInfo target,
            LayoutConstraints<?> aConstraints) throws Exception {
        RADComponent<?> targetComp = target.targetComponent;
        if (target.targetType == TargetType.LAYOUT) {
            return setContainerLayout((Class<LayoutManager>) compClass, targetComp);
        }
        if (target.targetType == TargetType.BORDER & targetComp instanceof RADVisualComponent<?>) {
            return setComponentBorder((Class<Border>) compClass, (RADVisualComponent<?>) targetComp);
        }
        RADComponent<?> newRadComp = null;
        if (target.componentType == ComponentType.VISUAL) {
            newRadComp = addVisualComponent((Class<? extends Component>) compClass, targetComp, -1, aConstraints);
        } else {
            if (ButtonGroup.class.isAssignableFrom(compClass)) {
                newRadComp = addButtonGroup((Class<ButtonGroup>) compClass, targetComp);
            } else if (DbGridColumn.class.isAssignableFrom(compClass)) {
                newRadComp = addGridColumn((Class<DbGridColumn>) compClass, targetComp);
            } else if (RowsetFeatureDescriptor.class.isAssignableFrom(compClass)) {
                newRadComp = addMapLayer((Class<RowsetFeatureDescriptor>) compClass, targetComp);
            }
        }
        return newRadComp;
    }

    private RADComponent<?> copyComponent2(RADComponent<?> sourceComp,
            RADComponent<?> copiedComp,
            TargetInfo target) throws Exception {
        RADComponent<?> targetComp = target.targetComponent;
        // if layout or border is to be copied from a meta component, we just
        // apply the cloned instance, but don't copy the meta component
        if (target.targetType == TargetType.LAYOUT) {
            return copyAndApplyLayout(sourceComp, targetComp);
        } else if (target.targetType == TargetType.BORDER) {
            return copyAndApplyBorder(sourceComp, (RADVisualComponent<?>) targetComp);
        } else {
            // in other cases we need a copy of the source radcomponent
            if (sourceComp instanceof RADVisualComponent<?>) {
                LayoutSupportManager.storeConstraints((RADVisualComponent<?>) sourceComp);
            }
            boolean newlyAdded;
            if (copiedComp == null) { // copy the source radcomponent
                copiedComp = makeCopy(sourceComp);
                if (copiedComp == null) { // copying failed (for a mystic reason)
                    return null;
                }
                newlyAdded = true;
            } else {
                newlyAdded = false;
            }
            if (target.targetType == TargetType.VISUAL) {
                RADVisualComponent<?> newVisual = (RADVisualComponent<?>) copiedComp;
                LayoutConstraints constraints;
                if (targetComp != null) {
                    RADVisualContainer<?> targetCont = (RADVisualContainer<?>) targetComp;
                    LayoutSupportManager layoutSupport = targetCont.getLayoutSupport();
                    if (layoutSupport == null) {
                        constraints = null;
                    } else {
                        constraints = layoutSupport.getStoredConstraints(newVisual);
                    }
                } else {
                    constraints = null;
                }
                copiedComp = addVisualComponent2(newVisual, targetComp, 0, constraints, newlyAdded);
                // might be null if layout support did not accept the component
            } else if (target.targetType == TargetType.OTHER) {
                if (copiedComp instanceof RADButtonGroup) {
                    addButtonGroup((RADButtonGroup) copiedComp, targetComp, newlyAdded);
                } else if (copiedComp instanceof RADModelMapLayer) {
                    addMapLayer((RADModelMapLayer) copiedComp, targetComp, newlyAdded);
                } else if (copiedComp instanceof RADModelGridColumn) {
                    addGridColumn((RADModelGridColumn) copiedComp, targetComp, newlyAdded);
                }
            }

            return copiedComp;
        }
    }

    /**
     * This is a central place for deciding whether a bean can be added or
     * applied to given target component. It returns a TargetInfo object
     * representing the target operation and type of radcomponent to be created,
     * or null if the bean can't be used. Determining the target placement is
     * more strict for copy/cut/paste (paramaters canUseParent and
     * defaultToOthers set to false), and less strict for visual (drag&drop)
     * operations (canUseParent and defaultToOthers set to true). In the latter
     * case the actual target component can be different - it is returned in the
     * targetComponent field of TargetInfo.
     */
    private static TargetInfo getTargetInfo(Class<?> beanClass,
            RADComponent<?> targetComp,
            boolean canUseParent,
            boolean defaultToOthers) {
        TargetInfo target = new TargetInfo();

        if (targetComp != null) {
            if (LayoutSupportDelegate.class.isAssignableFrom(beanClass)
                    || LayoutManager.class.isAssignableFrom(beanClass)) {   // layout manager
                RADVisualContainer<?> targetCont = getVisualContainer(targetComp, canUseParent);
                if (targetCont != null && !targetCont.hasDedicatedLayoutSupport()) {
                    target.targetType = TargetType.LAYOUT;
                } else {
                    return null;
                }
            } else if (Border.class.isAssignableFrom(beanClass)) { // border
                if (targetComp instanceof RADVisualComponent
                        && JComponent.class.isAssignableFrom(targetComp.getBeanClass())) {
                    target.targetType = TargetType.BORDER;
                } else {
                    return null;
                }
            } else if (FormUtils.isVisualizableClass(beanClass)) {
                // visual component
                if (targetComp != null
                        && (java.awt.Window.class.isAssignableFrom(beanClass)
                        || java.applet.Applet.class.isAssignableFrom(beanClass)
                        || !java.awt.Component.class.isAssignableFrom(beanClass))) {
                    // visual component that cna't have a parent
                    if (defaultToOthers) {
                        targetComp = null; // will go to Other Components
                    } else {
                        return null;
                    }
                }

                RADVisualContainer<?> targetCont = getVisualContainer(targetComp, canUseParent);
                while (targetCont != null) {
                    if (targetCont.canAddComponent(beanClass)) {
                        target.targetType = TargetType.VISUAL;
                        targetComp = targetCont;
                        break;
                    } else if (canUseParent) {
                        targetCont = targetCont.getParentComponent();
                    } else {
                        targetCont = null;
                    }
                }
                if (targetCont == null) {
                    if (defaultToOthers) {
                        targetComp = null; // will go to Other Components
                    } else {
                        return null;
                    }
                }
            }
        }
        if (targetComp == null) {
            target.targetType = TargetType.OTHER;
        } else {
            if (targetComp instanceof RADModelMap || targetComp instanceof RADModelGrid) {
                target.targetType = TargetType.OTHER;
            } else if (targetComp instanceof RADModelGridColumn) {
                target.targetType = TargetType.OTHER;
            }
        }
        target.targetComponent = targetComp;

        if (FormUtils.isVisualizableClass(beanClass)) {
            target.componentType = ComponentType.VISUAL;
        } else {
            target.componentType = ComponentType.NON_VISUAL;
        }

        return target;
    }

    private static RADVisualContainer<?> getVisualContainer(RADComponent<?> targetComp, boolean canUseParent) {
        if (targetComp instanceof RADVisualContainer<?>) {
            return (RADVisualContainer<?>) targetComp;
        } else if (canUseParent && targetComp instanceof RADVisualComponent<?>) {
            return (RADVisualContainer<?>) targetComp.getParentComponent();
        } else {
            return null;
        }
    }

    static boolean isTransparentLayoutComponent(RADComponent<?> radComp) {
        return radComp != null
                && radComp.getBeanClass() == JScrollPane.class; // NOI18N
    }

    // ---------
    private RADComponent<?> makeCopy(RADComponent<?> sourceComp) throws Exception {
        RADComponent<?> newComp = null;

        if (sourceComp instanceof RADVisualContainer<?>) {
            newComp = new RADVisualContainer<>();
        } else if (sourceComp instanceof RADVisualComponent<?>) {
            if (sourceComp instanceof RADModelScalarComponent) {
                if (sourceComp instanceof RADColumnView<?>) {
                    newComp = new RADColumnView<>();
                } else {
                    newComp = new RADModelScalarComponent<>();
                }
            } else if (sourceComp instanceof RADModelGrid) {
                newComp = new RADModelGrid();
            } else if (sourceComp instanceof RADModelMap) {
                newComp = new RADModelMap();
            } else {
                newComp = new RADVisualComponent<>();
            }
        } else {
            if (sourceComp instanceof RADModelGridColumn) {
                newComp = new RADModelGridColumn();
            } else if (sourceComp instanceof RADModelMapLayer) {
                newComp = new RADModelMapLayer();
            } else if (sourceComp instanceof RADButtonGroup) {
                newComp = new RADButtonGroup();
            }
        }

        newComp.initialize(formModel);
        if (sourceComp != sourceComp.getFormModel().getTopRADComponent()) {
            String newName = (sourceComp.getName() != null && !sourceComp.getName().isEmpty()) ? formModel.findFreeComponentName(sourceComp.getName()) : formModel.findFreeComponentName(sourceComp.getBeanClass());
            newComp.setStoredName(newName);
        }

        try {
            newComp.initInstance(sourceComp.getBeanClass());
            newComp.setInModel(true);
        } catch (Exception ex) { // this is rather unlikely to fail
            ErrorManager em = ErrorManager.getDefault();
            em.annotate(ex, FormUtils.getBundleString("MSG_ERR_CannotCopyInstance")); // NOI18N
            em.notify(ex);
            return null;
        }

        // 1st - copy subcomponents
        if (sourceComp instanceof ComponentContainer) {
            RADComponent<?>[] sourceSubs = ((ComponentContainer) sourceComp).getSubBeans();
            RADComponent<?>[] newSubs = new RADComponent<?>[sourceSubs.length];

            for (int i = 0; i < sourceSubs.length; i++) {
                RADComponent<?> newSubComp = makeCopy(sourceSubs[i]);
                if (newSubComp == null) {
                    return null;
                }
                newSubs[i] = newSubComp;
            }

            ((ComponentContainer) newComp).initSubComponents(newSubs);

            // 2nd - clone layout support
            if (sourceComp instanceof RADVisualContainer<?>) {
                RADVisualComponent<?>[] newComps =
                        new RADVisualComponent<?>[newSubs.length];
                System.arraycopy(newSubs, 0, newComps, 0, newSubs.length);

                LayoutSupportManager sourceLayout =
                        ((RADVisualContainer<?>) sourceComp).getLayoutSupport();

                if (sourceLayout != null) {
                    RADVisualContainer<?> newCont = (RADVisualContainer<?>) newComp;
                    newCont.checkLayoutSupport();
                    newCont.getLayoutSupport().copyLayoutDelegateFrom(sourceLayout, newComps);
                }
            }
            // 3rd - clone column view
            if (sourceComp instanceof RADModelGridColumn) {
                assert newComp instanceof RADModelGridColumn;
                RADModelGridColumn sourceColumn = (RADModelGridColumn) sourceComp;
                RADModelGridColumn newColumn = (RADModelGridColumn) newComp;
                RADColumnView<? super DbControlPanel> newColumnView = (RADColumnView<? super DbControlPanel>) makeCopy(sourceColumn.getViewControl());
                // Let's revoke some work, obsolete for column view component
                newColumnView.setInModel(false);
                newColumnView.getBeanInstance().setModel(null);
                if (newColumnView.getConstraints() != null) {
                    newColumnView.getConstraints().clear();
                }
                // Set resulting view component to new column
                newColumn.setViewControl(newColumnView);
            }
        }

        // 4th - copy changed properties, except the name property
        int copyMode = FormUtils.DISABLE_CHANGE_FIRING;
        if (formModel == sourceComp.getFormModel()) {
            copyMode |= FormUtils.PASS_DESIGN_VALUES;
        }
        java.util.List<RADProperty<?>> filtered = sourceComp.getBeanProperties(COPIED_PROPERTY_FILTER, false);
        java.util.List<String> filteredNames = new ArrayList<>();
        for (RADProperty<?> prop : filtered) {
            filteredNames.add(prop.getName());
        }
        RADProperty<?>[] sourceProps = filtered.toArray(new RADProperty<?>[]{});
        RADProperty<?>[] newProps = newComp.getBeanProperties(filteredNames.toArray(new String[]{}));
        assert sourceProps.length == newProps.length;

        FormUtils.copyProperties(sourceProps, newProps, copyMode);
        // 5th - make model-aware controls aware of our model
        if (sourceComp.getBeanInstance() instanceof DbControl) {
            assert newComp.getBeanInstance() instanceof DbControl;
            DbControl destDbControl = (DbControl) newComp.getBeanInstance();
            if (formModel.getDataObject().getClient() != null) {
                destDbControl.setModel(formModel.getDataObject().getModel());
            }
        }

        // 6th - copy layout constraints
        if (sourceComp instanceof RADVisualComponent<?>
                && newComp instanceof RADVisualComponent<?>) {
            Map<String, LayoutConstraints<?>> constraints = ((RADVisualComponent<?>) sourceComp).getConstraints();
            Map<String, LayoutConstraints<?>> newConstraints = new HashMap<>();

            for (Map.Entry<String, LayoutConstraints<?>> entry : constraints.entrySet()) {
                String layoutClassName = entry.getKey();
                LayoutConstraints<?> clonedConstr = entry.getValue().cloneConstraints();
                if (clonedConstr instanceof AbsoluteLayoutSupport.AbsoluteLayoutConstraints) {
                    AbsoluteLayoutSupport.AbsoluteLayoutConstraints ac = (AbsoluteLayoutSupport.AbsoluteLayoutConstraints) clonedConstr;
                    ac.offset(10, 10);
                }
                if (clonedConstr instanceof MarginLayoutSupport.MarginLayoutConstraints) {
                    // We assume, that component has a parent and it is not the root container
                    // because it has a constraints
                    MarginLayoutSupport.MarginLayoutConstraints mlc = (MarginLayoutSupport.MarginLayoutConstraints) clonedConstr;
                    Container targetContainer = sourceComp.getParentComponent().getBeanInstance();
                    Component sourceBean = (Component) sourceComp.getBeanInstance();
                    MarginLayoutSupport.mutate(mlc.getConstraintsObject(), targetContainer.getWidth(), targetContainer.getHeight(), sourceBean.getLocation().x + 10, sourceBean.getLocation().y + 10, sourceBean.getWidth(), sourceBean.getHeight());
                }
                newConstraints.put(layoutClassName, clonedConstr);
            }
            ((RADVisualComponent<?>) newComp).getConstraints().putAll(newConstraints);
        }

        // 7th - copy events 
        Event[] sourceEvents = sourceComp.getAllEvents();
        String[][] eventHandlers = new String[sourceEvents.length][];
        for (int eventsIdx = 0; eventsIdx < sourceEvents.length; eventsIdx++) {
            eventHandlers[eventsIdx] = sourceEvents[eventsIdx].getEventHandlers();
        }

        FormEvents formEvents = formModel.getFormEvents();
        Event[] targetEvents = newComp.getAllEvents();
        assert sourceEvents.length == targetEvents.length;
        for (int targetEventsIdx = 0; targetEventsIdx < targetEvents.length; targetEventsIdx++) {

            Event targetEvent = targetEvents[targetEventsIdx];
            if (targetEvent == null) {
                continue; // [uknown event error - should be reported!]
            }
            String[] handlers = eventHandlers[targetEventsIdx];
            for (int handlersIdx = 0; handlersIdx < handlers.length; handlersIdx++) {
                String newHandlerName;
                String oldHandlerName = handlers[handlersIdx];
                String sourceVariableName = sourceComp.getName();
                String targetVariableName = newComp.getName();

                int idx = oldHandlerName.indexOf(sourceVariableName);
                if (idx >= 0) {
                    newHandlerName = oldHandlerName.substring(0, idx)
                            + targetVariableName
                            + oldHandlerName.substring(idx + sourceVariableName.length());
                } else {
                    newHandlerName = targetVariableName
                            + oldHandlerName;
                }
                newHandlerName = formEvents.findFreeHandlerName(newHandlerName);

                String bodyText = null;
                if (sourceComp.getFormModel() != formModel) {
                    // copying to different form -> let's copy also the event handler content
                    FormsJsCodeGenerator javaCodeGenerator =
                            ((FormsJsCodeGenerator) FormEditor.getCodeGenerator(sourceComp.getFormModel()));
                    bodyText = javaCodeGenerator.getEventHandlerText(oldHandlerName);
                }

                try {
                    formEvents.attachEvent(targetEvent, newHandlerName, bodyText);
                } catch (IllegalArgumentException ex) {
                    // [incompatible handler error - should be reported!]
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }
        return newComp;
    }

    // --------
    private RADComponent<?> addVisualComponent(Class<? extends Component> compClass,
            RADComponent<?> targetComp,
            int aIndex,
            LayoutConstraints<?> aConstraints) throws Exception {
        RADVisualComponent<?> newRadComp = createVisualComponent(compClass);

        if (java.awt.Window.class.isAssignableFrom(compClass)
                || java.applet.Applet.class.isAssignableFrom(compClass)) {
            targetComp = null;
        }

        return addVisualComponent2(newRadComp, targetComp, aIndex, aConstraints, true);
    }

    private RADVisualComponent<?> createVisualComponent(Class<?> compClass) {
        RADVisualComponent<?> newRadComp = null;
        RADVisualContainer<?> newRadCont = FormUtils.isContainer(compClass) ? new RADVisualContainer<>() : null;
        // initialize radcomponent and its bean instance
        if (DbGrid.class.isAssignableFrom(compClass)) {
            newRadComp = new RADModelGrid();
        } else if (DbMap.class.isAssignableFrom(compClass)) {
            newRadComp = new RADModelMap();
        } else if (ScalarDbControl.class.isAssignableFrom(compClass)) {
            newRadComp = new RADModelScalarComponent<>();
        } else {
            newRadComp = newRadCont == null ? new RADVisualComponent<>() : newRadCont;
        }

        newRadComp.initialize(formModel);
        if (initComponentInstance(newRadComp, compClass)) {
            prepareDefaultLayoutSize(newRadComp.getBeanInstance(), newRadCont != null);
            if (newRadCont != null) {
                // prepare layout support (the new component is a container)
                boolean knownLayout = false;
                Throwable layoutEx = null;
                try {
                    newRadCont.checkLayoutSupport();
                    LayoutSupportManager laysup = newRadCont.getLayoutSupport();
                    knownLayout = laysup.prepareLayoutDelegate(false);
                } catch (RuntimeException ex) { // silently ignore, try again as non-container
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    return null;
                } catch (Exception ex) {
                    layoutEx = ex;
                } catch (LinkageError ex) {
                    layoutEx = ex;
                }

                if (!knownLayout) {
                    if (layoutEx == null) {
                        // no LayoutSupportDelegate found for the container
                        System.err.println("[WARNING] No layout support found for " + compClass.getName()); // NOI18N
                        System.err.println("          Just a limited basic support will be used."); // NOI18N
                    } else { // layout support initialization failed
                        ErrorManager em = ErrorManager.getDefault();
                        em.annotate(
                                layoutEx,
                                FormUtils.getBundleString("MSG_ERR_LayoutInitFailed2")); // NOI18N
                        em.notify(layoutEx);
                    }

                    newRadCont.getLayoutSupport().setUnknownLayoutDelegate();
                }
            }
            newRadComp.setStoredName(formModel.findFreeComponentName(compClass));
            // for some components, we initialize their properties with some
            // non-default values e.g. a label on buttons, checkboxes
            return (RADVisualComponent<?>) defaultVisualComponentInit(newRadComp);
        } else {
            return null; // failure (reported)
        }
    }

    private RADVisualComponent<?> addVisualComponent2(RADVisualComponent<?> newRadComp,
            RADComponent<?> targetComp,
            int aIndex,
            LayoutConstraints<?> aConstraints,
            boolean newlyAdded) throws Exception {
        // Issue 65254: beware of nested JScrollPanes
        if ((targetComp != null) && JScrollPane.class.isAssignableFrom(targetComp.getBeanClass())) {
            Object bean = newRadComp.getBeanInstance();
            if (bean instanceof JScrollPane) {
                RADVisualContainer<?> radCont = (RADVisualContainer<?>) newRadComp;
                newRadComp = radCont.getSubComponent(0);
            }
        }

        // get parent container into which the new component will be added
        RADVisualContainer<?> parentCont;
        if (targetComp != null) {
            parentCont = targetComp instanceof RADVisualContainer
                    ? (RADVisualContainer<?>) targetComp
                    : (RADVisualContainer<?>) targetComp.getParentComponent();
        } else {
            parentCont = null;
        }

        defaultTargetInit(newRadComp, parentCont);

        // add the new radcomponent to the model
        if (parentCont != null) {
            try {
                formModel.addVisualComponent(newRadComp, parentCont, aIndex, aConstraints, newlyAdded);
            } catch (RuntimeException ex) {
                // LayoutSupportDelegate may not accept the component
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                return null;
            }
        } else {
            formModel.addComponent(newRadComp, null, newlyAdded);
        }

        if (newRadComp.getBeanInstance() instanceof DbControl) {
            DbControl dbControl = (DbControl) newRadComp.getBeanInstance();
            if (formModel.getDataObject().getClient() != null) {
                dbControl.setModel(formModel.getDataObject().getModel());
            }
        }
        return newRadComp;
    }

    private RADButtonGroup addButtonGroup(Class<ButtonGroup> compClass,
            RADComponent<?> targetComp) throws Exception {
        assert javax.swing.ButtonGroup.class.isAssignableFrom(compClass);
        RADButtonGroup newRadComp = new RADButtonGroup();
        newRadComp.initialize(formModel);
        if (!initComponentInstance(newRadComp, compClass)) {
            return null;
        }
        addButtonGroup(newRadComp, targetComp, true);
        return newRadComp;
    }

    private void addButtonGroup(RADButtonGroup newRadComp,
            RADComponent<?> targetComp,
            boolean newlyAdded) {
        ComponentContainer targetCont =
                targetComp instanceof ComponentContainer
                && !(targetComp instanceof RADVisualContainer<?>)
                ? (ComponentContainer) targetComp : null;
        if (newlyAdded) {
            newRadComp.setStoredName(formModel.findFreeComponentName(ButtonGroup.class));
        }
        formModel.addComponent(newRadComp, targetCont, newlyAdded);
    }

    private RADModelGridColumn addGridColumn(Class<DbGridColumn> compClass,
            RADComponent<?> targetComp) throws Exception {
        assert DbGridColumn.class.isAssignableFrom(compClass);
        RADModelGridColumn newRadComp = new RADModelGridColumn();
        newRadComp.initialize(formModel);
        if (initComponentInstance(newRadComp, compClass)) {
            addGridColumn(newRadComp, targetComp, true);
            return newRadComp;
        } else {
            return null;
        }
    }

    private void addGridColumn(RADModelGridColumn newComp,
            RADComponent<?> targetComp,
            boolean newlyAdded) {
        ComponentContainer targetCont =
                targetComp instanceof RADModelGrid
                || targetComp instanceof RADModelGridColumn
                ? (ComponentContainer) targetComp : null;
        if (newlyAdded) {
            if (!newComp.isInModel() || formModel.getRADComponent(newComp.getName()) != newComp) {
                newComp.setStoredName(formModel.findFreeComponentName("Column"));
            }
        }
        formModel.addComponent(newComp, targetCont, newlyAdded);
    }

    private RADModelMapLayer addMapLayer(Class<RowsetFeatureDescriptor> compClass,
            RADComponent<?> targetComp) throws Exception {
        assert RowsetFeatureDescriptor.class.isAssignableFrom(compClass);
        RADModelMapLayer newRadComp = new RADModelMapLayer();
        newRadComp.initialize(formModel);
        if (initComponentInstance(newRadComp, compClass)) {
            addMapLayer(newRadComp, targetComp, true);
            return newRadComp;
        } else {
            return null;
        }
    }

    private void addMapLayer(RADModelMapLayer newComp,
            RADComponent<?> targetComp,
            boolean newlyAdded) {
        ComponentContainer targetCont =
                targetComp instanceof RADModelMap
                ? (ComponentContainer) targetComp : null;
        if (newlyAdded) {
            if (!newComp.isInModel() || formModel.getRADComponent(newComp.getName()) != newComp) {
                newComp.setStoredName(formModel.findFreeComponentName("Layer"));
            }
        }
        formModel.addComponent(newComp, targetCont, newlyAdded);
    }

    private RADComponent<?> setContainerLayout(Class<LayoutManager> layoutClass,
            RADComponent<?> targetComp) {
        // get container on which the layout is to be set
        RADVisualContainer<?> radCont;
        if (targetComp instanceof RADVisualContainer<?>) {
            radCont = (RADVisualContainer<?>) targetComp;
        } else {
            radCont = (RADVisualContainer<?>) targetComp.getParentComponent();
            if (radCont == null) {
                return null;
            }
        }

        LayoutSupportDelegate layoutDelegate = null;
        Throwable t = null;
        try {
            if (LayoutManager.class.isAssignableFrom(layoutClass)) {
                // LayoutManager -> find LayoutSupportDelegate for it
                layoutDelegate = LayoutSupportRegistry.getRegistry(formModel).createSupportForLayout(layoutClass);
            } else if (LayoutSupportDelegate.class.isAssignableFrom(layoutClass)) {
                // LayoutSupportDelegate -> use it directly
                layoutDelegate = LayoutSupportRegistry.createSupportInstance(layoutClass);
            }
        } catch (Exception | LinkageError ex) {
            t = ex;
        }
        if (t != null) {
            String msg = FormUtils.getFormattedBundleString(
                    "FMT_ERR_LayoutInit", // NOI18N
                    new Object[]{layoutClass.getName()});

            ErrorManager em = ErrorManager.getDefault();
            em.annotate(t, msg);
            em.notify(t);
            return null;
        }

        if (layoutDelegate == null) {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                    FormUtils.getFormattedBundleString(
                    "FMT_ERR_LayoutNotFound", // NOI18N
                    new Object[]{layoutClass.getName()}),
                    NotifyDescriptor.WARNING_MESSAGE));

            return null;
        }

        try {
            formModel.setContainerLayout(radCont, layoutDelegate);
        } catch (Exception | LinkageError ex) {
            t = ex;
        }
        if (t != null) {
            String msg = FormUtils.getFormattedBundleString(
                    "FMT_ERR_LayoutInit", // NOI18N
                    new Object[]{layoutClass.getName()});

            ErrorManager em = ErrorManager.getDefault();
            em.annotate(t, msg);
            em.notify(t);
            return null;
        }
        return radCont;
    }

    private RADComponent<?> copyAndApplyLayout(RADComponent<?> sourceComp,
            RADComponent<?> targetComp) {
        try {
            RADVisualContainer<?> targetCont = (RADVisualContainer<?>) setContainerLayout((Class<LayoutManager>) sourceComp.getBeanClass(), targetComp);

            // copy properties additionally to handle design values
            FormProperty<?>[] sourceProps = sourceComp.getKnownBeanProperties();
            FormProperty<?>[] targetProps =
                    targetCont.getLayoutSupport().getAllProperties();
            int copyMode = FormUtils.CHANGED_ONLY
                    | FormUtils.DISABLE_CHANGE_FIRING;
            if (formModel == sourceComp.getFormModel()) {
                copyMode |= FormUtils.PASS_DESIGN_VALUES;
            }

            FormUtils.copyProperties(sourceProps, targetProps, copyMode);
        } catch (Exception | LinkageError ex) { // ignore
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        return targetComp;
    }

    private RADComponent<?> setComponentBorder(Class<? extends Border> borderClass,
            RADVisualComponent<?> targetComp) {
        FormProperty<NbBorder> prop = getBorderProperty(targetComp);
        if (prop != null) {
            try { // set border property
                Border border = CreationFactory.<Border>createInstance(borderClass);
                prop.setValue(new NbBorder(border));
            } catch (Exception | LinkageError ex) {
                showInstErrorMessage(ex);
                return null;
            }
            return targetComp;
        } else {
            return null;
        }
    }

    private void setComponentBorderProperty(NbBorder borderInstance,
            RADVisualComponent<?> targetComp) {
        FormProperty<NbBorder> prop = getBorderProperty(targetComp);
        if (prop != null) {
            try { // set border property
                prop.setValue(borderInstance);
            } catch (Exception ex) { // should not happen
                ErrorManager.getDefault().notify(ex);
            }
        }
    }

    private RADComponent<?> copyAndApplyBorder(RADComponent<?> sourceComp,
            RADVisualComponent<?> targetComp) {
        try {
            Border borderInstance = (Border) sourceComp.createBeanInstance();
            NbBorder designBorder = new NbBorder(borderInstance);

            FormProperty<?>[] sourceProps = sourceComp.getKnownBeanProperties();
            FormProperty<?>[] targetProps = designBorder.getProperties();
            int copyMode = FormUtils.CHANGED_ONLY | FormUtils.DISABLE_CHANGE_FIRING;
            if (formModel == sourceComp.getFormModel()) {
                copyMode |= FormUtils.PASS_DESIGN_VALUES;
            }

            FormUtils.copyProperties(sourceProps, targetProps, copyMode);

            setComponentBorderProperty(designBorder, targetComp);
        } catch (Exception | LinkageError ex) { // ignore
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        return targetComp;
    }

    private FormProperty<NbBorder> getBorderProperty(RADComponent<?> targetComp) {
        FormProperty<NbBorder> prop;
        if (JComponent.class.isAssignableFrom(targetComp.getBeanClass())
                && (prop = targetComp.<FormProperty<NbBorder>>getRADProperty("border")) != null) // NOI18N
        {
            return prop;
        }

        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                FormUtils.getBundleString("MSG_BorderNotApplicable"), // NOI18N
                NotifyDescriptor.INFORMATION_MESSAGE));

        return null;
    }

    // --------
    Class<?> prepareClass(final String classSource) {
        return prepareClass0(classSource);
    }

    private Class<?> prepareClass0(String classSource) {
        Throwable error = null;
        String className = classSource;
        Class<?> loadedClass = null;
        try {
            if (FormLAF.inLAFBlock()) {
                // Force update to new class loader
                FormLAF.setUseDesignerDefaults(null);
                FormLAF.setUseDesignerDefaults(formModel);
            }
            loadedClass = FormUtils.loadSystemClass(className);
        } catch (Exception | LinkageError ex) {
            error = ex;
        }

        if (loadedClass == null) {
            showClassLoadingErrorMessage(error, classSource);
        }

        return loadedClass;
    }

    private static void showClassLoadingErrorMessage(Throwable ex,
            String classSource) {
        ErrorManager em = ErrorManager.getDefault();
        String msg = FormUtils.getFormattedBundleString(
                "FMT_ERR_CannotLoadClass4", // NOI18N
                new Object[]{classSource});
        em.annotate(ex, msg);
        em.notify(ErrorManager.USER, ex); // Issue 65116 - don't show the exception to the user
        em.notify(ErrorManager.INFORMATIONAL, ex); // Make sure the exception is in the console and log file
    }

    private boolean initComponentInstance(RADComponent<?> radComp, Class<?> compClass) {
        try {
            radComp.initInstance(compClass);
        } catch (Exception | LinkageError ex) {
            showInstErrorMessage(ex);
            return false;
        }
        return true;
    }

    private static void showInstErrorMessage(Throwable ex) {
        ErrorManager em = ErrorManager.getDefault();
        em.annotate(ex,
                FormUtils.getBundleString("MSG_ERR_CannotInstantiate")); // NOI18N
        em.notify(ex);
    }

    // --------
    // default component initialization
    private RADComponent<?> defaultVisualComponentInit(RADVisualComponent<?> newRadComp) {
        Object comp = newRadComp.getBeanInstance();
        String varName = newRadComp.getName();
        // Map of propertyNames -> propertyValues
        Map<String, Object> changes = new HashMap<>();

        changes.put("name", varName);
        if (comp instanceof JLabel) {
            if ("".equals(((JLabel) comp).getText())) { // NOI18N
                changes.put("text", varName); // NOI18N
            }
        } else if (comp instanceof JTextField) {
            if ("".equals(((JTextField) comp).getText())) { // NOI18N
                changes.put("text", varName); // NOI18N
            }
        } else if (comp instanceof JMenuItem) {
            if ("".equals(((JMenuItem) comp).getText())) { // NOI18N
                changes.put("text", varName); // NOI18N
            }
            if (comp instanceof JCheckBoxMenuItem) {
                changes.put("selected", Boolean.TRUE); // NOI18N
            }
            if (comp instanceof JRadioButtonMenuItem) {
                changes.put("selected", Boolean.TRUE); // NOI18N
            }
        } else if (comp instanceof AbstractButton) { // JButton, JToggleButton, JCheckBox, JRadioButton
            String txt = ((AbstractButton) comp).getText();
            if ((txt == null) || "".equals(txt)) { // NOI18N
                changes.put("text", varName); // NOI18N
            }
//            if (comp instanceof JCheckBox || comp instanceof JRadioButton) {
//                if (((JToggleButton)comp).getBorder() instanceof javax.swing.plaf.UIResource) {
//                    changes.put("border", BorderFactory.createEmptyBorder()); // NOI18N
//                    changes.put("margin", new Insets(0, 0, 0, 0)); // NOI18N
//                }
//            }
        } else if (comp instanceof JTable) {
        } else if (comp instanceof JToolBar) {
            changes.put("rollover", true); // NOI18N
        } else if (comp instanceof JInternalFrame) {
            changes.put("visible", true); // NOI18N
        } else if (comp instanceof Button) {
            if ("".equals(((Button) comp).getLabel())) { // NOI18N
                changes.put("label", varName); // NOI18N
            }
        } else if (comp instanceof Checkbox) {
            if ("".equals(((Checkbox) comp).getLabel())) { // NOI18N
                changes.put("label", varName); // NOI18N
            }
        } else if (comp instanceof Label) {
            if ("".equals(((Label) comp).getText())) { // NOI18N
                changes.put("text", varName); // NOI18N
            }
        } else if (comp instanceof TextField) {
            if ("".equals(((TextField) comp).getText())) { // NOI18N
                changes.put("text", varName); // NOI18N
            }
        } else if (comp instanceof JComboBox<?>) {
            ComboBoxModel<String> model = ((JComboBox<String>) comp).getModel();
            if ((model == null) || (model.getSize() == 0)) {
                String prefix = NbBundle.getMessage(RADComponentCreator.class, "FMT_CreatorComboBoxItem"); // NOI18N
                prefix += ' ';
                ComboBoxModel<String> propValue = new DefaultComboBoxModel<>(new String[]{
                            prefix + 1, prefix + 2, prefix + 3, prefix + 4
                        });
                changes.put("model", propValue); // NOI18N
            }

        } else if (comp instanceof JList<?>) {
            ListModel<String> model = ((JList<String>) comp).getModel();
            if ((model == null) || (model.getSize() == 0)) {
                String prefix = NbBundle.getMessage(RADComponentCreator.class, "FMT_CreatorListItem"); // NOI18N
                prefix += ' ';
                DefaultListModel<String> defaultModel = new DefaultListModel<>();
                for (int i = 1; i < 6; i++) {
                    defaultModel.addElement(prefix + i); // NOI18N
                }
                changes.put("model", defaultModel); // NOI18N
            }
        } else if (comp instanceof JTextArea) {
            JTextArea textArea = (JTextArea) comp;
            if (textArea.getRows() == 0) {
                changes.put("rows", new Integer(5)); // NOI18N
            }
            if (textArea.getColumns() == 0) {
                changes.put("columns", new Integer(20)); // NOI18N
            }
        } else if (comp instanceof JTextPane) {
            ((JTextPane) comp).setContentType("text/plain");
        } else if (comp instanceof JEditorPane) {
            ((JEditorPane) comp).setEditorKitForContentType("text/html", new HtmlContentEditorKit());
            ((JEditorPane) comp).setContentType("text/html");
        }

        for (Map.Entry<String, Object> change : changes.entrySet()) {
            String propName = change.getKey();
            Object propValue = change.getValue();
            FormProperty<Object> prop = newRadComp.<FormProperty<Object>>getRADProperty(propName);
            if (prop != null) {
                try {
                    prop.setChangeFiring(false);
                    prop.setValue(propValue);
                    prop.setChangeFiring(true);
                } catch (Exception e) {
                    // never mind, ignore
                }
            }
        }

        // more initial modifications...
        if (shouldEncloseByScrollPane(newRadComp.getBeanInstance())) {
            // hack: automatically enclose some components into scroll pane
            // [PENDING check for undo/redo!]
            RADVisualContainer<?> radScroll = (RADVisualContainer<?>) createVisualComponent(JScrollPane.class);
            // Mark this scroll pane as automatically created.
            // Some action (e.g. delete) behave differently on
            // components in such scroll panes.
            radScroll.add(newRadComp);
            Container scroll = (Container) radScroll.getBeanInstance();
            Component inScroll = (Component) newRadComp.getBeanInstance();
            radScroll.getLayoutSupport().addComponentsToContainer(
                    scroll, scroll, new Component[]{inScroll}, 0);
            newRadComp = radScroll;
        } else if (newRadComp instanceof RADVisualContainer<?> && newRadComp.getBeanInstance() instanceof JMenuBar) {
            // for menubars create initial menu [temporary?]
            RADVisualContainer<?> menuCont = (RADVisualContainer<?>) newRadComp;
            Container menuBar = (Container) menuCont.getBeanInstance();
            RADVisualComponent<?> menuComp = createVisualComponent(JMenu.class);
            menuComp.setStoredName(formModel.findFreeComponentName("mnuFile"));
            try {
                (menuComp.<RADProperty<String>>getRADProperty("text")) // NOI18N
                        .setValue(FormUtils.getBundleString("CTL_DefaultFileMenu")); // NOI18N
            } catch (Exception ex) {
                // never mind, ignore
            }
            Component menu = (Component) menuComp.getBeanInstance();
            menuCont.add(menuComp);
            menuCont.getLayoutSupport().addComponentsToContainer(
                    menuBar, menuBar, new Component[]{menu}, 0);

            menuComp = createVisualComponent(JMenu.class);
            menuComp.setStoredName(formModel.findFreeComponentName("mnuEdit"));
            try {
                (menuComp.<RADProperty<String>>getRADProperty("text")) // NOI18N
                        .setValue(FormUtils.getBundleString("CTL_DefaultEditMenu")); // NOI18N
            } catch (Exception ex) {
                // never mind, ignore
            }
            menu = (Component) menuComp.getBeanInstance();
            menuCont.add(menuComp);
            menuCont.getLayoutSupport().addComponentsToContainer(
                    menuBar, menuBar, new Component[]{menu}, 1);
        }
        return newRadComp;
    }

    private static boolean shouldEncloseByScrollPane(Object bean) {
        return (bean instanceof JList) || (bean instanceof JTable)
                || (bean instanceof JTree) || (bean instanceof JTextArea)
                || (bean instanceof JTextPane) || (bean instanceof JEditorPane);
    }

    /**
     * Initial setting for components that can't be done until knowing where
     * they are to be added to (type of target container). E.g. button
     * properties are adjusted when added to a toolbar.
     */
    private static void defaultTargetInit(RADComponent<?> radComp, RADComponent<?> target) {
        Object targetComp = target != null ? target.getBeanInstance() : null;

        if (radComp.getBeanClass().equals(JSeparator.class)) {
            if (targetComp instanceof JToolBar) {
                // hack: change JSeparator to JToolBar.Separator
                try {
                    radComp.initInstance(JToolBar.Separator.class);
                } catch (Exception ex) {
                } // should not fail with JDK class
                return;
            } else if (targetComp instanceof JMenu || targetComp instanceof JPopupMenu) {
                // hack: change JSeparator to JPopupMenu.Separator
                try {
                    radComp.initInstance(JPopupMenu.Separator.class);
                } catch (Exception ex) {
                } // should not fail with JDK class
                return;

            }
        }

        Object comp = radComp.getBeanInstance();
        Map<String, Object> changes = null;

        if (comp instanceof AbstractButton && targetComp instanceof JToolBar) {
            if (changes == null) {
                changes = new HashMap<>();
            }
            changes.put("focusable", false); // NOI18N
            changes.put("horizontalTextPosition", SwingConstants.CENTER); // NOI18N
            changes.put("verticalTextPosition", SwingConstants.BOTTOM); // NOI18N
        }

        if (changes != null) {
            for (Map.Entry<String, Object> e : changes.entrySet()) {
                FormProperty<Object> prop = radComp.<FormProperty<Object>>getRADProperty(e.getKey());
                if (prop != null) {
                    try {
                        prop.setChangeFiring(false);
                        prop.setValue(e.getValue());
                        prop.setChangeFiring(true);
                    } catch (Exception ex) {
                        // never mind, ignore
                    }
                }
            }
        }
    }

    public static Dimension prepareDefaultLayoutSize(Component comp, boolean isContainer) {
        int width = -1;
        int height = -1;
        if (comp instanceof JToolBar || comp instanceof JMenuBar) {
            width = 100;
            height = 25;
        } else if (isContainer) {
            if (comp instanceof Window || comp instanceof java.applet.Applet) {
                width = 400;
                height = 300;
            } else {
                width = 100;
                height = 100;
            }
        } else if (comp instanceof JSeparator) {
            width = 50;
            height = 10;
        } else if (comp instanceof DbImage) {
            width = 100;
            height = 100;
        } else if (comp instanceof DbText) {
            width = 100;
            height = 100;
        }

        if (width >= 0 && height >= 0) {
            Dimension size = new Dimension(width, height);
            if (comp instanceof JComponent) {
                ((JComponent) comp).setPreferredSize(size);
            }
            return size;
        } else {
            return null;
        }
    }
}