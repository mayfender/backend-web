angular.module('sbAdminApp').controller('SearchWorkingCtrl', function($rootScope, $stateParams, $scope, $state, $filter, $base64, $http, $localStorage, $translate, $ngConfirm, FileUploader, urlPrefix, loadData) {
	
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.headers = loadData.headers;
	$scope.dymList = loadData.dymList;
	$scope.dymSearch = loadData.dymSearch;
	$scope.headersPayment = loadData.headersPayment;
	$scope.users = loadData.users;
	$scope.taskDetails = loadData.taskDetails;
	$scope.totalItems = loadData.totalItems;
	
	$scope.isSmsEnable = loadData.isSmsEnable;
	if($scope.isSmsEnable) {
		$scope.smsMessages =  loadData.smsMessages;
	}
	
	$scope.maxSize = 5;
	$scope.$parent.headerTitle = 'แสดงข้อมูลงาน';
	$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;
	$scope.dateColumnNames = [
	                          {col: 'sys_traceDate', text:'วันที่ติดตาม'},
	                          {col: 'sys_appointDate', text:'วันนัดชำระ'},
	                          {col: 'sys_nextTimeDate', text:'วันนัด Call'}
	                          ];
	
	if($stateParams.parentId) {		
		$scope.idActive = $stateParams.parentId;
		$scope.isEditable = $rootScope.group6 ? ($scope.taskDetails[0].sys_owner_id[0] == $rootScope.userId) : true;
	}
	
	var today = new Date($rootScope.serverDateTime);
	var lastCol;
	initGroup();
	
	$scope.searchBtn = function(from, dsf) {
		$scope.formData.currentPage = 1;
		if(from == 'detail') {			
			$scope.search(false, function() {
				/*if($scope.taskDetails.length == 0) {
					$rootScope.systemAlert('warn', 'ไม่พบข้อมูล');
				} else {
					$scope.$$childHead.firstTask();					
				}*/
				$scope.$$childHead.firstTask();					
			}, dsf);
		} else {			
			$scope.search(false);
		}
	}
	
	$scope.search = function(isNewLoad, callback, dsf) {
		var datFromObj = $("input[name='dateFrom']").data("DateTimePicker");
		var dateToObj = $("input[name='dateTo']").data("DateTimePicker");
		
		var dateFrom = datFromObj && datFromObj.date();
		var dateTo = dateToObj && dateToObj.date();
		
		if(dateFrom) {
			$scope.formData.dateFrom = dateFrom.toDate();
			$scope.formData.dateFrom.setSeconds(0);
			$scope.formData.dateFrom.setMilliseconds(0);
		} else {
			if($scope.formData.dateFrom) {
				datFromObj.date($scope.formData.dateFrom);
			} else {
				$scope.formData.dateFrom = null;				
			}
		}
		if(dateTo) {
			$scope.formData.dateTo = dateTo.toDate();
			$scope.formData.dateTo.setSeconds(59);
			$scope.formData.dateTo.setMilliseconds(999);
		} else {
			if($scope.formData.dateTo) {
				dateToObj.date($scope.formData.dateTo);
			} else {				
				$scope.formData.dateTo = null;			
			}
		}
		
		$http.post(urlPrefix + '/restAct/taskDetail/find', {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			productId: $rootScope.workingOnProduct.id,
			columnName: $scope.column,
			order: $scope.order,
			isActive: true,
			fromPage: $scope.fromPage,
			keyword: $scope.formData.keyword,
			owner: $scope.formData.owner,
			isPgs: $scope.formData.isPgs,
			isNoTrace: $scope.formData.isNoTrace,
			dateColumnName: $scope.formData.dateColumnName,
			dateFrom: $scope.formData.dateFrom,
			dateTo: $scope.formData.dateTo,
			codeName: $scope.formData.codeName,
			codeValue: $scope.formData.codeValue,
			dymSearchFiedName: $scope.formData.dymSearchFieldName && $scope.formData.dymSearchFieldName.fieldName,
			dymSearchFiedVal: $scope.formData.dymSearchValue,
			dsf: dsf ? dsf.field : null
		}).then(function(data) {
			loadData = data.data;
			
			if(loadData.statusCode != 9999) {
				$rootScope.systemAlert(loadData.statusCode);
				return;
			}
			
			if(loadData.taskDetails) {				
				$scope.taskDetails = loadData.taskDetails;
			} else {
				$scope.taskDetails = null;
			}
			
			$scope.totalItems = loadData.totalItems;
			
			if(isNewLoad) {		
				$scope.headers = loadData.headers;
				$scope.users = loadData.users;
				$scope.headersPayment = loadData.headersPayment; 
				$scope.dymList = loadData.dymList;
			}
			
			callback && callback();
			chkSelected();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.goToItem = function(keyEvent) {
		if (keyEvent.which !== 13 || !$scope.formData.itemToGo) return;
		
		if($scope.formData.itemToGo > $scope.totalItems) {
			$scope.formData.itemToGo = null;
			return;
		}
		
		var page = parseInt(($scope.formData.itemToGo - 1) / $scope.formData.itemsPerPage);
		$scope.formData.currentPage = (page + 1);
		$scope.pageChanged(function(){
			var index = 0;
			var task;
			for(var i in $scope.taskDetails) {
				task = $scope.taskDetails[i]
				index = (++i + (($scope.formData.currentPage - 1) * $scope.formData.itemsPerPage));
				
				if($scope.formData.itemToGo == index) {
					task.rowIndex = index;
					$scope.view(task);
					break;
				}
			}
			$scope.formData.itemToGo = null;
		});
	}
	
	$scope.clearSearchForm = function(isNewLoad, from) {
		$scope.formData.currentPage = 1;
		$scope.formData.keyword = null;
		$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;
		$scope.formData.dateColumnName = null;
		$scope.formData.isPgs = null;
		$scope.formData.isNoTrace = null;
		
		$scope.formData.dateFrom = null;
		$scope.formData.dateTo = null;
		$("input[name='dateFrom']").data("DateTimePicker") && $("input[name='dateFrom']").data("DateTimePicker").date(null);
		$("input[name='dateTo']").data("DateTimePicker") && $("input[name='dateTo']").data("DateTimePicker").date(null);
		
		$scope.formData.codeName = null;
		$scope.formData.codeValue = null;
		$scope.codeNameChange();
		$scope.formData.dymSearchFieldName = null;
		$scope.formData.dymSearchValue = null;
		
		if(from == 'detail') {
			$scope.searchBtn(from);
		} else {			
			$scope.search(isNewLoad);
		}
	}
	
	$scope.columnOrder = function(col, from) {
		$scope.column = col;
		
		if(lastCol != $scope.column) {
			$scope.order = null;
		}
		
		if($scope.order == 'desc' || $scope.order == null) {			
			$scope.order = 'asc';
		} else if($scope.order == 'asc') {
			$scope.order = 'desc';
		}
		
		lastCol = $scope.column;
		$scope.searchBtn(from);
	}
	
	$scope.view = function(data) {
		$scope.lastTaskView = data;
		$scope.idActive = data.id;
		
		$scope.getCurrentIndex();
		
		$scope.isEditable = $rootScope.group6 ? (data.sys_owner_id[0] == $rootScope.userId) : true;
		$state.go('dashboard.working.search.view', {id: data.id, productId: $rootScope.workingOnProduct.id, rowIndex: data.rowIndex});
	}
	
	$scope.getCurrentIndex = function() {
		for(var i in $scope.taskDetails) {
			if($scope.taskDetails[i].id == $scope.idActive) {
				$scope.currentIndex = i;
				break;
			}
		}
	}
	
	$scope.$parent.changeProduct = function(prod) {
		if(prod == $rootScope.workingOnProduct) return;
		
		$scope.column = 'sys_nextTimeDate';
		$scope.order = 'asc';
		$scope.formData.itemsPerPage = 10;
		$rootScope.workingOnProduct = prod;
		$scope.clearSearchForm(true);
	}
	
	//---------------------------------: Check Box :----------------------------------------
	$scope.chk = {}
	$scope.chk.selected = new Set();
	$scope.chk.smsSelect = function(e, data) {
		e.stopPropagation();
		
		if (e.target.checked === undefined) {
			setTimeout(function(){ 
				$(e.currentTarget).children().click();
			}, 10);
		} else {
			if(e.target.checked) {
				$scope.chk.selected.add(data.id);
			} else {
				$scope.chk.selected.delete(data.id);
			}
		}
	}
	$scope.chk.selectAll = function(e) {
		for(var x in $scope.taskDetails) {
			if(e.target.checked) {
				$scope.taskDetails[x].selector = true;
				$scope.chk.selected.add($scope.taskDetails[x].id);
			} else {
				$scope.taskDetails[x].selector = false;
				$scope.chk.selected.delete($scope.taskDetails[x].id);				
			}
		}
	}
	function chkSelected() {
		if($scope.chk.selected.size == 0) {
			return;
		}
		
		$scope.chk.selectorAll = false;
		for(var x in $scope.taskDetails) {
			if($scope.chk.selected.has($scope.taskDetails[x].id)) {
				$scope.taskDetails[x].selector = true;
			}
		}
	}
	
	//---------------------------------: SMS :----------------------------------------
	$scope.sms = {}
	var smsSelected;
	$scope.sms.addSmsList = function(taskDetailId) {
		var buttons = {};
		
		if(taskDetailId) {
			$scope.chk.selected = new Set();
			$scope.chk.selected.add(taskDetailId);
		}
		
		for(var x in $scope.smsMessages) {
			buttons["button_" + x] = {
				text: $scope.smsMessages[x].fieldName,
				btnClass: 'btn-blue',
				action: function(scope, button){
					smsSelected = $filter('filter')($scope.smsMessages, {fieldName: button.text})[0];
					var time = 1;
					scope.message = smsSelected.fieldValue.replace(/\${([^}]+)\}/g, (match, index, originalString) => {
						  return "{P" + (time++) + "}";
					});
					this.buttons.confirm.setDisabled(false);
					return false;
				 }
			};
		}
		buttons.confirm = {
				 text: 'บันทึก',
				 disabled: 'disabled',
				 btnClass: 'btn-orange',
				 action: function(scope){
					$http.post(urlPrefix + '/restAct/sms/save', {
						ids: Array.from($scope.chk.selected),
						messageField: smsSelected.fieldName,
						productId: $rootScope.workingOnProduct.id
					}).then(function(data) {
						var result = data.data;
							
						if(result.statusCode != 9999) {
							$rootScope.systemAlert(result.statusCode);
							return;
						}
						
						$scope.chk.selected = new Set();
						for(var x in $scope.taskDetails) {
							$scope.taskDetails[x].selector = false;
						}
						
						if(taskDetailId) {
							$rootScope.systemAlert(result.statusCode, 'บันทึกข้อมูลสำเร็จ');							
						} else {
							if($rootScope.group2) {							
								$state.go('dashboard.sms', {});
							} else {
								$rootScope.systemAlert(result.statusCode, 'บันทึกข้อมูลสำเร็จ');
							}							
						}
					}, function(response) {
						//
					});
				 }
			};

		$ngConfirm({
			 title: 'เลือกข้อความ SMS',
			 closeIcon: true,
			 content: '<strong>{{ message }}</strong>',
			 buttons: buttons
		 });
	}
	//---------------------------------: Paging :----------------------------------------
	$scope.pageChanged = function(callback) {
		$scope.search(false, callback);
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search(false);
	}
	//---------------------------------: Paging :----------------------------------------
	
	
	$scope.dateColumnNameChanged = function() {
		if(!$scope.formData.dateColumnName) {
			$scope.formData.dateFrom = null;
			$scope.formData.dateTo = null;
			$("input[name='dateFrom']").data("DateTimePicker").date(null);
			$("input[name='dateTo']").data("DateTimePicker").date(null);
		} else {
			initDate();
		}
	}
	
	//---------------------------------: Dynamic List :----------------------------------------
	$scope.codeNameChange = function() {
		$scope.selectedCodeName = $filter('filter')($scope.dymList, {fieldName: $scope.formData.codeName})[0];
		
		if(!$scope.selectedCodeName) {
			$scope.codeGroups = null;
			return;
		}
		
		if($scope.selectedCodeName.dymListDetGroup) {
			$scope.codeGroups = $scope.selectedCodeName.dymListDetGroup;
		} else {
			$scope.codeGroups = null;
		}
	}
	
	$scope.changeGroup = function(gp) {
		$scope.selectedCodeName.groupSelected = gp;
		$scope.selectedCodeName.dymListDet = $filter('filter')($scope.selectedCodeName.dymListDetDummy || $scope.selectedCodeName.dymListDet, {groupId: gp['_id']});
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
	//---------------------------------: Dynamic List :----------------------------------------
	
	$scope.initDateEl = function () {
		$('.input-daterange .dtPicker').each(function() {
			$(this).datetimepicker({
				format: 'DD/MM/YYYY HH:mm',
				showClear: true,
				showTodayButton: true,
				locale: 'th'
			}).on('dp.hide', function(e){
				
			}).on('dp.change', function(e){
				if($(e.target).attr('name') == 'dateFrom') {
					console.log('dateFrom change');
					
					var dateTo = $("input[name='dateTo']").data("DateTimePicker");
					if(!dateTo.date() || !e.date) return;
					
					dateTo.date(e.date.hours(dateTo.date().hours()).minutes(dateTo.date().minutes()));
				} else if($(e.target).attr('name') == 'dateTo') {
					console.log('dateTo change');
					
					var dateTo = e.date;
					if(!dateTo) return;
					
					var dateFrom = $("input[name='dateFrom']").data("DateTimePicker");
					
					if(dateTo.isBefore(dateFrom.date())) {
						dateFrom.date(dateTo.hours(dateFrom.date().hours()).minutes(dateFrom.date().minutes()));
					}
				}
			});
		});
	}
	
	function initDate() {
		$scope.formData.dateFrom = angular.copy(today);
		$scope.formData.dateFrom.setHours(0,0,0,0);
		
		$scope.formData.dateTo = angular.copy(today);
		$scope.formData.dateTo.setHours(23,59,0,0);
		
		$("input[name='dateFrom']").data("DateTimePicker").date($scope.formData.dateFrom);
		$("input[name='dateTo']").data("DateTimePicker").date($scope.formData.dateTo);
	}
	
});