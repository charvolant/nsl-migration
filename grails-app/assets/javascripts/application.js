// This is a manifest file that'll be compiled into application.js.
//
// Any JavaScript file within this directory can be referenced here using a relative path.
//
// You're free to add application-wide JavaScript to this file, but it's generally better 
// to create separate JavaScript files as needed.
//
//= require bootstrap
//= require jquery
//= require_tree .
//= require_self

if (typeof jQuery !== 'undefined') {
	(function($) {
		$('#spinner').ajaxStart(function() {
			$(this).fadeIn();
		}).ajaxStop(function() {
			$(this).fadeOut();
		});
	})(jQuery);
}

function mapperLinks() {
    $('[mapper-link]').each(function() {
        var element = $(this)
        var link = element.attr("mapper-link");
        $.getJSON(link, function(data) {
            var href = null;
            for (var i = 0; i < data.length; i++) {
                var ref = data[i];
                if (ref.preferred)
                    href = ref.link;
            }
            if (href != null) {
                element.html("<a href=\"" + href + "\">" + element.html() + "</a>");
            }
        });
    })
}
