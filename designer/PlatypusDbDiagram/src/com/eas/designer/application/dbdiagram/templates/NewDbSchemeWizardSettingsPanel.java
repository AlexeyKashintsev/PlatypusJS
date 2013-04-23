/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.designer.application.dbdiagram.templates;

import com.eas.client.cache.PlatypusFiles;
import com.eas.designer.explorer.project.PlatypusProject;
import java.awt.Component;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;

/**
 *
 * @author mg
 */
public class NewDbSchemeWizardSettingsPanel implements WizardDescriptor.Panel,
        WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {

    public static final String CONNECTION_PROP_NAME = "connectionId";
    public static final String SCHEMA_PROP_NAME = "schema";
    protected PlatypusProject project;
    private WizardDescriptor wizardDescriptor;
    protected DbSchemeSettingsVisualPanel component;

    public NewDbSchemeWizardSettingsPanel(PlatypusProject aProject) {
        super();
        project = aProject;
    }

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new DbSchemeSettingsVisualPanel(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void readSettings(Object settings) {
        try {
            wizardDescriptor = (WizardDescriptor) settings;
            component.read(wizardDescriptor);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    @Override
    public void storeSettings(Object settings) {
        try {
            WizardDescriptor d = (WizardDescriptor) settings;
            component.store(d);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    @Override
    public boolean isValid() {
        try {
            getComponent();
            return component.valid(wizardDescriptor);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
            return false;
        }
    }
    private final Set<ChangeListener> listeners = new HashSet<>(1); // or can use ChangeSupport in NB 6.0

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        Set<ChangeListener> ls;
        synchronized (listeners) {
            ls = new HashSet<>(listeners);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : ls) {
            l.stateChanged(ev);
        }
    }

    @Override
    public void validate() throws WizardValidationException {
    }

    @Override
    public boolean isFinishPanel() {
        return false;
    }

    public boolean isConnectionElement(FileObject aFile) throws Exception {
        return PlatypusFiles.CONNECTION_EXTENSION.equalsIgnoreCase(aFile.getExt());
    }

    public PlatypusProject getProject() {
        return project;
    }
}