angular.module('sbAdminApp').controller('FingerPrintReportCtrl', function($rootScope, $scope, $state, $base64, $http, $translate, urlPrefix, loadData) {
	
	console.log(loadData);
	
	$scope.maxSize = 5;
	$scope.formData = {
			currentPage : 1, itemsPerPage: 10,
			startTime: new Date(), endTime: new Date()
	};
	$scope.datas = loadData.fingerDet;
	$scope.totalItems = loadData.totalItems;
	
	$scope.times = {
			format: 'HH:mm',
			minTime: '07:00',
			maxTime: '18:00',
			step: '1h'
	};
	$scope.formData.startTime.setHours(07, 00);
	$scope.formData.endTime.setHours(18, 00);
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/fingerDet/search', $scope.formData).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.datas = data.data.fingerDet;
			$scope.totalItems = data.data.totalItems;
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
	$scope.clearSearchForm = function() {
		$scope.formData.name = null;
		$scope.search();
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