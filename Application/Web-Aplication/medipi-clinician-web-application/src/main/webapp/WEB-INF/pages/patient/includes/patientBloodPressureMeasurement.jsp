<script type="text/javascript" charset="utf8" src="/plugins/chart-js/Chart.js"></script>
<script type="text/javascript" charset="utf8" src="/plugins/chart-js/Chart.bundle.js"></script>
<script type="text/javascript" charset="utf8" src="/js/common/chart.js.common.js"></script>
<script type="text/javascript" charset="utf8" src="/js/patient/includes/patient.blood.pressure.measurement.js"></script>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<div class="accordion-section">
	<div class="accordion-head" id="accordion-head">
		<a href="#" class="on" aria-expanded="true" id="${param.canvasId}Header"><c:out value="${param.displayName} (${param.diastolicAttributeName}, ${param.systolicAttributeName})"/></a>
	</div>
	<div class="accordion-body form-horizontal" style="display: block">
		<div class="row">
			<div class="col-sm-10">
				<canvas id="${param.canvasId}" width="100%" height="30%" />
			</div>
			<div class="col-sm-2">
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
					<tr>
						<td id="${param.measurementSystolicMinValueId}"></td>
						<td id="${param.measurementSystolicMaxValueId}"></td>
					</tr>
					<tr>
						<td id="${param.measurementDiastolicMinValueId}"></td>
						<td id="${param.measurementDiastolicMaxValueId}"></td>
					</tr>
				</table>
			</div>
		</div>
	</div>
</div>
<script type="text/javascript">
	var includeObject = {
				patientUUID : '${param.patientUUID}',
				recordingDeviceType : '${param.recordingDeviceType}',
				displayName : '${param.displayName}',
				canvasId : '${param.canvasId}',
				systolicAttributeId : '${param.systolicAttributeId}',
				systolicAttributeName : '${param.systolicAttributeName}',
				diastolicAttributeId : '${param.diastolicAttributeId}',
				diastolicAttributeName : '${param.diastolicAttributeName}',
				recentMeasurementDateId : '${param.recentMeasurementDateId}',
				recentMeasurementValueId : '${param.recentMeasurementValueId}',
				measurementSystolicMinValueId : '${param.measurementSystolicMinValueId}',
				measurementSystolicMaxValueId : '${param.measurementSystolicMaxValueId}',
				measurementDiastolicMinValueId : '${param.measurementDiastolicMinValueId}',
				measurementDiastolicMaxValueId : '${param.measurementDiastolicMaxValueId}'
			};
	measurement.initChart(includeObject);
</script>