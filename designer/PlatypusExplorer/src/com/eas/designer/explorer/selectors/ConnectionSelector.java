/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eas.designer.explorer.selectors;

import com.eas.designer.application.indexer.PlatypusPathRecognizer;
import java.util.Set;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mg
 */
public class ConnectionSelector extends AppElementSelector{

    public ConnectionSelector(FileObject aRoot) {
        super(aRoot);
    }

    @Override
    public void fillAllowedMimeTypes(Set<String> allowedMimeTypes) {
        allowedMimeTypes.add(PlatypusPathRecognizer.CONNECTION_MIME_TYPE);
    }

}
