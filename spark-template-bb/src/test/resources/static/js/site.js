$(function(){
  maybeStickToBottom();
});

Intercooler.ready(function(content){


    content.find("#input-box").keypress(function (e) {
        if(e.which == 13 && !e.shiftKey) {
            $(this).closest('form').submit();
        }
    });

    content.find('.sticky-to-bottom').on('scroll', function() {
        if(Math.round($(this).scrollTop() + $(this).innerHeight(), 10) >= Math.round($(this)[0].scrollHeight, 10)) {
          $(this).addClass('stuck')
        } else {
          $(this).removeClass('stuck')
        }
    })
});

function stickMessages() {
  $('#messages').addClass('stuck')
}

function maybeStickToBottom() {
    $(".sticky-to-bottom.stuck").each(function(){
      var elt = $(this)[0];
      elt.scrollTop = elt.scrollHeight;
    });
}