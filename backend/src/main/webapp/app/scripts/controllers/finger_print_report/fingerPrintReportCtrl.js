angular.module('sbAdminApp').controller('FingerPrintReportCtrl', function($rootScope, $scope, $state, $base64, $http, $translate, urlPrefix) {
	
	console.log('FingerPrintReportCtrl');
	
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
//	$scope.totalItems
	
	$scope.models = {
			time1: new Date(),
			time2: new Date(),
			time3: new Date(),
			format: 'h:mm a',
			minTime: '9:00 am',
			maxTime: '9:00 pm',
			step: '15'
		};
	
	
	
	 
	 
	 
	 
	 
	 
	 
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