<title>MediPi : Patient</title>
<jsp:include page="/WEB-INF/pages/headers/header.jsp" />
<script type="text/javascript" charset="utf8" src="/js/common/common.ui.util.js"></script>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<div class="accordion-section">
	<div class="accordion-head" id="accordion-head">
		<a href="#" class="on" aria-expanded="true" id="patientDetails">Patient Details</a>
	</div>
	<div class="accordion-body form-horizontal" style="display: block">
		<ul class="summary-three-col">
			<li><label class="label-display" for="name">Name:</label> <label class="label-text" for="name" id="name">${patient.firstName}&nbsp;${patient.lastName}</label></li>
			<li><label class="label-display" for="dob">Date of Birth:</label> <label class="label-text" for="dob" id="dob">
					<fmt:formatDate value="${patient.dateOfBirth}" pattern="dd-MMM-yyyy" />
				</label></li>
			<li><label class="label-display" for="nhsNumber">NHS Number:</label> <label class="label-text" for="nhsNumber" id="nhsNumber">${patient.nhsNumber}</label></li>
		</ul>
	</div>
</div>

<!-- Body temperature accordion -->
<jsp:include page="/WEB-INF/pages/patient/includes/patientMeasurement.jsp">
	<jsp:param name="patientId" value="${patient.patientId}"/>
	<jsp:param name="attributeId" value="20"/>
	<jsp:param name="accordionTitle" value="Temperature"/>
	<jsp:param name="chartHeader" value="Temperature"/>
	<jsp:param name="canvasId" value="temperatureCanvas"/>
	<jsp:param name="recentMeasurementDateId" value="temperatureRecentMeasurementDateId"/>
	<jsp:param name="recentMeasurementValueId" value="temperatureRecentMeasurementValueId"/>
	<jsp:param name="measurementMinValueId" value="temperatureMeasurementMinValueId"/>
	<jsp:param name="measurementMaxValueId" value="temperatureMeasurementMaxValueId"/>
	<jsp:param name="suggestedMinValue" value="34"/>
	<jsp:param name="suggestedMaxValue" value="37"/>
</jsp:include>

<!-- Pulse rate accordion -->
<jsp:include page="/WEB-INF/pages/patient/includes/patientMeasurement.jsp">
	<jsp:param name="patientId" value="${patient.patientId}"/>
	<jsp:param name="attributeId" value="1"/>
	<jsp:param name="accordionTitle" value="Pulse Rate"/>
	<jsp:param name="chartHeader" value="Pulse"/>
	<jsp:param name="canvasId" value="pulseRateCanvas"/>
	<jsp:param name="recentMeasurementDateId" value="pulseRateRecentMeasurementDateId"/>
	<jsp:param name="recentMeasurementValueId" value="pulseRateRecentMeasurementValueId"/>
	<jsp:param name="measurementMinValueId" value="pulseRateMeasurementMinValueId"/>
	<jsp:param name="measurementMaxValueId" value="pulseRateMeasurementMaxValueId"/>
	<jsp:param name="suggestedMinValue" value="70"/>
	<jsp:param name="suggestedMaxValue" value="100"/>
</jsp:include>

<!-- Weight accordion -->
<jsp:include page="/WEB-INF/pages/patient/includes/patientMeasurement.jsp">
	<jsp:param name="patientId" value="${patient.patientId}"/>
	<jsp:param name="attributeId" value="5"/>
	<jsp:param name="accordionTitle" value="Weight"/>
	<jsp:param name="chartHeader" value="Weight(kg)"/>
	<jsp:param name="canvasId" value="weightCanvas"/>
	<jsp:param name="recentMeasurementDateId" value="weightRecentMeasurementDateId"/>
	<jsp:param name="recentMeasurementValueId" value="weightRecentMeasurementValueId"/>
	<jsp:param name="measurementMinValueId" value="weightMeasurementMinValueId"/>
	<jsp:param name="measurementMaxValueId" value="weightMeasurementMaxValueId"/>
	<jsp:param name="suggestedMinValue" value="70"/>
	<jsp:param name="suggestedMaxValue" value="100"/>
</jsp:include>

<!-- Oxygen saturation accordion -->
<jsp:include page="/WEB-INF/pages/patient/includes/patientMeasurement.jsp">
	<jsp:param name="patientId" value="${patient.patientId}"/>
	<jsp:param name="attributeId" value="3"/>
	<jsp:param name="accordionTitle" value="Oxygen Saturation"/>
	<jsp:param name="chartHeader" value="SpO2"/>
	<jsp:param name="canvasId" value="oxygenSaturationCanvas"/>
	<jsp:param name="recentMeasurementDateId" value="oxygenSaturationRecentMeasurementDateId"/>
	<jsp:param name="recentMeasurementValueId" value="oxygenSaturationRecentMeasurementValueId"/>
	<jsp:param name="measurementMinValueId" value="oxygenSaturationMeasurementMinValueId"/>
	<jsp:param name="measurementMaxValueId" value="oxygenSaturationMeasurementMaxValueId"/>
	<jsp:param name="suggestedMinValue" value="90"/>
	<jsp:param name="suggestedMaxValue" value="100"/>
</jsp:include>

<!-- Blood pressure accordion -->
<jsp:include page="/WEB-INF/pages/patient/includes/patientBloodPressureMeasurement.jsp">
	<jsp:param name="patientId" value="${patient.patientId}"/>
	<jsp:param name="attributeIds" value="[9,10]"/>
	<jsp:param name="accordionTitle" value="Blood Pressure"/>
	<jsp:param name="chartHeaders" value="['Systolic', 'Diastolic']"/>
	<jsp:param name="canvasId" value="bloodPressureCanvas"/>
	<jsp:param name="recentMeasurementDateId" value="bloodPressureRecentMeasurementDateId"/>
	<jsp:param name="recentMeasurementValueId" value="bloodPressureRecentMeasurementValueId"/>
	<jsp:param name="measurementMinValueId" value="bloodPressureMeasurementMinValueId"/>
	<jsp:param name="measurementMaxValueId" value="bloodPressureMeasurementMaxValueId"/>
	<jsp:param name="suggestedMinValue" value="80"/>
	<jsp:param name="suggestedMaxValue" value="180"/>
</jsp:include>

<jsp:include page="/WEB-INF/pages/footers/footer.jsp" />