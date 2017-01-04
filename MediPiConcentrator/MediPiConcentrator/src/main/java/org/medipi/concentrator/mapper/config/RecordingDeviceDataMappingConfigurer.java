/*
 Copyright 2016  Richard Robinson @ NHS Digital <rrobinson@nhs.net>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.medipi.concentrator.mapper.config;

import ma.glasnost.orika.MapperFactory;
import org.springframework.stereotype.Component;
import org.medipi.concentrator.entities.RecordingDeviceData;

/**
 * Class to map/exclude data field names in the RecordingDeviceData class in order
 * that the RecordingDeviceData will serialise without recursive data patterns.
 *
 * @author rick@robinsonhq.com
 */

@Component
public class RecordingDeviceDataMappingConfigurer implements MappingConfigurer {

	@Override
	public void configure(final MapperFactory factory) {
		factory.classMap(RecordingDeviceData.class, RecordingDeviceData.class)
                        .exclude("patientUuid")
                        .byDefault().register();
	}
}