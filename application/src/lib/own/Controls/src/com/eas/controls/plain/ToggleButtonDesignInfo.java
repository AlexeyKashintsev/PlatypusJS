/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.controls.plain;

import com.eas.controls.ControlsDesignInfoVisitor;

/**
 *
 * @author mg
 */
public class ToggleButtonDesignInfo extends ButtonDesignInfo {

    public ToggleButtonDesignInfo() {
        super();
    }

    @Override
    public void accept(ControlsDesignInfoVisitor aVisitor) {
        aVisitor.visit(this);
    }
}