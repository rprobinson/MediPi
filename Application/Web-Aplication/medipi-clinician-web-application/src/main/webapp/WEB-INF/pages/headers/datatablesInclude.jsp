<link rel="stylesheet" type="text/css" href="/plugins/dataTables-1.10.9/media/css/jquery.dataTables.css">
<script type="text/javascript" charset="utf8" src="/plugins/dataTables-1.10.9/media/js/jquery.dataTables.js"></script>
<script type="text/javascript" charset="utf8" src="/js/common/dataTables.common.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		$.ajaxSetup(
		{
			cache: false
		});
	});

	function handleAjaxError(e, settings, techNote, message) {
		$("#errorMessage").html("Please try to reload the page and if the problem still persists, please contact the system administrator.")
		$("#errorMessageDiv").removeClass("hidden");
	}
</script>