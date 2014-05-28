/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.client.model.application;

import com.eas.client.events.PublishedSourcedEvent;
import com.eas.script.HasPublished;
import com.eas.script.ScriptFunction;

/**
 *
 * @author vv
 */
public class CursorPositionWillChangeEvent extends PublishedSourcedEvent {

    protected int oldIndex;
    protected int newIndex;

    public CursorPositionWillChangeEvent(HasPublished aSource, int aOldIndex, int aNewIndex) {
        super(aSource);
        oldIndex = aOldIndex;
        newIndex = aNewIndex;
    }

    private static final String OLD_INDEX_JSDOC = ""
            + "/**\n"
            + "* Cursor position the cursor is still on.\n"
            + "*/";

    @ScriptFunction(jsDoc = OLD_INDEX_JSDOC)
    public int getOldIndex() {
        return oldIndex;
    }

    private static final String NEW_INDEX_JSDOC = ""
            + "/**\n"
            + "* Cursor position the cursor will be set on.\n"
            + "*/";

    @ScriptFunction(jsDoc = NEW_INDEX_JSDOC)
    public int getNewIndex() {
        return newIndex;
    }

}
