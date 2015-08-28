package com.variant.web;

import org.junit.Test;
import static org.junit.Assert.*;

public class ViewSelectorByRequestPathTest {

	@Test
	public void matcherTest() {
		
		assertTrue(ViewSelectorByRequestPath.match("/foo", "/foo/"));
		assertTrue(ViewSelectorByRequestPath.match("/foo/", "/foo/"));
		assertFalse(ViewSelectorByRequestPath.match("/foo", "/foo"));
		assertFalse(ViewSelectorByRequestPath.match("/foo/", "/foo"));
		assertTrue(ViewSelectorByRequestPath.match("/~foo/", "/foo/"));
		assertTrue(ViewSelectorByRequestPath.match("/~f(o*)/", "/foo/"));
		assertTrue(ViewSelectorByRequestPath.match("//", "/foo/"));
		assertTrue(ViewSelectorByRequestPath.match("/~.*/", "/foo/"));
		assertFalse(ViewSelectorByRequestPath.match("///", "/foo/"));
		assertFalse(ViewSelectorByRequestPath.match("/foo//", "/foo/"));
		assertFalse(ViewSelectorByRequestPath.match("//foo/", "/foo/"));
		
		assertTrue(ViewSelectorByRequestPath.match("/foo/bar", "/foo/bar/"));
		assertTrue(ViewSelectorByRequestPath.match("/foo/bar/", "/foo/bar/"));		
		assertFalse(ViewSelectorByRequestPath.match("/foo/bar", "/foo/bar"));
		assertFalse(ViewSelectorByRequestPath.match("/foo/bar/", "/foo/bar"));
		assertTrue(ViewSelectorByRequestPath.match("//bar/", "/foo/bar/"));		
		assertTrue(ViewSelectorByRequestPath.match("/foo//", "/foo/bar/"));		
		assertTrue(ViewSelectorByRequestPath.match("///", "/foo/bar/"));		
		assertTrue(ViewSelectorByRequestPath.match("//", "/foo/bar/"));		
		assertTrue(ViewSelectorByRequestPath.match("/~f(o*)/~b(ar)?", "/foo/bar/"));		
		assertFalse(ViewSelectorByRequestPath.match("/~f(o*)/b(ar)?", "/foo/bar/"));		
		assertFalse(ViewSelectorByRequestPath.match("/~f(o*)/~b(ar)?//", "/foo/bar/"));		

	}
}
