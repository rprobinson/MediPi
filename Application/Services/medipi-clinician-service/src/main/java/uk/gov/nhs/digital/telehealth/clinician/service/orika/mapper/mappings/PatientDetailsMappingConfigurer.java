/*
 *
 * Copyright (C) 2016 Krishna Kuntala @ Mastek <krishna.kuntala@mastek.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.gov.nhs.digital.telehealth.clinician.service.orika.mapper.mappings;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;

import org.springframework.stereotype.Component;

import uk.gov.nhs.digital.telehealth.clinician.service.domain.Measurement;
import uk.gov.nhs.digital.telehealth.clinician.service.domain.Patient;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.PatientMaster;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.RecordingDeviceDataMaster;

import com.dev.ops.common.orika.mapper.config.MappingConfigurer;

@Component
public class PatientDetailsMappingConfigurer implements MappingConfigurer {

	@Override
	public void configure(final MapperFactory factory) {
		factory.classMap(Patient.class, PatientMaster.class).byDefault().register();

		//@formatter:off

		factory.classMap(PatientMaster.class, Patient.class).customize(new CustomMapper<PatientMaster, Patient>() {
			@Override
			public void mapAtoB(final PatientMaster patientMaster, final Patient patient, final MappingContext context) {
				if(!patientMaster.getPatientGroups().isEmpty()) {
					patient.setPatientGroupId(patientMaster.getPatientGroups().get(0).getPatientGroupId());
				}
			}
		}).byDefault().register();

		factory.classMap(RecordingDeviceDataMaster.class, Measurement.class)
		.byDefault()
		.field("dataValueTime", "dataTime")
		.field("dataValue", "value")
		//@formatter:on
		.register();
	}
}