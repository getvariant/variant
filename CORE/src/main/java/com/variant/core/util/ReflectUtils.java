package com.variant.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

public class ReflectUtils {

	/**
	 * List all static fields of a given type declared in a class or any of its superclasses.
	 * @param clazz
	 * @param type
	 * @return
	 */
	public static Collection<Field> getStaticFields(Class<?> clazz, Class<?> type) {
		ArrayList<Field> result = new ArrayList<Field>();
		for (Field field : clazz.getFields()) {
			if (Modifier.isStatic(field.getModifiers()) &&
				field.getType().isAssignableFrom(type)) {
				result.add(field);
			}
		}
		return result;
	}

	/**
	 * Find static method of a given name declared in a class or any of its superclasses.
	 * Behavior is undefined for overloaded methods, where more the name matches more
	 * than one method
	 * @param clazz
	 * @param name
	 * @return
	 */
	public static Method getStaticMethod(Class<?> clazz, String name) {
		for (Method method : clazz.getMethods()) {
			if (Modifier.isStatic(method.getModifiers()) &&
				method.getName().equals(name)) {
				return method;
			}
		}
		return null;
	}

	public static Constructor<?> getConstructor(Class<?> klass, Object arg) {
				
		for (Constructor<?> constr : klass.getConstructors()) {
			// Try to instantiate and if it works we're done.  Probably expensive.
			try {
				constr.newInstance(arg);
				return constr;
			}
			catch (Exception e) {
				//System.out.println("******** caught " + e.getClass().getName());
			}
		}
		return null;
	}

	/**
	 * Instantiate a class with the constructor that takes a given parameter class.
	 * If arg is null, a nullary constructor will be tried first, and if
	 * not found, one which takes the type given by argClass is used and is passed null.
	 * 
	 * @return null if proper constructor could not be found or the instantiated Object..
	 */
	public static Object instantiate(Class<?> klass, Class<?> argClass, Object arg) throws Exception {

		Object result = null;
		
		if (arg == null) {
			
			// First look for the nullary constructor
			Constructor<?> constructor = null;
			try {
				constructor = klass.getConstructor();
				result = constructor.newInstance();
			}
			catch (NoSuchMethodException e) {
				// Not provided -- may be okay.
			}
			
			if (constructor == null) {
				// Look for constructor which takes a single argument of any type
				// and pass it null
				try {
					constructor = klass.getConstructor(argClass);
					result = constructor.newInstance((Object)null);
		        }
				catch (NoSuchMethodException e) {
					return null;  // We didn't find the right constructor
				}
			}
		
		}
		else {

			// Look for the right constructor. This is not trivial because there may not be
			// the constructor that takes the runtime type, so we'll go up the inheritance
			// chain to find the closest that will.
			try {
				Constructor<?> constructor = klass.getConstructor(argClass);
				result = constructor.newInstance(arg);
			}
			catch (NoSuchMethodException e) {
				return null;
			}
		}
			
		return result;
	}
	
	public static Object instantiate(String className, Class<?> argClass, Object arg) throws Exception {
		Class<?> cls = Class.forName(className);
		return instantiate(cls, argClass, arg);
	}
}
