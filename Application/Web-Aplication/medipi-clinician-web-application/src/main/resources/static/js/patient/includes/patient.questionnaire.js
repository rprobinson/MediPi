var ALLOWED_NUMBER_OF_DIGITS_AFTER_DECIMAL = 1;

var measurement = {
    getData: function (includeObject) {
        var formattedstudentListArray = [];
        var data = null;
        $.ajax({
            async: false,
            url: "/clinician/patient/patientMeasurements/" + includeObject.patientUUID + "/" + includeObject.attributeId,
            dataType: "json",
            success: function (measurements) {
                data = measurements;
            },
            error: function (request, status, error) {
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
    renderChart: function (chartData, measurements, includeObject) {
        var context2D = document.getElementById(includeObject.canvasId).getContext("2d");
        var timeFormat = 'DD/MM/YYYY HH:mm';
        var myChart = new Chart(context2D, {
            type: 'bar',
            data: chartData,
            options: {
                responsive: true,
                tooltips: {
                	bodyFontColor: "#000000", //#000000
                    bodyFontSize: 10,
                    bodyFontStyle: "bold",
                    bodyFontColor: '#FFFFFF',
                    bodyFontFamily: "'Helvetica', 'Arial', sans-serif",
                    footerFontSize: 10,
                    callbacks: {
                      label: function(tooltipItem, chartData) {
                    	  var valueItem = $.parseJSON(measurements[tooltipItem.index].value);
                    	  var tooltip = ["Conversation:"];
                    	  tooltip = $.merge($.merge([], tooltip), valueItem.conversation)
                    	  tooltip.push("");
                    	  tooltip.push("Advice:");
                    	  tooltip.push(valueItem.advice);
                    	  return tooltip;
                      }
                    }
                  },
                elements: {
                    point: {
                        radius: 0
                    }
                },
                scales: {
                    xAxes: [{
                            barPercentage: 1,
                            scaleLabel: {
                                display: true,
                            }
                        }
                    ],
                    yAxes: [{
                            display: true,
                            scaleLabel: {
                                show: false,
                            },
                            ticks: {
                                callback: function(label, index, labels) {
                                	if(label == 1) {
                                		return "Green flag";
                                	} else if(label == -1) {
                                		return "Red flag";
                                	}else {
                                		return "";
                                	}
                                },
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
        measurement.renderChart(chartData, measurements, includeObject);
    }
};