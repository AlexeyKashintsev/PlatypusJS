/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.designer.explorer.j2ee;

import com.eas.designer.explorer.project.PlatypusProject;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author vv
 */
public class PlatypusWebModule extends J2eeModuleProvider implements J2eeModuleImplementation2,
        ModuleChangeReporter,
        EjbChangeDescriptor {

    public static final String MODULE_DEFAULT_URL = "/WebApplication3"; //NOI18N
    public static final String WEB_DIRECTORY = "web"; //NOI18N
    public static final String WEB_INF_DIRECTORY = "WEB-INF"; //NOI18N
    public static final String META_INF_DIRECTORY = "META-INF"; //NOI18N
    protected final PlatypusProject project;
    private J2eeModule j2eeModule;
    private PropertyChangeSupport propertyChangeSupport;

    public PlatypusWebModule(PlatypusProject aProject) {
        super();
        project = aProject;
    }

    @Override
    public J2eeModule getJ2eeModule() {
        if (j2eeModule == null) {
            j2eeModule = J2eeModuleFactory.createJ2eeModule(this);
        }
        return j2eeModule;
    }

    @Override
    public ModuleChangeReporter getModuleChangeReporter() {
        return this;
    }

    @Override
    public void setServerInstanceID(String severInstanceID) {
        project.getSettings().setJ2eeServerId(severInstanceID);
    }

    @Override
    public String getServerInstanceID() {
        return "tomcat70:home=/home/vv/apache-tomcat-7.0.39";//project.getSettings().getJ2eeServerId();
    }

    @Override
    public String getServerID() {
        String inst = getServerInstanceID();
        String id;
        if (inst != null) {
            try {
                id = Deployment.getDefault().getServerInstance(inst).getServerID();
                return id;
            } catch (InstanceRemovedException ex) {
                return null;
            }
        }
        return null;
    }

    @Override
    public J2eeModule.Type getModuleType() {
        return J2eeModule.Type.WAR;
    }

    @Override
    public String getModuleVersion() {
        return WebApp.VERSION_3_0;
    }

    @Override
    public String getUrl() {
        return MODULE_DEFAULT_URL;
    }

    @Override
    public FileObject getArchive() throws IOException {
        return null;
    }

    @Override
    public Iterator<J2eeModule.RootedEntry> getArchiveContents() throws java.io.IOException {
        FileObject content = getContentDirectory();
        content.refresh();
        return new IT(content);
    }

    @Override
    public FileObject getContentDirectory() throws IOException {
        return project.getProjectDirectory().getFileObject(WEB_DIRECTORY);
    }

    @Override
    public <T> MetadataModel<T> getMetadataModel(Class<T> arg0) {
        return null;
    }

    @Override
    public File getResourceDirectory() {
        return null;
    }

    @Override
    public File getDeploymentConfigurationFile(String name) {
        try {
            String webInfRelativePath = formatRelativePath(WEB_INF_DIRECTORY);
            String metaInfRelativePath = formatRelativePath(META_INF_DIRECTORY);
            FileObject dir;
            String path;
            if (name.startsWith(webInfRelativePath)) {
                path = name.substring(webInfRelativePath.length());
                dir = getWebInfDir();
            } else if (name.startsWith(metaInfRelativePath)) {
                path = name.substring(webInfRelativePath.length());
                dir = getMetaInfDir();
            } else {
                return null;
            }
            return FileUtil.toFile(dir.getFileObject(path));
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);         
        }
        return null;
    }
    
    private String formatRelativePath(String directoryName) {
        return directoryName  + "/"; //NOI18
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        getPropertyChangeSupport().addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        getPropertyChangeSupport().removePropertyChangeListener(listener);
    }

    @Override
    public EjbChangeDescriptor getEjbChanges(long timestamp) {
        return this;
    }

    @Override
    public boolean isManifestChanged(long timestamp) {
        return false;
    }

    public FileObject getWebInfDir() throws IOException {
        return getContentDirectory().getFileObject(WEB_INF_DIRECTORY);
    }

    public FileObject getMetaInfDir() throws IOException {
        return getContentDirectory().getFileObject(META_INF_DIRECTORY);
    }

    private synchronized PropertyChangeSupport getPropertyChangeSupport() {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        return propertyChangeSupport;
    }

    @Override
    public boolean ejbsChanged() {
        return false;
    }

    @Override
    public String[] getChangedEjbs() {
        return new String[]{};
    }

    private static class IT implements Iterator<J2eeModule.RootedEntry> {

        ArrayList<FileObject> ch;
        FileObject root;

        private IT(FileObject f) {
            this.ch = new ArrayList<>();
            ch.add(f);
            this.root = f;
        }

        @Override
        public boolean hasNext() {
            return !ch.isEmpty();
        }

        @Override
        public J2eeModule.RootedEntry next() {
            FileObject f = ch.get(0);
            ch.remove(0);
            if (f.isFolder()) {
                f.refresh();
                FileObject chArr[] = f.getChildren();
                ch.addAll(Arrays.asList(chArr));
            }
            return new FSRootRE(root, f);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class FSRootRE implements J2eeModule.RootedEntry {

        FileObject f;
        FileObject root;

        FSRootRE(FileObject root, FileObject f) {
            this.f = f;
            this.root = root;
        }

        @Override
        public FileObject getFileObject() {
            return f;
        }

        @Override
        public String getRelativePath() {
            return FileUtil.getRelativePath(root, f);
        }
    }
}