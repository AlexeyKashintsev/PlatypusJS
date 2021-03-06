package com.bearsoft.gwt.ui.widgets;

import java.util.Date;

import com.google.gwt.user.datepicker.client.DateBox;

public class DecoratedDateBox extends DateBox {

	protected DateTimePicker picker;

	public DecoratedDateBox() {
		super();
	}

	public DecoratedDateBox(DateTimePicker aPicker, Date date, Format format) {
		super(aPicker, date, format);
		picker = aPicker;
	}

	@Override
	public Date getValue() {
		return super.getValue();
	}

	@Override
	public void setValue(Date date, boolean fireEvents) {
		super.setValue(date, fireEvents);
	}

	@Override
	public void showDatePicker() {
	}

}
