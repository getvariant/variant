package com.variant.core.flashpoint;

public interface FlashpointListener<F extends Flashpoint> {
	
	public void reached(F flashpoint);

}