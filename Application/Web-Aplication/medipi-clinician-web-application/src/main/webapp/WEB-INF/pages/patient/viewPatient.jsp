<title>MediPi : Patient</title>
<jsp:include page="/WEB-INF/pages/headers/header.jsp" />
<jsp:include page="/WEB-INF/pages/headers/datatablesInclude.jsp" />
<script type="text/javascript" charset="utf8" src="/js/common/common.ui.util.js"></script>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<div class="accordion-section">
	<div class="accordion-head" id="accordion-head">
		<a href="#" class="on" aria-expanded="true" id="employeeDetails">Personal Details</a>
	</div>
	<div class="accordion-body form-horizontal" style="display: block">
		<div class="form-group">
			<label class="control-label col-sm-3" for="employeeId">Employee Id:</label>
			<label class="control-label" for="employeeId" id="employeeId">${employee.employeeId}</label>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-3" for="employeeNumber">Employee Number:</label>
			<label class="control-label" for="employeeNumber" id="employeeNumber">${employee.employeeNumber}</label>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-3" for="firstName">First name:</label>
			<label class="control-label" for="firstName">${employee.firstName}</label>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-3" for="lastName">Last name:</label>
			<label class="control-label" for="lastName">${employee.lastName}</label>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-3" for="salary">Salary:</label>
			<label class="control-label" for="salary">${employee.salary}</label>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-3" for="mobileNumber">Mobile number:</label>
			<label class="control-label" for="mobileNumber">${employee.mobileNumber}</label>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-3" for="emailAddress">Email address:</label>
			<label class="control-label" for="emailAddress">${employee.emailAddress}</label>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-3" for="googleAuthenticatorKey">Google Authenticator QR Code:</label>
			<%-- <label class="control-label" for="googleAuthenticatorKey">${employee.googleAuthenticatorKey}</label> --%>
			<img alt="" height="300px" width="300px" src="/getGoogleAuthenticatorQRCode/${employee.employeeNumber}/${employee.googleAuthenticatorKey}/${employee.firstName} ${employee.lastName}">
		</div>
	</div>
</div>
<div class="accordion-section">
	<div class="accordion-head" id="accordion-head">
		<a href="#" class="on" aria-expanded="true" id="employeeDetails">Address Details</a>
	</div>
	<div class="accordion-body form-horizontal" style="display: block">
		<div class="form-group">
			<label class="control-label col-sm-3" for="address.addressLine1">Line 1:</label>
			<label class="control-label" for="address.addressLine1">${employee.address.addressLine1}</label>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-3" for="addressLine2">Line 2:</label>
			<label class="control-label" for="address.addressLine2">${employee.address.addressLine2}</label>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-3" for="address.postCode">Postcode:</label>
			<label class="control-label" for="address.postCode">${employee.address.postCode}</label>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-3" for="address.city">City:</label>
			<label class="control-label" for="address.city">${employee.address.city}</label>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-3" for="address.state">State/ County/ Province:</label>
			<label class="control-label" for="address.state">${employee.address.state}</label>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-3" for="address.country">Country:</label>
			<label class="control-label" for="address.country">${employee.address.country}</label>
		</div>
		<div class="span7 pull-right text-right">
			<input class="btn btn-large btn-primary" id="btn_modify_employee" type="button" value="Modify Employee" name="modifyEmployee" onclick="window.location.href='/employee?employeeId=${employee.employeeId}'">
		</div>
	</div>
</div>
<jsp:include page="/WEB-INF/pages/footers/footer.jsp" />
<script>
	$(document).ready(function() {
		showActiveMenu(NAVIGATION_LINK_MAP.EMPLOYEE);
	});
</script>