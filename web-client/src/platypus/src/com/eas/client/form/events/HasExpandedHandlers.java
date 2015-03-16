package com.eas.client.form.events;

import com.google.gwt.event.shared.HandlerRegistration;

public interface HasExpandedHandlers<T> {

	public HandlerRegistration addExpandedHandler(final ExpandedHandler<T> aHandler);

}
