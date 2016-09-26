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
                    borderDash: [10, 7]
                },
                {
                    label: includeObject.chartHeader,
                    borderColor: 'rgba(53,94,142,1)',
                    backgroundColor: 'rgba(53,94,142,1)',
                    fill: false,
                    data: jsonData.mapValue('value')
                },
                {
                    label: "Max",
                    fill: false,
                    borderColor: 'rgba(196,0,0,1)',
                    backgroundColor: 'rgba(196,0,0,1)',
                    data: jsonData.mapValue('maxValue'),
                    borderDash: [10, 5]
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