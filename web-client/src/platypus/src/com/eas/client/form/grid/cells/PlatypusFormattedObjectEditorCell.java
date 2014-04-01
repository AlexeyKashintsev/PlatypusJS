/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.client.form.grid.cells;

import java.text.ParseException;

import com.bearsoft.gwt.ui.widgets.ObjectFormat;
import com.bearsoft.gwt.ui.widgets.grid.cells.RenderedPopupEditorCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author mg
 */
public class PlatypusFormattedObjectEditorCell extends RenderedPopupEditorCell<Object> {

	protected ObjectFormat format;

	public PlatypusFormattedObjectEditorCell() {
		super(new TextBox());
	}

	public PlatypusFormattedObjectEditorCell(Widget aEditor) {
		super(aEditor);
	}

	public PlatypusFormattedObjectEditorCell(Widget aEditor, ObjectFormat aFormat) {
		super(aEditor);
		format = aFormat;
	}

	public ObjectFormat getFormat() {
		return format;
	}

	public void setFormat(ObjectFormat aValue) {
		format = aValue;
	}

	@Override
	protected void renderCell(Context context, Object value, SafeHtmlBuilder sb) {
		if (format != null) {
			try {
				sb.appendEscaped(format.format(value));
			} catch (ParseException e) {
				sb.appendEscaped(e.getMessage());
			}
		} else {
			sb.appendEscaped(String.valueOf(value));
		}
	}
}
