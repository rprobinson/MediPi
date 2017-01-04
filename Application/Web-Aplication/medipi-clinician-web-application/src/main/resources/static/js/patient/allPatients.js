var refreshViewFrequency;
$(document).ready(function() {
	refreshViewFrequency = $("#refreshViewFrequency").val();
	//refreshViewFrequency = 1000000;
	showActiveMenu(NAVIGATION_LINK_MAP.PATIENT);
});

(function getPatientsDetails() {
	$.ajax( {
		url: '/clinician/patient/patientsJSON',
		success: function(patients) {
			hideErrorDiv();
			$("#patientTiles").empty();
			$.each(patients, function(counter, patient) {
				$("#patientTiles").prepend(getPatientTile(patient));
			});
		},
		complete: function() {
			//Set patient list refresh w.r.t refreshFrequency
			setTimeout(getPatientsDetails, refreshViewFrequency);
		},
		error: function(request, status, error) {
			showErrorDiv(request.responseText);
		}
	});
})();

function getPatientTile(patient) {
	var tileType;
	var tileStyle;
	if(patient.patientStatus === "INCOMPLETE_SCHEDULE") {
		tileStyle = "incomplete-schedule";
		tileType = tileStyle + "-tile";
	} else if(patient.patientStatus === "IN_THRESHOLD")  {
		tileStyle = "smiley";
		tileType = tileStyle + "-tile";
	} else if(patient.patientStatus === "OUT_OF_THRESHOLD")  {
		tileStyle = "frowney";
		tileType = tileStyle + "-tile";
	} else {
		tileStyle = "cannot-calculate";
		tileType = tileStyle + "-tile";
	}


	var tileDiv =
		'<div class="col-sm-2" id="patient-' + patient.patientUUID + '">' +
			'<a href="/clinician/patient/' + patient.patientUUID +'">' +
				'<table class="tile-view ' + tileType + '">' +
					'<tr>' +
						'<th scope="col">' +
							patient.dateOfBirth.getStringDate_DDmmmYYYY_From_Timestamp() +
						'</th>' +
					'</tr>' +
					'<tr>' +
						'<td class="limit-indicator ' + tileStyle + '"></td>' +
					'</tr>' +
					'<tr>' +
						'<th scope="col">' +
							patient.firstName + ' ' +
							patient.lastName +
						'</th>' +
					'</tr>' +
				'</table>' +
			'</a>' +
		'</div>';
	return tileDiv;
}