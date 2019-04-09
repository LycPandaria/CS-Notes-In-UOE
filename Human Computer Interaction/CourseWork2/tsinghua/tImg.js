var slideshow=new SLIDERMOVE.slider.slide('slideshow',{
		id:'Bimg',
		auto:5,
		resume:true,
		vertical:false,
		autonavitem:true,
		navid:'tImgNav',
		activeclass:'current',
		position:0,
		rewind: false,
		elastic:false,
		left:'',
		right:''
	});
$(function(){
	
	$("#Bimg li").css("width",$("#Bimg").outerWidth());
	 if( !($.browser.msie && parseInt( $.browser.version )<9)){
		$(window).bind("resize load",function(){
		$("#Bimg li").css("width",$("#Bimg").outerWidth());
		if($("#Bimg img").outerHeight()>0)$("#Bimg").css("height",$("#Bimg img").outerHeight());
		slideshow.relad();
		})
	 }

	})						
