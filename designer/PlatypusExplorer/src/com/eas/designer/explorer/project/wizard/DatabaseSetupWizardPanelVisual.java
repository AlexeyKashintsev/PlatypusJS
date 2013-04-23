/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.designer.explorer.project.wizard;

import com.eas.client.ClientFactory;
import com.eas.client.settings.DbConnectionSettings;
import com.eas.client.Client;
import com.eas.designer.explorer.project.ui.BuildJdbcUrlPanel;
import com.eas.designer.explorer.project.ui.ProjectDatabaseCustomizer;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import java.util.Properties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

public class DatabaseSetupWizardPanelVisual extends JPanel implements DocumentListener {

    // wizard descriptor properties
    public static final String PROP_SUCCESS = "successfulConnection";
    public static final String PROP_SETTINGS = "DbConnectionSettings";
    // settings props
    public static final String PROP_USER = "user";
    public static final String PROP_PASSWORD = "password";
    public static final String PROP_SCHEMA = "schema";
    protected static final String defaultConnectionErrorMsg = NbBundle.getMessage(DatabaseSetupWizardPanelVisual.class, "DatabaseSetupWizardPanelVisual.connectionNotTried");
    protected String connectionErrorMsg = defaultConnectionErrorMsg;
    private DatabaseSetupWizardPanel panel;

    public DatabaseSetupWizardPanelVisual(DatabaseSetupWizardPanel aWizardStep) {
        initComponents();
        panel = aWizardStep;
        // Register listener on the textFields to make the automatic updates
        jdbcUrlTextField.getDocument().addDocumentListener(this);
        dbUserTextField.getDocument().addDocumentListener(this);
        dbSchemaTextField.getDocument().addDocumentListener(this);
        dbUserPasswordTextField.getDocument().addDocumentListener(this);
    }

    public String getJdbcUrl() {
        return jdbcUrlTextField.getText();
    }

    public String getUser() {
        return dbUserTextField.getText();
    }

    public String getPassword() {
        return dbUserPasswordTextField.getText();
    }

    public String getSchema() {
        return dbSchemaTextField.getText();
    }

    private DbConnectionSettings constructConnectionSettings() throws Exception {
        String url = getJdbcUrl();
        String user = getUser();
        String password = getPassword();
        String schema = getSchema();
        DbConnectionSettings dbSettings = new DbConnectionSettings();
        Properties props = new Properties();
        props.put(PROP_USER, user);
        props.put(PROP_PASSWORD, password);
        props.put(PROP_SCHEMA, schema);
        dbSettings.setUrl(url);
        dbSettings.setInitSchema(false);
        dbSettings.setInfo(props);
        return dbSettings;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jdbcUrlLabel = new javax.swing.JLabel();
        jdbcUrlTextField = new javax.swing.JTextField();
        dbUserLabel = new javax.swing.JLabel();
        dbUserTextField = new javax.swing.JTextField();
        dbUserPasswordLabel = new javax.swing.JLabel();
        dbSchemaLabel = new javax.swing.JLabel();
        dbSchemaTextField = new javax.swing.JTextField();
        btnTestConnection = new javax.swing.JButton();
        btnBuildJdbcUrl = new javax.swing.JButton();
        dbUserPasswordTextField = new javax.swing.JPasswordField();

        jdbcUrlLabel.setLabelFor(jdbcUrlTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jdbcUrlLabel, org.openide.util.NbBundle.getMessage(DatabaseSetupWizardPanelVisual.class, "DatabaseSetupWizardPanelVisual.projectNameLabel.text")); // NOI18N

        dbUserLabel.setLabelFor(dbUserTextField);
        org.openide.awt.Mnemonics.setLocalizedText(dbUserLabel, org.openide.util.NbBundle.getMessage(DatabaseSetupWizardPanelVisual.class, "DatabaseSetupWizardPanelVisual.projectLocationLabel.text")); // NOI18N

        dbUserPasswordLabel.setLabelFor(dbUserPasswordTextField);
        org.openide.awt.Mnemonics.setLocalizedText(dbUserPasswordLabel, org.openide.util.NbBundle.getMessage(DatabaseSetupWizardPanelVisual.class, "DatabaseSetupWizardPanelVisual.createdFolderLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dbSchemaLabel, org.openide.util.NbBundle.getMessage(DatabaseSetupWizardPanelVisual.class, "DatabaseSetupWizardPanelVisual.dbSchemaLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnTestConnection, org.openide.util.NbBundle.getMessage(DatabaseSetupWizardPanelVisual.class, "DatabaseSetupWizardPanelVisual.btnTestConnection.text")); // NOI18N
        btnTestConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTestConnectionActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnBuildJdbcUrl, org.openide.util.NbBundle.getMessage(DatabaseSetupWizardPanelVisual.class, "DatabaseSetupWizardPanelVisual.btnBuildJdbcUrl.text")); // NOI18N
        btnBuildJdbcUrl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuildJdbcUrlActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnTestConnection, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dbUserLabel)
                            .addComponent(jdbcUrlLabel)
                            .addComponent(dbSchemaLabel)
                            .addComponent(dbUserPasswordLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dbUserTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                            .addComponent(dbSchemaTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                            .addComponent(jdbcUrlTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnBuildJdbcUrl)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(dbUserPasswordTextField))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jdbcUrlLabel)
                    .addComponent(jdbcUrlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnBuildJdbcUrl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dbSchemaLabel)
                    .addComponent(dbSchemaTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dbUserLabel)
                    .addComponent(dbUserTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dbUserPasswordLabel)
                    .addComponent(dbUserPasswordTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnTestConnection))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnTestConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTestConnectionActionPerformed
        try {
            DbConnectionSettings dbSettings = constructConnectionSettings();
            try {
                Client client = ClientFactory.getInstance(dbSettings);
                client.shutdown();
                connectionErrorMsg = null;
            } catch (Exception ex) {
                connectionErrorMsg = ex.getLocalizedMessage();
            }
            updateTexts(null);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_btnTestConnectionActionPerformed

    private void btnBuildJdbcUrlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuildJdbcUrlActionPerformed
        BuildJdbcUrlPanel p = new BuildJdbcUrlPanel();
        DialogDescriptor d = new DialogDescriptor(p, p.getTitle());
        Object result = DialogDisplayer.getDefault().notify(d);
        if (DialogDescriptor.OK_OPTION.equals(result)) {
            jdbcUrlTextField.setText(p.getJdbcUrl());
        }
    }//GEN-LAST:event_btnBuildJdbcUrlActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuildJdbcUrl;
    private javax.swing.JButton btnTestConnection;
    private javax.swing.JLabel dbSchemaLabel;
    private javax.swing.JTextField dbSchemaTextField;
    private javax.swing.JLabel dbUserLabel;
    private javax.swing.JLabel dbUserPasswordLabel;
    private javax.swing.JPasswordField dbUserPasswordTextField;
    private javax.swing.JTextField dbUserTextField;
    private javax.swing.JLabel jdbcUrlLabel;
    private javax.swing.JTextField jdbcUrlTextField;
    // End of variables declaration//GEN-END:variables

    @Override
    public void addNotify() {
        super.addNotify();
        jdbcUrlTextField.requestFocus();
    }

    public boolean valid(WizardDescriptor wd) throws Exception {
        String url = getJdbcUrl();
        String user = getUser();
        String password = getPassword();
        String schema = getSchema();
        /*
         if (url == null || url.isEmpty()
         || user == null || user.isEmpty()
         || password == null || password.isEmpty()
         || schema == null || schema.isEmpty()) {
         wd.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(DatabaseSetupWizardPanelVisual.class, "DatabaseSetupWizardPanelVisual.missingSettings"));
         return false;
         }
         */
        if (connectionErrorMsg != null) {
            wd.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, connectionErrorMsg);
        } else {
            wd.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, NbBundle.getMessage(DatabaseSetupWizardPanelVisual.class, "DatabaseSetupWizardPanelVisual.connectionEstablished"));
        }
        return true;
    }

    void store(WizardDescriptor wd) throws Exception {
        wd.putProperty(PROP_SETTINGS, constructConnectionSettings());
    }

    void read(WizardDescriptor wd) {
        DbConnectionSettings dbSettings = (DbConnectionSettings) wd.getProperty(PROP_SETTINGS);
        if (dbSettings != null) {
            jdbcUrlTextField.setText(dbSettings.getUrl());
            dbUserTextField.setText(dbSettings.getInfo().getProperty(PROP_USER));
            dbUserPasswordTextField.setText(dbSettings.getInfo().getProperty(PROP_PASSWORD));
            dbSchemaTextField.setText(dbSettings.getInfo().getProperty(PROP_SCHEMA));
        }
    }

    // Implementation of DocumentListener --------------------------------------
    @Override
    public void changedUpdate(DocumentEvent e) {
        connectionErrorMsg = defaultConnectionErrorMsg;
        updateTexts(e);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        connectionErrorMsg = defaultConnectionErrorMsg;
        updateTexts(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        connectionErrorMsg = defaultConnectionErrorMsg;
        updateTexts(e);
    }

    /**
     * Handles changes in the url username password and schema
     */
    private void updateTexts(DocumentEvent e) {
        if (panel != null) {
            panel.fireChangeEvent(); // Notify that the panel changed
        }
    }
}