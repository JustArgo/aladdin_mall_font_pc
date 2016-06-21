$(document).ready(function(){
    var initModalWrap = {
        init: function(){
            var modalWrap = '<div class="modal fade in" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">';
                    modalWrap += '<div class="modal-dialog">';
                        modalWrap += '<div class="modal-content">';
                            modalWrap += '</div>';
                                modalWrap += '</div>';
                                    modalWrap += '</div>';
            
            $('body').append(modalWrap);
        },
        appendCont: function(self,callback){
            var className = self.data('class'),
                modelContent = $('.modal .modal-content'),
                selfScrollTop = $(window).scrollTop(),
                bodyH = $('body').height();
            $('html,body').animate({scrollTop: 0},400);
            $('.modal-dialog').addClass(className);
            $('#myModal').off().on('shown.bs.modal', function () {
                if(bodyH> $(window).height()){
                    $('.modal-open .modal').css({'height':bodyH});
                }
            }).on('hide.bs.modal', function () {
                $('html,body').animate({scrollTop: selfScrollTop},400);
            }).on('hidden.bs.modal', function(self){
                $('.modal-dialog').removeClass(className);
                modelContent.html('');
            });
            if(self.data('href')){
                var href = self.data('href');
                $.get(href, function(data){
                    modelContent.html(data);
                    if(self.data('callback')){
                        cus_callback = self.data('callback');
                        eval(cus_callback);
                    }
                    if(callback){
                        callback();
                    }
                });
            }
        }
    }
    
    window.initModalWrap = initModalWrap;

    $('.carousel').carousel({
      interval: 3000,
      pause: 'hover'
    });

    $('.jq-subToCart').on('click', function(){
        var value = parseInt($(this).next().val());
        $(this).next().val(value - 1);
    });

    $('.jq-addToCart').on('click', function(){
        var value = parseInt($(this).prev().val());
        $(this).prev().val(value + 1);
    });

    initModalWrap.init();

    $('body').on('click', '.jq-deleteProduct', function(){
        var _this = $(this);
        initModalWrap.appendCont(_this,function(){

        });
    });

    $('body').on('click', '.jq-addStore', function(){
        var _this = $(this);
        initModalWrap.appendCont(_this,function(){

        });
    });
});