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
package uk.gov.nhs.digital.telehealth.clinician.web.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import uk.gov.nhs.digital.telehealth.clinician.service.entities.Clinician;
import uk.gov.nhs.digital.telehealth.clinician.web.db.ClinicianRepository;
import uk.gov.nhs.digital.telehealth.clinician.web.db.ClinicianRoleRepository;

@Service("customClinicianDetailsService")
public class CustomClinicianDetailsService implements UserDetailsService {
	private final ClinicianRepository clinicianRepository;
	private final ClinicianRoleRepository clinicianRoleRepository;

	@Autowired
	public CustomClinicianDetailsService(final ClinicianRepository clinicianRepository, final ClinicianRoleRepository clinicianRoleRepository) {
		this.clinicianRepository = clinicianRepository;
		this.clinicianRoleRepository = clinicianRoleRepository;
	}

	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		Clinician clinician = clinicianRepository.findByUserName(username);
		if(null == clinician) {
			throw new UsernameNotFoundException("No user present with username: " + username);
		} else {
			List<String> userRoles = clinicianRoleRepository.findRoleByUserName(username);
			return new CustomClinicianDetails(clinician, userRoles);
		}
	}
}