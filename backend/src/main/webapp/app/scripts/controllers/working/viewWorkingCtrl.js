angular.module('sbAdminApp').controller('ViewWorkingCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, $timeout, FileUploader, urlPrefix, loadData) {
	
	if($stateParams.fromPage == 'alert') {
		$scope.$parent.$parent.url = 'dashboard.notification';
	} else if($stateParams.fromPage == 'trace') {
		$scope.$parent.$parent.url = 'dashboard.traceResult';
	} else if($stateParams.fromPage == 'payment') {
		$scope.$parent.$parent.url = 'dashboard.payment.detail';
	} else if($stateParams.fromPage == 'payOnline') {
		$scope.$parent.$parent.url = 'dashboard.payOnlineChecking';
	} else {		
		$scope.$parent.$parent.url = 'dashboard.working.search';
	}
	
	$scope.taskDetailPerm = loadData.taskDetail;
	$scope.calParams = loadData.calParams;
	var discountFieldsPerm = angular.copy($scope.calParams.discountFields);
	
	$scope.taskDetail = [loadData.taskDetail];
	$scope.groupDatas = loadData.groupDatas;
	
	if($rootScope.workingOnProduct.productSetting.pocModule == 1) {
		$scope.groupDatas.push({id: -1, name: 'เว็บไซต์ กยศ', isKys: true});
	}
	
	$scope.$parent.$parent.iconBtn = 'fa-long-arrow-left';
//	$scope.isDisableNoticePrintBtn = ($rootScope.group6 && loadData.isDisableNoticePrint) ? true : false;
	$scope.isDisableNoticePrintBtn = loadData.isDisableNoticePrint ? true : false;
	$scope.isDisableNotice = loadData.isDisableNoticePrint;
	$scope.isHideComment = loadData.isHideComment;
	$scope.createdByLog = loadData.createdByLog;
	$scope.rowIndex = $stateParams.rowIndex || 1;
	$scope.discount = {};
	$scope.discount.finalBalance = $scope.taskDetailPerm[$scope.calParams.balanceColumnName];
	$scope.readMore = [];
	
	$scope.userEditable = $rootScope.group4 ? loadData.userEditable : true;
	$scope.isDisableBtnShow = $rootScope.group6 ? loadData.isDisableBtnShow : true;
	
	var othersGroupDatas;
	var relatedData;
	var relatedDetail = new Array();
	var lastGroupActive = $scope.groupDatas[0];
	var taskDetailId = loadData.taskDetail['_id'];
	var relatedMenuId;
	var traceIdDummy, traceId;
	var countView = 0;
	var customerName;
	var isKeepData = false;
	lastGroupActive.btnActive = true;
	$scope.fieldName = $filter('orderBy')(loadData.colFormMap[$scope.groupDatas[0].id], 'detOrder');

	$scope.tabActionMenus = [{id: 1, name: 'บันทึกการติดตาม', url: './views/working/tab_trace.html', btnActive: true},
	                         {id: 7, name: 'ยอดประมาณการ', url: './views/working/tab_forecast.html'},
	                         {id: 2, name: 'ที่อยู่ใหม่', url: './views/working/tab_addr.html'},
	                         {id: 5, name: 'บัญชีพ่วง', url: './views/working/tab_related.html'},
	                         {id: 6, name: 'Payment', url: './views/working/tab_payment.html'}];
	
	if($scope.calParams.balanceColumnName) {
		$scope.tabActionMenus.push({id: 3, name: 'คำนวณ', url: './views/working/tab_cal.html'});
	}
	if(loadData.showUploadDoc) {		
		$scope.tabActionMenus.push({id: 8, name: 'ไฟล์เอกสาร', url: './views/working/tab_doc.html'});
	}
	if(loadData.seizure) {		
		$scope.tabActionMenus.push({id: 9, name: 'การยึด', url: './views/working/tab_seizure.html'});
	}
	
	
	$scope.lastTabActionMenuActive = $scope.tabActionMenus[0];
	
	$scope.askModalObj = {};
	$scope.askModalObj.init = {};
	$scope.askModalObj.trace = {};
	$scope.askModalObj.init.traceData = loadData.traceResp;
	$scope.askModalObj.init.itemsPerPage = 5;
	$scope.askModalObj.init.currentPage = 1;
	$scope.askModalObj.init.maxSize = 5;	
	$scope.askModalObj.init.maxlength = loadData.textLength > 0 ? loadData.textLength : -1;
	$scope.askModalObj.init.currentlength = 0;
	$scope.askModalObj.comment = loadData.comment;
	
	$scope.addrObj = {};
	$scope.addrObj.names = ['ที่อยู่ทร', 'ที่อยู่ที่ทำงาน', 'ที่อยู่ส่งเอกสาร', 'อื่นๆ']; 
	$scope.addrObj.items = loadData.addresses;
	
	$scope.forecastObj = {itemsPerPage: 5, currentPage: 1, maxSize: 5};
	
	if(loadData.payTypes && loadData.payTypes.length > 0) {
		$scope.forecastObj.payTypeList = loadData.payTypes;
	} else {
		$scope.forecastObj.payTypeList = [{name: 'ปิดบัญชี'}, {name: 'ผ่อนปิด', isRound: true}, {name: 'จ่ายขั้นต่ำ'}]; 
	}
	var forecastRound = $filter('filter')($scope.forecastObj.payTypeList, {isRound: true});
	$scope.forecastObj.items = new Array();
	
	$scope.relatedObj = {};
	
	$scope.paymentObj = {};
	$scope.paymentObj.paymentDetails = loadData.paymentDetails;
	$scope.paymentObj.paymentTotalItems = loadData.paymentTotalItems;
	$scope.paymentObj.formData = {currentPage : 1, itemsPerPage: 5};
	$scope.paymentObj.sums = loadData.paymentSums;
	$scope.currentPageActive = $scope.$parent.formData.currentPage;
	
	$scope.document = {itemsPerPage: 5, currentPage: 1, maxSize: 5};
	$scope.seizure = {data: {}};
	
	$scope.dymList = loadData.dymList;
	$("#taskDetailStick").stick_in_parent();
	
	$scope.datePickerOptions = {
		    format: 'dd/mm/yyyy',
		    autoclose: true,
		    todayBtn: true,
		    clearBtn: true,
		    todayHighlight: true,
		    language: 'th-en'
		};
	
	initGroup();
	
	function checkLoadType(isNext) {
		if(lastId != taskDetailId) $scope.loanType = '';
		
		var loanType = '';
		if($scope.loanType) {
			if(isNext) {
				if($scope.loanType == 'F101') {
					loanType = 'F201';
				} else if($scope.loanType == 'F201') {
					loanType = 'F101';
				}
			} else {
				return $scope.loanType;
			}
		}
		return loanType;
	}
	
	var lastId;
	$scope.goToKYS = function(loanType) {
		if($scope.isKYS && ((lastId != taskDetailId) || loanType)){
			lastId = angular.copy(taskDetailId);
			
			$http.get(urlPrefix + '/restAct/paymentOnlineCheck/getHtml?id=' + taskDetailId + '&productId=' + $rootScope.workingOnProduct.id + '&loanType=' + loanType).then(function(data) {
				var result = data.data;
				
				if(result.statusCode != 9999) {
					$rootScope.systemAlert(result.statusCode);
					return;
				}
				
				$("#kys").attr('srcdoc', result.html);
				
				$scope.loanType = result.loanType;
				$scope.kysIsError = result.isError;
			}, function(response) {
				$rootScope.systemAlert(response.status);
			});
		}
	}
	
	
	
	$scope.view = function(data, tab, index) {
		
		if(taskDetailId == data.id) return;
		
		taskDetailId = data.id;
		
		if(data.rowIndex) {
			$scope.rowIndex = data.rowIndex;
		}
		
		$scope.isEditable = $rootScope.group6 ? (data.sys_owner_id[0] == $rootScope.userId) : true;
		$scope.$parent.idActive = data.id;
		$scope.$parent.getCurrentIndex();
		$scope.currentPageActive = $scope.$parent.formData.currentPage;
		
		$http.post(urlPrefix + '/restAct/taskDetail/view', {
    		id: data.id,
    		traceCurrentPage: $scope.askModalObj.init.currentPage, 
    		traceItemsPerPage: $scope.askModalObj.init.itemsPerPage,
    		productId: $stateParams.productId,
    		currentPagePayment: $scope.paymentObj.formData.currentPage,
    		itemsPerPagePayment: $scope.paymentObj.formData.itemsPerPage,
    		isOldTrace: $scope.askModalObj.isOldTrace
    	}).then(function(data){
    		if(index != null && index < countView) {
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
			$scope.forecastObj.items = new Array();
			
			if(lastGroupActive.menu) {
				relatedData = loadData.relatedData[lastGroupActive.menu];
				$scope.taskDetail = relatedData.othersData;
			} else {
				$scope.taskDetail = [loadData.taskDetail];
			}
			
			//---: 
			$scope.taskDetailPerm = loadData.taskDetail;
			$scope.discount.finalBalance = $scope.taskDetailPerm[$scope.calParams.balanceColumnName];
			
			if($scope.lastTabActionMenuActive.id == 5) {
				$scope.relatedObj.search();				
			} else if($scope.lastTabActionMenuActive.id == 7) {
				$scope.forecastObj.currentPage = 1;
				$scope.forecastObj.find();
			} else if($scope.lastTabActionMenuActive.id == 8) {
				$scope.document.currentPage = 1;
				$scope.document.getDoc();
			} else if($scope.lastTabActionMenuActive.id == 9) {
				$scope.seizure.getData();
			}
			
			$scope.paymentObj.paymentDetails = loadData.paymentDetails;
			$scope.paymentObj.paymentTotalItems = loadData.paymentTotalItems;
			$scope.paymentObj.sums = loadData.paymentSums;
			$scope.askModalObj.comment = loadData.comment;
			traceId = null;
			$scope.discount.operators = new Array();
			$scope.discount.reqVal = null;
			$scope.discount.calType = 1;
			$scope.readMore = [];
			
			
			delete $scope.discount.loss;
			$scope.calParams.discountFields = angular.copy(discountFieldsPerm);
			discountFieldsDyn();
    	}, function(response) {
    		$rootScope.systemAlert(response.status);
    	});
	}
	
	$scope.changeTab = function(group) {
		if($scope.groupDatas.length == 1) return;
		
		if(group.isKys) {
			$scope.isKYS = true;
			$scope.goToKYS('');
		} else {
			var fields;
			$scope.isKYS = false;
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
		}
		
		lastGroupActive.btnActive = false;
		lastGroupActive = group;
		group.btnActive = true;
		$scope.readMore = [];
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
		
		if(menu.id == 5 && $scope.relatedTaskDetails == null) { 
			// Related data tab
			$scope.relatedObj.search();
		} else if(menu.id == 7 && $scope.forecastObj.items.length == 0) {
			// Forecast
			$scope.forecastObj.find();
		} else if(menu.id == 8) {
			$scope.document.getDoc();
		} else if(menu.id == 9) {
			$scope.seizure.getData();
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
	
	$scope.captureKYS = function() {
		var loanType = checkLoadType();
		
		$http.get(urlPrefix + '/restAct/paymentOnlineCheck/getHtml2Pdf?productId=' + $rootScope.workingOnProduct.id + '&id=' + taskDetailId + '&loanType=' + loanType, 
				{responseType: 'arraybuffer'}
		).then(function(data) {	
			var a = document.createElement("a");
			document.body.appendChild(a);
			a.style = "display: none";
			
			var fileName = decodeURIComponent(data.headers('fileName'));
			var file = new Blob([data.data]);
	        var url = URL.createObjectURL(file);
	        
	        a.href = url;
	        a.download = fileName;
	        a.click();
	        a.remove();
	        
	        window.URL.revokeObjectURL(url); //-- Clear blob on client
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.disable = function() {
		var isConfirm = confirm('ยืนยันยุติการติดตาม');
	    if(!isConfirm) return;	
	    
		$http.post(urlPrefix + '/restAct/taskDetail/taskDisable', {
			taskIds: [taskDetailId],
			productId: $stateParams.productId
		}).then(function(data) {
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
	
	//------------------------------: Modal dialog :------------------------------------
    var myModal;
	var isDismissModal;
	var address;
	$scope.noticeMenu = function(addr, noticeForms, cusNameParam) {
		address = addr;
		customerName = cusNameParam;
		
		$http.post(urlPrefix + '/restAct/noticeManager/find', {
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
		
			if(!myModal) {
				myModal = $('#myModal').modal();
				myModal.on('shown.bs.modal', function (e) {
					
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
	function initDateEl() {
		$('.dtPicker').each(function() {
			$(this).datetimepicker({
				format: 'DD/MM/YYYY HH:mm',
				showClear: true,
				showTodayButton: true,
				locale: 'th',
//				defaultDate: moment().hours(8).minutes(30).seconds(0).milliseconds(0),
				widgetPositioning : {
					horizontal: 'left',
		            vertical: 'bottom'
				}
			}).on('dp.hide', function(e){
				
			}).on('dp.change', function(e){
				if($(e.target).attr('name') == 'appointDate') {
					if($scope.askModalObj.trace.isNotUseDateRelate) {
						$scope.askModalObj.trace.isNotUseDateRelate = false;
						return;
					}
					
					console.log('appointDate change');
					var nextTimeDate = $("input[name='nextTimeDate']").data("DateTimePicker");
					if(!e.date) {
						$scope.askModalObj.trace.appointDate = null;
						$scope.askModalObj.trace.appointAmount = null;
					} else {						
						nextTimeDate.date(e.date.seconds(0).milliseconds(0));
						$scope.askModalObj.trace.appointDate = nextTimeDate.date().toDate();
					}
				} else if($(e.target).attr('name') == 'nextTimeDate') {
					console.log('nextTimeDate change');
				}
			});
		});
	}
	
	
	
	
	
	var isDismissModalAsk;
	var myModalAsk;
	var traceUpdatedIndex;
	$scope.askModal = function(data, i) {
		traceUpdatedIndex = i;
		
		initDateEl();
		
		//-----: Clear value
		if(!isKeepData) {			
			$scope.askModalObj.trace = angular.copy(data) || {};
			for(i in $scope.dymList) $scope.dymList[i].dymListVal = null;
		}
		
		if(data) {
			$scope.askModalObj.trace.isNotUseDateRelate = true;
			if($scope.askModalObj.trace.appointDate) {
				$("input[name='appointDate']").data("DateTimePicker").date(moment($scope.askModalObj.trace.appointDate).seconds(0).milliseconds(0));				
			} else {
				$scope.askModalObj.trace.isNotUseDateRelate = false;
				$("input[name='appointDate']").data("DateTimePicker").date(null);
			}
			
			if($scope.askModalObj.trace.nextTimeDate) {
				$("input[name='nextTimeDate']").data("DateTimePicker").date(moment($scope.askModalObj.trace.nextTimeDate).seconds(0).milliseconds(0));				
			} else {
				$("input[name='nextTimeDate']").data("DateTimePicker").date(null);
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
				
				if(!listDet) continue;
				list.dymListVal = listDet['_id'];
				
				if(!listDet.groupId) continue;
				
				group = $filter('filter')(list.dymListDetGroup, {_id: listDet.groupId})[0];
				$scope.changeGroup(list, group);
			}
		} else {
			if(!isKeepData) {				
				$("input[name='appointDate']").data("DateTimePicker").date(null);
				$("input[name='nextTimeDate']").data("DateTimePicker").date(null);
			}
		}
		
		isKeepData = false;
		$scope.askModalObj.actionCodeChanged();
		
		//--------------------------------------------
		$scope.disNotice = true;
		
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
		$scope.disNotice = false;
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
	$scope.askModalObj.askModalSave = function(isToForecast) {
		var dymVal = new Array();
		var now = new Date();
		var list;
		
		for(i in $scope.dymList) {
			list = $scope.dymList[i];
			dymVal.push({fieldName: list.fieldName, value: list.dymListVal});
		}
		
		var appointDate = $("input[name='appointDate']").data("DateTimePicker").date();
		var nextTimeDate = $("input[name='nextTimeDate']").data("DateTimePicker").date();
		
		if(appointDate) {
			$scope.askModalObj.trace.appointDate = appointDate.toDate();
			$scope.askModalObj.trace.appointDate.setSeconds(0);
			$scope.askModalObj.trace.appointDate.setMilliseconds(0);
		} else {
			$scope.askModalObj.trace.appointDate = null;
		}
		
		if(nextTimeDate) {
			$scope.askModalObj.trace.nextTimeDate = nextTimeDate.toDate();
			$scope.askModalObj.trace.nextTimeDate.setSeconds(0);
			$scope.askModalObj.trace.nextTimeDate.setMilliseconds(0);
		} else {
			$scope.askModalObj.trace.nextTimeDate = null;
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
						taskUpdated.sys_appointAmount = $scope.askModalObj.trace.appointAmount;
						taskUpdated.sys_nextTimeDate = $scope.askModalObj.trace.nextTimeDate;
					}
				} else {
					taskUpdated.sys_appointDate = $scope.askModalObj.trace.appointDate;
					taskUpdated.sys_appointAmount = $scope.askModalObj.trace.appointAmount;
					taskUpdated.sys_nextTimeDate = $scope.askModalObj.trace.nextTimeDate;					
				}
				taskUpdated.sys_compareDateStatus = result.traceStatus;
			}
			
			if(traceUpdatedIndex == null) {
				taskUpdated.sys_traceDate = result.traceDate;
			}
			
			if(isToForecast) {				
				$timeout(function() {
					angular.element("button[id='7']").triggerHandler('click');
				}, 0);
				
				$scope.forecastObj.addItem({
					appointDate: $scope.askModalObj.trace.appointDate, 
					appointAmount: $scope.askModalObj.trace.appointAmount
				});
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
			productId: $stateParams.productId,
			isOldTrace: $scope.askModalObj.isOldTrace
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
				
				if(!noticeItem || noticeItem.length == 0) continue;
				
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
	$scope.askModalObj.trigerIsOldTrace = function() {
		$scope.askModalObj.searchTrace();
	}
	$scope.$watch('askModalObj.trace.resultText', function() {
		if($scope.askModalObj.trace.resultText) {
			$scope.askModalObj.init.currentlength = $scope.askModalObj.trace.resultText.length;
		} else {
			$scope.askModalObj.init.currentlength = 0;
		}
    });
	
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
//		if($scope.isDisableNoticePrintBtn) {
			$http.post(urlPrefix + '/restAct/noticeManager/saveToPrint', {
				noticeId: id,
				dateInput: dateInput,
				taskDetailId: taskDetailId,
				productId: $stateParams.productId,
				address: address,
				customerName: customerName
			}).then(function(data) {
				var result = data.data;
				
				if(result.statusCode != 9999) {
					$rootScope.systemAlert(data.data.statusCode);
					return;
				}
				
				$rootScope.systemAlert(result.statusCode, 'Save Success');
				$scope.dismissModal();
			}, function(response) {
				$rootScope.systemAlert(response.status);
			});
		/*} else {
			$http.post(urlPrefix + '/restAct/noticeManager/download', {
				id: id,
				dateInput: dateInput,
				taskDetailId: taskDetailId,
				productId: $stateParams.productId,
				address: address,
				isFillTemplate: true,
				customerName: customerName
			}, {responseType: 'arraybuffer'}).then(function(data) {	
				
				var file = new Blob([data.data], {type: 'application/pdf'});
		        var fileURL = URL.createObjectURL(file);
		        window.open(fileURL);
		        window.URL.revokeObjectURL(fileURL);  //-- Clear blob on client
				
		        $scope.dismissModal();
			}, function(response) {
				$rootScope.systemAlert(response.status);
			});
		}*/
	}
	
	//-----------------------------------------: Start Forecast Tab :------------------------------------------------------
	function forecastDateTime() {
		setTimeout(function() {
			$('.forecastAppointDate').each(function() {
				$(this).datetimepicker({
					format: 'DD/MM/YYYY HH:mm',
					showClear: true,
					showTodayButton: true,
					locale: 'th',
					widgetPositioning : {
						horizontal: 'left',
			            vertical: 'top'
					}
				}).on('dp.hide', function(e){					
					
				}).on('dp.show', function(e){
					/*var datetimepicker = $('.bootstrap-datetimepicker-widget:last');
					var top;
					if (datetimepicker.hasClass('top')) {
						top = $(this).offset().top - $(this).outerHeight() - 920;
					}  else if (datetimepicker.hasClass('bottom')) {
            	    	top = $(this).offset().top - $(this).outerHeight() - 598;
					}
	       			datetimepicker.css({
	       				top: top + 'px',
	       				bottom: 'auto'
	       			});*/
				}).on('dp.change', function(e){
					
				});
			});
		}, 100);
	}
	$scope.forecastObj.edit = function(rowform) {
		rowform.$show();
		forecastDateTime();
	}
	$scope.forecastObj.addItem = function(params) {
		forecastDateTime();
		
		$scope.forecastObj.inserted = {payType: $scope.forecastObj.payTypeList[0]};
		
		var item = $scope.forecastObj.items[0];
		if(item) {
			$scope.forecastObj.inserted.payType = item.payType;
			$scope.forecastObj.inserted.round = item.round + 1;
			$scope.forecastObj.inserted.totalRound = item.totalRound;
			$scope.forecastObj.inserted.forecastPercentage = item.forecastPercentage;
		}
		
		if(params) {
			$scope.forecastObj.inserted.appointDate = params.appointDate;
			$scope.forecastObj.inserted.appointAmount = params.appointAmount;
		}
		$scope.forecastObj.items.unshift($scope.forecastObj.inserted);
    };
    $scope.forecastObj.cancelNewItem = function(item) {
    	$scope.forecastObj.inserted = null;
    	for(i in $scope.forecastObj.items) {
    		if($scope.forecastObj.items[i] == item) {
    			$scope.forecastObj.items.splice(i, 1);
    		}
    	}
    }
    $scope.forecastObj.removeItem = function(id) {
		var deleteUser = confirm('ยืนยันการลบข้อมูล');
	    if(!deleteUser) return;
	    
	    $http.post(urlPrefix + '/restAct/forecast/remove', {
	    	id: id,
			contractNo: $scope.askModalObj.init.traceData.contractNo,
			currentPage: $scope.forecastObj.currentPage, 
			itemsPerPage: $scope.forecastObj.itemsPerPage,
			productId: $stateParams.productId
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.forecastObj.items = result.forecastList || new Array();
			$scope.forecastObj.totalItems = result.totalItems;
			$scope.forecastObj.inserted = null;
			mapPayType();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	};
	
	$scope.forecastObj.saveItem = function(data, item, index) {
		
		data.appointDate = $('#appointDate_index_'+index).data("DateTimePicker").date();
		$scope.appointDateErr = (data.appointDate == null) ? true : false;
		$scope.appointAmountErr = (data.appointAmount == null) ? true : false;
		
		if($scope.appointDateErr || $scope.appointAmountErr) {
			return "Cann't be empty";
		}
		
		if(!data.payType.isRound) {
			data.round = null;
			data.totalRound = null;
		}
		
		$http.post(urlPrefix + '/restAct/forecast/save', {
			id: item._id,
			payTypeName: data.payType.name,
			round: data.round,
			totalRound: data.totalRound,
			appointDate: data.appointDate,
			appointAmount: data.appointAmount,
			forecastPercentage: data.forecastPercentage,
			paidAmount: data.paidAmount,
			comment: data.comment,
			contractNo: $scope.askModalObj.init.traceData.contractNo,
			currentPage: $scope.forecastObj.currentPage, 
			itemsPerPage: $scope.forecastObj.itemsPerPage,
			productId: $stateParams.productId
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			if(!item._id) {
				$scope.forecastObj.inserted = null;
				$scope.forecastObj.totalItems = result.totalItems;
			}
			$scope.forecastObj.items = result.forecastList;
			mapPayType();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.forecastObj.find = function() {
		$http.post(urlPrefix + '/restAct/forecast/find', {
			contractNo: $scope.askModalObj.init.traceData.contractNo,
			currentPage: $scope.forecastObj.currentPage, 
			itemsPerPage: $scope.forecastObj.itemsPerPage,
			productId: $stateParams.productId
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.forecastObj.items = result.forecastList || new Array();
			mapPayType();
			
			$scope.forecastObj.totalItems = result.totalItems;
			$scope.forecastObj.inserted = null;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.forecastObj.changeItemPerPage = function() {
		$scope.forecastObj.currentPage = 1;
		$scope.forecastObj.find();
	}
	
	function mapPayType() {
		var item, isRound;
		for(var x in $scope.forecastObj.items) {
			item = $scope.forecastObj.items[x];
			isRound = false;
			
			if(forecastRound.length > 0) {
				for(var y in forecastRound) {
					if(forecastRound[y].name == item.payTypeName) {
						isRound = true;
					}
				}
			}
			item.payType = {name: item.payTypeName, isRound: isRound};
		}
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
			isActive: true,
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
		
		/*if(!val) {		
			$rootScope.systemAlert(1000, 'Can not update');
			return;
		} */
		
		var params = {
					id: taskDetailId,
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
		if($scope.currentPageActive != $scope.$parent.formData.currentPage) {
			$scope.$parent.formData.currentPage = $scope.currentPageActive; 
			$scope.$parent.pageChanged(function(){$scope.nextPrev(isNext, true)});
			return;
		}
		
		if(isChangeIndex) {			
			isNext ? $scope.$parent.currentIndex++ : $scope.$parent.currentIndex--;
		}
		
		var nextTask = $scope.$parent.taskDetails[$scope.$parent.currentIndex];
		var isFound = false;
		
		if(nextTask) {
			$scope.rowIndex = ($scope.$parent.currentIndex + 1 + (($scope.$parent.formData.currentPage - 1) * $scope.$parent.formData.itemsPerPage));
			countView++;
			$scope.view(nextTask, null, countView);
			isFound = true;
			countIsCount = 0;
		}
		
		if(!isFound && countIsCount == 0) {
			isNext ? $scope.$parent.formData.currentPage++ : $scope.$parent.formData.currentPage--;
			$scope.currentPageActive = $scope.$parent.formData.currentPage;
			if(isNext) {
				$scope.$parent.currentIndex = 0;				
			} else {
				$scope.$parent.currentIndex = $scope.formData.itemsPerPage - 1;
			}
			$scope.$parent.pageChanged(function(){$scope.nextPrev(isNext, false)});
			countIsCount = 1;
		}
	}
	
	$scope.firstTask = function () {
		if($scope.$parent.taskDetails.length == 0) {
			$rootScope.systemAlert('warn', 'ไม่พบข้อมูล');
			$scope.taskDetail = [[]];
			$scope.askModalObj.init.traceData = [];
			$scope.paymentObj.paymentDetails = null;
			$scope.paymentObj.paymentTotalItems = null;
			$scope.paymentObj.sums = null;
			$scope.askModalObj.comment = null;
			taskDetailId = null;
		} else {			
			var task = $scope.$parent.taskDetails[0];
			task.rowIndex = 1;
			task && $scope.view(task);
		}
	}
	
	$scope.changeGroup = function(list, gp) {
		if(gp) {			
			list.groupSelected = gp;
		} else {
			gp = list.groupSelected;
		}
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
	
	//-------------------------------------------: Discount :--------------------------------------------------
	$scope.discount.plusIcon = urlPrefix + '/app/images/plus.png';
	$scope.discount.minusIcon = urlPrefix + '/app/images/minus.png';
	$scope.discount.multiplyIcon = urlPrefix + '/app/images/multiply.png';
	$scope.discount.divideIcon = urlPrefix + '/app/images/divide.png';
	$scope.discount.balanceHeader = $filter('filter')($scope.fieldName, {columnName: $scope.calParams.balanceColumnName}, true)[0];
	$scope.discount.operators = new Array();
	$scope.discount.result = 0;
	$scope.discount.calType = 1;
	
	$scope.discount.addOperator = function(op, sign) {
		$scope.discount.operators.push({operator: op, sign: sign});
	}
	$scope.discount.deleteOperator = function(index) {
		$scope.discount.operators.splice(index, 1);
		$scope.discount.observeOperator();
	}
	$scope.discount.observeOperator = function() {
		var result = $scope.taskDetailPerm[$scope.calParams.balanceColumnName];
		var op;
		for(var x in $scope.discount.operators) {
			op = $scope.discount.operators[x];
			
			if(op.val > 0) {				
				result = eval(result + ' ' + op.operator + ' ' + op.val);
			}
		}
		
		$scope.discount.finalBalance = result;
		$scope.discount.cal();
	}
	$scope.discount.cal = function() {
		if($scope.discount.calType == 1) {
			$scope.discount.cusDiscount = Math.abs($scope.discount.reqVal * 100 / $scope.discount.finalBalance - 100);
			$scope.discount.loss = $scope.discount.finalBalance - $scope.discount.reqVal;
		} else {
			$scope.discount.loss = $scope.discount.reqVal / 100 * $scope.discount.finalBalance;
			$scope.discount.cusDiscount = $scope.discount.finalBalance - $scope.discount.loss;
		}
	}
	function discountFieldsDyn() {
		var discountFields; 
		for(var x in $scope.calParams.discountFields) {
			discountFields = $scope.calParams.discountFields[x];
			var disCountFields = discountFields.fieldValue.split(/\{([^}]+)\}/);
			
			var result = "";
			var dummy;
			var branceketIndex;
			for(var y in disCountFields) {
				dummy = disCountFields[y].trim();
				if(!dummy) continue;
				
				if((branceketIndex = dummy.indexOf('(')) == -1) {
					branceketIndex = dummy.indexOf(')');
				} 
				
				dummy = dummy.replace('(', '').replace(')', '');
				
				if((dummy.length == 1) && (dummy.indexOf('+') > -1 || dummy.indexOf('-') > -1 || dummy.indexOf('*') > -1 || dummy.indexOf('/') > -1)) {
					result += dummy;
				} else {
					if(dummy.startsWith("$")) {
						dummy = dummy.replace('$','');
						var liveFields = new Array();
						
						if(dummy == 'keyPay') {
							liveFields.push({fieldResult: $scope.discount.finalBalance - $scope.discount.loss});
						} else {							
							liveFields = $filter('filter')($scope.calParams.discountFields, {fieldName: dummy});
						}
						
						if(liveFields.length > 0) {
							result += reBraceket(branceketIndex, liveFields[0].fieldResult);
						} else {
							result += reBraceket(branceketIndex, dummy);
						}
					} else {
						result += reBraceket(branceketIndex, $scope.taskDetailPerm[dummy]);
					}
				}
			}
			discountFields.fieldResult = eval(result);
		}
	}
	$scope.$watch('discount.loss', function() {
		discountFieldsDyn();
    });
	function reBraceket(index, result) {
		if(index > -1) {
			if(index == 0) {
				result = '(' + result;
			} else {
				result = result + ')';						
			}
		}
		return result;
	}
	//-------------------------------------------: Discount :--------------------------------------------------
	
	
	
	
	$scope.showMore = function(f, detail) {
		$scope.readMore.fieldName = f.columnNameAlias || f.columnName;
		
		if(f.dataType == 'sys_owner') {
			$scope.readMore.val = detail[f.columnName][0].showname;
		} else if(f.dataType == 'date') {
			if(detail[f.columnName]) {
				$scope.readMore.val = new Date(detail[f.columnName]);
			} else {
				$scope.readMore.val = null;				
			}
		} else if(f.dataType == 'num') {
			$scope.readMore.val = detail[f.columnName];
		} else {
			$scope.readMore.val = detail[f.columnName];
		}
		
		$scope.readMore.isEditable = !(f.dataType == 'sys_owner');
		$scope.readMore.f = f;
		$scope.readMore.detail = detail;
		$scope.readMore.isEditMode = false;
	}
	$scope.showMoreUpdate = function() {
		$scope.readMore.isEditMode = false;
		
		if($scope.readMore.f.dataType == 'date' && $scope.readMore.val) {
			$scope.readMore.val = new Date($scope.readMore.val);
		}
		
		$scope.updateData(
				$scope.readMore.f.columnName, 
				$scope.readMore.f.columnNameAlias, 
				$scope.readMore.val,
				$scope.readMore.f.dataType,
				$scope.readMore.detail
				);
		
		$scope.readMore.detail[$scope.readMore.f.columnName] = $scope.readMore.val;
	}
	
	//-------------------------------------------: Seizure :--------------------------------------------------
	$scope.seizure.updateData = function(key, value) {
		$http.post(urlPrefix + '/restAct/document/updateSeizure', {
			key: key,
			value: value,
			contractNo: $scope.askModalObj.init.traceData.contractNo,
			prodId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	$scope.seizure.getData = function() {
		$http.get(urlPrefix + '/restAct/document/getSeizure?prodId='+$rootScope.workingOnProduct.id+'&contractNo='+$scope.askModalObj.init.traceData.contractNo).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			$scope.seizure.data = result.seizures && result.seizures[0];
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	//-------------------------------------------: Upload Document :--------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------
	$scope.document.download = function(id) {
		$http.get(urlPrefix + '/restAct/document/downloadDoc?id=' + id + '&productId=' + $rootScope.workingOnProduct.id, {responseType: 'arraybuffer'}).then(function(data) {	
			var a = document.createElement("a");
			document.body.appendChild(a);
			a.style = "display: none";
			
			var fileName = decodeURIComponent(data.headers('fileName'));
			var file = new Blob([data.data]);
	        var url = URL.createObjectURL(file);
	        
	        a.href = url;
	        a.download = fileName;
	        a.click();
	        a.remove();
	        
	        window.URL.revokeObjectURL(url); //-- Clear blob on client
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	$scope.document.deleteDoc = function($event, id) {		
		var deleteUser = confirm('ยืนยันการลบข้อมูล');
	    if(!deleteUser) return;	
	    
	    $http.get(urlPrefix + '/restAct/document/deleteDoc?productId=' + $rootScope.workingOnProduct.id + '&id='+id).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.document.currentPage = 1;
			$scope.document.getDoc();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	$scope.document.getDoc = function() {
		$http.post(urlPrefix + '/restAct/document/findUploadDoc', {
			currentPage: $scope.document.currentPage,
			itemsPerPage: $scope.document.itemsPerPage,
			contractNo: $scope.askModalObj.init.traceData.contractNo,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.document.documents = result.documents;
			$scope.document.totalItems = result.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	$scope.document.pageChanged = function() {
		$scope.document.getDoc();
	}
	$scope.document.changeItemPerPage = function() {
		$scope.document.currentPage = 1;
		$scope.document.getDoc();
	}
	
	$scope.convert = function(item) {
		if($scope.document.comment) {
			item.formData[0].comment = $scope.document.comment;
		}
		
		item.formData[0].contractNo = $scope.askModalObj.init.traceData.contractNo;
		item.upload();
	}
	
	uploader = $scope.uploader = new FileUploader({
		url: urlPrefix + '/restAct/document/uploadDoc', 
        headers:{'X-Auth-Token': $localStorage.token[$rootScope.username]},
        formData: [{productId: $rootScope.workingOnProduct.id, type: 1}]
    });
	
	 // FILTERS
    uploader.filters.push({
        name: 'customFilter',
        fn: function(item /*{File|FileLikeObject}*/, options) {
            return this.queue.length < 1;
        }
    });
    
    // FILTERS
    uploader.filters.push({
        name: 'customFilter',
        fn: function(item /*{File|FileLikeObject}*/, options) {
        	// File size have to < 15 MB
            return item.size <= 15000000;
        }
    });

    // CALLBACKS
    uploader.onWhenAddingFileFailed = function(item /*{File|FileLikeObject}*/, filter, options) {
        console.info('onWhenAddingFileFailed', item, filter, options);
    };
    uploader.onAfterAddingFile = function(fileItem) {
        console.info('onAfterAddingFile', fileItem);
    };
    uploader.onAfterAddingAll = function(addedFileItems) {
        console.info('onAfterAddingAll', addedFileItems);
    };
    uploader.onBeforeUploadItem = function(item) {
        console.info('onBeforeUploadItem', item);
    };
    uploader.onProgressItem = function(fileItem, progress) {
        console.info('onProgressItem', fileItem, progress);
    };
    uploader.onProgressAll = function(progress) {
        console.info('onProgressAll', progress);
    };
    uploader.onSuccessItem = function(fileItem, response, status, headers) {
        console.info('onSuccessItem', fileItem, response, status, headers);
    };
    uploader.onErrorItem = function(fileItem, response, status, headers) {
        console.info('onErrorItem', fileItem, response, status, headers);
        $rootScope.systemAlert(-1, ' ', fileItem.file.name + ' ไม่สามารถแปลงไฟล์ได้ กรุณาตรวจสอบรูปแบบไฟล์');
    };
    uploader.onCancelItem = function(fileItem, response, status, headers) {
        console.info('onCancelItem', fileItem, response, status, headers);
    };
    uploader.onCompleteItem = function(fileItem, response, status, headers) {
    	console.info('onCompleteItem', fileItem, response, status, headers);
        
    	if(response.statusCode != 9999) return;
    	
    	$scope.document.currentPage = 1;
		$scope.document.getDoc();
		
    	setTimeout(function(){ 
    		$scope.document.comment = '';
    		uploader.clearQueue();
    		$scope.$apply();
    	}, 2000);
    };
    uploader.onCompleteAll = function() {
        console.info('onCompleteAll');
    };
    
	
	//-------------------------------------------------------
	angular.element(document).ready(function () {
		$scope.forecastObj.find();
    });
	
}).directive('setFocus', function(){
	  return{
	      scope: {setFocus: '='},
	      link: function(scope, element){
	         if(scope.setFocus) element[0].focus();             
	      }
	  };
});