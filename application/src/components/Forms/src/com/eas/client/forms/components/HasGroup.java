/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.client.forms.components;

import com.eas.client.forms.api.containers.ButtonGroup;

/**
 *
 * @author mg
 */
public interface HasGroup {

    public ButtonGroup getButtonGroup();

    public void setButtonGroup(ButtonGroup aGroup);
}
