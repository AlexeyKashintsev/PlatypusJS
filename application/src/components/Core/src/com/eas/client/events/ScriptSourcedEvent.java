/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.client.events;

import org.mozilla.javascript.Scriptable;

/**
 *
 * @author vv
 */
public class ScriptSourcedEvent {

    protected Scriptable source;

    public ScriptSourcedEvent(Scriptable source) {
        this.source = source;
    }

    public Scriptable getSource() {
        return source;
    }
}