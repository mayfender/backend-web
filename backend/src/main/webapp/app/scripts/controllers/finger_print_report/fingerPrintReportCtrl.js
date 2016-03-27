angular.module('sbAdminApp').controller('FingerPrintReportCtrl', function($rootScope, $scope, $state, $base64, $http, $translate, urlPrefix) {
	
	console.log('FingerPrintReportCtrl');
	
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
//	$scope.totalItems
	
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