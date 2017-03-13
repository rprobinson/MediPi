<title>MediPi : Error</title>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:include page="/WEB-INF/pages/headers/header.jsp" />
<script type="text/javascript" charset="utf8" src="/js/common/common.ui.util.js"></script>
<jsp:include page="/WEB-INF/pages/footers/footer.jsp" />
<script type="text/javascript">
$(document).ready(function() {
	showErrorDiv("${exception.message}");
});
</script>