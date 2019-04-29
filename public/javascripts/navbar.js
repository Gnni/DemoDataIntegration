/**
 * Created by guenterhesse on 13/09/16.
 */
$(".nav a").on("click", function(){
    if(!$(this).parent().hasClass('active')) {
        $('#main').toggle();
        $(".nav").find(".active").removeClass("active");
        $(this).parent().addClass("active");
    }
});