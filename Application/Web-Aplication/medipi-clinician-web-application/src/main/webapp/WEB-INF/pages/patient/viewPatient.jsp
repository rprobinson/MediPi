<title>MediPi : Patient</title>
<jsp:include page="/WEB-INF/pages/headers/header.jsp" />
<jsp:include page="/WEB-INF/pages/headers/datatablesInclude.jsp" />
<script type="text/javascript" charset="utf8" src="/js/common/common.ui.util.js"></script>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="dateValue" class="java.util.Date"/>
<div class="accordion-section">
	<div class="accordion-head" id="accordion-head">
		<a href="#" class="on" aria-expanded="true" id="patientDetails">Patient Details</a>
	</div>
	<div class="accordion-body form-horizontal" style="display: block">
		<div class="form-group">
			<label class="control-label col-sm-2" for="nhsNumber">NHS Number:</label>
			<label class="control-label" for="nhsNumber" id="nhsNumber">${patient.nhsNumber}</label>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-2" for="name">Name:</label>
			<label class="control-label" for="name" id="name">${patient.firstName}&nbsp;${patient.lastName}</label>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-2" for="dob">Date of Birth:</label>
			<label class="control-label" for="dob" id="dob"><fmt:formatDate value="${patient.dateOfBirth}" pattern="dd-MMM-yyyy" /></label>
		</div>
	</div>
	<div class="accordion-head" id="accordion-head">
		<a href="#" class="on" aria-expanded="true" id="patientDetails">Patient Details</a>
	</div>
	<div class="accordion-body form-horizontal" style="display: block">
		<ul class="summary-three-col">
			<li><label class="label-display" for="nhsNumber">NHS Number:</label> <label class="label-text" for="nhsNumber" id="nhsNumber">${patient.nhsNumber}</label></li>
			<li><label class="label-display" for="name">Name:</label> <label class="label-text" for="name" id="name">${patient.firstName}&nbsp;${patient.lastName}</label></li>
			<li><label class="label-display" for="dob">Date of Birth:</label> <label class="label-text" for="dob" id="dob"><fmt:formatDate value="${patient.dateOfBirth}" pattern="dd-MMM-yyyy" /></label></li>
		</ul>
	</div>
</div>
<div class="accordion-section">
	<div class="accordion-head" id="accordion-head">
		<a href="#" class="on" aria-expanded="true" id="recentMeasurements">Recent Measurements</a>
	</div>
	<div class="accordion-body form-horizontal" style="display: block">
		<table class="table table-striped">
			<thead>
				<tr>
					<th>Reading type</th>
					<th>Device</th>
					<th>Recent value</th>
					<th class="w150">Date</th>
					<th></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${recentReadings}" var="recentReading">
					<tr>
						<td>
							<b><c:out value="${recentReading.readingType}" /></b>
						</td>
						<td>
							<b><c:out value="${recentReading.device}" /></b>
						</td>
						<td>
							<c:out value="${recentReading.data}" />
						</td>
						<td>
							<c:set var="dataTime" value="${recentReading.dataTime}" />
							<jsp:setProperty name="dateValue" property="time" value="${dataTime}"/>
							<fmt:formatDate value="${dateValue}" pattern="dd-MMM-yyyy HH:mm:ss" />
						</td>
						<td>
							<a href="#">History</a>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</div>
<jsp:include page="/WEB-INF/pages/footers/footer.jsp" />
<script>
	$(document).ready(function() {
		showActiveMenu(NAVIGATION_LINK_MAP.PATIENT);
	});
</script>