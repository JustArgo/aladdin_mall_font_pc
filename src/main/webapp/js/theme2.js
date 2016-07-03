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
        if(value == 0) return false;
        $(this).next().val(value - 1);
    });

    $('.jq-addToCart').on('click', function(){
        var value = parseInt($(this).prev().val());
        $(this).prev().val(value + 1);
    });

    $('.jq-prodSub').on('click', function(){
        var value = parseInt($('.jq-prodNum').val());
        if(value == 0) return false;
        $('.jq-prodNum').val(value - 1);
    });

    $('.jq-prodAdd').on('click', function(){
        var value = parseInt($('.jq-prodNum').val());
        $('.jq-prodNum').val(value + 1);
    });

    initModalWrap.init();

    /* popup */
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

    $('body').on('click', '.jq-cardValidate', function(){
        var _this = $(this);
        initModalWrap.appendCont(_this,function(){

        });
    });
    
    $('body').on('click', '.jq-cardDelete', function(){
        var _this = $(this);
        initModalWrap.appendCont(_this,function(){

        });
    });

    $('body').on('click', '.jq-alipay', function(){
        var _this = $(this);
        initModalWrap.appendCont(_this,function(){

        });
    });
    
    $('body').on('click', '.jq-paymentPopup', function(){
        var _this = $(this);
        initModalWrap.appendCont(_this,function(){

        });
    });

    $('body').on('click', '.jq-orderError', function(){
        var _this = $(this);
        initModalWrap.appendCont(_this,function(){

        });
    });

    $('body').on('click', '.jq-addressPopup', function(){
        var _this = $(this);
        initModalWrap.appendCont(_this,function(){

        });
    });

    $('body').on('click', '.jq-orderCancel', function(){
        var _this = $(this);
        initModalWrap.appendCont(_this,function(){

        });
    });

    $('.jq-address').on('click', '.col-md-3', function(){
        $(this).toggleClass('active');
        $(this).siblings().removeClass('active');
    });

    $('.jq-choosen').on('click', 'span', function(){
        $(this).toggleClass('active');
        $(this).siblings().removeClass('active');
    });

    $('.jq-slider').on('click', '> li', function(){
        $(this).toggleClass('active');
        $(this).siblings().removeClass('active');
    });

    $('.jq-cardTab').on('click', function(){
        $(this).parent().find('.submenu').toggle();
    });

    $('.jq-submenu').on('click', 'li', function(){
        var wrap = $(this).html();
        $('.jq-cardTab').html(wrap);
    });

    $('.jq-openAddress').on('click',  function(){
        $(this).hide();
        $('.jq-address').show();
        $('.jq-closeAddress').css('display', 'block');
    });

    $('.jq-closeAddress').on('click',  function(){
        $(this).hide();
        $('.jq-address').hide();
        $('.jq-address').eq(0).show();
        $('.jq-openAddress').show();
    });
});