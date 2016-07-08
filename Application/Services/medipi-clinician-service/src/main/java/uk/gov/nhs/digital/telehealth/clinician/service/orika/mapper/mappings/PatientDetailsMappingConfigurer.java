package uk.gov.nhs.digital.telehealth.clinician.service.orika.mapper.mappings;

import ma.glasnost.orika.MapperFactory;

import org.springframework.stereotype.Component;

import uk.gov.nhs.digital.telehealth.clinician.service.domain.Patient;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.PatientMaster;

import com.dev.ops.common.orika.mapper.config.MappingConfigurer;

@Component
public class PatientDetailsMappingConfigurer implements MappingConfigurer {

	@Override
	public void configure(final MapperFactory factory) {
		factory.classMap(Patient.class, PatientMaster.class).byDefault().register();
	}
}