				$(function(){
							var section=$(".PicNavWrap section"),sectionli=$(".PicNavWrap section ul li"),sectionul=$(".PicNavWrap section ul");
							navWidth();
							
							$('.PicNavWrap a.left').bind('click',function(){
								if($(this).hasClass('disabled') || sectionul.is(":animated")){return false;}
									var scrolldist=(section.outerWidth()-Math.abs(sectionul.position().left)<=0)?section.outerWidth():Math.abs(sectionul.position().left);
									sectionul.animate({"left":"+="+scrolldist},function(){
									$('.PicNavWrap a.right').addClass('on');
									if($(sectionul).position().left>=0){
										$('.PicNavWrap a.left').removeClass('on');
									}
								});
							})
							$('.PicNavWrap a.right').bind('click',function(){
								if($(this).hasClass('disabled') || sectionul.is(":animated")){return false;}
									var leavedist=sectionul.outerWidth()-section.outerWidth()-Math.abs(sectionul.position().left);
									var scrolldist=(leavedist>=section.outerWidth())?section.outerWidth():leavedist;
									sectionul.animate({"left":"-="+scrolldist},function(){
									$('.PicNavWrap a.left').addClass('on');
									if(section.outerWidth()>=(sectionul.outerWidth()-Math.abs(sectionul.position().left))){
										$('.PicNavWrap a.right').removeClass('on');
									}
								});
							})

				
							
				})
				var slideshow=new SLIDERMOVE.slider.slide('slideshow',{
							id:'Bimg',
							auto:5,
							resume:true,
							vertical:false,
							autonavitem:false,
							navid:'PicNav',
							activeclass:'current',
							position:0,
							rewind: false,
							elastic:false,
							callback:txtinfo,
							left:'',
							right:''
						});
						
						 if( !($.browser.msie && parseInt( $.browser.version )<9)){
									$(window).bind("resize load",function(){
										navWidth();
										slideshow.relad();
									})
						 }

												
						function txtinfo(idx)
						{
							$("#PicIntro li").hide().eq(idx).fadeIn();
						}
						
						function navWidth()
						{
							$("#Bimg img").css("width",$("#Bimg").outerWidth());
							if($("#Bimg img").outerHeight()>0)$("#Bimg").css("height",$("#Bimg img").outerHeight());
							var section=$(".PicNavWrap section"),sectionli=$(".PicNavWrap section ul li"),sectionul=$(".PicNavWrap section ul");
							section.css("width","auto");
							section.css({"width":section.outerWidth()});
							sectionul.css({"width":sectionli.outerWidth()*sectionli.length,"position":"absolute"});
							if(section.outerWidth()<sectionul.outerWidth()){
										$('.PicNavWrap a.right').addClass('on');
									}
							
						}			
