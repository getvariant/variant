package com.variant.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

public class VariantReflectUtils {

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

}
