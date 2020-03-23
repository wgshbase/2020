var dbnamecode;

function queryGrid(keyword){
	var dbname=$("#sele option:selected").val(); 
	console.log(keyword);
	console.log(dbname);
	dbnamecode = dbname;
	$("#jqGrid").jqGrid('clearGridData'); 
	var postData = $('#jqGrid').jqGrid("getGridParam", "postData");  
	$.each(postData, function (k, v) { 
		if(k == 'keyword'){
			postData[k] = keyword;
		}
		if(k == 'dbname'){
			postData[k] = dbname;
		}
	});  
	$("#jqGrid").jqGrid({
		url: baseURL + 'newsquery/searchCrawlerProblemByQuery',
		datatype: "json",
		postData:{'keyword':keyword,'dbname':dbname},
		colModel: [			
		           { label: 'ID', name: 'id', index: "id", hidden:true,width: 45, key: true },
		           { label: '域名', name: 'siteDomain', width: 30},
                { label: '最近采集日期', name: 'crawlerdate', width: 25},
			    { label: '最近发布日期', name: 'pubdate', width: 25},
			    { label: '问题原因', name: 'crawlerProblem', width: 25},
		           ],
		           viewrecords: true,
		           height: 385,
		           rowNum: 10,
		           rowList : [10,30,50],
		           rownumbers: true, 
		           rownumWidth: 25, 
		           autowidth:true,
		           multiselect: false,
		           pager: "#jqGridPager",
		           jsonReader : {
		        	   root: "page.list",
		        	   page: "page.currPage",
		        	   total: "page.totalPage",
		        	   records: "page.totalCount"
		           },
		           prmNames : {
		        	   page:"page", 
		        	   rows:"limit", 
		        	   order: "order"
		           },
		           gridComplete:function(){
		        	   //隐藏grid底部滚动条
		        	   $("#jqGrid").closest(".ui-jqgrid-bdiv").css({ "overflow-x" : "hidden" }); 
		           }
	}).trigger("reloadGrid");
}

var vm = new Vue({
	el:'#rrapp',
	data:{
		q:{
			key: ""
		},
	},
	methods: {
		query: function () {
			queryGrid(vm.q.key)
		},
	}
});

$(function(){
	queryGrid("");
})


function querybyid(id){
	var iddb = {'id':id,'dbname':dbnamecode};
	console.log(iddb);
	var str = JSON.stringify(iddb);
	var url = "newsquery/searchByid";
	$.ajax({
		type: "POST",
	    url: baseURL + url,
        contentType: "application/json",
	    data: str,
	    success: function(info){
	    	console.log(info)
	    	$("div#content_main").html("");

            if(info.contentTr != null) {
                $("div#content_main").append("<div class=\"con-chin col-md-6\">" + info.engContent + "</div>");
                //$("div#content_main").append("<div class=\"content_main_con\">"+info.contentTr+"</div>");
                $("div#content_main").append("<div class=\"con-eng col-md-6\"><p style=\"text-align: center;\">" + info.titleTr + "</p>" + info.chinContent + "</div>");
            } else {
                $("div#content_main").append("<div class=\"content_main_title\" >"+info.title+"</div>");
                $("div#content_main").append("<div class=\"content_main_dis\" >"+info.pubdate+"</div>");
                $("div#content_main").append("<div class=\"content_main_con\">"+info.content+"</div>");
	    	}
		}
	});
	$("#mymodal").modal('show');
}