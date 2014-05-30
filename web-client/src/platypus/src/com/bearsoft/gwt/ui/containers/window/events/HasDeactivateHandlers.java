/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bearsoft.gwt.ui.containers.window.events;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 *
 * @author mg
 * @param <T> Type being deactivated
 */
public interface HasDeactivateHandlers<T> extends HasHandlers {

    /**
     * Adds a {@link DeactivateEvent} handler.
     *
     * @param handler the handler
     * @return the registration for the event
     */
    HandlerRegistration addDeactivateHandler(DeactivateHandler<T> handler);
}
