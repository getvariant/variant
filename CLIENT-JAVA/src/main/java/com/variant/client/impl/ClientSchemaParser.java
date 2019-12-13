package com.variant.client.impl;

import com.variant.core.schema.parser.SchemaParserServerless;

/**
 * Client side schema parser is the same as core, but uses a null hooks service
 * because client side life-cycle hooks follow different semantics.
 * 
 * @author Igor.
 *
 */
public class ClientSchemaParser extends SchemaParserServerless { }
