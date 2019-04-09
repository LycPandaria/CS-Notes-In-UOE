window.initSearch = function() {
    var $form = $("#searchForm");
    $form.attr("action", "http://search.tsinghua.edu.cn/web");
    $form.submit(function() {
        if ($.trim($form.find("input[name='query']").val()) == "") {
            alert("搜索内容不能为空");
            return false;
        }
    });
};
window.searchSub = function() {
    $("#searchForm").submit();
};
window.initSearch();







