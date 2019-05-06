package com.variant.core.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.variant.core.schema.Flusher;
import com.variant.core.schema.Hook;
import com.variant.core.schema.Meta;

/**
 * @author Igor
 */
public class MetaImpl implements Meta {
	
	private String name = null;
	private String comment = null;
	private Flusher flusher = null;
	
	// Hooks are keyed by name.
	private LinkedList<Hook> hooks = new LinkedList<Hook>();

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
	public String getComment() {
		return comment;
	}

	@Override
	public List<Hook> getHooks() {
		ArrayList<Hook> result = new ArrayList<Hook>(hooks.size());
		result.addAll(hooks);
		return Collections.unmodifiableList(result);
	}

	@Override
	public Flusher getFlusher() {
		return flusher;
	}
		
    //---------------------------------------------------------------------------------------------//
	//                                       PUBLIC EXT                                            //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Add meta-level life-cycle hook to this schema
	 * @param hook
	 * @return true if hook didn't exist, false if did.
	 */
	public void addHook(Hook hook) {
		hooks.add(hook);
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
