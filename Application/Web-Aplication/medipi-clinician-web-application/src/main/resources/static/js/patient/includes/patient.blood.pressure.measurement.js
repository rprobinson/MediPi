var measurement = {
    getData: function (includeObject, attributeId) {
        var formattedstudentListArray = [];
        var data = null;
        $.ajax({
            async: false,
            url: "/clinician/patient/patientMeasurements/" + includeObject.patientUUID + "/" + attributeId,
            dataType: "json",
            success: function (pulseData) {
                data = pulseData;
            },
            error: function(request, status, error) {
            	showDefaultErrorDiv();
            }
        });
        return data;
    },

    getLatestAttributeThreshold: function (includeObject, attributeId) {
        var formattedstudentListArray = [];
        var data = null;
        $.ajax({
            async: false,
            url: "/clinician/attributeThreshold/" + includeObject.patientUUID + "/" + attributeId,
            dataType: "json",
            success: function (attributeThreshold) {
                data = attributeThreshold;
            },
            error: function(request, status, error) {
            	if(request.status != 200) {
            		showDefaultErrorDiv();
            	}
            }
        });
        return data;
    },

    createChartData: function (jsonData, includeObject) {
        return {
            labels: jsonData[0].timeMapProperty('dataTime'),
            datasets: [
				{
				    label: includeObject.diastolicAttributeName + " min",
				    fill: false,
				    borderColor: 'rgba(111,255,175,1)',
				    backgroundColor: 'rgba(111,255,175,1)',
				    data: jsonData[1].mapValue('minValue'),
				    borderDash: [10, 7],
				    lineTension: 0
				},
				{
                    label: includeObject.diastolicAttributeName,
                    fill: false,
                    borderColor: 'rgba(0,176,80,1)',
                    backgroundColor: 'rgba(0,176,80,1)',
                    data: jsonData[1].mapValue('value'),
                    lineTension: 0
                },
				{
				    label: includeObject.diastolicAttributeName + " max",
				    fill: false,
				    borderColor: 'rgba(0,111,50,1)',
				    backgroundColor: 'rgba(0,111,50,1)',
				    data: jsonData[1].mapValue('maxValue'),
				    borderDash: [10, 5],
				    lineTension: 0
				},
				{
				    label: includeObject.systolicAttributeName + " min",
				    fill: false,
				    borderColor: 'rgba(174,198,225,1)',
				    backgroundColor: 'rgba(174,198,225,1)',
				    data: jsonData[0].mapValue('minValue'),
				    borderDash: [10, 7],
				    lineTension: 0
				},
                {
                    label: includeObject.systolicAttributeName,
                    borderColor: 'rgba(53,94,142,1)',
                    backgroundColor: 'rgba(53,94,142,1)',
                    fill: false,
                    data: jsonData[0].mapValue('value'),
                    lineTension: 0
                },
				{
				    label: includeObject.systolicAttributeName + " max",
				    fill: false,
				    borderColor: 'rgba(30,53,81,1)',
				    backgroundColor: 'rgba(30,53,81,1)',
				    data: jsonData[0].mapValue('maxValue'),
				    borderDash: [10, 5],
				    lineTension: 0
				}
            ]
        };
    },

    renderChart: function (chartData, includeObject) {
        var context2D = document.getElementById(includeObject.canvasId).getContext("2d");
        var timeFormat = 'DD/MM/YYYY HH:mm';
        var myChart = new Chart(context2D, {
            type: 'line',
            data: chartData,
            options: {
                responsive: true,
                scales: {
                	xAxes: [{
                		type: "time",
                		time: {
                			format: timeFormat,
                			tooltipFormat: 'll HH:mm'
                		},
                		scaleLabel: {
                			display: true,
                		}
                	}],
                    yAxes: [{
                        display: true,
                        scaleLabel: {
                            show: true,
                        },
                        ticks: {
                            suggestedMin: includeObject.suggestedMinValue,
                            suggestedMax: includeObject.suggestedMaxValue,
                        }
                    }]
                }
            }
        });
        return myChart;
    },

    updateRecentMeasuremnts: function (systolicData, diastolicData, includeObject) {
    	var measurementIndicatorClass;
    	$("#" + includeObject.recentMeasurementDateId).html(systolicData != null ? systolicData.dataTime.getStringDate_DDMMYYYY_HHmm_From_Timestamp() : "- - -");
    	if(systolicData != null && diastolicData != null) {
    		var bloodPressureMeasurements = "<u>" + systolicData.value + "</u><br/>" + diastolicData.value;
    		$("#" + includeObject.recentMeasurementValueId).html(bloodPressureMeasurements);

    		/*//If within min and max limits
    		if(systolicData.minValue == null || systolicData.maxValue == null || diastolicData.minValue == null || diastolicData.maxValue == null) {
    			measurementIndicatorClass = "amber";
	        } else if(parseFloat(systolicData.minValue) <= parseFloat(systolicData.value) && parseFloat(systolicData.value) <= parseFloat(systolicData.maxValue) && parseFloat(diastolicData.minValue) <= parseFloat(diastolicData.value) && parseFloat(diastolicData.value) <= parseFloat(diastolicData.maxValue)) {
	        	measurementIndicatorClass = "green";
	        } else {
	        	measurementIndicatorClass = "red";
	        }*/

    		if(systolicData.alertStatus == "OUT_OF_THRESHOLD" || diastolicData.alertStatus == "OUT_OF_THRESHOLD"){
	        	$("#" + includeObject.recentMeasurementValueId).attr("class", "red");
	        } else if(systolicData.alertStatus == "IN_THRESHOLD" && diastolicData.alertStatus == "IN_THRESHOLD") {
	        	$("#" + includeObject.recentMeasurementValueId).attr("class", "green");
	        } else if(systolicData.alertStatus == "EXPIRED_MEASUREMENT" || diastolicData.alertStatus == "EXPIRED_MEASUREMENT"){
	        	$("#" + includeObject.recentMeasurementValueId).attr("class", "grey");
	        } else {
	        	$("#" + includeObject.recentMeasurementValueId).attr("class", "amber");
	        }

        } else {
        	$("#" + includeObject.recentMeasurementValueId).html("- - -");
        	measurementIndicatorClass = "amber";
        }
    	$("#" + includeObject.recentMeasurementValueId).attr("class", measurementIndicatorClass);
    },

    updateAttributeThreshold: function (systolicAttributeThreshold, diastolicAttributeThreshold) {
    	//Update systolic threshold values
        $("#" + includeObject.measurementSystolicMinValueId).html(systolicAttributeThreshold != null ? (systolicAttributeThreshold.thresholdLowValue != null ? systolicAttributeThreshold.thresholdLowValue : "- - -") : "- - -");
        $("#" + includeObject.measurementSystolicMinValueId + "-value").val(systolicAttributeThreshold != null ? (systolicAttributeThreshold.thresholdLowValue != null ? systolicAttributeThreshold.thresholdLowValue : "") : "");
        $("#" + includeObject.measurementSystolicMaxValueId).html(systolicAttributeThreshold != null ? (systolicAttributeThreshold.thresholdHighValue != null ? systolicAttributeThreshold.thresholdHighValue : "- - -") : "- - -");
        $("#" + includeObject.measurementSystolicMaxValueId + "-value").val(systolicAttributeThreshold != null ? (systolicAttributeThreshold.thresholdHighValue != null ? systolicAttributeThreshold.thresholdHighValue : "- - -") : "");

        //Update diastolic threshold values
        $("#" + includeObject.measurementDiastolicMinValueId).html(diastolicAttributeThreshold != null ? (diastolicAttributeThreshold.thresholdLowValue != null ? diastolicAttributeThreshold.thresholdLowValue : "- - -") : "- - -");
        $("#" + includeObject.measurementDiastolicMinValueId + "-value").val(diastolicAttributeThreshold != null ? (diastolicAttributeThreshold.thresholdLowValue != null ? diastolicAttributeThreshold.thresholdLowValue : "") : "");
        $("#" + includeObject.measurementDiastolicMaxValueId).html(diastolicAttributeThreshold != null ? (diastolicAttributeThreshold.thresholdHighValue != null ? diastolicAttributeThreshold.thresholdHighValue : "- - -") : "- - -");
        $("#" + includeObject.measurementDiastolicMaxValueId + "-value").val(diastolicAttributeThreshold != null ? (diastolicAttributeThreshold.thresholdHighValue != null ? diastolicAttributeThreshold.thresholdHighValue : "- - -") : "");
    },

    initChart: function (includeObject) {
        var systolicData = measurement.getData(includeObject, includeObject.systolicAttributeId);
        var diastolicData = measurement.getData(includeObject, includeObject.diastolicAttributeId);
        var lastSystolicData = systolicData.lastObject();
        var lastDiastolicData = diastolicData.lastObject();

        var systolicAttributeThreshold = measurement.getLatestAttributeThreshold(includeObject, includeObject.systolicAttributeId);
        var diastolicAttributeThreshold = measurement.getLatestAttributeThreshold(includeObject, includeObject.diastolicAttributeId);

        chartData = measurement.createChartData([systolicData, diastolicData], includeObject);
        measurement.renderChart(chartData, includeObject);

        measurement.updateRecentMeasuremnts(lastSystolicData, lastDiastolicData, includeObject);
        measurement.updateAttributeThreshold(systolicAttributeThreshold, diastolicAttributeThreshold);
    }
};

/*******************************************************************************
 * BEGIN: Functions related to editable threshold values.
 ******************************************************************************/
function showBloodPressureEditableFields(canvasId) {
	var formId = "#" + canvasId +"-attributeThreshold";

	//Systolic:Existing values for threshold.
	var existingSystolicThresholdLowValueTD = $("#" + canvasId + "-systolic-threshold").find("[name='existingSystolicThresholdLowValue']");
	var existingSystolicThresholdHighValueTD = $("#" + canvasId + "-systolic-threshold").find("[name='existingSystolicThresholdHighValue']");

	var existingSystolicThresholdLowValue = existingSystolicThresholdLowValueTD.html();
	var existingSystolicThresholdHighValue = existingSystolicThresholdHighValueTD.html();

	//Systolic:Edited values for threshold
	var editedSystolicThresholdLowValueTD = $("#" + canvasId + "-systolic-modify-threshold").find("[name='systolic.thresholdLowValue']");
	var editedSystolicThresholdHighValueTD = $("#" + canvasId + "-systolic-modify-threshold").find("[name='systolic.thresholdHighValue']");

	//Systolic:Replace the values in the editable fields from the database.
	editedSystolicThresholdLowValueTD.val(existingSystolicThresholdLowValue);
	editedSystolicThresholdHighValueTD.val(existingSystolicThresholdHighValue);

	//Diastolic:Existing values for threshold.
	var existingDiastolicThresholdLowValueTD = $("#" + canvasId + "-diastolic-threshold").find("[name='existingDiastolicThresholdLowValue']");
	var existingDiastolicThresholdHighValueTD = $("#" + canvasId + "-diastolic-threshold").find("[name='existingDiastolicThresholdHighValue']");

	var existingDiastolicThresholdLowValue = existingDiastolicThresholdLowValueTD.html();
	var existingDiastolicThresholdHighValue = existingDiastolicThresholdHighValueTD.html();

	//Diastolic:Edited values for threshold
	var editedDiastolicThresholdLowValueTD = $("#" + canvasId + "-diastolic-modify-threshold").find("[name='diastolic.thresholdLowValue']");
	var editedDiastolicThresholdHighValueTD = $("#" + canvasId + "-diastolic-modify-threshold").find("[name='diastolic.thresholdHighValue']");

	//Diastolic:Replace the values in the editable fields from the database.
	editedDiastolicThresholdLowValueTD.val(existingDiastolicThresholdLowValue);
	editedDiastolicThresholdHighValueTD.val(existingDiastolicThresholdHighValue);

	$("#" + canvasId + "-systolic-threshold").addClass("hidden");
	$("#" + canvasId + "-diastolic-threshold").addClass("hidden");
	$("#" + canvasId + "-btn_modify_thresholds").addClass("hidden");

	$("#" + canvasId + "-systolic-modify-threshold").removeClass("hidden");
	$("#" + canvasId + "-diastolic-modify-threshold").removeClass("hidden");
	$("#" + canvasId + "-btn_update_thresholds").removeClass("hidden");
	$("#" + canvasId + "-btn_cancel_update").removeClass("hidden");
}

function hideBloodPressureEditableFields(canvasId) {
	$("#" + canvasId + "-systolic-modify-threshold").addClass("hidden");
	$("#" + canvasId + "-diastolic-modify-threshold").addClass("hidden");
	$("#" + canvasId + "-btn_update_thresholds").addClass("hidden");
	$("#" + canvasId + "-btn_cancel_update").addClass("hidden");

	$("#" + canvasId + "-systolic-threshold").removeClass("hidden");
	$("#" + canvasId + "-diastolic-threshold").removeClass("hidden");
	$("#" + canvasId + "-btn_modify_thresholds").removeClass("hidden");
	hideErrorDiv();
	hideSuccessDiv();
}

function submitBloodPressureAttributeThreshold(canvasId) {
	var formId = "#" + canvasId +"-attributeThreshold";

	//Systolic:Existing values for threshold.
	var existingSystolicThresholdLowValueTD = $("#" + canvasId + "-systolic-threshold").find("[name='existingSystolicThresholdLowValue']");
	var existingSystolicThresholdHighValueTD = $("#" + canvasId + "-systolic-threshold").find("[name='existingSystolicThresholdHighValue']");

	var existingSystolicThresholdLowValue = existingSystolicThresholdLowValueTD.html();
	var existingSystolicThresholdHighValue = existingSystolicThresholdHighValueTD.html();

	//Systolic:Edited values for threshold
	var editedSystolicThresholdLowValueTD = $("#" + canvasId + "-systolic-modify-threshold").find("[name='systolic.thresholdLowValue']");
	var editedSystolicThresholdHighValueTD = $("#" + canvasId + "-systolic-modify-threshold").find("[name='systolic.thresholdHighValue']");

	var editedSystolicThresholdLowValue = editedSystolicThresholdLowValueTD.val();
	var editedSystolicThresholdHighValue = editedSystolicThresholdHighValueTD.val();

	//Diastolic:Existing values for threshold.
	var existingDiastolicThresholdLowValueTD = $("#" + canvasId + "-diastolic-threshold").find("[name='existingDiastolicThresholdLowValue']");
	var existingDiastolicThresholdHighValueTD = $("#" + canvasId + "-diastolic-threshold").find("[name='existingDiastolicThresholdHighValue']");

	var existingDiastolicThresholdLowValue = existingDiastolicThresholdLowValueTD.html();
	var existingDiastolicThresholdHighValue = existingDiastolicThresholdHighValueTD.html();

	//Diastolic:Edited values for threshold
	var editedDiastolicThresholdLowValueTD = $("#" + canvasId + "-diastolic-modify-threshold").find("[name='diastolic.thresholdLowValue']");
	var editedDiastolicThresholdHighValueTD = $("#" + canvasId + "-diastolic-modify-threshold").find("[name='diastolic.thresholdHighValue']");

	var editedDiastolicThresholdLowValue = editedDiastolicThresholdLowValueTD.val();
	var editedDiastolicThresholdHighValue = editedDiastolicThresholdHighValueTD.val();


	if(existingSystolicThresholdLowValue == editedSystolicThresholdLowValue && existingSystolicThresholdHighValue == editedSystolicThresholdHighValue && existingDiastolicThresholdLowValue == editedDiastolicThresholdLowValue && existingDiastolicThresholdHighValue == editedDiastolicThresholdHighValue) {
		//no need to update as there are no changes for threshold values.
		hideBloodPressureEditableFields(canvasId);
	} else {
		$.ajax({
			type: $(formId).attr("method"),
			url: $(formId).attr("action"),
			data: $(formId).serialize(),
			success: function(data) {
				//update the values on screen.
				existingSystolicThresholdLowValueTD.html(data.systolic.thresholdLowValue);
				existingSystolicThresholdHighValueTD.html(data.systolic.thresholdHighValue);
				existingDiastolicThresholdLowValueTD.html(data.diastolic.thresholdLowValue);
				existingDiastolicThresholdHighValueTD.html(data.diastolic.thresholdHighValue);

				hideBloodPressureEditableFields(canvasId);
				hideErrorDiv();
				showSuccessDiv("Thresholds have been updated.");
			},
			error: function(request, status, error) {
				hideSuccessDiv();
				showErrorDiv(request.responseText);
			}
		});
	}
	//returning false so that the form submission should not happen as we are handling form submission via ajax.
	return false;
}

/**
 * Function which prevents the end user from entering non decimal values.
 * Also, allows the decimal values upto one decimal point only.
 */
$('.number').keypress(function(event) {
    var $this = $(this);
    if ((event.which != 46 || $this.val().indexOf('.') != -1) &&
       ((event.which < 48 || event.which > 57) &&
       (event.which != 0 && event.which != 8))) {
           event.preventDefault();
    }

    var text = $(this).val();
    if ((event.which == 46) && (text.indexOf('.') == -1)) {
        setTimeout(function() {
            if ($this.val().substring($this.val().indexOf('.')).length > ALLOWED_NUMBER_OF_DIGITS_AFTER_DECIMAL + 1) {
                $this.val($this.val().substring(0, $this.val().indexOf('.') + ALLOWED_NUMBER_OF_DIGITS_AFTER_DECIMAL + 1));
            }
        }, 1);
    }

    if ((text.indexOf('.') != -1) &&
        (text.substring(text.indexOf('.')).length > ALLOWED_NUMBER_OF_DIGITS_AFTER_DECIMAL) &&
        (event.which != 0 && event.which != 8) &&
        ($(this)[0].selectionStart >= text.length - ALLOWED_NUMBER_OF_DIGITS_AFTER_DECIMAL)) {
            event.preventDefault();
    }
});
/*******************************************************************************
 * END: Functions related to editable threshold values.
 ******************************************************************************/