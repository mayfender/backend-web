angular.module('sbAdminApp').controller('ViewWorkingCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, urlPrefix, loadData) {
	
	console.log(loadData);
	
	$scope.taskDetail = loadData.taskDetail;
	$scope.groupDatas = loadData.groupDatas;
	var othersGroupDatas;
	var relatedData;
	var relatedDetail = new Array();
	var lastGroupActive = $scope.groupDatas[0];
	var isFirstTimeWorkTab = true;
	var taskDetailId = $stateParams.id;
	var myModalAsk;
	lastGroupActive.btnActive = true;
	$scope.fieldName = $filter('orderBy')(loadData.colFormMap[$scope.groupDatas[0].id], 'detOrder');
	$scope.tabActionMenus = [{id: 1, name: 'บันทึกการติดตาม', url: './views/working/tab_1.html', btnActive: true}, 
	                         {id: 2, name: 'ที่อยู่ใหม่', url: './views/working/tab_2.html'}, 
	                         /*{id: 3, name: 'ประวัติการนัดชำระ', url: './views/working/tab_3.html'}, 
	                         {id: 4, name: 'payment', url: './views/working/tab_4.html'},*/ 
	                         {id: 5, name: 'บัญชีพ่วง', url: './views/working/tab_5.html'},
	                         {id: 6, name: 'ข้อมูลงาน', url: './views/working/tab_6.html'}];
	$scope.lastTabActionMenuActive = $scope.tabActionMenus[0];
	
	$scope.askModalObj = {};
	$scope.askModalObj.init = {};
	$scope.askModalObj.trace = {};
	$scope.askModalObj.init.traceData = loadData.traceResp;
	$scope.askModalObj.init.itemsPerPage = 5;
	$scope.askModalObj.init.currentPage = 1;
	$scope.askModalObj.init.maxSize = 5;
	$scope.askModalObj.init.actionCodes = loadData.actionCodes;
	$scope.askModalObj.init.resultCodeGroups = loadData.resultCodeGroups;
	$scope.askModalObj.init.resultGroup = loadData.resultCodeGroups[0];
	$scope.askModalObj.init.resultCodesDummy = loadData.resultCodes;
	$scope.askModalObj.init.resultCodes = $filter('filter')($scope.askModalObj.init.resultCodesDummy, {resultGroupId: $scope.askModalObj.init.resultGroup.id});
	
	//--------------------------------------------------------------------------------------------------------------------------------------------------------------
	
	$scope.view = function(id) {
		taskDetailId = id;
		$scope.idActive = id;
		$http.post(urlPrefix + '/restAct/taskDetail/view', {
    		id: id,
    		traceCurrentPage: $scope.askModalObj.init.currentPage, 
    		traceItemsPerPage: $scope.askModalObj.init.itemsPerPage,
    		productId: $localStorage.setting.currentProduct	
    	}).then(function(data){
    		var result = data.data;
    		
    		if(result.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}
    
    		loadData = result;
    		$scope.askModalObj.init.traceData = loadData.traceResp;
    		
    		if(lastGroupActive.menu) {
    			relatedData = loadData.relatedData[lastGroupActive.menu];
    			$scope.taskDetail = relatedData.othersData;
    		} else {
    			$scope.taskDetail = loadData.taskDetail;    			
    		}
    	}, function(response) {
    		$rootScope.systemAlert(response.status);
    	});
	}
	
	$scope.changeTab = function(group) {
		if($scope.groupDatas.length == 1) return;
		var fields;
		
		if(group.menu) {
			relatedData = loadData.relatedData[group.menu];
			$scope.taskDetail = relatedData.othersData;
			fields = relatedData.othersColFormMap[group.id];
		} else {
			$scope.taskDetail = loadData.taskDetail;
			fields = loadData.colFormMap[group.id];
		}
		
		$scope.fieldName = $filter('orderBy')(fields, 'detOrder');			
		lastGroupActive.btnActive = false;
		lastGroupActive = group;
		group.btnActive = true;
	}
	
	for(x in loadData.relatedData) {
		relatedData = loadData.relatedData[x];
		othersGroupDatas = relatedData.othersGroupDatas;
		
		for(i in othersGroupDatas) {
			othersGroupDatas[i].menu = x;
		}
		
		$scope.groupDatas = $scope.groupDatas.concat(othersGroupDatas);		
	}
	
	$scope.changeTabAction = function(menu) {
		
		if($scope.lastTabActionMenuActive == menu) return;
		
		if(menu.id == 6 && isFirstTimeWorkTab) {
			$scope.formData.itemsPerPage = 5;
			$scope.search();
			isFirstTimeWorkTab = false;
		}
		
		$scope.lastTabActionMenuActive.btnActive = false;
		$scope.lastTabActionMenuActive = menu;
		menu.btnActive = true;
		myModalAsk = null;
	}
	
	//------------------------------: Modal dialog :------------------------------------
    var myModal;
	var isDismissModal;
	var address;
	$scope.noticeMenu = function(addr) {
		address = addr;
		
		$http.post(urlPrefix + '/restAct/notice/find', {
			isInit: false,
			enabled: true,
			currentPage: 1, 
			itemsPerPage: 1000,
			productId: $localStorage.setting.currentProduct	
		}).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			console.log(data.data);
			$scope.files = data.data.files;
		
			if(!myModal) {
				myModal = $('#myModal').modal();			
				myModal.on('hide.bs.modal', function (e) {
					if(!isDismissModal) {
						return e.preventDefault();
					}
					isDismissModal = false;
				});
				myModal.on('hidden.bs.modal', function (e) {
					//
  				});
			} else {			
				myModal.modal('show');
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.dismissModal = function() {
		isDismissModal = true;
		myModal.modal('hide');
	}
	
	//------------------------------: Modal dialog Ask:------------------------------------
	var isDismissModalAsk;
	$scope.askModal = function(data) {
		var datePickerOptions = {
		    format: 'dd/mm/yyyy',
		    autoclose: true,
		    todayBtn: true,
		    clearBtn: true,
		    todayHighlight: true,
		    language: 'th-en'
		};
		
		$('.datepickerAppointDate').datepicker(datePickerOptions);
		$('.datepickerNextTimeDate').datepicker(datePickerOptions);
		
		$scope.askModalObj.trace = data || {};
		
		if(data) {
			$scope.askModalObj.trace.appointDate = $scope.askModalObj.trace.appointDate && new Date($scope.askModalObj.trace.appointDate);
			$scope.askModalObj.trace.nextTimeDate = $scope.askModalObj.trace.nextTimeDate && new Date($scope.askModalObj.trace.nextTimeDate);
			$('.datepickerAppointDate').datepicker('update', $filter('date')($scope.askModalObj.trace.appointDate, 'dd/MM/yyyy'));
			$('.datepickerNextTimeDate').datepicker('update', $filter('date')($scope.askModalObj.trace.nextTimeDate, 'dd/MM/yyyy'));
			
			var resCode = $filter('filter')($scope.askModalObj.init.resultCodesDummy, {id: data.resultCode})[0];
			var groupId = $filter('filter')($scope.askModalObj.init.resultCodeGroups, {id: resCode.resultGroupId})[0];
			$scope.askModalObj.changeResultGroups(groupId);
		}
		
		if(!myModalAsk) {
			myModalAsk = $('#myModal_ask').modal();			
			myModalAsk.on('hide.bs.modal', function (e) {
				if(!isDismissModalAsk) {
					return e.preventDefault();
				}
				isDismissModalAsk = false;
			});
			myModalAsk.on('hidden.bs.modal', function (e) {
				//
			});
		} else {			
			myModalAsk.modal('show');
		}	
	}
	
	$scope.dismissModalAsk = function() {
		isDismissModalAsk = true;
		myModalAsk.modal('hide');
	}

	$scope.askModalObj.changeItemPerPage = function() {
		$scope.askModalObj.init.currentPage = 1;
		$scope.askModalObj.searchTrace();
	}
	$scope.askModalObj.pageChanged = function() {
		$scope.askModalObj.searchTrace();
	}
	$scope.askModalObj.appointDateClick = function() {
		if($scope.askModalObj.trace.appointDate) {
			$scope.askModalObj.trace.nextTimeDate = $scope.askModalObj.trace.appointDate;			
		}
	}
	$scope.askModalObj.changeResultGroups = function(gp) {
		$scope.askModalObj.init.resultGroup = gp;
		$scope.askModalObj.init.resultCodes = $filter('filter')($scope.askModalObj.init.resultCodesDummy, {resultGroupId: gp.id});
	}
	$scope.askModalObj.askModalSave = function() {
		$http.post(urlPrefix + '/restAct/traceWork/save', {
			id: $scope.askModalObj.trace.id,
			resultText: $scope.askModalObj.trace.resultText,
			tel: $scope.askModalObj.trace.tel,
			appointDate: $scope.askModalObj.trace.appointDate,
			nextTimeDate: $scope.askModalObj.trace.nextTimeDate,
			actionCode: $scope.askModalObj.trace.actionCode,
			resultCode: $scope.askModalObj.trace.resultCode,
			taskDetailId: taskDetailId,
			productId: $localStorage.setting.currentProduct	
		}).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			$scope.askModalObj.searchTrace();
			$scope.dismissModalAsk();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	$scope.askModalObj.searchTrace = function() {
		$http.post(urlPrefix + '/restAct/traceWork/find', {
			currentPage: $scope.askModalObj.init.currentPage, 
			itemsPerPage: $scope.askModalObj.init.itemsPerPage,
			contractNo: $scope.askModalObj.init.traceData.contractNo,
			productId: $localStorage.setting.currentProduct	
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.askModalObj.init.traceData.traceWorks = result.traceWorks;
			$scope.askModalObj.init.traceData.totalItems = result.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	$scope.askModalObj.editTrace = function() {
		console.log('editTrace');
	}
	//------------------------------------------------
	
	
	
	$scope.printNotice = function(id) {
		$http.post(urlPrefix + '/restAct/notice/download', {
			id: id,
			taskDetailId: taskDetailId,
			productId: $localStorage.setting.currentProduct,
			address: address,
			isFillTemplate: true
		}, {responseType: 'arraybuffer'}).then(function(data) {	
			var a = document.createElement("a");
			document.body.appendChild(a);
			a.style = "display: none";
			
			var fileName = decodeURIComponent(data.headers('fileName'));
				
			var type = fileName.endsWith('.doc') ? 'application/msword' : 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
			var file = new Blob([data.data], {type: type});
	        var url = URL.createObjectURL(file);
	        
	        a.href = url;
	        a.download = fileName;
	        a.click();
	        a.remove();
	        
	        window.URL.revokeObjectURL(url); //-- Clear blob on client
	        $scope.dismissModal();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
});