package com.variant.core.schema.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.variant.core.schema.Flusher;
import com.variant.core.schema.Meta;
import com.variant.core.schema.MetaScopedHook;

/**
 * @author Igor
 */
public class MetaImpl implements Meta {
	
	private String name = null;
	private String comment = null;
	private Flusher flusher = null;	
	private List<MetaScopedHook> hooks = null;

	/**
	 */
	public MetaImpl(String name) {
		this.name = name;
	}
					
    //---------------------------------------------------------------------------------------------//
	//                                    PUBLIC INTERFACE                                         //
	//---------------------------------------------------------------------------------------------//	

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Optional<String> getComment() {
		return Optional.ofNullable(comment);
	}

	@Override
	public Optional<List<MetaScopedHook>> getHooks() {
		return hooks == null ? Optional.empty() : Optional.of(Collections.unmodifiableList(hooks));
	}

	@Override
	public Optional<Flusher> getFlusher() {
		return Optional.ofNullable(flusher);
	}
		
    //---------------------------------------------------------------------------------------------//
	//                                       PUBLIC EXT                                            //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Add meta-level life-cycle hook to this schema
	 * @param hook
	 * @return true if hook didn't exist, false if did.
	 */
	public void SetHooks(List<MetaScopedHook> hooks) {
		this.hooks = hooks;
	}

	/**
	 * Add a comment, if and when we have it.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	/**
	 * Add a flusher, if and when we have it.
	 */
	public void setFlusher(Flusher flusher) {
		this.flusher = flusher;
	}

}
