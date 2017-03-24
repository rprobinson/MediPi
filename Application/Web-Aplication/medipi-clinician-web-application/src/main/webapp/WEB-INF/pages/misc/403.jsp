<title>MediPi : Error</title>
<jsp:include page="/WEB-INF/pages/headers/header.jsp" />
<script type="text/javascript" charset="utf8" src="/js/common/common.ui.util.js"></script>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<form action="/login" method="post">
	<div class="lc-block">
		<div class="alert-danger">
			<h3>You do not have permission to access this page!</h3>
		</div>
		<form action="/logout" method="post">
			<input type="submit" class="button red big" value="Sign in as different user" /> <input
				type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
		</form>
	</div>
<jsp:include page="/WEB-INF/pages/footers/footer.jsp" />
</body>
</html>
