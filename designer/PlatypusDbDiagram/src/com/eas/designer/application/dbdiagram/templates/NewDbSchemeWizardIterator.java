/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.designer.application.dbdiagram.templates;

import com.eas.designer.application.indexer.IndexerQuery;
import com.eas.designer.explorer.files.wizard.NewApplicationElementWizardIterator;
import com.eas.designer.explorer.files.wizard.NewApplicationElementWizardNamePanel;
import com.eas.designer.explorer.project.PlatypusProject;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author mg
 */
public class NewDbSchemeWizardIterator extends NewApplicationElementWizardIterator {

    public static final String PLATYPUS_DIAGRAM_DB_ID_PARAM_NAME = "datamodelDbId";
    public static final String PLATYPUS_DIAGRAM_SCHEMA_PARAM_NAME = "datamodelSchemaName";

    public NewDbSchemeWizardIterator() {
        super();
    }

    @TemplateRegistration(
            folder = "Platypus application elements",
    position = 300,
    content = "PlatypusDbDiagramTemplate.pd",
    displayName = "#Templates/Other/PlatypusDbDiagramTemplate",
    description = "Diagram.html",
    scriptEngine = "freemarker")
    public static NewApplicationElementWizardIterator createIterator() {
        return new NewDbSchemeWizardIterator();
    }

    @Override
    protected WizardDescriptor.Panel<WizardDescriptor>[] createPanels(WizardDescriptor wiz) {
        try {
            Project project = Templates.getProject(wiz);
            assert project instanceof PlatypusProject;
            PlatypusProject pProject = (PlatypusProject) project;
            return new WizardDescriptor.Panel[]{new NewDbSchemeWizardSettingsPanel(pProject), new NewApplicationElementWizardNamePanel()};
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
            return new WizardDescriptor.Panel[]{};
        }
    }

    @Override
    protected String[] createSteps() {
        return new String[]{
                    NbBundle.getMessage(NewApplicationElementWizardIterator.class, "LBL_ChooseFileTypeStep"),
                    NbBundle.getMessage(NewDbSchemeWizardSettingsPanel.class, "LBL_DbSchemeSettingsStep"),
                    NbBundle.getMessage(NewApplicationElementWizardIterator.class, "LBL_CreateApplicationElementStep")
                };
    }

    @Override
    protected Map<String, String> achieveParameters(WizardDescriptor aWiz) {
        Map<String, String> parameters = new HashMap<>();
        String dbId = null;
        Object oConnection = aWiz.getProperty(NewDbSchemeWizardSettingsPanel.CONNECTION_PROP_NAME);
        if (oConnection instanceof String) {
            dbId = (String) oConnection;
        } else if (oConnection instanceof FileObject) {
            dbId = IndexerQuery.file2AppElementId((FileObject) oConnection);
        }
        String schema = (String) aWiz.getProperty(NewDbSchemeWizardSettingsPanel.SCHEMA_PROP_NAME);
        parameters.put(PLATYPUS_DIAGRAM_DB_ID_PARAM_NAME, String.valueOf(dbId));
        if (schema != null) {
            parameters.put(PLATYPUS_DIAGRAM_SCHEMA_PARAM_NAME, PLATYPUS_DIAGRAM_SCHEMA_PARAM_NAME + "=\"" + schema + "\"");
        } else {
            parameters.put(PLATYPUS_DIAGRAM_SCHEMA_PARAM_NAME, "");
        }
        return parameters;
    }
}