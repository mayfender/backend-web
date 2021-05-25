angular.module('sbAdminApp').controller('NotificationCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, $timeout, FileUploader, urlPrefix, loadData) {
	
	console.log($stateParams.notificationGroup);
	
	$scope.groupAlertNum = loadData.groupAlertNum;
	$scope.notificationList = loadData.notificationList
	$scope.totalItems = loadData.totalItems;
	$scope.users = loadData.users;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.formData.userId = $rootScope.userId;
	$scope.isAllUser = $rootScope.group1_1 ? false : true;
	
	$scope.notificationGroups = [{id: 1, name: 'นัดชำระ', isActive: false, isHide: false}, 
	                             {id: 2, name: 'นัด Call', isActive: false, isHide: false}, 
	                             {id: 3, name: 'ทั่วไป', isActive: false, isHide: false}];
	
	$scope.isTakeActionMenus = [{id: 1, name: 'ยังไม่ได้ดู', isActive: true},
	                            {id: 2, name: 'ดูแล้ว', isActive: false},
	                            {id: 3, name: 'ยังไม่ได้ดู & ดูแล้ว', isActive: false},
	                            {id: 4, name: 'รายการใหม่', isActive: false}];
	
	if($rootScope.group1_1) {		
		$scope.lastGroupActive = $scope.notificationGroups[2];
		buttonHide([1,2]);
		$scope.allUserNoSelect = $rootScope.showname;
	} else {
		$scope.allUserNoSelect = '--ทั้งหมด--';
		$scope.lastGroupActive = $scope.notificationGroups[0];		
	}
	
	//--: Come from notification popup.
	if($stateParams.notificationGroup) {
		$scope.lastGroupActive = $scope.notificationGroups[$stateParams.notificationGroup - 1];
	}
	
	$scope.lastGroupActive.isActive = true;
	
	$scope.lastTakeActionMenuActive = $scope.isTakeActionMenus[0];
	$scope.actionCode = 1;
	$scope.mode = 1; // 1 = create, 2 = edit;
	
	//---------------------------------------------------------------------------------------------------
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/notification/getAlert', {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			group: $scope.lastGroupActive.id,
			actionCode: $scope.actionCode,
			productId: $rootScope.workingOnProduct.id,
			userId: $scope.formData.userId
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.notificationList = result.notificationList;	
			$scope.totalItems = result.totalItems;
			
			$scope.groupAlertNum = result.groupAlertNum;			
			
			calAlertNum();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.booking = function() {
		$scope.formData.date.setSeconds(0);
		$scope.formData.date.setMilliseconds(0);
		
		$http.post(urlPrefix + '/restAct/notification/booking', {
			isLog: true,
			id: $scope.formData.id,
			subject: $scope.formData.subject,
			detail: $scope.formData.detail,
			bookingDateTime: $scope.formData.date,
			group: 3,
			userId: $rootScope.userId,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			if($scope.mode == 1) {
				if($scope.lastGroupActive.id != 3) {
					$scope.changeGroup($scope.notificationGroups[2], true);
				}
				if($scope.lastTakeActionMenuActive.id != 4) {
					$scope.actionMenu($scope.isTakeActionMenus[3])				
				} else {
					$scope.search();
				}
			} else {
				$scope.search();
			}
			
			$scope.clear();
			$rootScope.systemAlert(result.statusCode, 'บันทึกสำเร็จ');
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.takeAction = function(req) {
		$http.post(urlPrefix + '/restAct/notification/takeAction', {
			isLog: true,
			id: req._id,
			isTakeAction: req.isTakeAction,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			var group = $filter('filter')($scope.notificationGroups, {id: req.group})[0];
			if(req.isTakeAction) {
				group.alertNum--;
				$rootScope.alertNum--;
			} else {
				group.alertNum++;
				$rootScope.alertNum++;				
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.remove = function(id) {
		var isDelete = confirm('ยืนยันการลบข้อมูล');
	    if(!isDelete) return;
	    
		$http.post(urlPrefix + '/restAct/notification/remove', {
			isLog: true,
			id: id,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.search();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.view = function(data) {
		if($scope.lastGroupActive.id == 3) {
			if($scope.lastTakeActionMenuActive.id == 4) {
				$scope.mode = 2;
			} else {
				$scope.mode = 3;
				if(!data.isTakeAction) {
					data.isTakeAction = true;
					$scope.takeAction(data);
				}
			}
			$scope.formData.id = data._id;
			$scope.formData.subject = data.subject;
			$scope.formData.detail = data.detail;
			$scope.formData.date = new Date(data.bookingDateTime);
		} else {
			if($scope.lastTakeActionMenuActive.id != 4) {
				if(!data.isTakeAction) {
					$rootScope.alertNum--;
					data.isTakeAction = true;
					$scope.takeAction(data);
				}
			}
			$state.go('dashboard.working.search.view', {contractNo: data.contractNo, parentId: data._id, productId: $rootScope.workingOnProduct.id, fromPage: 'alert'});
		}
	}
	
	$scope.changeUser = function() {
		if($scope.formData.userId == $rootScope.userId) {
			buttonHide();
		} else {
			if($rootScope.group0) {
				buttonHide();				
			} else {
				buttonHide([3]);				
			}
		}
		
		if($scope.changeGroup($scope.notificationGroups[0]) == 0) {
			$scope.search();
		}
	}
	
	$scope.checkAllUser = function() {
		if($scope.isAllUser) {
			$scope.allUserNoSelect = '--ทั้งหมด--';
			$scope.formData.userId = null;
			
			if($rootScope.group0) {				
				buttonHide();
			} else {
				buttonHide([3]);				
			}
			
			$scope.changeGroup($scope.notificationGroups[0]);
		} else {
			$scope.allUserNoSelect = $rootScope.showname;
			$scope.formData.userId = $rootScope.userId;
			buttonHide([1,2]);
			$scope.formData.owner = null;
			$scope.changeGroup($scope.notificationGroups[2]);
		}
	}
	
	$scope.clear = function() {
		$scope.mode = 1;
		$scope.formData.id = null;
		$scope.formData.subject = null;
		$scope.formData.detail = null;
		
		$scope.formData.date = null;
		$("input[name='date']").data("DateTimePicker").date(null);
	}
	
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	
	$scope.changeGroup = function(group, isIgnoreSearch) {
		if(group.id == $scope.lastGroupActive.id) return 0;
		
		group.isActive = true;
		$scope.lastGroupActive.isActive = false;
		$scope.lastGroupActive = group;
		
		if(!isIgnoreSearch) {
			$scope.search();
		}
	}
	
	$scope.actionMenu = function(menu) {
		if(menu.id == $scope.lastTakeActionMenuActive.id) return;
		
		$scope.actionCode = menu.id; 
		$scope.lastTakeActionMenuActive = menu;
		$scope.search();
	}
	
	function calAlertNum() {	
		var alertNum, notGroup;
		for(var y in $scope.notificationGroups) {		
			notGroup = $scope.notificationGroups[y];
			notGroup.alertNum = 0;
			
			for(var x in $scope.groupAlertNum) {
				alertNum = $scope.groupAlertNum[x];
				
				if(alertNum['_id'] == notGroup.id) {
					notGroup.alertNum = alertNum.alertNum;
					break;
				}
			}
		}
	}
	
	function buttonHide(params) {
		var notGrp;
		for(var x in $scope.notificationGroups) {
			notGrp = $scope.notificationGroups[x];
			notGrp.isHide = false;
			
			for(var y in params) {				
				if(notGrp.id == params[y]) {
					notGrp.isHide = true;
				}
			}
		}
	}
	
	//-----------------------------------
	calAlertNum();
	
	
	
	//-----------------------------------
	$('.input-daterange .dtPicker').each(function() {
		$(this).datetimepicker({
			format: 'DD/MM/YYYY HH:mm',
			showClear: true,
			showTodayButton: true,
			locale: 'th'
		}).on('dp.hide', function(e){
			
		}).on('dp.change', function(e){
			if(e.date) {
				$scope.formData.date = e.date.toDate();
			} else {
				$scope.formData.date = null;				
			}
		});
	});
	
	
	
});