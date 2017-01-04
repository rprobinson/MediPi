/*****************************************************************************************************************
 * BEGIN: Data table related constants.
 * ****************************************************************************************************************/
//var const DATATABLE_OLANGUAGE='"sLengthMenu" : "_MENU_","sInfo" : "Showing:_START_ to _END_ of _TOTAL_ ","sInfoEmpty" : "Showing: 0 to _END_ of _TOTAL_ ","sSearch" : "Filter:  ","sZeroRecords" : "No Patient found","sLengthMenu" : \'<select id="sbox_\' + PATIENTS_TABLE_ID + \'" name="table_\' + PATIENTS_TABLE_ID + \'"_length">\' + \'<option value="10">10</option>\' + \'<option value="25">25</option>\' + \'<option value="50">50</option>\' + \'<option value="100">100</option>\' + \'</select>\'';
var defaultDataTableConstants = {
	sLengthMenu: "_MENU_",
	sInfo: "Showing:_START_ to _END_ of _TOTAL_ ",
	sInfoEmpty: "Showing: 0 to _END_ of _TOTAL_ ",
	sSearch: "Filter:  ",
	sZeroRecords: "No {recordType} found",
	sLengthMenu: "<select id='sbox_{datatableName}' name='table_{datatableName}_length'><option value='10'>10</option><option value='25'>25</option><option value='50'>50</option><option value='100'>100</option></select>",
	sDom: "<\"top\"fCTrt<\"bottom\"lip><\"clear\">"
};
/*****************************************************************************************************************
 * END: Data table related constants.
 * ****************************************************************************************************************/

/*****************************************************************************************************************
 * BEGIN: Date Time sorting function.
 * ****************************************************************************************************************/
var customDateDDMMMYYYYToOrd = function(date) {
	"use strict";
	var reggie = /(\d{2})-([a-zA-Z]{3})-(\d{4}) (\d{2}):(\d{2})/;
	var dateArray = reggie.exec(date);
	var dateObject = new Date((dateArray[3]), $.inArray((dateArray[2]), ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]), (dateArray[1]), (dateArray[4]), (dateArray[5]));
	return dateObject;
};

/*******************************************************************************
 * END: Date Time sorting function.
 ******************************************************************************/

/*******************************************************************************
 * Define the sorts ascending or descending.
 ******************************************************************************/
jQuery.fn.dataTableExt.oSort['date-dd-mmm-yyyy-asc'] = function(a, b) {
	"use strict"; // let's avoid tom-foolery in this function
	var ordA = customDateDDMMMYYYYToOrd(a), ordB = customDateDDMMMYYYYToOrd(b);
	return (ordA < ordB) ? -1 : ((ordA > ordB) ? 1 : 0);
};

jQuery.fn.dataTableExt.oSort['date-dd-mmm-yyyy-desc'] = function(a, b) {
	"use strict"; // let's avoid tom-foolery in this function
	var ordA = customDateDDMMMYYYYToOrd(a), ordB = customDateDDMMMYYYYToOrd(b);
	return (ordA < ordB) ? 1 : ((ordA > ordB) ? -1 : 0);
};
/*******************************************************************************
 * End Date time Sorting functions.
 ******************************************************************************/

var customDateDDMMMYYYY = function(date) {
	"use strict";
	var reggie = /(\d{2})-([a-zA-Z]{3})-(\d{4})/;
	var dateArray = reggie.exec(date);
	var dateObject = new Date((dateArray[3]), $.inArray((dateArray[2]), ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]), (dateArray[1]));
	return dateObject;
};

jQuery.fn.dataTableExt.oSort['dateOnly-dd-mmm-yyyy-asc'] = function(a, b) {
	"use strict"; // let's avoid tom-foolery in this function
	var ordA = customDateDDMMMYYYY(a), ordB = customDateDDMMMYYYY(b);
	return (ordA < ordB) ? -1 : ((ordA > ordB) ? 1 : 0);
};

jQuery.fn.dataTableExt.oSort['dateOnly-dd-mmm-yyyy-desc'] = function(a, b) {
	"use strict"; // let's avoid tom-foolery in this function
	var ordA = customDateDDMMMYYYY(a), ordB = customDateDDMMMYYYY(b);
	return (ordA < ordB) ? 1 : ((ordA > ordB) ? -1 : 0);
};

/*******************************************************************************
 * BEGIN: Datatable related functions.
 ******************************************************************************/

/**
 * This method gives the information about the data table.
 * @param oSettings
 */
$.fn.dataTableExt.oApi.fnPagingInfo = function(oSettings) {
	return {
		"iStart": oSettings._iDisplayStart,
		"iEnd": oSettings.fnDisplayEnd(),
		"iLength": oSettings._iDisplayLength,
		"iTotal": oSettings.fnRecordsTotal(),
		"iFilteredTotal": oSettings.fnRecordsDisplay(),
		"iPage": oSettings._iDisplayLength === -1 ? 0 : Math.ceil(oSettings._iDisplayStart / oSettings._iDisplayLength),
		"iTotalPages": oSettings._iDisplayLength === -1 ? 0 : Math.ceil(oSettings.fnRecordsDisplay() / oSettings._iDisplayLength)
	};
};

/**
 * Checks number of check boxes are selected and udpdates
 * select all flag for the current page.
 * @param tableId
 * @param checkboxName
 * @param selectAllStatusArray
 * @param currentPageNumber
 */
function _tableRedrawCallback(tableId, checkboxName, selectAllStatusArray, currentPageNumber) {
	var currentRows = $('#' + tableId + ' tbody tr');
	var selectAllFlag = true;
	$.each(currentRows, function() {
		$(this).find(':checkbox[name=' + checkboxName + ']').each(function() {
			if(!$(this).prop('checked')) {
				selectAllFlag = false;
			}
		});
	});
	if(currentRows.length < 2) {
		selectAllFlag = false;
	}
	selectAllStatusArray[currentPageNumber] = selectAllFlag;
};

/**
 * Filter the dropdown values of datatable if the user
 * enters the filter criteria.
 * @param tableId
 * @param dropDownId
 */
function _updateDropDown(tableId) {
	var dropDownId = "#sbox_" + tableId;
	var oTable = $('#' + tableId).dataTable();
	var tableRecords = oTable.fnSettings().fnRecordsDisplay();
	$(dropDownId).find('option:eq(0)').show().prop('disabled', false);
	$(dropDownId).find('option:eq(1)').show().prop('disabled', false);
	$(dropDownId).find('option:eq(2)').show().prop('disabled', false);
	$(dropDownId).find('option:eq(3)').show().prop('disabled', false);
	if(tableRecords <= 10) {
		$(dropDownId).find('option:eq(1)').hide().prop('disabled', true);
		$(dropDownId).find('option:eq(2)').hide().prop('disabled', true);
		$(dropDownId).find('option:eq(3)').hide().prop('disabled', true);
	} else if(tableRecords <= 25) {
		$(dropDownId).find('option:eq(2)').hide().prop('disabled', true);
		$(dropDownId).find('option:eq(3)').hide().prop('disabled', true);
	} else if(tableRecords <= 50) {
		$(dropDownId).find('option:eq(3)').hide().prop('disabled', true);
	}
}

/**
 *  Updates the dropdown w.r.t. number of rows visible in the datatable
 *  values of datatable if the user enters the filter criteria.
 * @param tableId
 */
function _changeDropDownValueAfterFilter(tableId) {
	var recordDisplayedOnPage = $('#' + tableId + ' tbody tr').length;
	if(recordDisplayedOnPage <= 10) {
		$('#sbox_' + tableId).val('10');
	} else if(recordDisplayedOnPage <= 25) {
		$('#sbox_' + tableId).val('25');
	} else if(recordDisplayedOnPage <= 50) {
		$('#sbox_' + tableId).val('50');
	} else if(recordDisplayedOnPage > 50) {
		$('#sbox_' + tableId).val('100');
	}
};

/**
 * Sets ajax caching to false.
 */
$(document).ready(function() {
	$.ajaxSetup(
	{
		cache: false
	});
});

/**
 * Handles ajax errors related to datatables.
 */
function handleAjaxError(e, settings, techNote, message) {
	$("#errorMessage").html("Please try to reload the page and if the problem still persists, please contact the system administrator.")
	$("#errorMessageDiv").removeClass("hidden");
}
/*******************************************************************************
 * END: Datatable related functions.
 ******************************************************************************/