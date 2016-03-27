angular.module('sbAdminApp').controller('FingerPrintReportCtrl', function($rootScope, $scope, $state, $base64, $http, $translate, urlPrefix, loadData) {
	
	console.log(loadData);
	
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.datas = loadData.fingerDet;
	$scope.totalItems = loadData.totalItems;
	
	$scope.times = {
			timeStart: new Date(),
			timeEnd: new Date(),
			format: 'HH:mm',
			minTime: '07:00',
			maxTime: '17:00',
			step: '1h'
	};
	$scope.times.timeStart.setHours(07, 00);
	$scope.times.timeEnd.setHours(07, 00);
	
	
	 
	 
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/fingerDet/search', {
			userNameShow: $scope.formData.userNameShow,
			userName: $scope.formData.userName,
			role: $scope.formData.role,
			status: $scope.formData.status,
			currentPage: $scope.formData.currentPage,
	    	itemsPerPage: $scope.itemsPerPage
		}).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.data.users = data.data.users;
			$scope.totalItems = data.data.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	 
	 
	 
	 
	//------------------------------: Calendar :------------------------------------
	$scope.openStartDate = function($event) {
	    $event.preventDefault();
	    $event.stopPropagation();
	
	    $scope.startDatePicker = true;
	}
	$scope.openEnddate = function($event) {
	    $event.preventDefault();
	    $event.stopPropagation();
	
	    $scope.endDatePicker = true;
	}
	//------------------------------------------------------------------

});