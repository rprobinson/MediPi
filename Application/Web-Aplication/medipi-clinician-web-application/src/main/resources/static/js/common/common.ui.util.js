/*******************************************************************************
 * BEGIN : accordion script
 ******************************************************************************/
$("body").on('click', '.accordion-head a:first-child', function(event) {
	event.preventDefault();
	$(this).parents('.accordion-head').next('.accordion-body').slideToggle("fast");

	if($(this).hasClass('on')) {
		$(this).removeClass('on').addClass('off');
		$(this).attr("aria-expanded", "false");
	} else if($(this).hasClass('off')) {
		$(this).removeClass('off');
		$(this).addClass('on');
		$(this).attr("aria-expanded", "true");
	}
});
/*******************************************************************************
 * END : accordion script
 ******************************************************************************/
