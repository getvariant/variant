package com.variant.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.variant.core.schema.SchemaElement;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ParserEventListener {

	SchemaElement value();
}
