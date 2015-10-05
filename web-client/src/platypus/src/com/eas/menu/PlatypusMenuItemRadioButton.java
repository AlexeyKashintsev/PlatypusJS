package com.eas.menu;

import com.eas.predefine.HasPublished;
import com.eas.ui.ButtonGroup;
import com.eas.ui.HasEventsExecutor;
import com.eas.ui.HasJsFacade;
import com.eas.ui.HasPlatypusButtonGroup;
import com.eas.ui.events.ActionEvent;
import com.eas.ui.events.ActionHandler;
import com.eas.ui.events.EventsExecutor;
import com.eas.ui.events.HasActionHandlers;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public class PlatypusMenuItemRadioButton extends MenuItemRadioButton implements HasActionHandlers, HasJsFacade, HasPlatypusButtonGroup, HasEventsExecutor {

	protected EventsExecutor eventsExecutor;
	protected JavaScriptObject published;
	protected String name;
	//
	protected boolean settingValue;
	//

	protected ButtonGroup group;

	public PlatypusMenuItemRadioButton() {
		super(false, "", false);
	}

	public PlatypusMenuItemRadioButton(Boolean aValue, String aText, boolean asHtml) {
		super(aValue, aText, asHtml);
	}

	protected int actionHandlers;
	protected HandlerRegistration valueChangeReg;

	@Override
	public HandlerRegistration addActionHandler(ActionHandler handler) {
		final HandlerRegistration superReg = super.addHandler(handler, ActionEvent.getType());
		if (actionHandlers == 0) {
			valueChangeReg = addValueChangeHandler(new ValueChangeHandler<Boolean>() {

				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					if (!settingValue)
						ActionEvent.fire(PlatypusMenuItemRadioButton.this, PlatypusMenuItemRadioButton.this);
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
					assert valueChangeReg != null : "Erroneous use of addActionHandler/removeHandler detected in PlatypusMenuItemRadioButton";
					valueChangeReg.removeHandler();
					valueChangeReg = null;
				}
			}
		};
	}

	@Override
	public EventsExecutor getEventsExecutor() {
		return eventsExecutor;
	}

	@Override
	public void setEventsExecutor(EventsExecutor aExecutor) {
		eventsExecutor = aExecutor;
	}

	@Override
	public String getJsName() {
		return name;
	}

	@Override
	public void setJsName(String aValue) {
		name = aValue;
	}

	@Override
	public ButtonGroup getButtonGroup() {
		return group;
	}

	@Override
	public void setButtonGroup(ButtonGroup aValue) {
		group = aValue;
	}

	@Override
	public void mutateButtonGroup(ButtonGroup aGroup) {
		if (group != aGroup) {
			if (group != null)
				group.remove((HasPublished) this);
			group = aGroup;
			if (group != null)
				group.add((HasPublished) this);
		}
	}

	public boolean getPlainValue() {
		if (getValue() != null)
			return getValue();
		else
			return false;
	}

	public void setPlainValue(boolean value) {
		settingValue = true;
		try {
			super.setValue(value, true);
		} finally {
			settingValue = false;
		}
	}

	@Override
	public JavaScriptObject getPublished() {
		return published;
	}

	@Override
	public void setPublished(JavaScriptObject aValue) {
		if (published != aValue) {
			published = aValue;
			if (published != null) {
				publish(this, aValue);
			}
		}
	}

	private native static void publish(HasPublished aWidget, JavaScriptObject published)/*-{
		Object.defineProperty(published, "text", {
			get : function() {
				return aWidget.@com.eas.menu.PlatypusMenuItemRadioButton::getText()();
			},
			set : function(aValue) {
				aWidget.@com.eas.menu.PlatypusMenuItemRadioButton::setText(Ljava/lang/String;)(aValue != null ? '' + aValue : null);
			}
		});
		Object.defineProperty(published, "selected", {
			get : function() {
				return aWidget.@com.eas.menu.PlatypusMenuItemRadioButton::getPlainValue()();
			},
			set : function(aValue) {
				aWidget.@com.eas.menu.PlatypusMenuItemRadioButton::setPlainValue(Z)(aValue != null && !!aValue);
			}
		});
		Object.defineProperty(published, "buttonGroup", {
			get : function() {
				var buttonGroup = aWidget.@com.eas.ui.HasPlatypusButtonGroup::getButtonGroup()();
				return @com.eas.predefine.Utils::checkPublishedComponent(Ljava/lang/Object;)(buttonGroup);					
			},
			set : function(aValue) {
				aWidget.@com.eas.ui.HasPlatypusButtonGroup::mutateButtonGroup(Lcom/eas/ui/ButtonGroup;)(aValue != null ? aValue.unwrap() : null);
			}
		});
	}-*/;
}
