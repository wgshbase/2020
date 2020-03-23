var entityNamecode;

$(function () {
	vm.getEntitys();
	$("#jqGrid").jqGrid({
		url: baseURL + 'dataexport/searchByQuery',
		datatype: "json",
		postData:{'keyWord':"",'entity':vm.q.entitySelected,'begindate':vm.q.begindate,'enddate':vm.q.enddate},
		colModel: [			
	           { label: '采集源ID', name: '_id', index: "_id", hidden:true,width: 45, key: true },
	           { label: '标题', name: 'title', width: 180 ,formatter: function(value, options, row){
	        	   return "<a  href='javascript:vm.querybyid(\""+row._id+"\")'>" + row.title + "</a>";
	           }},
	           { label: '发布时间', name: 'pubDate', width: 50},
	           { label: '网站', name: 'crawler_site', width: 25},
	           { label: '项目', name: 'crawler_project', width: 20},
	           { label: '审核状态', name: 'status', index: 'status', width: 25,formatter: "select", editoptions:{value:"0:初始;1:通过;-1:未通过;-2:重复"}},
	           { label: '采集时间', name: 'crawler_date', index: 'crawler_date', width: 40 ,formatter: function (value, options, row) {
	                 if(!row.crawler_date) return "未知";
	                 var timestamp = row.crawler_date*1;
	                 var date = new Date(timestamp);    // 根据时间戳生成的时间对象
	                 return dateFtt("yyyy-MM-dd hh:mm:ss",date);
	             }}
	           ],
	           viewrecords: true,
	           height: 600,
	           rowNum: 30,
	           rowList : [10,30,50],
	           rownumbers: true, 
	           rownumWidth: 50, 
	           autowidth:true,
	           multiselect: true,
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
	        	   // 隐藏grid底部滚动条
	        	   $("#jqGrid").closest(".ui-jqgrid-bdiv").css({ "overflow-x" : "hidden" }); 
	           },
	           loadComplete: function() {
    		        jQuery("#jqGrid").jqGrid('setLabel', 'rn', '序号', {
    			        'text-align': 'center',
    			        'vertical-align': 'middle',
    			        "width": "50"
    			    });
    			    $("table[role='grid']").each(function () {//jqgrid 创建的表格都有role属性为grid
    			        $('.' + $(this).attr("class") + ' tr:first th:first').css("width", "50"); //使表头的序号列宽度为40
    			        $('.' + $(this).attr("class") + ' tr:first td:first').css("width", "50"); // 使表体的序号列宽度为40
    			    })
    			}

		});
	
});
function queryGrid(){
	
	vm.showList = true;
	vm.showtranslateButton=false;
	var page = $("#jqGrid").jqGrid('getGridParam','page');
	$("#jqGrid").jqGrid('setGridParam',{ 
        postData:{'keyWord': vm.q.key,'entity':vm.q.entitySelected,'begindate':vm.q.begindate,'enddate':vm.q.enddate},
        page:page
    }).trigger("reloadGrid");
};

var vm = new Vue({
	el:'#rrapp',
	data:{
		q:{
			key: "",
			begindate: "",
			enddate: "",
			entitySelected: "news_zhs"
		},
		entitys:{},
		showList:true,
		showtranslateButton:false,
		info: {
            status: 1,
            type: 0,
            domain: '',
            roleIdList: [],
            jsonData: '',
            id:''
        },
	},
	methods: {
		query: function () {
			queryGrid();
		},
		getEntitys: function(){
			$.get(baseURL + "newsquery/select", function(r){
				vm.entitys = r.list;
			});
		},
		reload:function(){
			queryGrid();
		},
		translate:function(id){
			var iddb = {'id':id,'entity':vm.q.entitySelected};
			var str = JSON.stringify(iddb);
			var url = "newsquery/searchByid";
			$.ajax({
				type: "POST",
			    url: baseURL + url,
		        contentType: "application/json",
			    data: str,
			    success: function(info){
			    	$("div#content_main").html("");
			    	if(info.content_tr != null) {
			    		$(".tempTranslate a").remove();
		                $("div#content_main").append("<div class=\"con-eng col-md-6\"><h4 style=\"text-align: center;\"><b>" + info.title_tr + "</b></h4>" + info.content_tr + "</div>");
		                $("div#content_main").append("<div class=\"con-chin col-md-6\"><h4 style=\"text-align: center;\"><b>" + info.title + "</b></h4>" + info.content + "</div>");
		            } else {
		                $("div#content_main").append("<div class=\"content_main_title\" >"+info.title+"</div>");
		                if(info.pubDate) {
		                	$("div#content_main").append("<div class=\"content_main_dis\" ><h4 style=\"text-align: center;\"><b>发布时间: "+vm.getSubdate(info.pubDate, 0, 10)+"</b></h4></div>");
		                }
		                $("div#content_main").append("<div class=\"content_main_con\">"+info.content+"</div>");
			    	}
				}
			});
			$("#mymodal").modal('show');
		},
		querybyid: function(id) {
			var iddb = {'id':id,'entity':vm.q.entitySelected};
			var str = JSON.stringify(iddb);
			var url = "newsquery/searchByid";
			$.ajax({
				type: "POST",
			    url: baseURL + url,
		        contentType: "application/json",
			    data: str,
			    success: function(info){
			    	$("div#content_main").html("");
			    	vm.info.id = info._id;
			    	if(info.content_tr != null) {
			    		$(".tempTranslate a").remove();
			    		$(".tempTranslate").append("<a class=\"btn btn-primary\" href='javascript:vm.translate(\""+info._id+"\")\'>自动翻译</a>");
			    		$("#showtranslateButton").attr("style", "display:block");
		            } else {
		            	$("#showtranslateButton").attr("style", "display:none");
		            }
	                $("div#content_main").append("<div class=\"content_main_title\" >"+info.title+"</div>");
	                if(info.pubDate) {
	                	$("div#content_main").append("<div class=\"content_main_dis\" ><h4 style=\"text-align: center;\"><b>发布时间: "+vm.getSubdate(info.pubDate, 0, 10)+"</b></h4></div>");
	                }
	                $("div#content_main").append("<div class=\"content_main_con\">"+info.content+"</div>");
				}
			});
			$("#mymodal").modal('show');
		},
		getSubdate(dateStr, beginIndex, length) {
			dateStr = dateStr.substring(beginIndex, length);
			return dateStr;
		}
		
	}
});