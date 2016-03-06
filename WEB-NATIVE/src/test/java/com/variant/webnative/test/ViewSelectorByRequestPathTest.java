package com.variant.webnative.test;

import org.junit.Test;

import com.variant.webnative.StateSelectorByRequestPath;
import com.variant.webnative.StateSelectorByRequestPathTestFacade;

import static org.junit.Assert.*;

public class ViewSelectorByRequestPathTest {

	@Test
	public void matcherTest() {
		
		assertTrue(StateSelectorByRequestPathTestFacade.match("/foo", "/foo/"));
		assertTrue(StateSelectorByRequestPathTestFacade.match("/foo", "/foo"));
		assertTrue(StateSelectorByRequestPathTestFacade.match("/foo/", "/foo/"));
		assertTrue(StateSelectorByRequestPathTestFacade.match("/foo", "/foo"));
		assertTrue(StateSelectorByRequestPathTestFacade.match("/foo/", "/foo"));
		assertTrue(StateSelectorByRequestPathTestFacade.match("/foo//", "/foo"));
		assertTrue(StateSelectorByRequestPathTestFacade.match("/foo//", "/foo/"));
		assertFalse(StateSelectorByRequestPathTestFacade.match("/foo/", "/foo/bar"));
		assertTrue(StateSelectorByRequestPathTestFacade.match("/~foo/", "/foo/"));
		assertTrue(StateSelectorByRequestPathTestFacade.match("/~f(o*)/", "/foo/"));
		assertTrue(StateSelectorByRequestPathTestFacade.match("//", "/foo/"));
		assertTrue(StateSelectorByRequestPathTestFacade.match("/~.*/", "/foo/"));
		assertTrue(StateSelectorByRequestPathTestFacade.match("///", "/foo/"));
		assertTrue(StateSelectorByRequestPathTestFacade.match("/foo//", "/foo/"));
		assertFalse(StateSelectorByRequestPathTestFacade.match("//foo/", "/foo/"));
		
		assertTrue(StateSelectorByRequestPathTestFacade.match("/foo/bar", "/foo/bar/"));
		assertTrue(StateSelectorByRequestPathTestFacade.match("/foo/bar/", "/foo/bar/"));	
		assertTrue(StateSelectorByRequestPathTestFacade.match("/foo/bar", "/foo/bar"));
		assertFalse(StateSelectorByRequestPathTestFacade.match("/foo/bar/x", "/foo/bar"));
		assertTrue(StateSelectorByRequestPathTestFacade.match("//bar/", "/foo/bar/"));		
		assertTrue(StateSelectorByRequestPathTestFacade.match("//bar/", "/foo/bar/"));		
		assertTrue(StateSelectorByRequestPathTestFacade.match("/foo//", "/foo/bar/"));		
		assertTrue(StateSelectorByRequestPathTestFacade.match("///", "/foo/bar/"));		
		assertFalse(StateSelectorByRequestPathTestFacade.match("//", "/foo/bar/"));		
		assertTrue(StateSelectorByRequestPathTestFacade.match("/~f(o*)/~b(ar)?", "/foo/bar/"));		
		assertFalse(StateSelectorByRequestPathTestFacade.match("/~f(o*)/b(ar)?", "/foo/bar/"));		
		assertTrue(StateSelectorByRequestPathTestFacade.match("/~f(o*)/~b(ar)?/", "/foo/bar/"));	
		assertTrue(StateSelectorByRequestPathTestFacade.match("/~f(o*)/~b(ar)?//", "/foo/bar/"));	
		assertTrue(StateSelectorByRequestPathTestFacade.match("/~f(o*)/~b(ar)?///", "/foo/bar/"));	
		
		assertTrue(StateSelectorByRequestPathTestFacade.match("/petclinic/owners/~\\d+/", "/petclinic/owners/11"));
	}
}
