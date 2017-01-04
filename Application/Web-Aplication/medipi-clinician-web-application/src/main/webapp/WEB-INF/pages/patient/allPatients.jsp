<title>MediPi : Patients</title>
<jsp:include page="/WEB-INF/pages/headers/header.jsp" />
<script type="text/javascript" charset="utf8" src="/js/common/common.ui.util.js"></script>
<script type="text/javascript" charset="utf8" src="/js/patient/allPatients.js"></script>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<input id="refreshViewFrequency" type="hidden" value="${refreshViewFrequency}" />
<div class="accordion-body form-horizontal" style="display: block">
	<div id="patientTiles" class="row"></div>
</div>
<jsp:include page="/WEB-INF/pages/footers/footer.jsp" />