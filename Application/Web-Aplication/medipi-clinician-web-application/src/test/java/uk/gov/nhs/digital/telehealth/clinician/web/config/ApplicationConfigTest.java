package uk.gov.nhs.digital.telehealth.clinician.web.config;

import org.junit.Assert;
import org.junit.Test;

import uk.gov.nhs.digital.telehealth.clinician.web.configurations.WebApplicationConfig;

public class ApplicationConfigTest {

	@Test
	public void testBeans() {
		final WebApplicationConfig applicationConfig = new WebApplicationConfig();
		Assert.assertNotNull("Object should not be null.", applicationConfig.restTemplate());
	}
}