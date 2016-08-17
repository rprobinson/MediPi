var measurement = {
    getData: function (includeObject) {
        var formattedstudentListArray = [];
        var data = null;
        $.ajax({
            async: false,
            url: "/clinician/patient/patientMeasurements?patientId=" + includeObject.patientId + "&attributeId=" + includeObject.attributeId,
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
        var myChart = new Chart(context2D, {
            type: 'line',
            data: chartData,
            options: {
                responsive: true,
                scales: {
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
        $("#" + includeObject.measurementMinValueId).html(recentMeasurement != null ? recentMeasurement.minValue : "- - -");
        $("#" + includeObject.measurementMaxValueId).html(recentMeasurement != null ? recentMeasurement.maxValue : "- - -");

        //If within min and max limits
        if(recentMeasurement != null) {
        	/*if(includeObject.attributeId == 3) {
        		console.log("recentMeasurement.value <= recentMeasurement.maxValue:" + (parseFloat(recentMeasurement.minValue) <= parseFloat(recentMeasurement.value) <= parseFloat(recentMeasurement.maxValue)));
        	}*/
	        if(parseFloat(recentMeasurement.minValue) <= parseFloat(recentMeasurement.value) && parseFloat(recentMeasurement.value) <= parseFloat(recentMeasurement.maxValue)) {
	        	$("#" + includeObject.recentMeasurementValueId).attr("class", "green");
	        } else {
	        	$("#" + includeObject.recentMeasurementValueId).attr("class", "red");
	        }
        }
    },

    initChart: function (includeObject) {
        var data = measurement.getData(includeObject);
        chartData = measurement.createChartData(data, includeObject);
        measurement.renderChart(chartData, includeObject);
        measurement.updateRecentMeasuremnts(data.lastObject(), includeObject);
    }
};