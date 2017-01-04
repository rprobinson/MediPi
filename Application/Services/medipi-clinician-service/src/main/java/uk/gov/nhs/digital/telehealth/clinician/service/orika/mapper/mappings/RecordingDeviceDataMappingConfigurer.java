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

import ma.glasnost.orika.MapperFactory;

import org.medipi.clinical.entities.RecordingDeviceData;
import org.springframework.stereotype.Component;

import uk.gov.nhs.digital.telehealth.clinician.service.entities.RecordingDeviceDataMaster;

import com.dev.ops.common.orika.mapper.config.MappingConfigurer;

@Component
public class RecordingDeviceDataMappingConfigurer implements MappingConfigurer {

	@Override
	public void configure(final MapperFactory factory) {

		//@formatter:off

		factory.classMap(RecordingDeviceDataMaster.class, RecordingDeviceData.class)
			.byDefault()
			.field("submittedTime", "downloadedTime")
			.field("recordingDeviceAttribute.attributeId", "attributeId.attributeId")
			.field("patient.patientUUID", "patientUuid.patientUuid")

		//@formatter:on
		.register();
	}
}