package uk.gov.nhs.digital.telehealth.clinician.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

public class BaseController {

	@Autowired
	protected RestTemplate restTemplate;

}