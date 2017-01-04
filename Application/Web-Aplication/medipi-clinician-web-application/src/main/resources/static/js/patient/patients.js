var PATIENTS_TABLE_ID = "patients";
var refreshViewFrequency;
$(document).ready(function() {
	refreshViewFrequency = $("#refreshViewFrequency").val();
	initialisePatientsDatatable();
	showActiveMenu(NAVIGATION_LINK_MAP.PATIENT);
});

/*******************************************************************************
 * START: initialise datatable.
 ******************************************************************************/
$.fn.dataTable.ext.errMode = 'none';
function initialisePatientsDatatable() {
	$('#' + PATIENTS_TABLE_ID).on('error.dt', function(e, settings, techNote, message) {
		handleAjaxError(e, settings, techNote, message);
	}).dataTable(
	{
		"sAjaxSource": "/clinician/patient/patientsJSON",
		"sAjaxDataProp": "patients",
		"sServerMethod": "GET",
		"sAjaxDataProp": "",
		"bProcessing": true,
		"bDeferRender": true,
		"sPaginationType": "full_numbers",
		//"aaSorting": [[4, 'desc']],
		"oLanguage":
		{
			"sLengthMenu": defaultDataTableConstants.sLengthMenu,
			"sInfo": defaultDataTableConstants.sInfo,
			"sInfoEmpty": defaultDataTableConstants.sInfoEmpty,
			"sSearch": defaultDataTableConstants.sSearch,
			"sZeroRecords": defaultDataTableConstants.sZeroRecords.replaceAll("{recordType}", PATIENTS_TABLE_ID),
			"sLengthMenu": defaultDataTableConstants.sLengthMenu.replaceAll("{datatableName}", PATIENTS_TABLE_ID)
		},
		"aoColumns": [
		{
			"mData": "patientUUID"
		},
		{
			"mData": null,
			"mRender": function(data, type, patient) {
				if(type === 'display') {
					return '<a href="/clinician/patient/' + patient.patientUUID + '" >' + patient.nhsNumber + '</a>';
				} else {
					return patient.nhsNumber;
				}
			}
		},
		{
			"mData": null,
			"mRender": function(data, type, patient) {
				if(type === 'display') {
					return '<a href="/clinician/patient/' + patient.patientUUID + '" >' + patient.firstName + " " + patient.lastName + '</a>';
				} else {
					return patient.firstName + " " + patient.lastName;
				}
			}
		},
		{
			"mData": null,
			"mRender": function(data, type, patient) {
				if(type === 'display' || type === 'filter') {
					return patient.dateOfBirth.getStringDate_DDmmmYYYY_From_Timestamp();
				} else {
					return patient.dateOfBirth;
				}
			}
		},
		{
			"mData": null,
			"mRender": function(data, type, patient) {
				if(type === 'display') {
					if(patient.critical) {
						return '<a href="/clinician/patient/' + patient.patientUUID + '" ><img src="/images/misc/red.png"></a>';
					} else {
						return '<a href="/clinician/patient/' + patient.patientUUID + '" ><img src="/images/misc/green.png"></a>';
					}
				} else {
					return patient.critical;
				}
			}
		}],
		"sDom": defaultDataTableConstants.sDom,
		"drawCallback": function(settings) {
			hideErrorDiv();
		}
	});

	setInterval( function () {
		$('#' + PATIENTS_TABLE_ID).dataTable().api().ajax.reload();
	}, refreshViewFrequency );
}

$(document).on('keyup', '#' + PATIENTS_TABLE_ID + '_wrapper input', function() {
	_changeDropDownValueAfterFilter(PATIENTS_TABLE_ID);
});

$(document).on('focus', "#sbox_" + PATIENTS_TABLE_ID, function(event) {
	_updateDropDown(PATIENTS_TABLE_ID);
});
/*******************************************************************************
 * END: initialise datatable.
 ******************************************************************************/