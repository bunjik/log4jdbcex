(function($) {
  $.fn.paintTable=function(config) {
    var defaults = {
    };

    return this.each(function() {
      // var numTh = $("table").find("th").length;
      var numTh = $(this).find("th").length;
      var isEven = true;
      // $("table tr").each(function() {
      $(this).find("tr").each(function() {
        if (numTh == $(this).find("td").length) {
          isEven = !isEven;
        }
        $(this).find("td").addClass(isEven ? "even" : "odd");
      });
    });
  };
})(jQuery);
