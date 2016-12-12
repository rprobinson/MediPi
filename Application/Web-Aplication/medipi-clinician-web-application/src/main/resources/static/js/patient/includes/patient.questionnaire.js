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

    createChartData: function (jsonData, includeObject) {
    	var colors = jsonData.mapQuestionnaireColor('value');
        return {
            labels: jsonData.timeMapProperty('dataTime'),
            datasets: [
                {
                    label: "",
                    borderColor: colors,
                    backgroundColor: colors,
                    fill: false,
                    data: jsonData.mapQuestionnaire('value'),
                    lineTension: 0
                }
            ]
        };
    },
    renderChart: function (chartData, includeObject) {
        var context2D = document.getElementById(includeObject.canvasId).getContext("2d");
        var timeFormat = 'DD/MM/YYYY HH:mm';
        var myChart = new Chart(context2D, {
            type: 'bar',
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
                            show: false,
                        },
                        ticks: {
                            suggestedMin: -1,
                            suggestedMax: 1,
                        }
                    }]
                },
                legend: {
                    display: false,
                }
            }
        });
        return myChart;
    },

    initChart: function (includeObject) {
        var measurements = measurement.getData(includeObject);
        chartData = measurement.createChartData(measurements, includeObject);
        measurement.renderChart(chartData, includeObject);
    }
};