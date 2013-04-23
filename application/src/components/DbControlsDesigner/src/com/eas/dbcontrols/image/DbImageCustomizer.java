/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DbImageCustomizer.java
 *
 * Created on 18.05.2009, 10:18:04
 */
package com.eas.dbcontrols.image;

import com.eas.client.datamodel.ModelElementRef;
import com.eas.client.datamodel.gui.selectors.ModelElementSelector;
import com.eas.dbcontrols.DbControlCustomizer;
import com.eas.dbcontrols.DbControlDesignInfo;
import com.eas.dbcontrols.DbControlsDesignUtils;
import com.eas.dbcontrols.actions.DmElementClearAction;
import com.eas.dbcontrols.actions.DmElementSelectAction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mg
 */
public class DbImageCustomizer extends DbControlCustomizer {

    protected final void fillActionMap() {
        getActionMap().put(DbImageFieldSelectAction.class.getSimpleName(), new DbImageFieldSelectAction());
        getActionMap().put(DbImageFieldClearAction.class.getSimpleName(), new DbImageFieldClearAction());
    }

    @Override
    protected void designInfoPropertyChanged(String aPropertyName, Object oldValue, Object newValue) {
        if (DbImageDesignInfo.DATAMODELELEMENT.equals(aPropertyName)) {
            updateDmElementView();
        } else if (DbImageDesignInfo.SELECTFUNCTION.equals(aPropertyName)) {
            updateSelectFunctionView();
        } else if (DbImageDesignInfo.SELECTONLY.equals(aPropertyName)) {
            updateSelectOnlyView();
        } else {
            updateView();
        }
        checkActionMap();
    }

    protected class DbImageFieldSelectAction extends DmElementSelectAction {

        @Override
        protected void processChangedDesignInfo(DbControlDesignInfo after) {
            ModelElementRef old = after.getDatamodelElement();
            ModelElementRef newRef = ModelElementSelector.selectDatamodelElement(getDatamodel(), old, ModelElementSelector.FIELD_SELECTION_SUBJECT, selectValidator, DbImageCustomizer.this, DbControlsDesignUtils.getLocalizedString("selectField"));
            if (newRef != null) {
                after.setDatamodelElement(newRef);
            }
        }
    }

    protected class DbImageFieldClearAction extends DmElementClearAction {

        @Override
        protected void processChangedDesignInfo(DbControlDesignInfo after) {
            after.setDatamodelElement(null);
        }
    }

    /** Creates new customizer DbImageCustomizer */
    public DbImageCustomizer() {
        super();
        fillActionMap();
        initComponents();
        txtDatamodelField.setText(DbControlsDesignUtils.getLocalizedString("notSelected"));

        comboSelectFunction.setModel(selectFunctionModel);
        cmbSelectFunction = comboSelectFunction;
        chkSelectOnly = checkSelectOnly;
    }

    @Override
    protected void updateView() {
        updateDmElementView();
        updateSelectFunctionView();
        updateSelectOnlyView();
    }

    private void updateDmElementView() {
        if (designInfo instanceof DbImageDesignInfo) {
            try {
                DbImageDesignInfo cinfo = (DbImageDesignInfo) designInfo;
                DbControlsDesignUtils.updateDmElement(getDatamodel(), cinfo.getDatamodelElement(), pnlDmField, fieldRenderer, txtDatamodelField, fieldsFont);
            } catch (Exception ex) {
                Logger.getLogger(DbImageDesignInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void updateSelectOnlyView() {
        DbImageDesignInfo cinfo = (DbImageDesignInfo) designInfo;
        checkSelectOnly.setSelected(cinfo.isSelectOnly());
    }

    private void updateSelectFunctionView() {
        DbImageDesignInfo cinfo = (DbImageDesignInfo) designInfo;
//        DbControlsDesignUtils.installScriptGenerator(comboSelectFunction, selectScriptGenerator);
        DbControlsDesignUtils.updateScriptItem(comboSelectFunction, cinfo.getSelectFunction());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlControl = new javax.swing.JPanel();
        pnlDmField = new javax.swing.JPanel();
        txtDatamodelField = new javax.swing.JTextField();
        pnlFieldButtons = new javax.swing.JPanel();
        btnDelField = new javax.swing.JButton();
        btnSelectField = new javax.swing.JButton();
        lblDmField = new javax.swing.JLabel();
        pnlSelectFunctionCustomizer = new javax.swing.JPanel();
        lblValueSelectHandler = new javax.swing.JLabel();
        pnlSelectFunction = new javax.swing.JPanel();
        btnDelSelectFunction = new javax.swing.JButton();
        comboSelectFunction = new javax.swing.JComboBox();
        checkSelectOnly = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout pnlControlLayout = new javax.swing.GroupLayout(pnlControl);
        pnlControl.setLayout(pnlControlLayout);
        pnlControlLayout.setHorizontalGroup(
            pnlControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 230, Short.MAX_VALUE)
        );
        pnlControlLayout.setVerticalGroup(
            pnlControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 2, Short.MAX_VALUE)
        );

        add(pnlControl, java.awt.BorderLayout.CENTER);

        pnlDmField.setLayout(new java.awt.BorderLayout());

        txtDatamodelField.setEditable(false);
        txtDatamodelField.setToolTipText(DbControlsDesignUtils.getLocalizedString("selectDatamodelField")); // NOI18N
        pnlDmField.add(txtDatamodelField, java.awt.BorderLayout.CENTER);

        pnlFieldButtons.setLayout(new java.awt.BorderLayout());

        btnDelField.setAction(getActionMap().get(DbImageFieldClearAction.class.getSimpleName() ));
        btnDelField.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pnlFieldButtons.add(btnDelField, java.awt.BorderLayout.CENTER);

        btnSelectField.setAction(getActionMap().get(DbImageFieldSelectAction.class.getSimpleName() ));
        btnSelectField.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pnlFieldButtons.add(btnSelectField, java.awt.BorderLayout.WEST);

        pnlDmField.add(pnlFieldButtons, java.awt.BorderLayout.EAST);

        lblDmField.setText(DbControlsDesignUtils.getLocalizedString("lblDmField")); // NOI18N
        pnlDmField.add(lblDmField, java.awt.BorderLayout.NORTH);

        lblValueSelectHandler.setText(DbControlsDesignUtils.getLocalizedString("lblSelectHandler")); // NOI18N

        pnlSelectFunction.setLayout(new java.awt.BorderLayout());

        btnDelSelectFunction.setAction(selectFunctionClearAction);
        btnDelSelectFunction.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pnlSelectFunction.add(btnDelSelectFunction, java.awt.BorderLayout.EAST);

        comboSelectFunction.setEditable(true);
        comboSelectFunction.setAction(selectFunctionChangeAction);
        pnlSelectFunction.add(comboSelectFunction, java.awt.BorderLayout.CENTER);

        checkSelectOnly.setAction(selectOnlyChangeAction);

        javax.swing.GroupLayout pnlSelectFunctionCustomizerLayout = new javax.swing.GroupLayout(pnlSelectFunctionCustomizer);
        pnlSelectFunctionCustomizer.setLayout(pnlSelectFunctionCustomizerLayout);
        pnlSelectFunctionCustomizerLayout.setHorizontalGroup(
            pnlSelectFunctionCustomizerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSelectFunctionCustomizerLayout.createSequentialGroup()
                .addComponent(lblValueSelectHandler)
                .addContainerGap(158, Short.MAX_VALUE))
            .addComponent(pnlSelectFunction, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
            .addGroup(pnlSelectFunctionCustomizerLayout.createSequentialGroup()
                .addComponent(checkSelectOnly)
                .addContainerGap())
        );
        pnlSelectFunctionCustomizerLayout.setVerticalGroup(
            pnlSelectFunctionCustomizerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSelectFunctionCustomizerLayout.createSequentialGroup()
                .addComponent(lblValueSelectHandler)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlSelectFunction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(checkSelectOnly))
        );

        pnlDmField.add(pnlSelectFunctionCustomizer, java.awt.BorderLayout.SOUTH);

        add(pnlDmField, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDelField;
    private javax.swing.JButton btnDelSelectFunction;
    private javax.swing.JButton btnSelectField;
    private javax.swing.JCheckBox checkSelectOnly;
    private javax.swing.JComboBox comboSelectFunction;
    private javax.swing.JLabel lblDmField;
    private javax.swing.JLabel lblValueSelectHandler;
    private javax.swing.JPanel pnlControl;
    private javax.swing.JPanel pnlDmField;
    private javax.swing.JPanel pnlFieldButtons;
    private javax.swing.JPanel pnlSelectFunction;
    private javax.swing.JPanel pnlSelectFunctionCustomizer;
    private javax.swing.JTextField txtDatamodelField;
    // End of variables declaration//GEN-END:variables
}