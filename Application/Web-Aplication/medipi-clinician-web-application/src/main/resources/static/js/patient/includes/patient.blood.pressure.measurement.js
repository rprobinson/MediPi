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
    createChartData: function (jsonData, includeObject) {
        return {
            labels: jsonData[0].timeMapProperty('dataTime'),
            datasets: [
                {
                    label: includeObject.diastolicAttributeName,
                    fill: false,
                    borderColor: 'rgba(0,176,80,1)',
                    backgroundColor: 'rgba(0,176,80,1)',
                    data: jsonData[1].mapValue('value'),
                    lineTension: 0
                },
                {
                    label: includeObject.systolicAttributeName,
                    borderColor: 'rgba(53,94,142,1)',
                    backgroundColor: 'rgba(53,94,142,1)',
                    fill: false,
                    data: jsonData[0].mapValue('value'),
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

    updateRecentMeasuremnts: function (recentMeasurement, includeObject) {
        $("#" + includeObject.recentMeasurementDateId).html(recentMeasurement != null ? recentMeasurement.dataTime.getStringDate_DDMMYYYY_From_Timestamp() : "- - -");
        $("#" + includeObject.recentMeasurementValueId).html(recentMeasurement != null ? recentMeasurement.value : "- - -");
        /*$("#" + includeObject.measurementMinValueId).html(recentMeasurement != null ? recentMeasurement.minValue : "- - -");
        $("#" + includeObject.measurementMaxValueId).html(recentMeasurement != null ? recentMeasurement.maxValue : "- - -");*/
        $("#" + includeObject.measurementMinValueId).html("- - -");
        $("#" + includeObject.measurementMaxValueId).html("- - -");

        if(recentMeasurement != null) {
        	$("#" + includeObject.recentMeasurementValueId).attr("class", "amber");
        }
        //If within min and max limits
        /*if(recentMeasurement != null) {
	        if(recentMeasurement.minValue <= recentMeasurement.value ||  recentMeasurement.maxValue >= recentMeasurement.value) {
	        	$("#" + includeObject.recentMeasurementValueId).attr("class", "green");
	        } else {
	        	$("#" + includeObject.recentMeasurementValueId).attr("class", "red");
	        }
	    }*/
    },

    initChart: function (includeObject) {
        var systolicData = measurement.getData(includeObject, includeObject.systolicAttributeId);
        var diastolicData = measurement.getData(includeObject, includeObject.diastolicAttributeId);
        chartData = measurement.createChartData([systolicData, diastolicData], includeObject);
        measurement.renderChart(chartData, includeObject);
        var lastSystolicData = systolicData.lastObject();
        var lastDiastolicData = diastolicData.lastObject();
        if(lastSystolicData != null && lastDiastolicData != null) {
        	lastSystolicData.value = "<u>" + lastSystolicData.value + "</u><br/>" + lastDiastolicData.value;
        }
        measurement.updateRecentMeasuremnts(lastSystolicData, includeObject);
    }
};