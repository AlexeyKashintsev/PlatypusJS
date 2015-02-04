/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SettingsDialog.java
 *
 * Created on 15.10.2009, 13:50:40
 */
package com.eas.client.model.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;

/**
 *
 * @author mg
 */
public class SettingsDialog extends javax.swing.JDialog {

    public interface Checker {

        public boolean check();
    }

    protected class OkAction extends AbstractAction {

        protected Checker checker;

        public OkAction(Checker aChecker) {
            checker = aChecker;
            putValue(Action.NAME, "OK");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (checker.check()) {
                okClose = true;
                dispose();
            }
        }
    }

    protected class CancelAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            okClose = false;
            dispose();
        }
    }
    protected boolean okClose;
    protected JComponent view;

    /**
     * Creates new form SettingsDialog
     */
    public SettingsDialog(Frame parent, JComponent aView, boolean modal, Checker aChecker) {
        super(parent, modal);
        initComponents();
        getContentPane().add(aView, BorderLayout.CENTER);
        view = aView;
        Action ac = new OkAction(aChecker);
        btnOk.setAction(ac);
    }

    public boolean isOkClose() {
        return okClose;
    }

    public void setOkClose(boolean okClose) {
        this.okClose = okClose;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bottomPanel = new javax.swing.JPanel();
        btnCancel = new javax.swing.JButton();
        btnOk = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);

        bottomPanel.setPreferredSize(new java.awt.Dimension(245, 45));

        btnCancel.setAction(new CancelAction());
        btnCancel.setText(DatamodelDesignUtils.getString("btnCancel")); // NOI18N

        btnOk.setText("OK");
        btnOk.setPreferredSize(btnCancel.getPreferredSize());

        javax.swing.GroupLayout bottomPanelLayout = new javax.swing.GroupLayout(bottomPanel);
        bottomPanel.setLayout(bottomPanelLayout);
        bottomPanelLayout.setHorizontalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, bottomPanelLayout.createSequentialGroup()
                .addContainerGap(209, Short.MAX_VALUE)
                .addComponent(btnOk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCancel)
                .addContainerGap())
        );
        bottomPanelLayout.setVerticalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, bottomPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancel))
                .addContainerGap())
        );

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOk;
    // End of variables declaration//GEN-END:variables
}
