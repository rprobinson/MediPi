package uk.gov.nhs.digital.telehealth.clinician.service.dao.impl;

import java.util.List;

import uk.gov.nhs.digital.telehealth.clinician.service.entities.PatientMaster;

import com.dev.ops.common.dao.generic.GenericDAO;
import com.dev.ops.common.domain.ContextInfo;

public interface PatientDAO extends GenericDAO<PatientMaster> {
	List<PatientMaster> fetchAllPatients(ContextInfo contextInfo);
}
