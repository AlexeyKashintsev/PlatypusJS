package com.eas.client.form.published.widgets.model;

import com.bearsoft.rowset.metadata.Field;
import com.eas.client.converters.BooleanRowValueConverter;
import com.eas.client.form.events.ActionEvent;
import com.eas.client.form.events.ActionHandler;
import com.eas.client.form.events.HasActionHandlers;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CheckBox;

public class ModelCheck extends PublishedDecoratorBox<Boolean> implements HasActionHandlers {

	public ModelCheck() {
		super(new CheckBox());
	}

	protected int actionHandlers;
	protected HandlerRegistration clickReg;

	@Override
	public HandlerRegistration addActionHandler(ActionHandler handler) {
		final HandlerRegistration superReg = super.addHandler(handler, ActionEvent.getType());
		if (actionHandlers == 0) {
			clickReg = ((CheckBox)decorated).addClickHandler(new ClickHandler() {

				@Override
                public void onClick(ClickEvent event) {
					ActionEvent.fire(ModelCheck.this, ModelCheck.this);
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
					assert clickReg != null : "Erroneous use of addActionHandler/removeHandler detected in ModelCheck";
					clickReg.removeHandler();
					clickReg = null;
				}
			}
		};
	}

	@Override
	public void setPublished(JavaScriptObject aValue) {
		super.setPublished(aValue);
		if (published != null) {
			publish(this, published);
		}
	}

	private static native void publish(ModelCheck aField, JavaScriptObject aPublished)/*-{
		Object.defineProperty(aPublished, "text", {
			get : function() {
				return aField.@com.eas.client.form.published.widgets.model.ModelCheck::getText()();
			},
			set : function(aValue) {
				if (aValue != null)
					aField.@com.eas.client.form.published.widgets.model.ModelCheck::setText(Ljava/lang/String;)('' + aValue);
				else
					aField.@com.eas.client.form.published.widgets.model.ModelCheck::setText(Ljava/lang/String;)(null);
			}
		});
		Object.defineProperty(aPublished, "value", {
			get : function() {
				var javaValue = aField.@com.eas.client.form.published.widgets.model.ModelCheck::getValue()();
				if (javaValue == null)
					return null;
				else
					return javaValue.@java.lang.Boolean::booleanValue()();
			},
			set : function(aValue) {
				if (aValue != null) {
					var javaValue = $wnd.P.boxAsJava((false != aValue));
					aField.@com.eas.client.form.published.widgets.model.ModelCheck::setValue(Ljava/lang/Boolean;Z)(javaValue, true);
				} else {
					aField.@com.eas.client.form.published.widgets.model.ModelCheck::setValue(Ljava/lang/Boolean;Z)(null, true);
				}
			}
		});
	}-*/;

	public String getText() {
		return ((CheckBox) decorated).getText();
	}

	public void setText(String aValue) {
		((CheckBox) decorated).setText(aValue);
	}

	@Override
	public Boolean getValue() {
		return super.getValue();
	}

	@Override
	public void setValue(Boolean value, boolean fireEvents) {
		super.setValue(value, fireEvents);
	}

	@Override
	public void setBinding(Field aField) throws Exception {
		super.setBinding(aField, new BooleanRowValueConverter());
	}
}
