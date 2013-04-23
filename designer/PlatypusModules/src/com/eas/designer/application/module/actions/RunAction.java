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
id = "com.eas.designer.application.module.actions.RunAction")
@ActionRegistration(displayName = "#CTL_RunAction")
@ActionReferences({
    @ActionReference(path = "Loaders/text/javascript/Actions", position = 150, separatorBefore = 125, separatorAfter = 175)
})
public final class RunAction implements ActionListener {

    private final PlatypusModuleSupport context;

    public RunAction(PlatypusModuleSupport aContext) {
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
                ProjectRunner.run(pProject, appElementId);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
}