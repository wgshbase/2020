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
	        	   return "<a  href='javascript:vm.view(\""+row._id+"\")'>" + row.title + "</a>";
	           }},
	           { label: '发布时间', name: 'pubDate', width: 50},
	           { label: '网站', name: 'crawler_site', width: 25},
	           { label: '项目', name: 'crawler_project', width: 20},
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
			entitySelected: "news_air"
		},
		entitys:{},
		showList:true,
		showEnContent:false,
		showCnContent:false,
		info: {
            status: 1,
            type: 0,
            domain: '',
            roleIdList: [],
            jsonData: '',
            nlpKeywords:'',
            nlpSummary:'',
            nlpNamedEntity:''
        },
        ue:null,
        ue2:null
	},
	methods: {
		query: function () {
			vm.showEnContent=false;
			vm.showCnContent=false;
			queryGrid();
		},
		getEntitys: function(){
			$.get(baseURL + "newsquery/select", function(r){
				vm.entitys = r.list;
			});
		},
		reload:function(){
			console.log("4"+vm.ue);
			//if(vm.ue) {
				UE.delEditor("container4");
				vm.ue=null;
				console.log("7");
			//}
				console.log("4"+vm.ue2);
			//if(vm.ue2) {
				UE.delEditor("container2");
				UE.delEditor("container3");
				vm.ue2=null;
				console.log("8");
			//}
			vm.showEnContent=false;
			vm.showCnContent=false;
			console.log("5");
			queryGrid();
			console.log("6");
		},
		view: function(id) {
			console.log("id "+id);
			if(null == id || id === '') {
				id = getSelectedRow();
			}
			if(id) {
				var iddb = {'id':id,'entity':vm.q.entitySelected};
				var str = JSON.stringify(iddb);
				var url = "newsquery/searchMongoById";
				$.ajax({
					async:false,
					type: "POST",
				    url: baseURL + url,
			        contentType: "application/json",
				    data: str,
				    success: function(info){
				    	console.log("1");
				    	$("#indexDiv").attr("style", "display:none");
				    	vm.info = info;
				    	if(info.content_tr != null) {
							vm.showEnContent = true;
							ue2 = new UE.ui.Editor(
			                           {
			                               initialFrameHeight:400,
			                               textarea: 'editorContent2',      //设置提交时编辑器内容的名字
			                               autoFloatEnabled: false,
			                               focus:false,
			                               autoHeightEnabled: false,
			                               sourceEditor: true,
			                               wordCount: false,               //关闭字数统计
			                               elementPathEnabled: false,      //关闭elementPath
			                           });
							ue2.render("container2");
							ue2.ready(function(){
								// 设置初始化的内容
								var content = $("#newscontenttr").val();
								initialFrameHeight:350,
								ue2.setContent(content);
								vm.info.content_tr = ue2.getContent();
								
							});
							 ue = new UE.ui.Editor(
				                       {
				                           initialFrameHeight:400,
				                           textarea: 'editorContent',      //设置提交时编辑器内容的名字
				                           autoFloatEnabled: false,
				                           focus:false,
				                           autoHeightEnabled: false,
				                           sourceEditor: true,
				                           wordCount: false,               //关闭字数统计
				                           elementPathEnabled: false,      //关闭elementPath
				                       });
							   ue.render("container3");
							   ue.ready(function(){
								   //offsetWidth
									// 设置初始化的内容
									var content = $("#newscontent").val();
									initialFrameHeight:350,
									ue.setContent(content);
									
								});
						} else {
							vm.showCnContent = true;
							 ue = new UE.ui.Editor(
				                       {
				                           initialFrameHeight:400,
				                           textarea: 'editorContent',      //设置提交时编辑器内容的名字
				                           autoFloatEnabled: false,
				                           focus:false,
				                           autoHeightEnabled: false,
				                           sourceEditor: true,
				                           wordCount: false,               //关闭字数统计
				                           elementPathEnabled: false,      //关闭elementPath
				                       });
							   ue.render("container4");
							   ue.ready(function(){
								   //offsetWidth
									// 设置初始化的内容
									var content = $("#newscontent").val();
									initialFrameHeight:350,
									ue.setContent(content);
									
								});
						}
					}
				});
				console.log("2");
			  
			   console.log("3");
				vm.showList = false;
				console.log("showEnContent:" + vm.showEnContent +  " showCnContent: " + vm.showCnContent);
				if(vm.showCnContent) {
					$("#showEnContent").attr("style", "display:none");
					$("#showCnContent").attr("style", "display:block");
				}
				if(vm.showEnContent) {
					$("#showCnContent").attr("style", "display:none");
					$("#showEnContent").attr("style", "display:block");
				}
					
			}
		},
		getProject: function() {
			var params = {'entity':vm.q.entitySelected};
			var str = JSON.stringify(params);
			var url = "dataexport/getProject";
			$.ajax({
				type: "POST",
			    url: baseURL + url,
		        contentType: "application/json",
			    data: str,
			    success: function(r){
			    	vm.project = r.project;
				}
			});
		},
		autoIndex: function() {
//			var id = $("#hiddenId").val();
//			var iddb = {'id':id,'entity':vm.q.entitySelected};
//			var str = JSON.stringify(iddb);
//			var url = "dataexport/extract";
//			$.ajax({
//				type: "POST",
//			    url: baseURL + url,
//		        contentType: "application/json",
//			    data: str,
//			    success: function(info){
			    	/*$("div#content_main").html("");
			    	if(info.content_tr != null) {
			    		$("div#content_main").append("<div class=\"content_main_con\"><h4 style=\"text-align:center\"><b>"+info.title_tr+"</b></h4></div>");
			    		$("div#content_main").append("<div class=\"content_main_con\">"+info.content_tr+"</div>");
			    		$("div#content_main").append("<div class=\"content_main_con\"><hr><b>关键词:</b><br>"+info.nlpKeywords+"</div>");
			    		$("div#content_main").append("<div class=\"content_main_con\"><hr><b>摘要:</b><br>"+info.nlpSummary+"</div>");
			    		$("div#content_main").append("<div class=\"content_main_con\"><hr><b>命名实体:</b><br>"+info.nlpNamedEntity+"</div>");
			    		
			    	} else {
			    		$("div#content_main").append("<div class=\"content_main_con\"><h4 style=\"text-align:center\"><b>"+info.title+"</b></h4></div>");
			    		$("div#content_main").append("<div class=\"content_main_con\">"+info.content+"</div>");
			    		$("div#content_main").append("<div class=\"content_main_con\"><hr><b>关键词:</b><br>"+info.nlpKeywords+"</div>");
			    		$("div#content_main").append("<div class=\"content_main_con\"><hr><b>摘要:</b><br>"+info.nlpSummary+"</div>");
			    		$("div#content_main").append("<div class=\"content_main_con\"><hr><b>命名实体:</b><br>"+info.nlpNamedEntity+"</div>");
			    	}*/
			    	$("#indexDiv").attr("style", "display:block");
			    	
//			    	vm.info = info;
			    	
//		    	}
//		    });
		},
		saveOrUpdate: function() {
			var url = "newsquery/update";
			// 特殊字符需要转义
			vm.info.content = ue.getContent().replace(/</g,'&lt;').replace(/>/g,'&gt;');
			if(vm.showEnContent) {
				vm.info.content_tr = ue2.getContent().replace(/</g,'&lt;').replace(/>/g,'&gt;');
			}
			vm.info.entity = vm.q.entitySelected;
            $.ajax({
                type: "POST",
                url: baseURL + url,
                contentType: "application/json",
                data: JSON.stringify(vm.info),
                success: function (r) {
                    if (r.code === 0) {
                        alert('更新成功', function () {
                            vm.reload();
                        });
                    } else {
                        alert(r.msg);
                    }
                }
            });
		},
		back: function() {
	          vm.reload();
		},
		getSubdate(dateStr, beginIndex, length) {
			dateStr = dateStr.substring(beginIndex, length);
			return dateStr;
		},
		validateDate: function () {
            if (isBlank(vm.q.begindate)) {
                alert("开始时间不能为空!");
                return true;
            }
            if (isBlank(vm.q.enddate)) {
                alert("结束时间不能为空!");
                return true;
            }
            if(vm.q.enddate < vm.q.begindate) {
            	alert("结束时间不能小于开始时间!");
            	return true;
            }

        }
	}
});