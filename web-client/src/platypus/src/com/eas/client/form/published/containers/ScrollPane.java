package com.eas.client.form.published.containers;

import com.bearsoft.gwt.ui.XElement;
import com.bearsoft.gwt.ui.containers.ScrollBoxPanel;
import com.eas.client.form.EventsExecutor;
import com.eas.client.form.events.AddEvent;
import com.eas.client.form.events.AddHandler;
import com.eas.client.form.events.HasAddHandlers;
import com.eas.client.form.events.HasHideHandlers;
import com.eas.client.form.events.HasRemoveHandlers;
import com.eas.client.form.events.HasShowHandlers;
import com.eas.client.form.events.HideEvent;
import com.eas.client.form.events.HideHandler;
import com.eas.client.form.events.RemoveEvent;
import com.eas.client.form.events.RemoveHandler;
import com.eas.client.form.events.ShowEvent;
import com.eas.client.form.events.ShowHandler;
import com.eas.client.form.published.HasComponentPopupMenu;
import com.eas.client.form.published.HasEventsExecutor;
import com.eas.client.form.published.HasJsFacade;
import com.eas.client.form.published.HasPublished;
import com.eas.client.form.published.menu.PlatypusPopupMenu;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

public class ScrollPane extends ScrollBoxPanel implements HasJsFacade, HasEnabled, HasComponentPopupMenu, HasEventsExecutor,
		HasShowHandlers, HasHideHandlers, HasResizeHandlers, HasAddHandlers, HasRemoveHandlers, HasChildrenPosition {

	/**
	 * Used to set the vertical scroll bar policy so that vertical scrollbars
	 * are displayed only when needed.
	 */
	public static final int VERTICAL_SCROLLBAR_AS_NEEDED = 20;
	/**
	 * Used to set the vertical scroll bar policy so that vertical scrollbars
	 * are never displayed.
	 */
	public static final int VERTICAL_SCROLLBAR_NEVER = 21;
	/**
	 * Used to set the vertical scroll bar policy so that vertical scrollbars
	 * are always displayed.
	 */
	public static final int VERTICAL_SCROLLBAR_ALWAYS = 22;

	/**
	 * Used to set the horizontal scroll bar policy so that horizontal
	 * scrollbars are displayed only when needed.
	 */
	public static final int HORIZONTAL_SCROLLBAR_AS_NEEDED = 30;
	/**
	 * Used to set the horizontal scroll bar policy so that horizontal
	 * scrollbars are never displayed.
	 */
	public static final int HORIZONTAL_SCROLLBAR_NEVER = 31;
	/**
	 * Used to set the horizontal scroll bar policy so that horizontal
	 * scrollbars are always displayed.
	 */
	public static final int HORIZONTAL_SCROLLBAR_ALWAYS = 32;

	protected EventsExecutor eventsExecutor;
	protected PlatypusPopupMenu menu;
	protected boolean enabled = true;
	protected String name;
	protected JavaScriptObject published;

	protected int verticalScrollBarPolicy;
	protected int horizontalScrollBarPolicy;

	public ScrollPane() {
		super();
		Element sollableElem = getElement().getFirstChild().cast();
		sollableElem.getStyle().setPosition(Position.ABSOLUTE);
		sollableElem.getStyle().setProperty("boxSizing", "border-box");
		sollableElem.getStyle().setWidth(100, Style.Unit.PCT);
		sollableElem.getStyle().setHeight(100, Style.Unit.PCT);
	}

	@Override
	public HandlerRegistration addAddHandler(AddHandler handler) {
		return addHandler(handler, AddEvent.getType());
	}

	@Override
	public HandlerRegistration addRemoveHandler(RemoveHandler handler) {
		return addHandler(handler, RemoveEvent.getType());
	}

	@Override
	public HandlerRegistration addResizeHandler(ResizeHandler handler) {
		return addHandler(handler, ResizeEvent.getType());
	}

	@Override
	public void onResize() {
		super.onResize();
		if (isAttached()) {
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
	public EventsExecutor getEventsExecutor() {
		return eventsExecutor;
	}

	@Override
	public void setEventsExecutor(EventsExecutor aExecutor) {
		eventsExecutor = aExecutor;
	}

	@Override
	public PlatypusPopupMenu getPlatypusPopupMenu() {
		return menu;
	}

	protected HandlerRegistration menuTriggerReg;

	@Override
	public void setPlatypusPopupMenu(PlatypusPopupMenu aMenu) {
		if (menu != aMenu) {
			if (menuTriggerReg != null)
				menuTriggerReg.removeHandler();
			menu = aMenu;
			if (menu != null) {
				menuTriggerReg = super.addDomHandler(new ContextMenuHandler() {

					@Override
					public void onContextMenu(ContextMenuEvent event) {
						event.preventDefault();
						event.stopPropagation();
						menu.setPopupPosition(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
						menu.show();
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
		if (!oldValue && enabled) {
			getElement().<XElement> cast().unmask();
		} else if (oldValue && !enabled) {
			getElement().<XElement> cast().disabledMask();
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

	public int getVerticalScrollBarPolicy() {
		return verticalScrollBarPolicy;
	}

	public int getHorizontalScrollBarPolicy() {
		return horizontalScrollBarPolicy;
	}

	public void setVerticalScrollBarPolicy(int aValue) {
		verticalScrollBarPolicy = aValue;
		applyPolicies();
	}

	public void setHorizontalScrollBarPolicy(int aValue) {
		horizontalScrollBarPolicy = aValue;
		applyPolicies();
	}

	protected void applyPolicies() {
		switch (horizontalScrollBarPolicy) {
		case HORIZONTAL_SCROLLBAR_ALWAYS:
			setHorizontalScrollPolicy(ScrollPolicy.ALLWAYS);
			break;
		case HORIZONTAL_SCROLLBAR_AS_NEEDED:
			setHorizontalScrollPolicy(ScrollPolicy.AUTO);
			break;
		case HORIZONTAL_SCROLLBAR_NEVER:
			setHorizontalScrollPolicy(ScrollPolicy.NEVER);
			break;
		default:
			setHorizontalScrollPolicy(ScrollPolicy.AUTO);
		}
		switch (verticalScrollBarPolicy) {
		case VERTICAL_SCROLLBAR_ALWAYS:
			setVerticalScrollPolicy(ScrollPolicy.ALLWAYS);
			break;
		case VERTICAL_SCROLLBAR_AS_NEEDED:
			setVerticalScrollPolicy(ScrollPolicy.AUTO);
			break;
		case VERTICAL_SCROLLBAR_NEVER:
			setVerticalScrollPolicy(ScrollPolicy.NEVER);
			break;
		default:
			setVerticalScrollPolicy(ScrollPolicy.AUTO);
		}
	}

	public static void ajustWidth(Widget aChild, int aValue) {
		if (aChild != null) {
			XElement xwe = aChild.getElement().<XElement> cast();
			int hDelta = xwe.getOffsetWidth() - xwe.getContentWidth();
			xwe.getStyle().setWidth(aValue - hDelta, Style.Unit.PX);
		}
	}

	public static void ajustHeight(Widget aChild, int aValue) {
		if (aChild != null) {
			XElement xwe = aChild.getElement().<XElement> cast();
			int hDelta = xwe.getOffsetHeight() - xwe.getContentHeight();
			xwe.getStyle().setHeight(aValue - hDelta, Style.Unit.PX);
		}
	}

	@Override
	public void setWidget(Widget w) {
		super.setWidget(w);
		AddEvent.fire(this, w);
	}

	@Override
	public boolean remove(Widget w) {
		boolean res = super.remove(w);
		if (res) {
			RemoveEvent.fire(this, w);
		}
		return res;
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
		published.add = function(toAdd){
			if(toAdd && toAdd.unwrap){
				if(toAdd.parent == published)
					throw 'A widget already added to this container';
				aWidget.@com.eas.client.form.published.containers.ScrollPane::setWidget(Lcom/google/gwt/user/client/ui/Widget;)(toAdd.unwrap());
			}
		}
		Object.defineProperty(published, "view", {
			get : function(){
				var widget = aWidget.@com.eas.client.form.published.containers.ScrollPane::getWidget()();
				return @com.eas.client.form.Publisher::checkPublishedComponent(Ljava/lang/Object;)(widget);
			},
			set : function(aValue){
				if(aValue != null)
					published.add(aValue);
				else
					published.clear();
			}
		});
	}-*/;

	@Override
	public int getTop(Widget aWidget) {
		assert aWidget.getParent() == this : "widget should be a child of this container";
		return 0;
	}

	@Override
	public int getLeft(Widget aWidget) {
		assert aWidget.getParent() == this : "widget should be a child of this container";
		return 0;
	}
}
