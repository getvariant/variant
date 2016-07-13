package com.variant.core.util;

import java.lang.reflect.Field;
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
		Field[] declaredFields = clazz.getFields();
		ArrayList<Field> result = new ArrayList<Field>();
		for (Field field : declaredFields) {
			if (Modifier.isStatic(field.getModifiers()) &&
				field.getType().isAssignableFrom(type)) {
				result.add(field);
			}
		}
		return result;
	}

}
