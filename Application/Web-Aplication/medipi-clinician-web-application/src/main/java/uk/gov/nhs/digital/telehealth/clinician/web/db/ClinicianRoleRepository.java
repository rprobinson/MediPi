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
package uk.gov.nhs.digital.telehealth.clinician.web.db;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.gov.nhs.digital.telehealth.clinician.service.entities.ClinicianRole;

@Repository
public interface ClinicianRoleRepository extends CrudRepository<ClinicianRole, Long> {

	@Query("select clinicianRole.role from ClinicianRole clinicianRole, Clinician clinician where clinician.userName=?1 and clinicianRole.clinicianId=clinician.clinicianId")
	public List<String> findRoleByUserName(String username);
}