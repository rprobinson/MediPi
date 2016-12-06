var ALLOWED_NUMBER_OF_DIGITS_AFTER_DECIMAL = 1;

var measurement = {
    getData: function (includeObject) {
        var formattedstudentListArray = [];
        var data = null;
        $.ajax({
            async: false,
            url: "/clinician/patient/patientMeasurements?patientUUID=" + includeObject.patientUUID + "&attributeName=" + includeObject.attributeName,
            dataType: "json",
            success: function (measurements) {
                data = measurements;
            },
            error: function(request, status, error) {
            	showDefaultErrorDiv();
            }
        });
        return data;
    },

    getLatestAttributeThreshold: function (includeObject) {
        var formattedstudentListArray = [];
        var data = null;
        $.ajax({
            async: false,
            url: "/clinician/attributeThreshold?patientUUID=" + includeObject.patientUUID + "&attributeName=" + includeObject.attributeName,
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
            labels: jsonData.timeMapProperty('dataTime'),
            datasets: [
                {
                    label: "Min",
                    fill: false,
                    borderColor: 'rgba(255,89,89,1)',
                    backgroundColor: 'rgba(255,89,89,1)',
                    data: jsonData.mapValue('minValue'),
                    borderDash: [10, 7],
                    lineTension: 0
                },
                {
                    label: includeObject.chartHeader,
                    borderColor: 'rgba(53,94,142,1)',
                    backgroundColor: 'rgba(53,94,142,1)',
                    fill: false,
                    data: jsonData.mapValue('value'),
                    lineTension: 0
                },
                {
                    label: "Max",
                    fill: false,
                    borderColor: 'rgba(196,0,0,1)',
                    backgroundColor: 'rgba(196,0,0,1)',
                    data: jsonData.mapValue('maxValue'),
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
                	},
                ],
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

    updateRecentMeasuremnts: function (recentMeasurement, includeObject) {
        $("#" + includeObject.recentMeasurementDateId).html(recentMeasurement != null ? recentMeasurement.dataTime.getStringDate_DDMMYYYY_From_Timestamp() : "- - -");
        $("#" + includeObject.recentMeasurementValueId).html(recentMeasurement != null ? recentMeasurement.value : "- - -");

        //If within min and max limits
        if(recentMeasurement != null) {
        	/*if(includeObject.attributeId == 3) {
        		console.log("recentMeasurement.value <= recentMeasurement.maxValue:" + (parseFloat(recentMeasurement.minValue) <= parseFloat(recentMeasurement.value) <= parseFloat(recentMeasurement.maxValue)));
        	}*/
	        if(recentMeasurement.minValue == null || recentMeasurement.maxValue == null) {
	        	$("#" + includeObject.recentMeasurementValueId).attr("class", "amber");
	        } else if(parseFloat(recentMeasurement.minValue) <= parseFloat(recentMeasurement.value) && parseFloat(recentMeasurement.value) <= parseFloat(recentMeasurement.maxValue)) {
	        	$("#" + includeObject.recentMeasurementValueId).attr("class", "green");
	        } else {
	        	$("#" + includeObject.recentMeasurementValueId).attr("class", "red");
	        }
        }
    },

    updateAttributeThreshold: function (attributeThreshold) {
        $("#" + includeObject.measurementMinValueId).html(attributeThreshold != null ? (attributeThreshold.thresholdLowValue != null ? attributeThreshold.thresholdLowValue : "- - -") : "- - -");
        $("#" + includeObject.measurementMinValueId + "-value").val(attributeThreshold != null ? (attributeThreshold.thresholdLowValue != null ? attributeThreshold.thresholdLowValue : "") : "");
        $("#" + includeObject.measurementMaxValueId).html(attributeThreshold != null ? (attributeThreshold.thresholdHighValue != null ? attributeThreshold.thresholdHighValue : "- - -") : "- - -");
        $("#" + includeObject.measurementMaxValueId + "-value").val(attributeThreshold != null ? (attributeThreshold.thresholdHighValue != null ? attributeThreshold.thresholdHighValue : "- - -") : "");
    },

    initChart: function (includeObject) {
        var measurements = measurement.getData(includeObject);
        var attributeThreshold = measurement.getLatestAttributeThreshold(includeObject);
        chartData = measurement.createChartData(measurements, includeObject);
        measurement.renderChart(chartData, includeObject);
        measurement.updateRecentMeasuremnts(measurements.lastObject(), includeObject);
        measurement.updateAttributeThreshold(attributeThreshold);
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