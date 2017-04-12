<title>MediPi : Login</title>
<jsp:include page="/WEB-INF/pages/headers/header.jsp" />
<script type="text/javascript" charset="utf8" src="/js/common/common.ui.util.js"></script>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<div class="container container-table">
    <div class="row vertical-center-row">
        <div class="text-center col-md-4 col-md-offset-4">
			<form class="form-horizontal" action="/login" method="post">
				<fieldset>
					<div class="accordion-section">
						<div class="accordion-body form-horizontal" style="display: block">
							<div class="form-group">
								<label class="control-label col-sm-3" for="username">Username:</label>
								<label class="control-label" for="username" id="username"><input type="text" class="style-4" name="username" placeholder="User Name" /></label>
							</div>
							<div class="form-group">
								<label class="control-label col-sm-3" for="password">Password:</label>
								<label class="control-label" for="password" id="password"><input type="password" class="style-4" name="password" placeholder="Password" /></label>
							</div>
							<div class="span7 pull-right text-right">
								<input class="btn btn-large btn-primary" id="login" type="submit" value="Login" name="login">
							</div>
							<div class="span7 pull-left text-left">
								<c:if test="${param.error ne null}">
									<div class="alert-danger">Invalid username and password.</div>
								</c:if>
							</div>
							<div class="form-group">
								<c:if test="${param.logout ne null}">
									<div class="alert-normal">You have been logged out.</div>
								</c:if>
							</div>
						</div>
					</div>
					<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
				</fieldset>
			</form>
		</div>
    </div>
</div>
<jsp:include page="/WEB-INF/pages/footers/footer.jsp" />
<script type="text/javascript">
/* $(document).ready(function() {
	console.log("Error:${error}");
	console.log("Logout:${param.logout}");
	if("${error}" === '') {
		showErrorDiv("Invalid username and password");
	}

	if("${param.logout}" != null) {
		showSuccessDiv("You have been logged out.");
	}
}); */
</script>