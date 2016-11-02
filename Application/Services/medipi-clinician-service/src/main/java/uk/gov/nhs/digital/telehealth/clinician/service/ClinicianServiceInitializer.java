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
package uk.gov.nhs.digital.telehealth.clinician.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan(basePackages = {"uk.gov.nhs.digital.telehealth.clinician.service.entities", "org.medipi.clinical.entities"})
@ComponentScan(basePackages = {"uk.gov.nhs.digital.telehealth.clinician.service", "com.dev.ops.common", "org.medipi.clinical.threshold", "org.medipi.clinical.dao", "org.medipi.clinical.logging"})
public class ClinicianServiceInitializer {

	public static void main(final String[] args) {
		SpringApplication.run(ClinicianServiceInitializer.class, args);
	}
}