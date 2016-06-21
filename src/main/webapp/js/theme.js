$(document).ready(function(){
    $('.carousel').carousel({
      interval: 3000,
      pause: 'hover'
    });

    $('.slideshow').each( function() {
        var $slideshow = $(this);
        $slideshow.imagesLoaded( function() {
            $slideshow.skidder({
                slideClass    : '.slide',
                animationType : 'css',
                scaleSlides   : true,
                maxWidth : 1300,
                maxHeight: 500,
                paging        : true,
                autoPaging    : true,
                pagingWrapper : ".skidder-pager",
                pagingElement : ".skidder-pager-dot",
                swiping       : true,
                leftaligned   : false,
                cycle         : true,
                jumpback      : false,
                speed         : 400,
                autoplay      : false,
                autoplayResume: false,
                interval      : 4000,
                transition    : "slide",
                afterSliding  : function() {},
                afterInit     : function() {}
            });
        });
    });

    var imgL = $(".jq-slider img").size();
    var mIndex = Math.floor( imgL/2 );
    function addLeft(){ 
        var winW = $(window).width();
        var imgW = $(".jq-slider img").width();
        var mLeft = (winW - imgW)/2 ;
        $(".jq-slider img").each(function(i){
            $(this).css('left',mLeft - 100*(mIndex-i) + 'px');
        });
    };

    addLeft();

    $(window).resize(function(){
         addLeft();
    });   
    
    $(".jq-slider img").each(function(i){
        if(i < mIndex){
            $(this).attr("class", "left");
        } else if (i == mIndex) {
            $(this).attr("class", "middle");
        } else {
            $(this).attr("class", "right");
        }
    });
        
    $(".jq-slider img").on('click', function() {
        var index = $(this).index();
        
        $(".jq-slider img").each(function(i){
            $(this).css('left',parseInt($(this).css('left') ) - 100*(index - mIndex) +"px");
         
            if (i < index) {
                $(this).attr("class", "left");
            } else if (i == index){
                $(this).attr("class", "middle");
            } else {
                $(this).attr("class", "right");
            }
        });

        mIndex = index;
    });  
});