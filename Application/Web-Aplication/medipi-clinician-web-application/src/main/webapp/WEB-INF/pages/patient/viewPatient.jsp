<title>MediPi : Patient</title>
<jsp:include page="/WEB-INF/pages/headers/header.jsp" />
<script type="text/javascript" charset="utf8" src="/js/common/common.ui.util.js"></script>
<script type="text/javascript" charset="utf8" src="/js/patient/view.patient.js"></script>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
	<div class="accordion-body form-horizontal" style="display: block">
		<ul class="summary-three-col">
			<li><label class="label-display" for="name">Name:</label> <label class="label-text" for="name" id="name">${patient.firstName}&nbsp;${patient.lastName}</label></li>
			<li><label class="label-display" for="dob">Date of Birth:</label> <label class="label-text" for="dob" id="dob">
					<fmt:formatDate value="${patient.dateOfBirth}" pattern="dd-MMM-yyyy" />
				</label></li>
			<li><label class="label-display" for="nhsNumber">NHS Number:</label> <label class="label-text" for="nhsNumber" id="nhsNumber">${patient.nhsNumber}</label></li>
		</ul>
	</div>

<!-- Body temperature accordion -->
<jsp:include page="/WEB-INF/pages/patient/includes/patientMeasurement.jsp">
	<jsp:param name="patientUUID" value="${patient.patientUUID}"/>
	<jsp:param name="attributeName" value="temperature"/>
	<jsp:param name="accordionTitle" value="Temperature"/>
	<jsp:param name="chartHeader" value="Temperature"/>
	<jsp:param name="canvasId" value="temperatureCanvas"/>
	<jsp:param name="recentMeasurementDateId" value="temperatureRecentMeasurementDateId"/>
	<jsp:param name="recentMeasurementValueId" value="temperatureRecentMeasurementValueId"/>
	<jsp:param name="measurementMinValueId" value="temperatureMeasurementMinValueId"/>
	<jsp:param name="measurementMaxValueId" value="temperatureMeasurementMaxValueId"/>
	<jsp:param name="suggestedMinValue" value="36"/>
	<jsp:param name="suggestedMaxValue" value="38"/>
</jsp:include>

<!-- Pulse rate accordion -->
<jsp:include page="/WEB-INF/pages/patient/includes/patientMeasurement.jsp">
	<jsp:param name="patientUUID" value="${patient.patientUUID}"/>
	<jsp:param name="attributeName" value="pulse"/>
	<jsp:param name="accordionTitle" value="Pulse Rate"/>
	<jsp:param name="chartHeader" value="Pulse"/>
	<jsp:param name="canvasId" value="pulseRateCanvas"/>
	<jsp:param name="recentMeasurementDateId" value="pulseRateRecentMeasurementDateId"/>
	<jsp:param name="recentMeasurementValueId" value="pulseRateRecentMeasurementValueId"/>
	<jsp:param name="measurementMinValueId" value="pulseRateMeasurementMinValueId"/>
	<jsp:param name="measurementMaxValueId" value="pulseRateMeasurementMaxValueId"/>
	<jsp:param name="suggestedMinValue" value="50"/>
	<jsp:param name="suggestedMaxValue" value="120"/>
</jsp:include>

<!-- Weight accordion -->
<jsp:include page="/WEB-INF/pages/patient/includes/patientMeasurement.jsp">
	<jsp:param name="patientUUID" value="${patient.patientUUID}"/>
	<jsp:param name="attributeName" value="weight"/>
	<jsp:param name="accordionTitle" value="Weight"/>
	<jsp:param name="chartHeader" value="Weight(kg)"/>
	<jsp:param name="canvasId" value="weightCanvas"/>
	<jsp:param name="recentMeasurementDateId" value="weightRecentMeasurementDateId"/>
	<jsp:param name="recentMeasurementValueId" value="weightRecentMeasurementValueId"/>
	<jsp:param name="measurementMinValueId" value="weightMeasurementMinValueId"/>
	<jsp:param name="measurementMaxValueId" value="weightMeasurementMaxValueId"/>
	<jsp:param name="suggestedMinValue" value="80"/>
	<jsp:param name="suggestedMaxValue" value="83"/>
</jsp:include>

<!-- Oxygen saturation accordion -->
<jsp:include page="/WEB-INF/pages/patient/includes/patientMeasurement.jsp">
	<jsp:param name="patientUUID" value="${patient.patientUUID}"/>
	<jsp:param name="attributeName" value="spo2"/>
	<jsp:param name="accordionTitle" value="Oxygen Saturation"/>
	<jsp:param name="chartHeader" value="SpO2"/>
	<jsp:param name="canvasId" value="oxygenSaturationCanvas"/>
	<jsp:param name="recentMeasurementDateId" value="oxygenSaturationRecentMeasurementDateId"/>
	<jsp:param name="recentMeasurementValueId" value="oxygenSaturationRecentMeasurementValueId"/>
	<jsp:param name="measurementMinValueId" value="oxygenSaturationMeasurementMinValueId"/>
	<jsp:param name="measurementMaxValueId" value="oxygenSaturationMeasurementMaxValueId"/>
	<jsp:param name="suggestedMinValue" value="85"/>
	<jsp:param name="suggestedMaxValue" value="110"/>
</jsp:include>

<!-- Blood pressure accordion -->
<jsp:include page="/WEB-INF/pages/patient/includes/patientBloodPressureMeasurement.jsp">
	<jsp:param name="patientUUID" value="${patient.patientUUID}"/>
	<jsp:param name="attributeNames" value="['systol','diastol']"/>
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

<!-- Questionnaire -->
<jsp:include page="/WEB-INF/pages/patient/includes/patientQuestionnaire.jsp">
	<jsp:param name="patientUUID" value="${patient.patientUUID}"/>
	<jsp:param name="attributeName" value="outcome"/>
	<jsp:param name="accordionTitle" value="Questionnaire"/>
	<jsp:param name="chartHeader" value="Questionnaire"/>
	<jsp:param name="canvasId" value="questionnaireCanvas"/>
</jsp:include>

<jsp:include page="/WEB-INF/pages/footers/footer.jsp" />