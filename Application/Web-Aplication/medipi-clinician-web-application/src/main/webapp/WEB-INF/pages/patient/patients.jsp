<title>MediPi : Patients List</title>
<jsp:include page="/WEB-INF/pages/headers/header.jsp" />
<jsp:include page="/WEB-INF/pages/headers/datatablesInclude.jsp" />
<script type="text/javascript" charset="utf8" src="/js/patient/patients.js"></script>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<input id="refreshViewFrequency" type="hidden" value="${refreshViewFrequency}"/>
<h1 class="span8 mB20">Patients</h1>
<div class="data-table-wrap">
	<table id="patients" class="display dataTable">
		<thead>
			<tr>
				<th class="patient-id-column">Patient Id</th>
				<th class="nhs-number-column">NHS Number</th>
				<th>Name</th>
				<th>DOB</th>
				<th class="action-column">Status</th>
			</tr>
		</thead>
	</table>
</div>
<jsp:include page="/WEB-INF/pages/footers/footer.jsp" />