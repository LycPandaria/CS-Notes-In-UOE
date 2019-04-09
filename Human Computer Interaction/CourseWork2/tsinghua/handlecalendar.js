
$(function() {
	var dates = [];
	var msg =[];
		$( "#datepicker" ).datepicker(
		{
			numberOfMonths: lod(),
			regional:'zh-CN',
			onSelect:function(dateText){
					alert($.inArray(dateText.replace(/-/g,"/"),dates)<0?'无事件':msg[$.inArray(dateText.replace(/-/g,"/"),dates)]);
				},
			beforeShowDay: highlightDays,
			beforeShow:function(){alert('')}
			
		}
	)
	 function highlightDays(date) {
		for (var i = 0; i < dates.length; i++) {
			if (new Date(dates[i]).toDateString() == date.toDateString()) {
						  return [true, 'highlight'];
				  }
		  }
		  return [true, ''];
		}  
	/*resizeDatepicker();
	function resizeDatepicker(){
		setTimeout(function() {  $('#datepicker .ui-datepicker-inline').css("width","450px"); }, 0);
	}*/
	
	$(window).bind("resize",function(){
			lod();
		})
	function lod()
	{
		if($(window).outerWidth()<=640 && $(window).outerWidth()>=480)
		{
			$( "#datepicker" ).datepicker( "option", "numberOfMonths", 2 ); 
			return 2
		}else if($(window).outerWidth()<480){
			$( "#datepicker" ).datepicker( "option", "numberOfMonths", 1 ); 
			return 1
		}else
		{
			$( "#datepicker" ).datepicker( "option", "numberOfMonths",3 ); 
			return 3
		}
	}
  
});
