/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.designer.application.module;

import com.bearsoft.rowset.metadata.Parameter;
import com.eas.client.model.Relation;
import com.eas.client.model.application.ApplicationDbEntity;
import com.eas.client.model.application.ApplicationDbModel;
import com.eas.client.model.application.ApplicationParametersEntity;
import com.eas.client.model.gui.ApplicationModelEditorView;
import com.eas.client.model.gui.selectors.SelectedField;
import com.eas.client.model.gui.selectors.SelectedParameter;
import com.eas.client.model.gui.view.ModelSelectionListener;
import com.eas.client.model.gui.view.ModelViewDragHandler;
import com.eas.client.model.gui.view.entities.EntityView;
import com.eas.client.model.gui.view.model.ApplicationModelView;
import com.eas.designer.application.HandlerRegistration;
import com.eas.designer.explorer.model.nodes.EntityNode;
import com.eas.designer.explorer.model.nodes.FieldNode;
import com.eas.designer.explorer.model.windows.ModelInspector;
import com.eas.designer.explorer.model.windows.QueriesDragHandler;
import com.eas.designer.explorer.model.windows.QueryDocumentJumper;
import com.eas.designer.explorer.selectors.QueriesSelector;
import com.eas.designer.explorer.selectors.TablesSelector;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.ErrorManager;
import org.openide.awt.UndoRedo;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;

/**
 * Top component which displays model
 */
public final class PlatypusModuleDatamodelView extends TopComponent implements MultiViewElement {

    protected class NodeSelectionListener implements PropertyChangeListener {

        protected boolean processing;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName()) ||
                    ExplorerManager.PROP_NODE_CHANGE.equals(evt.getPropertyName())) {
                if (!processing) {
                    processing = true;
                    try {
                        Node[] nodes = ModelInspector.getInstance().getExplorerManager().getSelectedNodes();
                        appModelEditor.getModelView().silentClearSelection();
                        appModelEditor.getModelView().clearEntitiesFieldsSelection();
                        for (Node node : nodes) {
                            EntityView<ApplicationDbEntity> ev;
                            if (node instanceof EntityNode<?>) {
                                ev = appModelEditor.getModelView().getEntityView(((EntityNode<ApplicationDbEntity>) node).getEntity());
                                appModelEditor.getModelView().silentSelectView(ev);
                            } else if (node instanceof FieldNode) {
                                ev = appModelEditor.getModelView().getEntityView(((EntityNode<ApplicationDbEntity>) node.getParentNode()).getEntity());
                                FieldNode fieldNode = (FieldNode) node;
                                if ((fieldNode.getField() instanceof Parameter) && !(ev.getEntity() instanceof ApplicationParametersEntity)) {
                                    ev.addSelectedParameter((Parameter) fieldNode.getField());
                                } else {
                                    ev.addSelectedField(fieldNode.getField());
                                }
                            }
                        }
                        setActivatedNodes(nodes);
                    } finally {
                        processing = false;
                    }
                }
            }
        }
    }
    public static final String PLATYPUS_MODULES_GROUP_NAME = "PlatypusModel";
    static final long serialVersionUID = 53142032923497728L;
    protected transient JPanel toolsPnl = new JPanel();
    protected transient MultiViewElementCallback callback;
    protected transient ApplicationModelEditorView appModelEditor;
    protected transient NodeSelectionListener exlorerSelectionListener = new NodeSelectionListener();
    protected transient HandlerRegistration clientChangeListener;
    protected transient ExplorerManager explorerManager;
    protected PlatypusModuleDataObject dataObject;

    public PlatypusModuleDatamodelView() {
        super();
    }

    public PlatypusModuleDatamodelView(PlatypusModuleDataObject aDataObject) throws Exception {
        this();
        setDataObject(aDataObject);
    }

    protected void setDataObject(PlatypusModuleDataObject aDataObject) throws Exception {
        dataObject = aDataObject;
        // Hack! NetBeans doesn't properly handle activated nodes in multi view's elements
        // So, we need to use dummy explorer manager and it's lookup, associated with this multiview element TopComponent
        // to produce satisfactory events.
        explorerManager = new ExplorerManager();
        associateLookup(new ProxyLookup(new Lookup[]{
                    ExplorerUtils.createLookup(explorerManager, getActionMap()),}));

        /*
         associateLookup(Lookups.proxy(new Lookup.Provider() {
         @Override
         public Lookup getLookup() {
         return Lookups.fixed(getActivatedNodes() != null ? (Object[]) getActivatedNodes() : new Object[]{});
         }
         }));
         */
        initModelEditorView();
        clientChangeListener = dataObject.addClientChangeListener(new Runnable() {
            @Override
            public void run() {
                try {
                    initModelEditorView();
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        });
    }

    public ApplicationModelView getModelView() {
        return appModelEditor.getModelView();
    }

    protected void initModelEditorView() throws Exception {
        removeAll();
        setLayout(new BorderLayout());
        if (dataObject.getClient() != null) {
            TablesSelector tablesSelector = new TablesSelector(dataObject.getAppRoot(), dataObject.getClient(), true, true, NbBundle.getMessage(PlatypusModuleDatamodelView.class, "LBL_PlatypusModule_View_Name"), PlatypusModuleDatamodelView.this);
            QueriesSelector queriesSelector = new QueriesSelector(dataObject.getAppRoot());
            appModelEditor = new ApplicationModelEditorView(tablesSelector, queriesSelector);
            appModelEditor.getModelView().addEntityViewDoubleClickListener(new QueryDocumentJumper<ApplicationDbEntity>());
            ApplicationDbModel model = dataObject.getModel();
            appModelEditor.setModel(model);
            appModelEditor.setBorder(new EmptyBorder(0, 0, 0, 0));
            add(appModelEditor, BorderLayout.CENTER);
            TransferHandler modelViewOriginalTrnadferHandler = appModelEditor.getModelView().getTransferHandler();
            if (modelViewOriginalTrnadferHandler instanceof ModelViewDragHandler) {
                appModelEditor.getModelView().setTransferHandler(new QueriesDragHandler((ModelViewDragHandler) modelViewOriginalTrnadferHandler, appModelEditor.getModelView()));
            }

            appModelEditor.setUndo(new UndoManager() {
                @Override
                public synchronized boolean addEdit(UndoableEdit anEdit) {
                    PlatypusModuleSupport ps = dataObject.getLookup().lookup(PlatypusModuleSupport.class);
                    ps.notifyModified();
                    ps.getModelUndo().undoableEditHappened(new UndoableEditEvent(this, anEdit));
                    return true;
                }
            });
            appModelEditor.getModelView().addModelSelectionListener(new ModelSelectionListener<ApplicationDbEntity>() {
                @Override
                public void selectionChanged(Set<ApplicationDbEntity> oldSelected, Set<ApplicationDbEntity> newSelected) {
                    try {
                        Node[] oldNodes = getActivatedNodes();
                        // Hack. When multi-view element with no any activated node is activated,
                        // NetBeans' property sheet stay with a node from previous multi-view element.
                        // So, we need to simulate non-empty activated nodes and take this into account
                        // here.
                        if (oldNodes != null && oldNodes.length == 1 && oldNodes[0] == (dataObject.getClient() != null ? dataObject.getModelNode() : dataObject.getNodeDelegate())) {
                            oldNodes = new Node[]{};
                        }
                        Node[] newNodes = ModelInspector.convertSelectedToNodes(dataObject.getModelNode(), oldNodes, oldSelected, newSelected);
                        // Hack! NetBeans doesn't properly handle activated nodes in multi view's elements
                        // So, we need to use dummy explorer manager and it's lookup, associated with this multiview element TopComponent
                        // to produce satisfactory events.
                        explorerManager.setSelectedNodes(newNodes);
                        setActivatedNodes(newNodes);
                    } catch (Exception ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }

                @Override
                public void selectionChanged(List<SelectedParameter<ApplicationDbEntity>> aParameters, List<SelectedField<ApplicationDbEntity>> aFields) {
                    try {
                        Node[] oldNodes = getActivatedNodes();
                        // Hack. When multi-view element with no any activated node is activated,
                        // NetBeans' property sheet stay with a node from previous multi-view element.
                        // So, we need to simulate non-empty activated nodes and take this into account
                        // here.
                        if (oldNodes != null && oldNodes.length == 1 && oldNodes[0] == (dataObject.getClient() != null ? dataObject.getModelNode() : dataObject.getNodeDelegate())) {
                            oldNodes = new Node[]{};
                        }
                        Node[] newNodes = ModelInspector.convertSelectedToNodes(dataObject.getModelNode(), oldNodes, aParameters, aFields);
                        // Hack! NetBeans doesn't properly handle activated nodes in multi view's elements
                        // So, we need to use dummy explorer manager and it's lookup, associated with this multiview element TopComponent
                        // to produce satisfactory events.
                        explorerManager.setSelectedNodes(newNodes);
                        setActivatedNodes(newNodes);
                    } catch (Exception ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }

                @Override
                public void selectionChanged(Collection<Relation<ApplicationDbEntity>> clctn, Collection<Relation<ApplicationDbEntity>> clctn1) {
                }
            });
            explorerManager.setRootContext(dataObject.getModelNode());
            componentActivated();
        } else {
            explorerManager.setRootContext(dataObject.getNodeDelegate());
            add(dataObject.getProject().generateDbPlaceholder(), BorderLayout.CENTER);
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws java.io.IOException {
        super.writeExternal(out);
        out.writeObject(dataObject);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        try {
            setDataObject((PlatypusModuleDataObject) in.readObject());
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void requestVisible() {
        if (callback != null) {
            callback.requestVisible();
        } else {
            super.requestVisible();
        }
    }

    @Override
    public void requestActive() {
        if (callback != null) {
            callback.requestActive();
        } else {
            super.requestActive();
        }
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
        updateName();
    }

    //Let's show the same popup menu items as for source editor
    @Override
    public Action[] getActions() {
        List<Action> actions = new ArrayList<>(Arrays.asList(super.getActions()));
        // XXX nicer to use MimeLookup for type-specific actions, but not easy; see org.netbeans.modules.editor.impl.EditorActionsProvider
        actions.add(null);
        actions.addAll(Utilities.actionsForPath("Editors/TabActions")); //NOI18N
        return actions.toArray(new Action[actions.size()]);
    }

    public void updateName() {
        setHtmlDisplayName(getHtmlDisplayName());
        setDisplayName(getDisplayName());
        if (callback != null) {
            callback.getTopComponent().setHtmlDisplayName(getHtmlDisplayName());
            callback.updateTitle(dataObject.getName());
        }
    }

    @Override
    public String getHtmlDisplayName() {
        String ldisplayName = "<html>" + dataObject.getName();
        if (dataObject.isModified()) {
            ldisplayName = "<html><b>" + dataObject.getName() + "</b>";
        }
        return ldisplayName;
    }

    @Override
    public void componentActivated() {
        try {
            if (dataObject.isValid() && dataObject.getClient() != null) {
                ModelInspector.getInstance().setNodesReflector(exlorerSelectionListener);
                ModelInspector.getInstance().setViewData(new ModelInspector.ViewData<>(getModelView(), getUndoRedo(), dataObject.getModelNode()));
                WindowManager wm = WindowManager.getDefault();
                final TopComponentGroup group = wm.findTopComponentGroup(PLATYPUS_MODULES_GROUP_NAME);
                if (group != null) {
                    group.open();
                }
                // Hack! NetBeans doesn't properly handle activated nodes in multi view's elements
                // So, we need to use dummy explorer manager and it's lookup, associated with this multiview element TopComponent
                // to produce satisfactory events.
                // Moreover, this code shouldn't be here at all, but NetBeans, doesn't refresh
                // property sheet on multi-view's activated element change, so we need to simulate
                // activated and selected nodes change.
                Node[] activated = getActivatedNodes();
                Node[] empty = new Node[]{};
                explorerManager.setSelectedNodes(empty);
                setActivatedNodes(empty);
                // Hack. When multi-view element with no any activated node is activated,
                // NetBeans' property sheet stay with a node from previous multi-view element.
                // So, we need to simulate non-empty activated nodes.
                if (activated == null || activated.length <= 0) {
                    activated = new Node[]{dataObject.getClient() != null ? dataObject.getModelNode() : dataObject.getNodeDelegate()};
                }
                explorerManager.setSelectedNodes(activated);
                setActivatedNodes(activated);
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        super.componentActivated();
    }

    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
        if (dataObject.getClient() != null && ModelInspector.getInstance().getViewData().getModelView() == getModelView()) {
            ModelInspector.getInstance().setNodesReflector(null);
            ModelInspector.getInstance().setViewData(null);
            WindowManager wm = WindowManager.getDefault();
            final TopComponentGroup group = wm.findTopComponentGroup(PLATYPUS_MODULES_GROUP_NAME); // NOI18N
            if (group != null) {
                group.close();
            }
        }
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
        dataObject.getLookup().lookup(PlatypusModuleSupport.class).shrink();
        if (clientChangeListener != null) {
            clientChangeListener.remove();
        }
    }

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return toolsPnl;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback aCallback) {
        callback = aCallback;
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Override
    public UndoRedo getUndoRedo() {
        return dataObject.getLookup().lookup(PlatypusModuleSupport.class).getModelUndo();
    }

    @Override
    protected String preferredID() {
        return PlatypusModuleDatamodelDescription.MODULE_DATAMODEL_VIEW_NAME;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }
}