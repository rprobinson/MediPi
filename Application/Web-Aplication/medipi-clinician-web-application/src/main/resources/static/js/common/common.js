var DEFAULT_EMPTY_STRING = "- - -";
/*******************************************************************************
 * BEGIN: String related operations.
 ******************************************************************************/
/**
 * Generic method to check if the string is empty.
 * Usage: var isEmptyBooleanValue = <string>.isEmpty();
 */
String.prototype.isEmpty = function() {
	return this === null || this === undefined || this.length === 0 || this === "";
};

/**
 * Generic method to check if the string is empty and return - - - or the string.
 * Usage: var displayString = <string>.returnDefaultIfStringEmpty();
 */
String.prototype.returnDefaultIfStringEmpty = function() {
	if(this.isEmpty()) {
		return DEFAULT_EMPTY_STRING;
	} else {
		return this;
	}
};

/**
 * Generic method to check if the string is empty and print - - - or the string.
 * Usage: var displayString = <string>.printDefaultIfStringEmpty();
 */
String.prototype.printDefaultIfStringEmpty = function() {
	var printString = DEFAULT_EMPTY_STRING;
	if(!this.isEmpty()) {
		printString = this;
	}
	document.write(printString);
};

/**
 * Generic method to get the boolean value of the string.
 * Usage: var isEmptyBooleanValue = <string>.booleanValue();
 */
String.prototype.booleanValue = function() {
	var returnValue = false;
	if(!this.isEmpty()) {
		returnValue = (this == "true" ? true : false);
	}
	return returnValue;
};

/**
 * Generic method to get UUID in splitted format.
 * Usage: var splittedUUID = <string>.getSplittedUUID();
 */
String.prototype.getSplittedUUID = function() {
	var PART_SIZE = 4;
	var parts = DEFAULT_EMPTY_STRING;
	if(!this.isEmpty() && this.length == 12) {
		parts = this.substr(0, PART_SIZE) + " ";
		parts += this.substr(PART_SIZE, PART_SIZE) + " ";
		parts += this.substr(PART_SIZE * 2, PART_SIZE);
	} else {
		return this;
	}
	return parts;
};

/**
 * Method to check whether its a valid number
 * Usage: var isValid = <string>.isValidNumber();
 */
String.prototype.isValidNumber = function() {
	var isValid = false;
	if(!this.isEmpty()) {
		var number = Number(this);
		if (!isNaN(number)) {
			isValid = true;
		}
	}
	return isValid;
};

/*******************************************************************************
 * END: String related operations.
 ******************************************************************************/
/*******************************************************************************
 * START: Number related operations.
 ******************************************************************************/
/**
 * Generic method which will return the string with 2 digits if a single digit
 * number is present by prepending 0.
 * Usage: var stringNumber = <number>.getDoubleDigitNumber();
 */
Number.prototype.getDoubleDigitNumber = function() {
	var numberString = this.toString();
	if(numberString.length == 1) {
		numberString = "0" + numberString;
	}
	return numberString;
};
/*******************************************************************************
 * END: Number related operations.
 ******************************************************************************/

/*******************************************************************************
 * BEGIN: Date related operations.
 ******************************************************************************/
var MONTH_NAMES = new Array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

/**
 * Generic method which will return the date in "dd-mmm-yyyy at hh:mm" string format.
 * Usage: var stringDate = <date>.getStringDate_DDmmmYYYY_at_HHmm();
 */
String.prototype.getStringDate_DDmmmYYYY_at_HHmm = function() {
	var returnString = DEFAULT_EMPTY_STRING;
	if(!this.isEmpty()) {
		var day = null;
		var month = null;
		var year = null;
		var hours = null;
		var minutes = null;
		var date = new Date(this);
		//handled this case for BST time.
		if(isNaN(date.getTime())) {
			var splittedValues = this.split(" ");
			day = splittedValues[2];
			month = splittedValues[1];
			year = splittedValues[5];
			var time = splittedValues[3].split(":");
			hours = time[0];
			minutes = time[1];
		} else {
			day = date.getDate().getDoubleDigitNumber();
			month = MONTH_NAMES[date.getMonth()];
			year = date.getFullYear();
			hours = date.getUTCHours().getDoubleDigitNumber();
			minutes = date.getUTCMinutes().getDoubleDigitNumber();
		}
		returnString = day + "-" + month + "-" + year + " at " + hours + ":" + minutes;
	}
	return returnString;
};

/**
 * Generic method which will return the date in "dd-mmm-yyyy" string format from date javascript dateinput in string format.
 * Usage: var stringDate = <date>.getStringDate_DDmmmYYYY();
 */
String.prototype.getStringDate_DDmmmYYYY = function() {
	var returnString = DEFAULT_EMPTY_STRING;
	if(!this.isEmpty()) {
		var date = new Date(this);
		var day = date.getDate().getDoubleDigitNumber();
		var month = date.getMonth();
		var year = date.getFullYear();
		returnString = day + "-" + MONTH_NAMES[month] + "-" + year;
	}
	return returnString;
};

Number.prototype.getStringDate_DDmmmYYYY_From_Timestamp = function() {
	var returnString = DEFAULT_EMPTY_STRING;
	//if(!this.isEmpty()) {
	var date = new Date(this);
	var day = date.getDate().getDoubleDigitNumber();
	var month = date.getMonth();
	var year = date.getFullYear();
	returnString = day + "-" + MONTH_NAMES[month] + "-" + year;
	//}
	return returnString;
};

Number.prototype.getStringDate_MMDDYYYY_From_Timestamp = function() {
	var returnString = DEFAULT_EMPTY_STRING;
	//if(!this.isEmpty()) {
	var date = new Date(this);
	var day = date.getDate().getDoubleDigitNumber();
	var month = date.getMonth();
	var year = date.getFullYear();
	returnString = month.getDoubleDigitNumber() + day + year;
	//}
	return returnString;
};

Number.prototype.getStringDate_DDMMYYYY_From_Timestamp = function() {
	var returnString = DEFAULT_EMPTY_STRING;
	//if(!this.isEmpty()) {
	var date = new Date(this);
	var day = date.getDate().getDoubleDigitNumber();
	var month = date.getMonth() + 1;
	var year = date.getFullYear();
	returnString = day + "/" + month.getDoubleDigitNumber() + "/" + year;
	//}
	return returnString;
};

Number.prototype.getStringDate_DDMMYYYY_HHmm_From_Timestamp = function() {
	var returnString = DEFAULT_EMPTY_STRING;
	var date = new Date(this);
	var day = date.getDate().getDoubleDigitNumber();
	var month = date.getMonth() + 1;
	var year = date.getFullYear();
	var hours = date.getHours().getDoubleDigitNumber();
	var minutes = date.getMinutes().getDoubleDigitNumber();
	returnString = day + "/" + month.getDoubleDigitNumber() + "/" + year + " " + hours + ":" + minutes;
	return returnString;
};

/**
 * Generic method which will return the date in "dd-mmm-yyyy at hh:mm" string format.
 * Usage: var stringDate = <date>.getStringDate_DDmmmYYYY_at_HHmm();
 */
String.prototype.getCurrentStringDate_DDmmmYYYY_at_HHmm = function() {
	var returnString = DEFAULT_EMPTY_STRING;
	var date = new Date();
	var day = date.getDate().getDoubleDigitNumber();
	var month = date.getMonth();
	var year = date.getFullYear();
	var hours = date.getHours().getDoubleDigitNumber();
	var minutes = date.getMinutes().getDoubleDigitNumber();
	returnString = day + "-" + MONTH_NAMES[month - 1] + "-" + year + " at " + hours + ":" + minutes;
	return returnString;
};

/**
 * Generic method which will return the date in "dd-mmm-yyyy at hh:mm" string format.
 * Usage: var stringDate = <date>.getStringDate_DDmmmYYYY_at_HHmm();
 * Input date format should be in 'dd-mmm-yyyyThh:mm'. As par ISO statndards date and time should be 'T' separated.
 */
String.prototype.getAtSeparatedFormatedDate = function() {
	var returnString = DEFAULT_EMPTY_STRING;
	var date = new Date(this);
	var day = date.getUTCDate().getDoubleDigitNumber();
	var month = date.getUTCMonth();
	var year = date.getUTCFullYear();
	var hours = date.getUTCHours().getDoubleDigitNumber();
	var minutes = date.getUTCMinutes().getDoubleDigitNumber();
	returnString = day + "-" + MONTH_NAMES[month] + "-" + year + " at " + hours + ":" + minutes;
	return returnString;
};

/**
 * Generic method which will return the date in "dd-mmm-yyyy" string format.
 * Usage: var stringDate = <timestamp>.getStringDate_DDmmmYYYY_From_Timestamp();
 */
String.prototype.getStringDate_DDmmmYYYY_From_Timestamp = function() {
	var returnString = DEFAULT_EMPTY_STRING;
	if(!this.isEmpty()) {
		var onlydate = this.split(" ")[0];
		var splittedDate = onlydate.split("-");
		var updatedMonth = splittedDate[1] - 1;
		var date = new Date(splittedDate[0], updatedMonth, splittedDate[2]);
		var day = date.getDate().getDoubleDigitNumber();
		var month = date.getMonth();
		var year = date.getFullYear();
		returnString = day + "-" + MONTH_NAMES[month] + "-" + year;
	}
	return returnString;
};

String.prototype.getDate_From_StringDDmmmYYYY = function() {
	var date = null;
	if(!this.isEmpty()) {
		var splittedDateArray = this.split("-");
		var splittedDate = splittedDateArray[0];
		var splittedMonth = splittedDateArray[1];
		var splittedYear = splittedDateArray[2];
		date = new Date(splittedYear, MONTH_NAMES.indexOf(splittedMonth), splittedDate);

	}
	return date;
};

/*******************************************************************************
 * END: Date related operations.
 ******************************************************************************/

//private method for UTF-8 encoding
function _utf8_encode(string) {
	string = string.replace(/\r\n/g, "\n");
	var utftext = "";

	for(var n = 0; n < string.length; n++) {

		var c = string.charCodeAt(n);

		if(c < 128) {
			utftext += String.fromCharCode(c);
		} else if((c > 127) && (c < 2048)) {
			utftext += String.fromCharCode((c >> 6) | 192);
			utftext += String.fromCharCode((c & 63) | 128);
		} else {
			utftext += String.fromCharCode((c >> 12) | 224);
			utftext += String.fromCharCode(((c >> 6) & 63) | 128);
			utftext += String.fromCharCode((c & 63) | 128);
		}

	}

	return utftext;
};

/*******************************************************************************
 * START: Function to measure performance.
 ******************************************************************************/
function getPerformanceInSeconds(startTime, endTime) {
	var timeMillis = endTime - startTime;
	return parseInt(timeMillis / 1000) + "." + timeMillis % 1000 + " sec ";
}

function getPerformanceInSeconds(startTime) {
	return getPerformanceInSeconds(startTime, new Date());
}
/*******************************************************************************
 * END: Function to measure performance.
 ******************************************************************************/

/******************************************************************************************************************
 * Autocomplete feature disabled against all the forms in the application : START
 ******************************************************************************************************************/
$(document).ready(function() {
	$(document).on('focus', ':input', function() {
		$(this).attr('autocomplete', 'off');
	});
});

/******************************************************************************************************************
 * Autocomplete feature disabled against all the forms in the application : END
 ******************************************************************************************************************/

/******************************************************************************************************************
 * Functions to show/hide elements : START
 ******************************************************************************************************************/
var DEFAULT_ERROR_MESSAGE = "Please try to reload the page and if the problem still persists, please contact the system administrator.";
var DEFAULT_SUCCESS_MESSAGE = "Successfully saved.";

function hideElement(elementId) {
	$("#" + elementId).addClass("hidden");
}

function hideErrorDiv() {
	hideElement("errorMessageDiv");
}

function showErrorDiv(message) {
	$("#errorMessage").html((message != null && message != "") ? message : DEFAULT_ERROR_MESSAGE);
	$("#errorMessageDiv").removeClass("hidden");
}

function showDefaultErrorDiv() {
	showErrorDiv(DEFAULT_ERROR_MESSAGE);
}

function hideSuccessDiv() {
	hideElement("successMessageDiv");
}

function showSuccessDiv(message) {
	$("#successMessage").html((message != null && message != "") ? message : DEFAULT_SUCCESS_MESSAGE);
	$("#successMessageDiv").removeClass("hidden");
}

function showDefaultSuccessDiv() {
	showErrorDiv(DEFAULT_SUCCESS_MESSAGE);
}
/******************************************************************************************************************
 * Functions to hide elements : END
 ******************************************************************************************************************/