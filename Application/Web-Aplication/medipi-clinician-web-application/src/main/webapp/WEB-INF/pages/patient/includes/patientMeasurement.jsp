<script type="text/javascript" charset="utf8" src="/plugins/chart-js/Chart.js"></script>
<script type="text/javascript" charset="utf8" src="/plugins/chart-js/Chart.bundle.js"></script>
<script type="text/javascript" charset="utf8" src="/js/common/chart.js.common.js"></script>
<script type="text/javascript" charset="utf8" src="/js/patient/includes/patient.measurement.js"></script>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<div class="accordion-section">
	<div class="accordion-head" id="accordion-head">
		<a href="#" class="on" aria-expanded="true" id="pulseRateDiv"><c:out value="${param.accordionTitle}"/></a>
	</div>
	<div class="accordion-body form-horizontal" style="display: block">
		<div class="row">
			<div class="col-sm-10">
				<canvas id="${param.canvasId}" width="100%" height="30%" />
			</div>
			<div class="col-sm-2">
				<form name="attributeThreshold" id="attributeThreshold" action="/clinician/patient/patients">
					<input type="hidden" name="patientUUID" value="${param.patientUUID}">
					<input type="hidden" name="attributeName" value="${param.attributeName}">
					<table class="measurement-attribute">
						<tr>
							<th scope="col" colspan="2" id="${param.recentMeasurementDateId}"></th>
						</tr>
						<tr>
							<td colspan="2" id="${param.recentMeasurementValueId}"></td>
						</tr>
						<tr>
							<th scope="col">Min</th>
							<th scope="col">Max</th>
						</tr>
						<tr id="${param.canvasId}-threshold">
							<td id="${param.measurementMinValueId}"></td>
							<td id="${param.measurementMaxValueId}"></td>
						</tr>
						<tr id="${param.canvasId}-modify-threshold">
							<td><input id="${param.measurementMinValueId}-value" name="minThreshold" type="text" size="1" maxlength="10"></td>
							<td><input id="${param.measurementMaxValueId}-value" name="maxThreshold" type="text" size="1" maxlength="10"></td>
						</tr>
					</table>
					<div class="span7 pull-left text-right">
						<input class="btn btn-xs btn-primary" id="${param.canvasId}-btn_modify_thresholds" type="button" value="Modify Thresholds" name="modifyThresholds">
					</div>
					<div class="span7 pull-right text-right">
						<input class="btn btn-xs btn-primary" id="${param.canvasId}-btn_update_thresholds" type="submit" value="Update Thresholds" name="updateThresholds">
					</div>
				</form>
			</div>
		</div>
	</div>
</div>
<script type="text/javascript">
	var includeObject = {patientUUID : '${param.patientUUID}', attributeName : '${param.attributeName}', canvasId : '${param.canvasId}', accordionTitle : '${param.accordionTitle}', chartHeader : '${param.chartHeader}', recentMeasurementDateId : '${param.recentMeasurementDateId}', recentMeasurementValueId : '${param.recentMeasurementValueId}', measurementMinValueId : '${param.measurementMinValueId}', measurementMaxValueId : '${param.measurementMaxValueId}', suggestedMinValue : '${param.suggestedMinValue}', suggestedMaxValue : '${param.suggestedMaxValue}'};
	measurement.initChart(includeObject);
	$("#" + includeObject.canvasId + "-modify-threshold").hide();
	$("#" + includeObject.canvasId + "-btn_update_thresholds").hide();

	var modifyThresholdButtonId = "#" + includeObject.canvasId + "-btn_modify_thresholds";
	//console.log(modifyThresholdButtonId);

	$("body").on("click", modifyThresholdButtonId, function() {
		$("#" + includeObject.canvasId + "-threshold").hide();
		$("#" + includeObject.canvasId + "btn_modify_thresholds").hide();

		$("#" + includeObject.canvasId + "-modify-threshold").show();
		$("#" + includeObject.canvasId + "-btn_update_thresholds").show();
		//console.log("on click called:end: " + modifyThresholdButtonId);
	});
</script>