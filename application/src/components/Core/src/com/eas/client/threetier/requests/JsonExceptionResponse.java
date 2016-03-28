/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.client.threetier.requests;

import com.eas.script.JsObjectException;

/**
 *
 * @author mg
 */
public class JsonExceptionResponse extends ExceptionResponse {

    protected String jsonContent;

    public JsonExceptionResponse(JsObjectException aException, String aJsonContent) {
        super(aException);
        jsonContent = aJsonContent;
    }

    public String getJsonContent() {
        return jsonContent;
    }

}
