package com.variant.core.svrstub;

import java.util.List;

import com.variant.core.exception.RuntimeInternalException;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;

public class TestServerStub implements Test {

	private String name;
	
	public TestServerStub(String name) {
		this.name = name;
	}
	
	@Override
	public Experience getControlExperience() {
		throw new RuntimeInternalException("Method not supported");
	}

	@Override
	public List<Test> getCovariantTests() {
		throw new RuntimeInternalException("Method not supported");
	}

	@Override
	public Experience getExperience(String arg0) {
		throw new RuntimeInternalException("Method not supported");
	}

	@Override
	public List<Experience> getExperiences() {
		throw new RuntimeInternalException("Method not supported");
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Schema getSchema() {
		throw new RuntimeInternalException("Method not supported");
	}

	@Override
	public List<OnState> getOnStates() {
		throw new RuntimeInternalException("Method not supported");
	}

	@Override
	public OnState getOnView(State arg0) {
		throw new RuntimeInternalException("Method not supported");
	}

	@Override
	public boolean isOn() {
		throw new RuntimeInternalException("Method not supported");
	}

	@Override
	public boolean isSerialWith(Test arg0) {
		throw new RuntimeInternalException("Method not supported");
	}

	@Override
	public boolean isConcurrentWith(Test other) {
		throw new RuntimeInternalException("Method not supported");
	}

	@Override
	public boolean isCovariantWith(Test other) {
		throw new RuntimeInternalException("Method not supported");
	}

	
}
