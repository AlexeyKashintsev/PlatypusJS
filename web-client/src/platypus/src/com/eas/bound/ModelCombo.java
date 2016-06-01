package com.eas.bound;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.eas.client.IdGenerator;
import com.eas.client.converters.StringValueConverter;
import com.eas.core.Utils;
import com.eas.ui.CommonResources;
import com.eas.ui.HasEmptyText;
import com.eas.ui.JavaScriptObjectKeyProvider;
import com.eas.ui.PublishedCell;
import com.eas.ui.events.ActionEvent;
import com.eas.ui.events.ActionHandler;
import com.eas.ui.events.HasActionHandlers;
import com.eas.widgets.WidgetsUtils;
import com.eas.widgets.boxes.StyledListBox;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;

public class ModelCombo extends ModelDecoratorBox<JavaScriptObject> implements HasEmptyText, HasActionHandlers {

	protected static final String CUSTOM_DROPDOWN_CLASS = "combo-field-custom-dropdown";
	protected JavaScriptObjectKeyProvider rowKeyProvider = new JavaScriptObjectKeyProvider();
	protected String keyForNullValue = String.valueOf(IdGenerator.genId());
	protected String emptyText;
	protected JavaScriptObject injected;
	protected JavaScriptObject displayList;
	protected String displayField;
	protected HandlerRegistration boundToList;
	protected HandlerRegistration boundToListElements;
	protected Runnable onRedraw;
	protected InputElement nonListMask = Document.get().createTextInputElement();

	protected boolean list = true;

	public ModelCombo() {
		super(new StyledListBox<JavaScriptObject>());
		StyledListBox<JavaScriptObject> box = (StyledListBox<JavaScriptObject>) decorated;
		box.addItem("...", keyForNullValue, null, "");
		box.getElement().addClassName(CUSTOM_DROPDOWN_CLASS);
		box.getElement().getStyle().setOverflow(Style.Overflow.HIDDEN);
		CommonResources.INSTANCE.commons().ensureInjected();
		box.getElement().addClassName(CommonResources.INSTANCE.commons().withoutDropdown());
		nonListMask.setReadOnly(true);
		nonListMask.addClassName(CommonResources.INSTANCE.commons().borderSized());
		nonListMask.getStyle().setPosition(Style.Position.ABSOLUTE);
		nonListMask.getStyle().setDisplay(Style.Display.NONE);
		nonListMask.getStyle().setTop(0, Style.Unit.PX);
		nonListMask.getStyle().setLeft(0, Style.Unit.PX);
		nonListMask.getStyle().setWidth(100, Style.Unit.PCT);
		nonListMask.getStyle().setHeight(100, Style.Unit.PCT);

		getElement().insertFirst(nonListMask);

		selectButton.getElement().addClassName("decorator-select-combo");
		clearButton.getElement().addClassName("decorator-clear-combo");
	}

	@Override
	public void setFocus(boolean focused) {
		if (list) {
			super.setFocus(focused);
		} else {
			nonListMask.focus();
		}
	}

	@Override
	protected int organizeButtonsContent() {
		int right = super.organizeButtonsContent();
		if (nonListMask != null) {
			nonListMask.getStyle().setPaddingRight(right, Style.Unit.PX);
		}
		return right;
	}

	public Runnable getOnRedraw() {
		return onRedraw;
	}

	public void setOnRedraw(Runnable aValue) {
		onRedraw = aValue;
	}

	protected int actionHandlers;
	protected HandlerRegistration valueChangeReg;

	@Override
	public HandlerRegistration addActionHandler(ActionHandler handler) {
		final HandlerRegistration superReg = super.addHandler(handler, ActionEvent.getType());
		if (actionHandlers == 0) {
			valueChangeReg = addValueChangeHandler(new ValueChangeHandler<JavaScriptObject>() {

				@Override
				public void onValueChange(ValueChangeEvent<JavaScriptObject> event) {
					if (!settingValue)
						ActionEvent.fire(ModelCombo.this, ModelCombo.this);
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
					assert valueChangeReg != null : "Erroneous use of addActionHandler/removeHandler detected in ModelDate";
					valueChangeReg.removeHandler();
					valueChangeReg = null;
				}
			}
		};
	}

	public void setValue(JavaScriptObject aValue, boolean fireEvents) {
		JavaScriptObject oldValue = getValue();
		if (oldValue != aValue) {
			StyledListBox<JavaScriptObject> box = (StyledListBox<JavaScriptObject>) decorated;
			int newValueIndex = box.indexOf(aValue);
			if (injected != null) {
				int injectedValueIndex = box.indexOf(injected);
				if (injectedValueIndex != -1) {
					box.removeItem(injectedValueIndex);
				}
			}
			injected = null;
			if (newValueIndex == -1) {
				injectValueItem(aValue);
				injected = aValue;
			}
			super.setValue(aValue, fireEvents);
			nonListMask.setValue(box.getSelectedItemText());
		}
	}

	@Override
	protected void onAttach() {
		super.onAttach();
	}

	private void injectValueItem(JavaScriptObject aValue) {
		assert aValue != null : "null met assumption failed in ModelCombo";
		StyledListBox<JavaScriptObject> box = (StyledListBox<JavaScriptObject>) decorated;
		String label = calcLabel(aValue);
		box.addItem(label != null ? label : "", aValue.hashCode() + "", aValue, "");
	}

	@Override
	protected void clearValue() {
		try {
			setJsValue(null, true);
			ActionEvent.fire(this, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public JavaScriptObject convert(Object aValue) {
		return aValue instanceof JavaScriptObject ? (JavaScriptObject) aValue : null;
	}

	public void redraw() {
		try {
			JavaScriptObject value = getValue();
			StyledListBox<JavaScriptObject> box = (StyledListBox<JavaScriptObject>) decorated;
			box.setSelectedIndex(-1);
			box.clear();
			box.addItem(calcLabel(null), keyForNullValue, null, "");
			box.setSelectedIndex(0);
			injected = null;
			boolean valueMet = false;
			if (null == value)
				valueMet = true;
			if (ModelCombo.this.list && displayList != null) {
				List<JavaScriptObject> jsoList = new JsArrayList(displayList);
				for (int i = 0; i < jsoList.size(); i++) {
					JavaScriptObject listItem = jsoList.get(i);
					if (listItem != null) {
						String _label = calcLabel(listItem);
						box.addItem(_label, listItem.hashCode() + "", listItem, "");
						if (listItem == value) {
							valueMet = true;
						}
					}
				}
			}
			if (!valueMet) {
				injectValueItem(value);
				injected = value;
			}
			int valueIndex = box.indexOf(value);
			box.setSelectedIndex(valueIndex);
			nonListMask.setValue(box.getSelectedItemText());
			if (onRedraw != null)
				onRedraw.run();
		} catch (Exception e) {
			Logger.getLogger(ModelCombo.class.getName()).log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public String calcLabel(JavaScriptObject aValue) {
		String label = aValue != null ? new StringValueConverter().convert(Utils.getPathData(aValue, displayField)) : "...";
		PublishedCell cell = WidgetsUtils.calcValuedPublishedCell(published, onRender, aValue, label != null ? label : "", null);
		if (cell != null && cell.getDisplay() != null && !cell.getDisplay().isEmpty()) {
			label = cell.getDisplay();
		}
		return label;
	}

	protected HasValue<JavaScriptObject> getDecorated() {
		return ((HasValue<JavaScriptObject>) decorated);
	}

	public String getText() {
		return ((StyledListBox<JavaScriptObject>) decorated).getText();
	}

	@Override
	public void setText(String text) {
	}

	@Override
	public String getEmptyText() {
		return emptyText;
	}

	@Override
	public void setEmptyText(String aValue) {
		emptyText = aValue;
		WidgetsUtils.applyEmptyText(getElement(), emptyText);
	}

	public void setPublished(JavaScriptObject aValue) {
		super.setPublished(aValue);
		if (published != null) {
			publish(this, published);
		}
	}

	private native static void publish(ModelCombo aWidget, JavaScriptObject aPublished)/*-{
	    var B = @com.eas.core.Predefine::boxing;
	    aPublished.redraw = function() {
          aWidget.@com.eas.bound.ModelCombo::redraw()();
        };
        Object.defineProperty(aPublished, "emptyText", {
	        get : function() {
	            return aWidget.@com.eas.ui.HasEmptyText::getEmptyText()();
	        },
	        set : function(aValue) {
	            aWidget.@com.eas.ui.HasEmptyText::setEmptyText(Ljava/lang/String;)(aValue!=null?''+aValue:null);
	        }
        });
        Object.defineProperty(aPublished, "value", {
	        get : function() {
	            return B.boxAsJs(aWidget.@com.eas.bound.ModelCombo::getJsValue()());
	        },
	        set : function(aValue) {
	 	        if (aValue != null) {
		            aWidget.@com.eas.bound.ModelCombo::setJsValue(Ljava/lang/Object;)(B.boxAsJava(aValue));
		        } else {
		            aWidget.@com.eas.bound.ModelCombo::setJsValue(Ljava/lang/Object;)(null);
		        }
	        }
        });
        Object.defineProperty(aPublished, "text", {
	        get : function() {
	            return aWidget.@com.eas.bound.ModelCombo::getText()();
	        }
        });
        Object.defineProperty(aPublished, "displayList", {
	        get : function() {
	            return aWidget.@com.eas.bound.ModelCombo::getDisplayList()();
	        },
	        set : function(aValue) {
	            aWidget.@com.eas.bound.ModelCombo::setDisplayList(Lcom/google/gwt/core/client/JavaScriptObject;)(aValue);
	        }
        });
        Object.defineProperty(aPublished, "displayField", {
	        get : function() {
	            return aWidget.@com.eas.bound.ModelCombo::getDisplayField()();
	        },
	        set : function(aValue) {
	            aWidget.@com.eas.bound.ModelCombo::setDisplayField(Ljava/lang/String;)(aValue != null ? '' + aValue : null);
	        }
        });
        Object.defineProperty(aPublished, "list", {
	        get : function() {
	            return aWidget.@com.eas.bound.ModelCombo::isList()();
	        },
	        set : function(aValue) {
	            aWidget.@com.eas.bound.ModelCombo::setList(Z)(false != aValue);
	        }
        });
    }-*/;

	public boolean isList() {
		return list;
	}

	public void setList(boolean aValue) {
		if (list != aValue) {
			list = aValue;
			StyledListBox<JavaScriptObject> box = (StyledListBox<JavaScriptObject>) decorated;
			if (list) {
				box.getElement().addClassName(CUSTOM_DROPDOWN_CLASS);
				box.getElement().getStyle().clearVisibility();
				nonListMask.getStyle().setDisplay(Style.Display.NONE);
				selectButton.getElement().addClassName("decorator-select-combo");
				clearButton.getElement().addClassName("decorator-clear-combo");
				nonListMask.removeClassName("form-control");
			} else {
				box.getElement().removeClassName(CUSTOM_DROPDOWN_CLASS);
				box.getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
				nonListMask.getStyle().setDisplay(Style.Display.INLINE_BLOCK);
				selectButton.getElement().removeClassName("decorator-select-combo");
				clearButton.getElement().removeClassName("decorator-clear-combo");
				nonListMask.addClassName("form-control");
			}
			redraw();
		}
	}

	public Object getJsValue() {
		return Utils.toJs(getValue());
	}

	public void setJsValue(Object aValue) throws Exception {
		setJsValue(aValue, true);
	}

	public void setJsValue(Object aValue, boolean fireEvents) throws Exception {
		setValue(convert(aValue), fireEvents);
	}

	public JavaScriptObject getDisplayList() {
		return displayList;
	}

	protected boolean changesQueued;

	protected void enqueueListChanges() {
		changesQueued = true;
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {

			@Override
			public void execute() {
				if (changesQueued) {
					changesQueued = false;
					redraw();
				}
			}
		});
	}

	protected boolean readdQueued;

	private void enqueueListReadd() {
		readdQueued = true;
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {

			@Override
			public void execute() {
				if (readdQueued) {
					readdQueued = false;
					if (boundToListElements != null) {
						boundToListElements.removeHandler();
						boundToListElements = null;
					}
					if (displayList != null) {
						boundToListElements = Utils.listenElements(displayList, new Utils.OnChangeHandler() {

							@Override
							public void onChange(JavaScriptObject anEvent) {
								enqueueListChanges();
							}
						});
					}
					redraw();
				}
			}
		});
	}

	protected void bindList() {
		if (displayList != null) {
			boundToList = Utils.listenPath(displayList, "length", new Utils.OnChangeHandler() {

				@Override
				public void onChange(JavaScriptObject anEvent) {
					enqueueListReadd();
				}
			});
			enqueueListReadd();
		}
	}

	protected void unbindList() {
		if (boundToList != null) {
			boundToList.removeHandler();
			boundToList = null;
			enqueueListReadd();
		}
	}

	public void setDisplayList(JavaScriptObject aValue) {
		if (displayList != aValue) {
			unbindList();
			displayList = aValue;
			bindList();
		}
	}

	public String getDisplayField() {
		return displayField;
	}

	public void setDisplayField(String aValue) {
		if (displayField != null ? !displayField.equals(aValue) : aValue != null) {
			unbindList();
			displayField = aValue;
			bindList();
		}
	}

	@Override
	protected void setReadonly(boolean aValue) {
	}

	@Override
	protected boolean isReadonly() {
		return false;
	}
}
