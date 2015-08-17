package com.variant.core.tests;

import java.util.Properties;

import org.junit.Test;
import static org.junit.Assert.*;

import com.variant.core.Variant;
import com.variant.core.VariantProperties;
import com.variant.core.util.VariantIoUtils;

public class VariantPropertiesTest {

	@Test
	public void test() throws Exception {
		Variant.bootstrap();
		Properties defaultProps = new Properties();
		defaultProps.load(VariantIoUtils.openResourceAsStream("/variant-defaults.props"));
		assertEquals(defaultProps.size(), VariantProperties.Keys.values().length);
		for (VariantProperties.Keys key: VariantProperties.Keys.values()) {
			assertEquals(defaultProps.getProperty(key.propName()), key.propValue());
		}
		Variant.shutdown();

		Variant.bootstrap();
	}

}
