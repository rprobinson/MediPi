<script type="text/javascript" charset="utf8" src="/plugins/chart-js/Chart.js"></script>
<script type="text/javascript" charset="utf8" src="/plugins/chart-js/Chart.bundle.js"></script>
<script type="text/javascript" charset="utf8" src="/js/common/chart.js.common.js"></script>
<script type="text/javascript" charset="utf8" src="/js/patient/includes/patient.measurement.js"></script>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<div class="accordion-section">
	<div class="accordion-head" id="accordion-head">
		<a href="#" class="on" aria-expanded="true" id="${param.canvasId}Header"><c:out value="${param.accordionTitle}"/></a>
	</div>
	<div class="accordion-body form-horizontal" style="display: block">
		<div class="row">
			<div class="col-sm-10">
				<canvas id="${param.canvasId}" width="100%" height="30%" />
			</div>
			<div class="col-sm-2">
				<form name="${param.canvasId}-attributeThreshold" id="${param.canvasId}-attributeThreshold" action="/clinician/attributeThreshold/" method="POST" onsubmit="return submitAttributeThreshold('${param.canvasId}')">
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
							<td id="${param.measurementMinValueId}" name="existingThresholdLowValue"></td>
							<td id="${param.measurementMaxValueId}" name="existingThresholdHighValue"></td>
						</tr>
						<tr class="hidden" id="${param.canvasId}-modify-threshold">
							<td><input id="${param.measurementMinValueId}-value" name="thresholdLowValue" type="text" class="number w65" maxlength="10"></td>
							<td><input id="${param.measurementMaxValueId}-value" name="thresholdHighValue" type="text" class="number w65" maxlength="10"></td>
						</tr>
					</table>
					<div class="span7 pull-left text-right" onclick="showEditableFields('${param.canvasId}')">
						<input class="btn btn-xs btn-primary" id="${param.canvasId}-btn_modify_thresholds" type="button" value="Modify Thresholds" name="modifyThresholds">
					</div>
					<div class="span7 pull-left text-right">
						<input class="btn btn-xs btn-primary hidden" id="${param.canvasId}-btn_update_thresholds" type="submit" value="Submit" name="updateThresholds">
					</div>
					<div class="span7 pull-left text-right" onclick="hideEditableFields('${param.canvasId}')">
						<input class="btn btn-xs btn-primary hidden" id="${param.canvasId}-btn_cancel_update" type="button" value="Cancel" name="cancelUpdate">
					</div>
				</form>
			</div>
		</div>
	</div>
</div>
<script type="text/javascript">
	var includeObject = {patientUUID : '${param.patientUUID}', attributeName : '${param.attributeName}', canvasId : '${param.canvasId}', accordionTitle : '${param.accordionTitle}', chartHeader : '${param.chartHeader}', recentMeasurementDateId : '${param.recentMeasurementDateId}', recentMeasurementValueId : '${param.recentMeasurementValueId}', measurementMinValueId : '${param.measurementMinValueId}', measurementMaxValueId : '${param.measurementMaxValueId}', suggestedMinValue : '${param.suggestedMinValue}', suggestedMaxValue : '${param.suggestedMaxValue}'};
	measurement.initChart(includeObject);

	/* $("#" + includeObject.canvasId + "-attributeThreshold").submit(function(e){

		console.log(e);
		//console.log("action:" + $("#" + includeObject.canvasId + "-attributeThreshold").attr('action'));
		$.ajax({
				type: "POST",
				url: $("#" + includeObject.canvasId + "-attributeThreshold").attr('action'),
				data: $("#" + includeObject.canvasId + "-attributeThreshold").serialize(), // serializes the form's elements.
				success: function(data) {
					alert(data); // show response from the php script.
				}
			});
			e.preventDefault();
	}); */
</script>