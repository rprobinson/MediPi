<title>MediPi : Patient</title>
<jsp:include page="/WEB-INF/pages/headers/header.jsp" />
<script type="text/javascript" charset="utf8" src="/js/common/common.ui.util.js"></script>
<script type="text/javascript" charset="utf8" src="/js/patient/view.patient.js"></script>
<script type="text/javascript" charset="utf8" src="/plugins/chart-js/Chart.js"></script>
<script type="text/javascript" charset="utf8" src="/plugins/chart-js/Chart.bundle.js"></script>
<script type="text/javascript" charset="utf8" src="/js/common/chart.js.common.js"></script>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
	<div class="accordion-body form-horizontal" style="display: block">
		<ul class="summary-three-col">
			<li><label class="label-display" for="name">Name:</label> <label class="label-text" for="name" id="name">${patient.firstName}&nbsp;${patient.lastName}</label></li>
			<li><label class="label-display" for="dob">Date of Birth:</label> <label class="label-text" for="dob" id="dob">
					<fmt:formatDate value="${patient.dateOfBirth}" pattern="dd-MMM-yyyy" />
				</label></li>
			<li><label class="label-display" for="nhsNumber">NHS Number:</label> <label class="label-text" for="nhsNumber" id="nhsNumber">${patient.nhsNumber}</label></li>
		</ul>
	</div>

<!-- Pulse, spO2, Temperature and Weight accordion -->
<c:forEach items="${similarDeviceAttributes}" var="similarDeviceAttribute" varStatus="counter">
	<jsp:include page="/WEB-INF/pages/patient/includes/patientMeasurement.jsp">
		<jsp:param name="patientUUID" value="${patient.patientUUID}"/>
		<jsp:param name="attributeId" value="${similarDeviceAttribute.attributeId}"/>
		<jsp:param name="attributeName" value="${similarDeviceAttribute.attributeName}"/>
		<jsp:param name="recordingDeviceType" value="${similarDeviceAttribute.recordingDevice.type}"/>
		<jsp:param name="displayName" value="${similarDeviceAttribute.recordingDevice.displayName}"/>
		<jsp:param name="canvasId" value="${similarDeviceAttribute.attributeName}Canvas${counter.count}"/>
		<jsp:param name="recentMeasurementDateId" value="${similarDeviceAttribute.attributeName}RecentMeasurementDateId${counter.count}"/>
		<jsp:param name="recentMeasurementValueId" value="${similarDeviceAttribute.attributeName}RecentMeasurementValueId${counter.count}"/>
		<jsp:param name="measurementMinValueId" value="${similarDeviceAttribute.attributeName}MeasurementMinValueId${counter.count}"/>
		<jsp:param name="measurementMaxValueId" value="${similarDeviceAttribute.attributeName}MeasurementMaxValueId${counter.count}"/>
	</jsp:include>
</c:forEach>

<!-- Blood pressure accordion -->
<c:forEach items="${bloodPressureDeviceAttributesList}" var="bloodPressureDeviceAttributes" varStatus="counter">
	<jsp:include page="/WEB-INF/pages/patient/includes/patientBloodPressureMeasurement.jsp">
		<jsp:param name="patientUUID" value="${patient.patientUUID}"/>
		<jsp:param name="recordingDeviceType" value="${bloodPressureDeviceAttributes.systolic.recordingDevice.type}"/>
		<jsp:param name="displayName" value="${bloodPressureDeviceAttributes.systolic.recordingDevice.displayName}"/>
		<jsp:param name="canvasId" value="${bloodPressureDeviceAttributes.systolic.attributeName}Canvas${counter.count}"/>
		<jsp:param name="systolicAttributeId" value="${bloodPressureDeviceAttributes.systolic.attributeId}"/>
		<jsp:param name="systolicAttributeName" value="${bloodPressureDeviceAttributes.systolic.attributeName}"/>
		<jsp:param name="diastolicAttributeId" value="${bloodPressureDeviceAttributes.diastolic.attributeId}"/>
		<jsp:param name="diastolicAttributeName" value="${bloodPressureDeviceAttributes.diastolic.attributeName}"/>
		<jsp:param name="recentMeasurementDateId" value="${bloodPressureDeviceAttributes.systolic.attributeName}RecentMeasurementDateId${counter.count}"/>
		<jsp:param name="recentMeasurementValueId" value="${bloodPressureDeviceAttributes.systolic.attributeName}RecentMeasurementValueId${counter.count}"/>
		<jsp:param name="measurementSystolicMinValueId" value="${bloodPressureDeviceAttributes.systolic.attributeName}MeasurementMinValueId${counter.count}"/>
		<jsp:param name="measurementSystolicMaxValueId" value="${bloodPressureDeviceAttributes.systolic.attributeName}MeasurementMaxValueId${counter.count}"/>
		<jsp:param name="measurementDiastolicMinValueId" value="${bloodPressureDeviceAttributes.diastolic.attributeName}MeasurementMinValueId${counter.count}"/>
		<jsp:param name="measurementDiastolicMaxValueId" value="${bloodPressureDeviceAttributes.diastolic.attributeName}MeasurementMaxValueId${counter.count}"/>
	</jsp:include>
</c:forEach>

<!-- Questionnaires accordion -->
<c:forEach items="${questionnaireDeviceAttributes}" var="questionnaireDeviceAttribute" varStatus="counter">
	<jsp:include page="/WEB-INF/pages/patient/includes/patientQuestionnaire.jsp">
		<jsp:param name="patientUUID" value="${patient.patientUUID}"/>
		<jsp:param name="attributeId" value="${questionnaireDeviceAttribute.attributeId}"/>
		<jsp:param name="attributeName" value="${questionnaireDeviceAttribute.attributeName}"/>
		<jsp:param name="recordingDeviceType" value="${questionnaireDeviceAttribute.recordingDevice.type}"/>
		<jsp:param name="displayName" value="${questionnaireDeviceAttribute.recordingDevice.displayName}"/>
		<jsp:param name="canvasId" value="${questionnaireDeviceAttribute.attributeName}Canvas${counter.count}"/>
	</jsp:include>
</c:forEach>

<jsp:include page="/WEB-INF/pages/footers/footer.jsp" />