package com.variant.core.schema;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.variant.core.LifecycleEvent;

@Retention(RetentionPolicy.RUNTIME)
public @interface EventDomain {

	LifecycleEvent.Domain value();
}
