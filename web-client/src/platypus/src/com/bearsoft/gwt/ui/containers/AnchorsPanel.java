/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bearsoft.gwt.ui.containers;

import com.bearsoft.gwt.ui.CommonResources;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author mg
 */
public class AnchorsPanel extends LayoutPanel {

    public AnchorsPanel() {
        super();
    }

    @Override
    public void insert(Widget widget, int beforeIndex) {
        super.insert(widget, beforeIndex);
        if(widget instanceof FocusWidget){
            widget.getElement().getStyle().clearRight();
            widget.getElement().getStyle().clearBottom();
            widget.getElement().getStyle().setWidth(100, Style.Unit.PCT);
            widget.getElement().getStyle().setHeight(100, Style.Unit.PCT);
            CommonResources.INSTANCE.commons().ensureInjected();
            widget.getElement().addClassName(CommonResources.INSTANCE.commons().borderSized());
        } 
    }

    @Override
    public void onResize() {
        super.onResize();
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        forceLayout();
    }

}