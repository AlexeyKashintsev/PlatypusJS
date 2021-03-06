package com.bearsoft.gwt.ui.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.CheckBox;

public class NullableCheckBox extends CheckBox {

	protected InputElement inputElem;
	protected Boolean value;

	public NullableCheckBox() {
	    this(DOM.createInputCheck());
	    setStyleName("gwt-CheckBox");
	}

	public NullableCheckBox(Element elem) {
		super(elem);
		inputElem = InputElement.as(elem);
		inputElem.setPropertyBoolean("indeterminate", true);		
		inputElem.getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
	}

	@Override
	public void setValue(Boolean aValue, boolean fireEvents) {
		Boolean oldValue = getValue();
		value = aValue;
		inputElem.setPropertyBoolean("indeterminate", value == null);
		if (value == null ? oldValue != null : !value.equals(oldValue)) {
			inputElem.setChecked(aValue != null ? aValue : false);
			inputElem.setDefaultChecked(aValue != null ? aValue : false);
			if (fireEvents) {
				ValueChangeEvent.fire(this, aValue);
			}
		}
	}

	@Override
	public Boolean getValue() {
		return value;
	}

	protected void ensureDomEventHandlers() {
		addDomHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				setValue(inputElem.isChecked(), true);
			}
		}, ChangeEvent.getType());
	}
}
