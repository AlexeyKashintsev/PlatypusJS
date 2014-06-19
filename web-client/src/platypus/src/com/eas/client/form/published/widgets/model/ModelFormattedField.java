package com.eas.client.form.published.widgets.model;

import java.text.ParseException;

import com.bearsoft.gwt.ui.widgets.FormattedObjectBox;
import com.bearsoft.rowset.Utils;
import com.bearsoft.rowset.metadata.Field;
import com.eas.client.converters.ObjectRowValueConverter;
import com.eas.client.form.ControlsUtils;
import com.eas.client.form.events.ActionEvent;
import com.eas.client.form.events.ActionHandler;
import com.eas.client.form.events.HasActionHandlers;
import com.eas.client.form.published.HasEmptyText;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public class ModelFormattedField extends PublishedDecoratorBox<Object> implements HasEmptyText, HasActionHandlers {

	protected String emptyText;
	
	public ModelFormattedField() {
		super(new FormattedObjectBox());
	}

	protected int actionHandlers;
	protected HandlerRegistration clickReg;

	@Override
	public HandlerRegistration addActionHandler(ActionHandler handler) {
		final HandlerRegistration superReg = super.addHandler(handler, ActionEvent.getType());
		if (actionHandlers == 0) {
			clickReg = addValueChangeHandler(new ValueChangeHandler<Object>() {

				@Override
                public void onValueChange(ValueChangeEvent<Object> event) {
					ActionEvent.fire(ModelFormattedField.this, ModelFormattedField.this);
                }

			});
		}
		actionHandlers++;
		return new HandlerRegistration() {
			@Override
			public void removeHandler() {
				superReg.removeHandler();
				actionHandlers--;
				if (actionHandlers == 0) {
					assert clickReg != null : "Erroneous use of addActionHandler/removeHandler detected in ModelFormattedField";
					clickReg.removeHandler();
					clickReg = null;
				}
			}
		};
	}

	public String getFormat(){
		return ((FormattedObjectBox)decorated).getPattern();
	}
	
	public void setFormat(String aValue) throws ParseException{
		((FormattedObjectBox)decorated).setPattern(aValue);
	}
	
	public void setFormatType(int aFormatType, String aPattern) throws ParseException{
		((FormattedObjectBox)decorated).setFormatType(aFormatType, aPattern);
	}
	
	public String getText(){
		return ((FormattedObjectBox)decorated).getText();
	}
	
	public void setText(String aValue){
		((FormattedObjectBox)decorated).setText(aValue);
	}
	
	public String getPattern(){
		return ((FormattedObjectBox)decorated).getPattern();
	}
	
	public void setPattern(String aPattern) throws ParseException{
		((FormattedObjectBox)decorated).setPattern(aPattern);
	}
	
	@Override
	public String getEmptyText() {
		return emptyText;
	}
	
	@Override
	public void setEmptyText(String aValue) {
		emptyText = aValue;
		ControlsUtils.applyEmptyText(getElement(), emptyText);
	}
	
	@Override
	public void setPublished(JavaScriptObject aValue) {
		super.setPublished(aValue);
		if (published != null) {
			publish(this, published);
		}
	}

	private native static void publish(ModelFormattedField aWidget, JavaScriptObject aPublished)/*-{
		Object.defineProperty(aPublished, "emptyText", {
			get : function() {
				return aWidget.@com.eas.client.form.published.HasEmptyText::getEmptyText()();
			},
			set : function(aValue) {
				aWidget.@com.eas.client.form.published.HasEmptyText::setEmptyText(Ljava/lang/String;)(aValue != null ? '' + aValue : null);
			}
		});
		Object.defineProperty(aPublished, "value", {
			get : function() {
				return $wnd.P.boxAsJs(aWidget.@com.eas.client.form.published.widgets.model.ModelFormattedField::getJsValue()());
			},
			set : function(aValue) {
				aWidget.@com.eas.client.form.published.widgets.model.ModelFormattedField::setJsValue(Ljava/lang/Object;)($wnd.P.boxAsJava(aValue));
			}
		});
		Object.defineProperty(aPublished, "text", {
			get : function() {
				return aWidget.@com.eas.client.form.published.widgets.model.ModelFormattedField::getText()();
			},
			set : function(aValue) {
				aWidget.@com.eas.client.form.published.widgets.model.ModelFormattedField::setText(Ljava/lang/String;)($wnd.P.boxAsJava(aValue));
			}
		});
		Object.defineProperty(aPublished, "format", {
			get : function() {
				return aWidget.@com.eas.client.form.published.widgets.model.ModelFormattedField::getPattern()();
			},
			set : function(aValue) {
				aWidget.@com.eas.client.form.published.widgets.model.ModelFormattedField::setPattern(Ljava/lang/String;)(aValue != null ? '' + aValue : null);
			}
		});
	}-*/;

	public Object getJsValue() {
		return Utils.toJs(getValue());
	}

	public void setJsValue(Object value) throws Exception {
		setValue(Utils.toJava(value), true);
	}

	@Override
    public void setBinding(Field aField) throws Exception {
		super.setBinding(aField, new ObjectRowValueConverter());
    }
}
