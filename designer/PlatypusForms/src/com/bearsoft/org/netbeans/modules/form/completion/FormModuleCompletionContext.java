/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bearsoft.org.netbeans.modules.form.completion;

import com.bearsoft.org.netbeans.modules.form.FormModel;
import com.bearsoft.org.netbeans.modules.form.PersistenceException;
import com.bearsoft.org.netbeans.modules.form.PlatypusFormDataObject;
import com.bearsoft.org.netbeans.modules.form.PlatypusFormSupport;
import com.eas.designer.application.module.PlatypusModuleDataObject;
import com.eas.designer.application.module.completion.CompletionContext;
import com.eas.designer.application.module.completion.CompletionPoint;
import com.eas.designer.application.module.completion.ModuleCompletionContext;
import java.util.Map;
import jdk.nashorn.internal.ir.VarNode;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.openide.ErrorManager;

/**
 *
 * @author vv
 */
public class FormModuleCompletionContext extends ModuleCompletionContext {

    public static final String LOAD_FORM_METHOD_NAME = "loadForm";//NOI18N
    
    public FormModuleCompletionContext(PlatypusModuleDataObject dataObject, Class<? extends Object> aClass) {
        super(dataObject);
    }

    @Override
    public void injectVarContext(Map<String, CompletionContext> contexts, VarNode varNode) {
        super.injectVarContext(contexts, varNode);
        if (isSystemObjectMethod(varNode.getAssignmentSource(), LOAD_FORM_METHOD_NAME)) {
            contexts.put(varNode.getName().getName(), new FormCompletionContext(this));
        }
    }
    
    @Override
    public void applyCompletionItems(CompletionPoint point, int offset, CompletionResultSet resultSet) throws Exception {
        super.applyCompletionItems(point, offset, resultSet);
    }

    protected synchronized FormModel getFormModel() {
        PlatypusFormDataObject formDataObject = (PlatypusFormDataObject) getDataObject();
        PlatypusFormSupport support = formDataObject.getLookup().lookup(PlatypusFormSupport.class);
        try {
            support.loadForm();
        } catch (PersistenceException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return support.getFormModel();

    }
}
