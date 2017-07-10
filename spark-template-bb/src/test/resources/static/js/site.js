$(function(){
    $("#input-box").keypress(function (e) {
        if(e.which == 13) {
            //submit form via ajax, this is not JS but server side scripting so not showing here
            $("#chatbox").append($(this).val() + "<br/>");
            $(this).val("");
            e.preventDefault();
        }
    });
});

function scrollMessagesToBottom() {
    var objDiv = document.getElementById("messages");
    objDiv.scrollTop = objDiv.scrollHeight;
    console.log("foo");
}