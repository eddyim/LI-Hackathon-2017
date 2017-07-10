$(document).ready(function(){
});

function scrollMessagesToBottom() {
    var objDiv = document.getElementById("messages");
    objDiv.scrollTop = objDiv.scrollHeight;
    console.log("scroll");
}