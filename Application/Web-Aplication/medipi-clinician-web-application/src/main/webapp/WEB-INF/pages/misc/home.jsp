<title>MediPi : Home</title>

<jsp:include page="/WEB-INF/pages/headers/header.jsp" />
<script type="text/javascript" charset="utf8" src="/js/common/common.ui.util.js"></script>
<h1>Welcome to MediPi Clinician Application</h1>
	<!-- <div class="form-group" id="oximeter_readings">
		<div class="row">
			<div class="col-sm-6">
				<div class="panel panel-primary">
					<div class="panel-body">
						<p><b>Welcome to MediPi Clinician Application</b></p>
					</div>
				</div>
			</div>
		</div>
	</div> -->
<!-- <img src="/images/misc/under_construction_animated.gif"> -->
<jsp:include page="/WEB-INF/pages/footers/footer.jsp" />
<script>
	$(document).ready(function() {
		showActiveMenu(NAVIGATION_LINK_MAP.HOME);
	});
</script>