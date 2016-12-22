<title>MediPi : Patient</title>
<jsp:include page="/WEB-INF/pages/headers/header.jsp" />
<script type="text/javascript" charset="utf8" src="/js/common/common.ui.util.js"></script>
<script type="text/javascript" charset="utf8" src="/js/patient/view.patient.js"></script>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
	<div class="accordion-body form-horizontal" style="display: block">
		<ul class="summary-three-col">
			<li><label class="label-display" for="name">Name:</label> <label class="label-text" for="name" id="name">${patient.firstName}&nbsp;${patient.lastName}</label></li>
			<li><label class="label-display" for="dob">Date of Birth:</label> <label class="label-text" for="dob" id="dob">
					<fmt:formatDate value="${patient.dateOfBirth}" pattern="dd-MMM-yyyy" />
				</label></li>
			<li><label class="label-display" for="nhsNumber">NHS Number:</label> <label class="label-text" for="nhsNumber" id="nhsNumber">${patient.nhsNumber}</label></li>
		</ul>
	</div>

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

<%-- <!-- Blood pressure accordion -->
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
</jsp:include> --%>

<c:forEach items="${questionnnaireDeviceAttributes}" var="questionnnaireDeviceAttribute" varStatus="counter">
	<jsp:include page="/WEB-INF/pages/patient/includes/patientQuestionnaire.jsp">
		<jsp:param name="patientUUID" value="${patient.patientUUID}"/>
		<jsp:param name="attributeId" value="${questionnnaireDeviceAttribute.attributeId}"/>
		<jsp:param name="attributeName" value="${questionnnaireDeviceAttribute.attributeName}"/>
		<jsp:param name="recordingDeviceType" value="${questionnnaireDeviceAttribute.recordingDevice.type}"/>
		<jsp:param name="displayName" value="${questionnnaireDeviceAttribute.recordingDevice.displayName}"/>
		<jsp:param name="canvasId" value="${similarDeviceAttribute.attributeName}Canvas${counter.count}"/>
	</jsp:include>
</c:forEach>

<jsp:include page="/WEB-INF/pages/footers/footer.jsp" />