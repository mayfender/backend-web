angular.module('sbAdminApp').controller('ViewWorkingCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, $timeout, urlPrefix, loadData) {
	
	$scope.taskDetail = [loadData.taskDetail];
	$scope.groupDatas = loadData.groupDatas;
	$scope.$parent.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.$parent.url = 'search';
	$scope.isDisableNoticePrintBtn = ($rootScope.group6 && loadData.isDisableNoticePrint) ? true : false;
	$scope.isDisableNotice = loadData.isDisableNoticePrint;
	var othersGroupDatas;
	var relatedData;
	var relatedDetail = new Array();
	var lastGroupActive = $scope.groupDatas[0];
	var taskDetailId = $stateParams.id;
	var relatedMenuId;
	var traceIdDummy, traceId;
	var countView = 0;
	var customerName;
	var isKeepData = false;
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
	$scope.askModalObj.comment = loadData.comment;
	$scope.askModalObj.noticeFiles = loadData.noticeFiles;
	
	$scope.addrObj = {};
	$scope.addrObj.names = ['ที่อยู่ทร', 'ที่อยู่ที่ทำงาน', 'ที่อยู่ส่งเอกสาร', 'อื่นๆ']; 
	$scope.addrObj.items = loadData.addresses;
	
	$scope.relatedObj = {};
	
	$scope.paymentObj = {};
	$scope.paymentObj.paymentDetails = loadData.paymentDetails;
	$scope.paymentObj.paymentTotalItems = loadData.paymentTotalItems;
	$scope.paymentObj.formData = {currentPage : 1, itemsPerPage: 5};
	$scope.paymentObj.sums = loadData.paymentSums;
	$scope.currentPageActive = $scope.$parent.formData.currentPage;
	
	$scope.dymList = loadData.dymList;
	$("#taskDetailStick").stick_in_parent();
	
	initGroup();
	
	$scope.view = function(data, tab, index) {
		
		if(taskDetailId == data.id) return;
		
		taskDetailId = data.id;
		$scope.isEditable = $rootScope.group4 ? (data.sys_owner_id[0] == $rootScope.userId) : true;
		$scope.$parent.idActive = data.id;
		$scope.$parent.getCurrentIndex();
		$scope.currentPageActive = $scope.$parent.formData.currentPage;
		
		$http.post(urlPrefix + '/restAct/taskDetail/view', {
    		id: data.id,
    		traceCurrentPage: $scope.askModalObj.init.currentPage, 
    		traceItemsPerPage: $scope.askModalObj.init.itemsPerPage,
    		productId: $stateParams.productId,
    		currentPagePayment: $scope.paymentObj.formData.currentPage,
    		itemsPerPagePayment: $scope.paymentObj.formData.itemsPerPage 
    	}).then(function(data){
    		console.log(index + ' - ' + countView);
    		if(index != null && index < countView) {
    			console.log('Not update view');
    			return;
    		}
    		
    		loadData = data.data;
    		
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
			$scope.paymentObj.sums = loadData.paymentSums;
			$scope.askModalObj.comment = loadData.comment;
			traceId = null;
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
			$scope.relatedObj.search();
		}
		if(menu.id == 2) {
			if(traceIdDummy) {
				traceId = angular.copy(traceIdDummy);
			} else {
				traceId = null;
			}
			
			traceIdDummy = null;
		}
		
		$scope.lastTabActionMenuActive.btnActive = false;
		$scope.lastTabActionMenuActive = menu;
		menu.btnActive = true;
	}
	
	//------------------------------: Modal dialog :------------------------------------
    var myModal;
	var isDismissModal;
	var address;
	$scope.noticeMenu = function(addr, noticeForms, cusNameParam) {
		address = addr;
		customerName = cusNameParam;
		
		$http.post(urlPrefix + '/restAct/notice/find', {
			enabled: true,
			currentPage: 1, 
			itemsPerPage: 1000,
			productId: $stateParams.productId,
			noticeForms: noticeForms
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			if(result.files && result.files.length == 1 && !result.files[0].isDateInput) {
				$scope.printNotice(result.files[0].id);
				return;
			}
			
			$scope.files = result.files;
			$scope.haveDateInput = $filter('filter')($scope.files, {isDateInput: true}).length > 0 ;
		
			if(!myModal) {
				myModal = $('#myModal').modal();			
				myModal.on('shown.bs.modal', function (e) {
					$('.date-input').each(function() {
					    $(this).datepicker({
					    	format: 'dd/mm/yyyy',
						    autoclose: true,
						    todayBtn: true,
						    clearBtn: true,
						    todayHighlight: true,
						    language: 'th-en'}
					    );
					});
				});
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
	
	$scope.keepData = function() {
		$scope.dismissModalAsk();
		isKeepData = true;
	}
	
	//------------------------------: Modal dialog Ask:------------------------------------
	var isDismissModalAsk;
	var myModalAsk;
	var traceUpdatedIndex;
	var disNotice;
	$scope.askModal = function(data, i) {
		traceUpdatedIndex = i;
		
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
		
		//-----: Clear value
		if(!isKeepData) {			
			$scope.askModalObj.trace = angular.copy(data) || {};
			for(i in $scope.dymList) $scope.dymList[i].dymListVal = null;
		}
		
		if(data) {
			$scope.askModalObj.trace.appointDate = $scope.askModalObj.trace.appointDate && new Date($scope.askModalObj.trace.appointDate);
			$scope.askModalObj.trace.nextTimeDate = $scope.askModalObj.trace.nextTimeDate && new Date($scope.askModalObj.trace.nextTimeDate);
			var today = new Date($rootScope.serverDateTime);
			
			if($scope.askModalObj.trace.appointDate) {
				$('.datepickerAppointDate').datepicker('update', $filter('date')($scope.askModalObj.trace.appointDate, 'dd/MM/yyyy'));
			} else {				
				$('.datepickerAppointDate').datepicker('update', $filter('date')(today, 'dd/MM/yyyy'));
				$('.datepickerAppointDate').val('');				
			}
			
			if($scope.askModalObj.trace.nextTimeDate) {
				$('.datepickerNextTimeDate').datepicker('update', $filter('date')($scope.askModalObj.trace.nextTimeDate, 'dd/MM/yyyy'));				
			} else {
				$('.datepickerNextTimeDate').datepicker('update', $filter('date')(today, 'dd/MM/yyyy'));
				$('.datepickerNextTimeDate').val('');							
			}
			
			var list, listSeleted;
			var listDet;
			var group;
			for(i in $scope.dymList) {
				list = $scope.dymList[i]
				listSeleted = data['link_' + list.fieldName][0];
				
				if(!listSeleted) continue;
				
				if(list.dymListDetDummy) {
					listDet = $filter('filter')(list.dymListDetDummy, {_id: listSeleted['_id']})[0];
				} else {
					listDet = $filter('filter')(list.dymListDet, {_id: listSeleted['_id']})[0];
				}
				
				list.dymListVal = listDet['_id'];
				
				if(!listDet.groupId) continue;
				
				group = $filter('filter')(list.dymListDetGroup, {_id: listDet.groupId})[0];
				$scope.changeGroup(list, group);
			}
		} else {
			if(!isKeepData) {				
				var today = new Date($rootScope.serverDateTime);
				$('.datepickerAppointDate').datepicker('update', $filter('date')(today, 'dd/MM/yyyy'));
				$('.datepickerAppointDate').val('');						
				$('.datepickerNextTimeDate').datepicker('update', $filter('date')(today, 'dd/MM/yyyy'));
				$('.datepickerNextTimeDate').val('');			
			}
		}
		
		isKeepData = false;
		$scope.askModalObj.actionCodeChanged();
		
		//--------------------------------------------
		disNotice = $scope.isDisableNoticePrintBtn;
		$scope.isDisableNoticePrintBtn = true;
		
		if(!myModalAsk) {	
			myModalAsk = $('#myModal_ask').modal();
			$(myModalAsk).draggable({scroll: false});
//			myModalAsk = $('#myModal_ask').modal({backdrop: false});			
			
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
		$scope.isDisableNoticePrintBtn = disNotice;
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
	$scope.askModalObj.askModalSave = function() {
		var dymVal = new Array();
		var list;
		
		for(i in $scope.dymList) {
			list = $scope.dymList[i];
			dymVal.push({fieldName: list.fieldName, value: list.dymListVal});
		}
		
		$http.post(urlPrefix + '/restAct/traceWork/save', {
			id: $scope.askModalObj.trace['_id'],
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
			productId: $stateParams.productId,
			templateId: $scope.askModalObj.trace.templateId,
			dymListVal: dymVal,
			
			addressNotice: $scope.askModalObj.trace.addressNotice == null ? null : {
				id: $scope.askModalObj.trace.addressNotice.id, 
				columnName: $scope.askModalObj.trace.addressNotice.columnName, 
				menuTable: $scope.askModalObj.trace.addressNotice.menuTable
			},
			
			addressNoticeStr: $scope.askModalObj.trace.addressNotice == null ? null : 
				$scope.askModalObj.trace.addressNotice.addrVal ? $scope.askModalObj.trace.addressNotice.addrVal : 
					$scope.askModalObj.trace.addressNotice.addr1 + ' ' + 
					$scope.askModalObj.trace.addressNotice.addr2 + ' ' + 
					$scope.askModalObj.trace.addressNotice.addr3 + ' ' + 
					$scope.askModalObj.trace.addressNotice.addr4
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			var taskUpdated = $filter('filter')($scope.$parent.taskDetails, {id: taskDetailId})[0];
			
			if(!(traceUpdatedIndex > 0)) {				
				if(traceUpdatedIndex == null) {
					if($scope.askModalObj.trace.appointDate || $scope.askModalObj.trace.nextTimeDate) {
						taskUpdated.sys_appointDate = $scope.askModalObj.trace.appointDate;						
						taskUpdated.sys_nextTimeDate = $scope.askModalObj.trace.nextTimeDate;
					}
				} else {
					taskUpdated.sys_appointDate = $scope.askModalObj.trace.appointDate;
					taskUpdated.sys_nextTimeDate = $scope.askModalObj.trace.nextTimeDate;					
				}
				taskUpdated.sys_compareDateStatus = result.traceStatus;
			}
			
			if(traceUpdatedIndex == null) {
				taskUpdated.sys_traceDate = result.traceDate;
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
	$scope.askModalObj.addAddr = function($event, id) {
		$event.stopPropagation();
		
		$timeout(function() {
            angular.element("button[id='2']").triggerHandler('click');
        }, 0);
		
		traceIdDummy = id;
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
	$scope.askModalObj.actionCodeChanged = function() {
		/*if(!$scope.askModalObj.trace.actionCode) {
			$scope.isShowMoreNoticePrintData = false;
			return;
		}*/
		
		/*var selectedActCode = $filter('filter')($scope.askModalObj.init.actionCodes, {id: $scope.askModalObj.trace.actionCode});
		
		if(!selectedActCode || selectedActCode.length == 0) {
			$scope.isShowMoreNoticePrintData = false;
			return;
		}
		
		if(selectedActCode[0].isPrintNotice && $scope.isDisableNotice) {
			$scope.isShowMoreNoticePrintData = true;
		} else {
			$scope.isShowMoreNoticePrintData = false;
		}*/
		
		$scope.addrList = new Array();
		var noticeItem;
		var relData;
		var addrVal;
		for(i in $scope.groupDatas) {
			if($scope.groupDatas[i].menu) {
				relData = loadData.relatedData[$scope.groupDatas[i].menu];
				noticeItem = $filter('filter')(relData.othersColFormMap[$scope.groupDatas[i].id], {isNotice: true});			
				
				if(noticeItem.length == 0) continue;
				
				for(j in noticeItem) {					
					for(y in relData.othersData) {
						addrVal = relData.othersData[y][noticeItem[j].columnName];
						if(addrVal) {
							$scope.addrList.push({columnName: noticeItem[j].columnName, addrVal: addrVal, menuTable: $scope.groupDatas[i].menu});							
						}
					}
				}
			} else {
				noticeItem = $filter('filter')(loadData.colFormMap[$scope.groupDatas[i].id], {isNotice: true});				
				
				if(noticeItem.length == 0) continue;
				
				for(j in noticeItem) {
					addrVal = $scope.taskDetail[0][noticeItem[j].columnName];
					if(addrVal) {
						$scope.addrList.push({columnName: noticeItem[j].columnName, addrVal: addrVal});						
					}
				}
			}
		}
		
		$scope.addrList = $scope.addrList.concat($scope.addrObj.items);
		
		if($scope.askModalObj.trace.addressNotice) {
			var isSetBack = false;
			
			for(i in $scope.addrList) {
				if($scope.askModalObj.trace.addressNotice.id) {					
					if($scope.addrList[i].id == $scope.askModalObj.trace.addressNotice.id) {
						$scope.askModalObj.trace.addressNotice = $scope.addrList[i];
						isSetBack = true;
						break;
					}
				} else if($scope.askModalObj.trace.addressNotice.menuTable){
					if($scope.addrList[i].menuTable == $scope.askModalObj.trace.addressNotice.menuTable &&
							$scope.addrList[i].columnName == $scope.askModalObj.trace.addressNotice.columnName) {
						$scope.askModalObj.trace.addressNotice = $scope.addrList[i];
						isSetBack = true;
						break;
					}
				} else if($scope.addrList[i].columnName == $scope.askModalObj.trace.addressNotice.columnName){
					$scope.askModalObj.trace.addressNotice = $scope.addrList[i];
					isSetBack = true;
					break;
				}
			}
			
			if(!isSetBack) {
				$scope.askModalObj.trace.addressNotice = null;
			}
			
		}
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
	$scope.printNotice = function(id, dateInput) {
		$http.post(urlPrefix + '/restAct/notice/download', {
			id: id,
			dateInput: dateInput,
			taskDetailId: taskDetailId,
			productId: $stateParams.productId,
			address: address,
			isFillTemplate: true,
			customerName: customerName
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
        $scope.addrObj.inserted = {name: '', addr1: '', addr2: '', addr3: '', addr4: '', tel: '', mobile: '', fax: '', traceId: traceId};
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
			productId: $stateParams.productId,
			traceId: traceId
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
	
	$scope.updateData = function(colName, colNameAlias, val, dataType, detail) {
		if(detail[colName + '_hide']) {
			detail[colName + '_hide'] = val;
		}
		
		if(!val) {		
			$rootScope.systemAlert(1000, 'Can not update');
			return;
		} 
		
		var params = {
					idCardNo: $scope.askModalObj.init.traceData.idCardNo,
					contractNo: $scope.askModalObj.init.traceData.contractNo,
					productId: $stateParams.productId,
					relatedMenuId : relatedMenuId,
					columnName: colName,
					columnNameAlias: colNameAlias,
					dataType: dataType
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
	
	var countIsCount = 0;
	$scope.nextPrev = function(isNext, isChangeIndex) {
		console.log($scope.currentPageActive);
		console.log($scope.$parent.formData.currentPage);
		
		if($scope.currentPageActive != $scope.$parent.formData.currentPage) {
			$scope.$parent.formData.currentPage = $scope.currentPageActive; 
			$scope.$parent.pageChanged(function(){$scope.nextPrev(isNext, true)});
			return;
		}
		
		if(isChangeIndex) {			
			isNext ? $scope.$parent.currentIndex++ : $scope.$parent.currentIndex--;
		}
		
		var nextTask = $scope.$parent.taskDetailIds[$scope.$parent.currentIndex];
		var isFound = false;
		var task;
		
		for(var i in $scope.$parent.taskDetails) {
			task = $scope.$parent.taskDetails[i]
			
			if(task.id == nextTask.id) {
				countView++;
				$scope.view(task, null, countView);
				isFound = true;
				countIsCount = 0;
				break;
			}
		}
		
		if(!isFound && countIsCount == 0) {
			isNext ? $scope.$parent.formData.currentPage++ : $scope.$parent.formData.currentPage--;
			$scope.currentPageActive = $scope.$parent.formData.currentPage;
			$scope.$parent.pageChanged(function(){$scope.nextPrev(isNext, false)});
			countIsCount = 1;
		}
	}
	
	$scope.firstTask = function () {
		$scope.$parent.taskDetails[0] && $scope.view($scope.$parent.taskDetails[0]);
	}
	
	$scope.changeGroup = function(list, gp) {
		list.groupSelected = gp;
		list.dymListDet = $filter('filter')(list.dymListDetDummy, {groupId: gp['_id']});
	}
	
	function initGroup() {
		var list;
		
		for(i in $scope.dymList) {
			list = $scope.dymList[i];
			list.groupSelected = list.dymListDetGroup[0];
			
			if(list.groupSelected) {				
				list.dymListDetDummy = list.dymListDet;
				list.dymListDet = $filter('filter')(list.dymListDetDummy, {groupId: list.groupSelected['_id']});
			}
		}
	}
	
});