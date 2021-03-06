package com.eas.client.form.published.widgets;

import com.bearsoft.gwt.ui.XElement;
import com.bearsoft.gwt.ui.widgets.DropDownButton;
import com.bearsoft.gwt.ui.widgets.ImageParagraph;
import com.eas.client.form.EventsExecutor;
import com.eas.client.form.events.ActionEvent;
import com.eas.client.form.events.ActionHandler;
import com.eas.client.form.events.HasActionHandlers;
import com.eas.client.form.events.HasHideHandlers;
import com.eas.client.form.events.HasShowHandlers;
import com.eas.client.form.events.HideEvent;
import com.eas.client.form.events.HideHandler;
import com.eas.client.form.events.ShowEvent;
import com.eas.client.form.events.ShowHandler;
import com.eas.client.form.published.HasComponentPopupMenu;
import com.eas.client.form.published.HasEventsExecutor;
import com.eas.client.form.published.HasJsFacade;
import com.eas.client.form.published.HasPublished;
import com.eas.client.form.published.menu.PlatypusPopupMenu;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.MenuBar;

public class PlatypusSplitButton extends DropDownButton implements HasActionHandlers, HasJsFacade, HasEnabled, HasComponentPopupMenu, HasEventsExecutor, HasShowHandlers, HasHideHandlers, HasResizeHandlers {

	protected EventsExecutor eventsExecutor;
	protected PlatypusPopupMenu contextMenu;
	protected boolean enabled = true;
	protected String name;
	protected JavaScriptObject published;

	public PlatypusSplitButton() {
		super("", false, null);
	}

	public PlatypusSplitButton(String aTitle, boolean asHtml, MenuBar aMenu) {
		super(aTitle, asHtml, aMenu);
	}

	public PlatypusSplitButton(String aTitle, boolean asHtml, ImageResource aImage, MenuBar aMenu) {
		super(aTitle, asHtml, aImage, aMenu);
	}

	@Override
	public HandlerRegistration addResizeHandler(ResizeHandler handler) {
		return addHandler(handler, ResizeEvent.getType());
	}

	@Override
	public void onResize() {
		super.onResize();
		if(isAttached()){
			ResizeEvent.fire(this, getElement().getOffsetWidth(), getElement().getOffsetHeight());
		}
	}

	@Override
	public HandlerRegistration addHideHandler(HideHandler handler) {
		return addHandler(handler, HideEvent.getType());
	}

	@Override
	public HandlerRegistration addShowHandler(ShowHandler handler) {
		return addHandler(handler, ShowEvent.getType());
	}

	@Override
	public void setVisible(boolean visible) {
		boolean oldValue = isVisible();
		super.setVisible(visible);
		if (oldValue != visible) {
			if (visible) {
				ShowEvent.fire(this, this);
			} else {
				HideEvent.fire(this, this);
			}
		}
	}

	@Override
	protected void showMenu() {
		if (menu instanceof PlatypusPopupMenu)
			((PlatypusPopupMenu)menu).showRelativeTo(chevron);
	}

	protected int actionHandlers;
	protected HandlerRegistration clickReg;
	protected HandlerRegistration keyDownReg;

	@Override
	public HandlerRegistration addActionHandler(ActionHandler handler) {
		final HandlerRegistration superReg = super.addHandler(handler, ActionEvent.getType());
		if (actionHandlers == 0) {
			clickReg = addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					ActionEvent.fire(PlatypusSplitButton.this, PlatypusSplitButton.this);
				}

			});
			keyDownReg = content.addKeyDownHandler(new KeyDownHandler(){

				@Override
                public void onKeyDown(KeyDownEvent event) {
					ActionEvent.fire(PlatypusSplitButton.this, PlatypusSplitButton.this);
                }});
		}
		actionHandlers++;
		return new HandlerRegistration() {
			@Override
			public void removeHandler() {
				superReg.removeHandler();
				actionHandlers--;
				if (actionHandlers == 0) {
					assert clickReg != null : "Erroneous use of addActionHandler/removeHandler detected in PlatypusSplitButton";
					clickReg.removeHandler();
					clickReg = null;
					keyDownReg.removeHandler();
					keyDownReg = null;
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
	public PlatypusPopupMenu getPlatypusPopupMenu() {
		return contextMenu;
	}

	protected HandlerRegistration menuTriggerReg;

	@Override
	public void setPlatypusPopupMenu(PlatypusPopupMenu aMenu) {
		if (contextMenu != aMenu) {
			if (menuTriggerReg != null)
				menuTriggerReg.removeHandler();
			contextMenu = aMenu;
			if (contextMenu != null) {
				menuTriggerReg = super.addDomHandler(new ContextMenuHandler() {

					@Override
					public void onContextMenu(ContextMenuEvent event) {
						event.preventDefault();
						event.stopPropagation();
						contextMenu.setPopupPosition(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
						contextMenu.show();
					}
				}, ContextMenuEvent.getType());
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean aValue) {
		boolean oldValue = enabled;
		enabled = aValue;
		if(!oldValue && enabled){
			getElement().<XElement>cast().unmask();
		}else if(oldValue && !enabled){
			getElement().<XElement>cast().disabledMask();
		}
	}

	@Override
	public String getJsName() {
		return name;
	}

	@Override
	public void setJsName(String aValue) {
		name = aValue;
	}

	public ImageParagraph getContent() {
		return content;
	}

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
		published.opaque = true;

		Object.defineProperty(published, "text", {
			get : function() {
				return aWidget.@com.eas.client.form.published.widgets.PlatypusSplitButton::getText()();
			},
			set : function(aValue) {
				aWidget.@com.eas.client.form.published.widgets.PlatypusSplitButton::setText(Ljava/lang/String;)(aValue!=null?''+aValue:null);
			}
		});
		Object.defineProperty(published, "icon", {
			get : function() {
				return aWidget.@com.eas.client.form.published.widgets.PlatypusSplitButton::getImageResource()();
			},
			set : function(aValue) {
				aWidget.@com.eas.client.form.published.widgets.PlatypusSplitButton::setImageResource(Lcom/google/gwt/resources/client/ImageResource;)(aValue);
			}
		});
		Object.defineProperty(published, "iconTextGap", {
			get : function() {
				return aWidget.@com.eas.client.form.published.widgets.PlatypusSplitButton::getIconTextGap()();
			},
			set : function(aValue) {
				aWidget.@com.eas.client.form.published.widgets.PlatypusSplitButton::setIconTextGap(I)(aValue);
			}
		});
		Object.defineProperty(published, "horizontalTextPosition", {
			get : function() {
				return aWidget.@com.eas.client.form.published.widgets.PlatypusSplitButton::getHorizontalTextPosition()();
			},
			set : function(aValue) {
				aWidget.@com.eas.client.form.published.widgets.PlatypusSplitButton::setHorizontalTextPosition(I)(+aValue);
			}
		});
		Object.defineProperty(published, "verticalTextPosition", {
			get : function() {
				return aWidget.@com.eas.client.form.published.widgets.PlatypusSplitButton::getVerticalTextPosition()();
			},
			set : function(aValue) {
				aWidget.@com.eas.client.form.published.widgets.PlatypusSplitButton::setVerticalTextPosition(I)(+aValue);
			}
		});

		Object.defineProperty(published, "horizontalAlignment", {
			get : function() {
				return aWidget.@com.eas.client.form.published.widgets.PlatypusSplitButton::getHorizontalAlignment()();
			},
			set : function(aValue) {
				aWidget.@com.eas.client.form.published.widgets.PlatypusSplitButton::setHorizontalAlignment(I)(+aValue);
			}
		});
		Object.defineProperty(published, "verticalAlignment", {
			get : function() {
				return aWidget.@com.eas.client.form.published.widgets.PlatypusSplitButton::getVerticalAlignment()();
			},
			set : function(aValue) {
				aWidget.@com.eas.client.form.published.widgets.PlatypusSplitButton::setVerticalAlignment(I)(+aValue);
			}
		});
		Object.defineProperty(published, "dropDownMenu", {
			get : function(){
				var menu = aWidget.@com.eas.client.form.published.widgets.PlatypusSplitButton::getMenu()();
				return @com.eas.client.form.Publisher::checkPublishedComponent(Ljava/lang/Object;)(menu);
			},
			set : function(aValue){
				aWidget.@com.eas.client.form.published.widgets.PlatypusSplitButton::setMenu(Lcom/google/gwt/user/client/ui/MenuBar;)(aValue != null ? aValue.unwrap() : null);
			}
		});
	}-*/;
}
