/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * RunDialog.java
 *
 * Created on Jul 13, 2009, 6:18:15 PM
 */
package com.eas.client.forms;

import com.jeta.forms.components.panel.FormPanel;
import java.awt.BorderLayout;
import javax.swing.WindowConstants;

/**
 *
 * @author pk
 */
public class RunDialog extends javax.swing.JDialog {

    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;
    private FormPanel formPanel;

    /** Creates new form RunDialog */
    public RunDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus() {
        return returnStatus;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
    }// </editor-fold>//GEN-END:initComponents

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        switch (getDefaultCloseOperation()) {
            case WindowConstants.HIDE_ON_CLOSE:
                setVisible(false);
                break;
            case WindowConstants.DISPOSE_ON_CLOSE:
                dispose();
                break;
            default:
                break;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                RunDialog dialog = new RunDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    private int returnStatus = RET_CANCEL;

    public FormPanel getFormPanel() {
        return formPanel;
    }

    protected void configureContentPane() {
        getContentPane().removeAll();
        getContentPane().add(formPanel, BorderLayout.CENTER);
        ///////scrollPane.setViewportView(formPanel);
        //pnlContent.add(formPanel, BorderLayout.CENTER);
    }

    protected void processFormPanel() {
        formPanel.setVisible(true);
        formPanel.initFromGridView(this);
        doLayout();
        //formPanel.getForm().postInitialize(formPanel);
    }

    protected void repaintFrame() {
        repaint();
    }

    public void setFormPanel(FormPanel aFormPanel) {
        formPanel = aFormPanel;
        configureContentPane();
        processFormPanel();
        repaintFrame();
    }
}