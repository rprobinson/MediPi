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
				    borderColor: 'rgba(255,220,113,1)',
				    backgroundColor: 'rgba(255,220,113,1)',
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
				    borderColor: 'rgba(179,134,0,1)',
				    backgroundColor: 'rgba(179,134,0,1)',
				    data: jsonData[1].mapValue('maxValue'),
				    borderDash: [10, 5],
				    lineTension: 0
				},
				{
				    label: includeObject.systolicAttributeName + " min",
				    fill: false,
				    borderColor: 'rgba(255,89,89,1)',
				    backgroundColor: 'rgba(255,89,89,1)',
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
				    borderColor: 'rgba(196,0,0,1)',
				    backgroundColor: 'rgba(196,0,0,1)',
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
            	elements: {
                    point:{
                        radius: 0
                    }
                },
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
    	$("#" + includeObject.recentMeasurementDateId).html(systolicData != null ? systolicData.dataTime.getStringDate_DDMMYYYY_From_Timestamp() : "- - -");
    	if(systolicData != null && diastolicData != null) {
    		$("#" + includeObject.recentMeasurementValueId).html("<u>" + systolicData.value + "</u><br/>" + diastolicData.value);
    		//If within min and max limits
    		if(systolicData.minValue == null || systolicData.maxValue == null || diastolicData.minValue == null || diastolicData.maxValue == null) {
    			measurementIndicatorClass = "amber";
	        } else if(parseFloat(systolicData.minValue) <= parseFloat(systolicData.value) && parseFloat(systolicData.value) <= parseFloat(systolicData.maxValue) && parseFloat(diastolicData.minValue) <= parseFloat(diastolicData.value) && parseFloat(diastolicData.value) <= parseFloat(diastolicData.maxValue)) {
	        	measurementIndicatorClass = "green";
	        } else {
	        	measurementIndicatorClass = "red";
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
function showEditableFields(canvasId) {
	var formId = "#" + canvasId +"-attributeThreshold";

	//Existing values for threshold.
	var existingThresholdLowValueTD = $("#" + canvasId + "-threshold").find("[name='existingThresholdLowValue']");
	var existingThresholdHighValueTD = $("#" + canvasId + "-threshold").find("[name='existingThresholdHighValue']");

	var existingThresholdLowValue = existingThresholdLowValueTD.html();
	var existingThresholdHighValue = existingThresholdHighValueTD.html();

	//Edited values for threshold
	var editedThresholdLowValueTD = $("#" + canvasId + "-modify-threshold").find("[name='thresholdLowValue']");
	var editedThresholdHighValueTD = $("#" + canvasId + "-modify-threshold").find("[name='thresholdHighValue']");

	//Reflect the values which are saved in the database.
	editedThresholdLowValueTD.val(existingThresholdLowValue);
	editedThresholdHighValueTD.val(existingThresholdHighValue);

	$("#" + canvasId + "-threshold").addClass("hidden");
	$("#" + canvasId + "-btn_modify_thresholds").addClass("hidden");

	$("#" + canvasId + "-modify-threshold").removeClass("hidden");
	$("#" + canvasId + "-btn_update_thresholds").removeClass("hidden");
	$("#" + canvasId + "-btn_cancel_update").removeClass("hidden");
}

function hideEditableFields(canvasId) {
	$("#" + canvasId + "-modify-threshold").addClass("hidden");
	$("#" + canvasId + "-btn_update_thresholds").addClass("hidden");
	$("#" + canvasId + "-btn_cancel_update").addClass("hidden");

	$("#" + canvasId + "-threshold").removeClass("hidden");
	$("#" + canvasId + "-btn_modify_thresholds").removeClass("hidden");
	hideErrorDiv();
	hideSuccessDiv();
}

function submitAttributeThreshold(canvasId) {
	var formId = "#" + canvasId +"-attributeThreshold";

	//Existing values for threshold.
	var existingThresholdLowValueTD = $("#" + canvasId + "-threshold").find("[name='existingThresholdLowValue']");
	var existingThresholdHighValueTD = $("#" + canvasId + "-threshold").find("[name='existingThresholdHighValue']");

	var existingThresholdLowValue = existingThresholdLowValueTD.html();
	var existingThresholdHighValue = existingThresholdHighValueTD.html();

	//Edited values for threshold
	var editedThresholdLowValueTD = $("#" + canvasId + "-modify-threshold").find("[name='thresholdLowValue']");
	var editedThresholdHighValueTD = $("#" + canvasId + "-modify-threshold").find("[name='thresholdHighValue']");

	var editedThresholdLowValue = editedThresholdLowValueTD.val();
	var editedThresholdHighValue = editedThresholdHighValueTD.val();

	if(existingThresholdLowValue == editedThresholdLowValue && existingThresholdHighValue == editedThresholdHighValue) {
		//no need to update as there are no changes for threshold values.
		hideEditableFields(canvasId);
	} else {
		$.ajax({
			type: $(formId).attr("method"),
			url: $(formId).attr("action"),
			data: $(formId).serialize(),
			success: function(data) {
				//update the values on screen.
				existingThresholdLowValueTD.html(data.thresholdLowValue);
				existingThresholdHighValueTD.html(data.thresholdHighValue);

				hideEditableFields(canvasId);
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