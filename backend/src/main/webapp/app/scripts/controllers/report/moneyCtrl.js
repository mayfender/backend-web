angular.module('sbAdminApp').controller('ReportMoneyCtrl', function($rootScope, $scope, $state, $http, $stateParams, $translate, $log, $stomp, $sce, toaster, urlPrefix, loadReport) {
	
	$scope.moneys = loadReport.moneys;
	$scope.format = "MM/yyyy";
	$scope.$parent.title = 'แสดงรายงาน';
	$scope.$parent.isMenu = false;
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/report/money', $scope.searchForm).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.moneys = data.data.moneys;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	//--------------------------------------------------------------------------
	
	$scope.$watch("searchForm.reportDate", function(newValue, oldValue) {
	    if(newValue != oldValue) {
	    	$scope.search();
	    }
	});

	$scope.openMonth = function($event) {
	    $event.preventDefault();
	    $event.stopPropagation();

	    $scope.monthPicker = true;
	}
	
});