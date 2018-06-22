angular.module('sbAdminApp').controller('NotificationCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, $timeout, FileUploader, urlPrefix, loadData) {
	
	$scope.notificationList = loadData.notificationList
	$scope.totalItems = loadData.totalItems;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.dateConf = {
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
		minTime: '08:00',
		maxTime: '20:00'
	};
	$scope.notificationGroups = [{id: 1, name: 'นัดชำระ', isActive: true, alertNum: 1}, 
	                             {id: 2, name: 'นัด Call', isActive: false}, 
	                             {id: 3, name: 'ทั่วไป', isActive: false, alertNum: 5}];
	
	$scope.isTakeActionMenus = [{id: 1, name: 'ทั้งหมด', isActive: true},
	                            {id: 2, name: 'ยังไม่ได้ดู', isActive: false},
	                            {id: 3, name: 'ดูแล้ว', isActive: false}];
	
	$scope.lastGroupActive = $scope.notificationGroups[0];
	$scope.lastTakeActionMenuActive = $scope.isTakeActionMenus[0];
	
	//---------------------------------------------------------------------------------------------------
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/notification/get', {
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
		
		$scope.lastTakeActionMenuActive = $scope.isTakeActionMenus[0];
		$scope.isTakeAction = null;
		
		$scope.search();
	}
	
	$scope.isTakeActionGet = function(menu) {
		$scope.isTakeAction = menu.id == 1 ? null : menu.id == 2 ? false : true; 
		$scope.lastTakeActionMenuActive = menu;
		$scope.search();
	}
	
});