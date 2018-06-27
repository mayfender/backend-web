angular.module('sbAdminApp').controller('NotificationCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, $timeout, FileUploader, urlPrefix, loadData) {
	
	var groupAlertNum = loadData.groupAlertNum;
	$scope.notificationList = loadData.notificationList
	$scope.totalItems = loadData.totalItems;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.dateConf = {
			startDate: 'd',
	    	format: 'dd/mm/yyyy',
		    autoclose: true,
		    todayBtn: true,
		    clearBtn: false,
		    todayHighlight: true,
		    language: 'th-en'
		}
	$scope.timesCfg = {
		format: 'HH:mm',
		step: '30m'
	};
	$scope.startTimesCfg = {
		minTime: (new Date().getHours() + 1) + ':00',
		maxTime: '20:00'
	};
	
	$scope.notificationGroups = [{id: 1, name: 'นัดชำระ', isActive: true}, 
	                             {id: 2, name: 'นัด Call', isActive: false}, 
	                             {id: 3, name: 'ทั่วไป', isActive: false}];
	
	$scope.isTakeActionMenus = [{id: 1, name: 'ยังไม่ได้ดู', isActive: true},
	                            {id: 2, name: 'ดูแล้ว', isActive: false},
	                            {id: 3, name: 'ทั้งหมด', isActive: false},
	                            {id: 4, name: 'รายการแจ้งเตือนใหม่', isActive: false}];
	
	$scope.lastGroupActive = $scope.notificationGroups[0];
	$scope.lastTakeActionMenuActive = $scope.isTakeActionMenus[0];
	$scope.isTakeAction = false;
	
	//---------------------------------------------------------------------------------------------------
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/notification/getAlert', {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			group: $scope.lastGroupActive.id,
			isTakeAction: $scope.isTakeAction,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.notificationList = result.notificationList;	
			$scope.totalItems = result.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.booking = function() {
		var bookingDateTime = new Date($scope.formData.date);
		bookingDateTime.setHours($scope.formData.time.getHours(), $scope.formData.time.getMinutes());
		
		$http.post(urlPrefix + '/restAct/notification/booking', {
			subject: $scope.formData.subject,
			detail: $scope.formData.detail,
			bookingDateTime: bookingDateTime,
			group: 3,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.formData.subject = null;
			$scope.formData.detail = null;
			$scope.formData.date = null;
			$scope.formData.time = null;
			
			$rootScope.systemAlert(result.statusCode, 'บันทึกสำเร็จ');
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	
	$scope.changeGroup = function(group) {
		group.isActive = true;
		$scope.lastGroupActive.isActive = false;
		$scope.lastGroupActive = group;
		
		$scope.search();
	}
	
	$scope.isTakeActionGet = function(menu) {
		$scope.isTakeAction = menu.id == 1 ? false : menu.id == 2 ? true : null; 
		$scope.lastTakeActionMenuActive = menu;
		$scope.search();
	}
	
	function calAlertNum() {
		var alertNum, notGroup;
		for(var x in groupAlertNum) {
			alertNum = groupAlertNum[x];
			for(var y in $scope.notificationGroups) {		
				notGroup = $scope.notificationGroups[y];
				if(alertNum['_id'] == notGroup.id) {
					notGroup.alertNum = alertNum.alertNum;
					break;
				}
			}
		}
	}
	
	//-----------------------------------
	calAlertNum();
	
});