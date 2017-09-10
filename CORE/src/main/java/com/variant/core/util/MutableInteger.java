package com.variant.core.util;

public class MutableInteger {

	private Integer value;
	
	/**
	 * 
	 * @param value
	 */
	public MutableInteger(Integer value) {
		this.value = value;
	}

	/**
	 * 
	 * @return
	 */
	public int intValue() {
		return value.intValue();
	}
	
	/*
	 * 
	 */
	public void add(Number arg) {
		value = new Integer(value.intValue() + arg.intValue());
	}
}
