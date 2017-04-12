var NAVIGATION_LINK_MAP =
{
	HOME: "#menu_home",
	ABOUT: "#menu_about",
	PATIENT: "#menu_manage"
};

function showActiveMenu(linkName) {
	for( var key in NAVIGATION_LINK_MAP) {
		$(NAVIGATION_LINK_MAP[key]).removeClass("active");
	}
	$(linkName).addClass("active");
}

$(document).on('click', '#logout', function() {
	$("#logoutForm").submit();
});