angular.module('sbAdminApp').controller('ViewWorkingCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, urlPrefix, loadData) {
	
	console.log(loadData);
	
	$scope.taskDetail = [loadData.taskDetail];
	$scope.groupDatas = loadData.groupDatas;
	$scope.$parent.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.$parent.url = 'search';
	var othersGroupDatas;
	var relatedData;
	var relatedDetail = new Array();
	var lastGroupActive = $scope.groupDatas[0];
	var taskDetailId = $stateParams.id;
	var relatedMenuId;
	lastGroupActive.btnActive = true;
	$scope.fieldName = $filter('orderBy')(loadData.colFormMap[$scope.groupDatas[0].id], 'detOrder');
	$scope.tabActionMenus = [{id: 1, name: 'บันทึกการติดตาม', url: './views/working/tab_trace.html', btnActive: true}, 
	                         {id: 2, name: 'ที่อยู่ใหม่', url: './views/working/tab_addr.html'}, 
	                         /*{id: 3, name: 'ประวัติการนัดชำระ', url: './views/working/tab_3.html'}, 
	                         {id: 4, name: 'payment', url: './views/working/tab_4.html'},*/ 
	                         {id: 5, name: 'บัญชีพ่วง', url: './views/working/tab_related.html'},
	                         {id: 6, name: 'Payment', url: './views/working/tab_payment.html'}];
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
	$scope.askModalObj.comment = loadData.comment;
	
	$scope.addrObj = {};
	$scope.addrObj.names = ['ที่อยู่ทร', 'ที่อยู่ที่ทำงาน', 'ที่อยู่ส่งเอกสาร', 'อื่นๆ']; 
	$scope.addrObj.items = loadData.addresses;
	
	$scope.relatedObj = {};
	
	$scope.paymentObj = {};
	$scope.paymentObj.paymentDetails = loadData.paymentDetails;
	$scope.paymentObj.paymentTotalItems = loadData.paymentTotalItems;
	$scope.paymentObj.formData = {currentPage : 1, itemsPerPage: 5};
	
	$scope.view = function(data, tab) {
		if(taskDetailId == data.id) return;
		
		$scope.$parent.lastTaskView = data;
		taskDetailId = data.id;
		$scope.isEditable = $rootScope.group4 ? (data.sys_owner_id[0] == $rootScope.userId) : true;
		$scope.$parent.idActive = data.id;
		
		$http.post(urlPrefix + '/restAct/taskDetail/view', {
    		id: data.id,
    		traceCurrentPage: $scope.askModalObj.init.currentPage, 
    		traceItemsPerPage: $scope.askModalObj.init.itemsPerPage,
    		productId: $stateParams.productId,
    		currentPagePayment: $scope.paymentObj.formData.currentPage,
    		itemsPerPagePayment: $scope.paymentObj.formData.itemsPerPage 
    	}).then(function(data){
    		var loadData = data.data;
    		
    		if(loadData.statusCode != 9999) {
    			$rootScope.systemAlert(loadData.statusCode);
    			return;
    		}
    
			$scope.askModalObj.init.traceData = loadData.traceResp;
			$scope.addrObj.items = loadData.addresses;
			if(tab != 'related') {
				$scope.relatedTaskDetails = null;    			
			}
			
			if(lastGroupActive.menu) {
				relatedData = loadData.relatedData[lastGroupActive.menu];
				$scope.taskDetail = relatedData.othersData;
			} else {
				$scope.taskDetail = [loadData.taskDetail];    			
			}
			
			if($scope.lastTabActionMenuActive.id == 5) {
				$scope.relatedObj.search();				
			}
			
			$scope.paymentObj.paymentDetails = loadData.paymentDetails;
			$scope.paymentObj.paymentTotalItems = loadData.paymentTotalItems;
			$scope.askModalObj.comment = loadData.comment;
    	}, function(response) {
    		$rootScope.systemAlert(response.status);
    	});
	}
	
	$scope.changeTab = function(group) {
		if($scope.groupDatas.length == 1) return;
		var fields;
		relatedMenuId = group.menu;
		
		if(group.menu) {
			relatedData = loadData.relatedData[group.menu];
			$scope.taskDetail = relatedData.othersData;
			fields = relatedData.othersColFormMap[group.id];
		} else {
			$scope.taskDetail = [loadData.taskDetail];
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
		
		if(menu.id == 5 && $scope.relatedTaskDetails == null) { // Related data tab
			console.log('Related data tab');
			$scope.relatedObj.search();
		}
		
		$scope.lastTabActionMenuActive.btnActive = false;
		$scope.lastTabActionMenuActive = menu;
		menu.btnActive = true;
	}
	
	//------------------------------: Modal dialog :------------------------------------
    var myModal;
	var isDismissModal;
	var address;
	$scope.noticeMenu = function(addr) {
		address = addr;
		
		$http.post(urlPrefix + '/restAct/notice/find', {
			enabled: true,
			currentPage: 1, 
			itemsPerPage: 1000,
			productId: $stateParams.productId	
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			console.log(result);
			if(result.files && result.files.length == 1) {
				$scope.printNotice(result.files[0].id);
				return;
			}
			
			$scope.files = result.files;
		
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
		if(!myModal) return;
		
		isDismissModal = true;
		myModal.modal('hide');
	}
	
	//------------------------------: Modal dialog Ask:------------------------------------
	var isDismissModalAsk;
	var myModalAsk;
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
		
		$scope.askModalObj.trace = angular.copy(data) || {};
		
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
			appointAmount: $scope.askModalObj.trace.appointAmount,
			nextTimeDate: $scope.askModalObj.trace.nextTimeDate,
			actionCode: $scope.askModalObj.trace.actionCode,
			resultCode: $scope.askModalObj.trace.resultCode,
			taskDetailId: taskDetailId,
			contractNo: $scope.askModalObj.init.traceData.contractNo,
			idCardNo: $scope.askModalObj.init.traceData.idCardNo,
			productId: $stateParams.productId	
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			console.log($scope.$parent.lastTaskView);
			
			$scope.$parent.lastTaskView.sys_appointDate = $scope.askModalObj.trace.appointDate;
			$scope.$parent.lastTaskView.sys_nextTimeDate = $scope.askModalObj.trace.nextTimeDate;
			$scope.$parent.lastTaskView.sys_compareDateStatus = result.traceStatus;
			
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
			productId: $stateParams.productId	
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
	$scope.askModalObj.deleteTraceDummy = function($event) {
		$event.stopPropagation();
	}
	$scope.askModalObj.deleteTrace = function($event, id) {
		$event.stopPropagation();
		
		var deleteUser = confirm('ยืนยันการลบข้อมูล');
	    if(!deleteUser) return;	
	    
	    $http.post(urlPrefix + '/restAct/traceWork/delete', {
	    	id: id,
			currentPage: $scope.askModalObj.init.currentPage, 
			itemsPerPage: $scope.askModalObj.init.itemsPerPage,
			contractNo: $scope.askModalObj.init.traceData.contractNo,
			taskDetailId: taskDetailId,
			productId: $stateParams.productId
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
	$scope.askModalObj.updateComment = function(data) {
		$http.post(urlPrefix + '/restAct/traceWork/updateComment', {
	    	comment: data,
			contractNo: $scope.askModalObj.init.traceData.contractNo,
			productId: $stateParams.productId
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	//------------------------------------------------
	
	$scope.paymentObj.changeItemPerPage = function() {
		$scope.paymentObj.formData.currentPage = 1;
		$scope.paymentObj.search();
	}
	$scope.paymentObj.pageChanged = function() {
		$scope.paymentObj.search();
	}
	$scope.paymentObj.search = function() {
		$http.post(urlPrefix + '/restAct/paymentDetail/find', {
			currentPage: $scope.paymentObj.formData.currentPage, 
			itemsPerPage: $scope.paymentObj.formData.itemsPerPage,
			contractNo: $scope.askModalObj.init.traceData.contractNo,
			productId: $stateParams.productId,
			columnName: 'sys_createdDateTime',
			order: 'desc'
		}).then(function(data) {
			loadData = data.data;
			
			if(loadData.statusCode != 9999) {
				$rootScope.systemAlert(loadData.statusCode);
				return;
			}
			
			$scope.paymentObj.paymentDetails = loadData.paymentDetails;
			$scope.paymentObj.paymentTotalItems = loadData.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	//-----------------------------------------------------
	$scope.printNotice = function(id) {
		$http.post(urlPrefix + '/restAct/notice/download', {
			id: id,
			taskDetailId: taskDetailId,
			productId: $stateParams.productId,
			address: address,
			isFillTemplate: true
		}, {responseType: 'arraybuffer'}).then(function(data) {	
			
//			var fileName = decodeURIComponent(data.headers('fileName'));
			
			var file = new Blob([data.data], {type: 'application/pdf'});
	        var fileURL = URL.createObjectURL(file);
	        window.open(fileURL);
	        window.URL.revokeObjectURL(fileURL);  //-- Clear blob on client
			
	        $scope.dismissModal();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	
	//-----------------------------------------: Start Address Tab :------------------------------------------------------
	$scope.addrObj.addItem = function() {
        $scope.addrObj.inserted = {name: '', addr1: '', addr2: '', addr3: '', addr4: '', tel: '', mobile: '', fax: ''};
        $scope.addrObj.items.push($scope.addrObj.inserted);
    };
    
    $scope.addrObj.cancelNewItem = function(item) {
    	for(i in $scope.addrObj.items) {
    		if($scope.addrObj.items[i] == item) {
    			$scope.addrObj.items.splice(i, 1);
    		}
    	}
    }

    $scope.addrObj.removeItem = function(index, id) {
		var deleteUser = confirm('ยืนยันการลบข้อมูล');
	    if(!deleteUser) return;
	    
	    $http.get(urlPrefix + '/restAct/address/delete?id='+id+'&productId='+$stateParams.productId).then(function(data) {
	    			
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.addrObj.items.splice(index, 1);
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	};
	
	$scope.addrObj.saveItem = function(data, item, index) {
		$http.post(urlPrefix + '/restAct/address/save', {
			id: item.id,
			name: data.name,
			addr1: data.addr1,
			addr2: data.addr2,
			addr3: data.addr3,
			addr4: data.addr4,
			tel: data.tel,
			mobile: data.mobile,
			fax: data.fax,
			idCardNo: $scope.askModalObj.init.traceData.idCardNo,
			contractNo: $scope.askModalObj.init.traceData.contractNo,
			productId: $stateParams.productId
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$scope.addrObj.cancelNewItem(item);
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			if(!item.id) {
				item.id = result.id;
				$scope.addrObj.inserted = {name: '', addr1: '', addr2: '', addr3: '', addr4: '', tel: '', mobile: '', fax: ''};
			}
		}, function(response) {
			$scope.addrObj.cancelNewItem(item);
			$rootScope.systemAlert(response.status);
		});
	}
	//-----------------------------------------: End Address Tab :------------------------------------------------------
	
	//-----------------------------------------: Start Related Tab :------------------------------------------------------
	$scope.relatedObj.search = function() {
		$http.post(urlPrefix + '/restAct/taskDetail/find', {
			idCardNo: $scope.askModalObj.init.traceData.idCardNo,
			currentPage: 1, 
			itemsPerPage: 100,
			productId: $stateParams.productId,
			fromPage: 'related_data'
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.relatedTaskDetails = result.taskDetails;	
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	
	$scope.$on("$destroy", function() {
		$scope.$parent.$parent.iconBtn = null;
    });
	
	$scope.updateData = function(colName, val) {
		console.log(colName + ' - ' + val + ' - ' + relatedMenuId);
		
		var params = {
					idCardNo: $scope.askModalObj.init.traceData.idCardNo,
					contractNo: $scope.askModalObj.init.traceData.contractNo,
					productId: $stateParams.productId,
					relatedMenuId : relatedMenuId,
					columnName: colName
				};
		
		if(val instanceof Date) {
			params.valueDate = val;
		} else {
			params.value = val;				
		}
		
		$http.post(urlPrefix + '/restAct/taskDetail/updateTaskData', params).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$rootScope.systemAlert(result.statusCode, 'Update Success');
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
});