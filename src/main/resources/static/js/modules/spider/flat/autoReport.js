var entityNamecode;

$(function () {
	vm.getEntitys();
	
});

var vm = new Vue({
	el:'#rrapp',
	data:{
		q:{
			key: "",
			begindate: "",
			enddate: "",
			entitySelected: "news",
			begindate:"",
			enddate:"",
			searchKeywords:""
		},
		checkboxEntitiesList:{},
		entityChecked: false,
        checkedEntitiesList:[],
		showList:true,
		info: {
            status: 1,
            type: 0,
            domain: '',
            roleIdList: [],
            jsonData: ''
        },
        checkboxReportList:[
                            {
                            	'id':'title',
                            	'name':'标题'
                            },
                            {
                            	'id':'pubDate',
                            	'name':'发布时间'
                            },
                            {
                            	'id':'url',
                            	'name':'原文链接'
                            },
                            {
                            	'id':'summary',
                            	'name':'摘要'
                            },
                              ],
		reportChecked: false,
        checkedReportList:[],
        
	},
	methods: {
		checkedAllEntities: function() {
			var _this = this;
            console.log(_this.checkedEntitiesList);
            console.log(_this.entityChecked);
            if (!_this.entityChecked) { //实现反选
                _this.checkedEntitiesList = [];
            } else { //实现全选
                _this.checkedEntitiesList = [];
                this.checkboxEntitiesList.forEach(function(item, index) {
                    _this.checkedEntitiesList.push(item.code);
                });
            }
		},
		createReport: function() {
			
			if(vm.validateDate()) {
				return;
			}
			
			var iddb = {'searchKeywords':vm.q.searchKeywords,'begindate':vm.q.begindate, 'enddate':vm.q.enddate, 
					'checkedEntitiesList':vm.checkedEntitiesList, 'checkedReportList':vm.checkedReportList};
			var str = JSON.stringify(iddb);
			var url = "dataexport/createReport";
			$.ajax({
				type: "POST",
			    url: baseURL + url,
		        contentType: "application/json",
			    data: str,
			    success: function(info){
			    	if(info.code === 1) {
			    		alert(info.msg);
			    		return;
			    	} else {
				    	$("div#content_main").html("");
				    	var $div = $("<div class=\"content_main_con\"></div>")
				    	$div.append("<b>关键词: </b>" + vm.q.searchKeywords + "<br>");
				    	if(vm.q.begindate) {
				    		$div.append("<b>开始时间: </b>" + vm.q.begindate + "<br>");
				    	}
				    	if(vm.q.enddate) {
				    		$div.append("<b>结束时间: </b>" + vm.q.enddate + "<br>");
				    	}
				    	$div.append("<b>数据源: </b>" + vm.checkedEntitiesList + "<br>");
				    	$div.append("<b>结果总数: </b>" + info.result.totalCount + "<br>");
				    	$div.append(info.result.list);
		                $("div#content_main").append($div);
				    	
		                $("#mymodal").modal('show');
			    	}
		    	}
		    });
		},
		checkedAllReport: function() {
			var _this = this;
			console.log(_this.checkedReportList);
			console.log(_this.reportChecked);
			if (!_this.reportChecked) { //实现反选
				_this.checkedReportList = [];
			} else { //实现全选
				_this.checkedReportList = [];
				this.checkboxReportList.forEach(function(item, index) {
					_this.checkedReportList.push(item.id);
				});
			}
		},
		query: function () {
			vm.showEnContent=false;
			vm.showCnContent=false;
			queryGrid();
		},
		getEntitys: function(){
			$.get(baseURL + "newsquery/select", function(r){
				vm.checkboxEntitiesList = r.list;
			});
		},
		reload:function(){
			queryGrid();
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
		back: function() {
	          vm.reload();
		},
		report: function() {
			var iddb = {'ids':ids,'entity':vm.q.entitySelected, 'project':vm.project};
			var str = JSON.stringify(iddb);
			var url = "dataexport/report";
			$.ajax({
				type: "POST",
			    url: baseURL + url,
		        contentType: "application/json",
			    data: str,
			    success: function(info){
			    	if(info.filepath === '') {
			    		alert("内部错误, 请联系管理员");
			    	} else {
			    		var form=$("<form>");
						form.attr('style', 'display:none');
						form.attr('target', '');
						form.attr('method', 'post');
						form.attr('action', baseURL + 'dataexport/downreport?token=' + token);
						
						var fileIdInput = $('<input>');
						fileIdInput.attr('type', 'hidden');
						fileIdInput.attr('name', 'filepath');
						fileIdInput.attr('value', info.filepath);
						
						$('body').append(form);
						form.append(fileIdInput);
						form.submit();
			    	}
			    	queryGrid();
		    	}
		    });
		},
		getSubdate(dateStr, beginIndex, length) {
			dateStr = dateStr.substring(beginIndex, length);
			return dateStr;
		},
		validateDate: function () {
            if (isBlank(vm.q.searchKeywords)) {
                alert("搜索关键词不能为空!");
                return true;
            }
            if (vm.q.begindate > vm.q.enddate) {
                alert("搜索结束时间不能小于开始时间!");
                return true;
            }
            if (isBlank(vm.checkedEntitiesList)) {
                alert("请选择导出数据源!");
                return true;
            }
            if (isBlank(vm.checkedReportList)) {
            	alert("请选择导出字段项!");
            	return true;
            }

        }
	}
});