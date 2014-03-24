/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bearsoft.gwt.ui.containers.window.events;

import com.google.gwt.event.shared.EventHandler;

/**
 *
 * @author mg
 * @param <T> Type being activated
 */
public interface ActivateHandler<T> extends EventHandler {

    public void onActivate(ActivateEvent<T> anEvent);
}
