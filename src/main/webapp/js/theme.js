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
        
            $('.modal-dialog').addClass(className);
            $('#myModal').off().on('shown.bs.modal', function () {
            }).on('hide.bs.modal', function () {
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

    $('.jq-slider').on('click', '> li', function(){
        var url = $(this).find('img').attr('src');
        $(this).toggleClass('active');
        $(this).siblings().removeClass('active');
        $('.jq-prodImage').attr('src', url);
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

    $('.jq-tabCont').on('click', function(){
        $(this).addClass('active').siblings().removeClass('active');
        var pageId = $(this).attr('data-href');
        $(pageId).addClass('active in').siblings().removeClass('active in');
    });

    /** cart **/
    $('.jq-store').on('click', function(){
        $(this).parent().find('.jq-addStore').fadeIn();
    });

    $('.jq-selectAll').on('click', function(){
        var isChecked = $(this).prop('checked');
        $('.jq-select').prop('checked', isChecked);
    });

    $('.jq-firstLevel').on('click', function(){
        var arrow = $(this).find('.glyphicon');
        if(arrow.hasClass('glyphicon-triangle-right')) {
            arrow.addClass('glyphicon-triangle-bottom').removeClass('glyphicon-triangle-right');
        } else {
            arrow.addClass('glyphicon-triangle-right').removeClass('glyphicon-triangle-bottom');
        }
        $(this).parent().find('.jq-fistMenu').slideToggle('slow');
    });

    $('.jq-secondLevel').on('click', function(){
        var arrow = $(this).find('.glyphicon');
        if(arrow.hasClass('glyphicon-triangle-right')) {
            arrow.addClass('glyphicon-triangle-bottom').removeClass('glyphicon-triangle-right');
        } else {
            arrow.addClass('glyphicon-triangle-right').removeClass('glyphicon-triangle-bottom');
        }
        $(this).parent().find('.jq-secondMenu').slideToggle('slow');
    });

    $('.jq-category').on('mouseenter', function(){
        $(this).find('.category-list').slideDown();
    }).on('mouseleave', function(){
        $(this).find('.category-list').slideUp();
    });

    /** countdown **/
    var countdown = setInterval(function(){
        var obj = $('body .jq-timer');
        var timer = parseInt(obj.text());
        if(timer > 0) {
            obj.text(timer - 1);
        }
    }, 1000);

    $('body').on('click', '.jq-start > a', function(){
        var index = $(this).index();
        var arr = $(this).parent().find('a');

        for(var i = 0; i < arr.length; i++){
            if(i <= index){
                $(arr[i]).addClass('active');
            }
        }

        $(this).parent().addClass('static');
    });

    $('.jq-start > a').on('mouseenter', function(){
        var index = $(this).index();
        var arr = $(this).parent().find('a');

        if(!$(this).parent().hasClass('static')){
            for(var i = 0; i < arr.length; i++){
                if(i <= index){
                    $(arr[i]).addClass('active');
                }
            }
        }
    }).on('mouseleave', function(){
        var index = $(this).index();
        var arr = $(this).parent().find('a');

        if(!$(this).parent().hasClass('static')){
            for(var i = 0; i < arr.length; i++){
                if(i <= index){
                    $(arr[i]).removeClass('active');
                }
            }
        }
    });

    $('.jq-category > li').on('mouseenter', function(){
        $(this).find('.category-menu').show();
    }).on('mouseleave', function(){
        $(this).find('.category-menu').hide();
    });
});