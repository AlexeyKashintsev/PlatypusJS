/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.designer.application.module.actions;

import com.eas.designer.application.indexer.IndexerQuery;
import com.eas.designer.application.module.PlatypusModuleSupport;
import com.eas.designer.explorer.project.PlatypusProject;
import com.eas.designer.explorer.project.ProjectRunner;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

@ActionID(category = "File",
id = "com.eas.designer.application.module.actions.DebugAction")
@ActionRegistration(displayName = "#CTL_DebugAction")
@ActionReferences({
    @ActionReference(path = "Loaders/text/javascript/Actions", position = 162)
})
public final class DebugAction implements ActionListener {

    private final PlatypusModuleSupport context;

    public DebugAction(PlatypusModuleSupport aContext) {
        super();
        context = aContext;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        Project project = FileOwnerQuery.getOwner(context.getDataObject().getPrimaryFile());
        if (project instanceof PlatypusProject) {
            try {
                PlatypusProject pProject = (PlatypusProject) project;
                String appElementId = IndexerQuery.file2AppElementId(context.getDataObject().getPrimaryFile());
                ProjectRunner.debug(pProject, appElementId);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
}