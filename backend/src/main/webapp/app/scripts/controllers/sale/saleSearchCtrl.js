angular.module('sbAdminApp').controller('SaleSearchCtrl', function($rootScope, $scope, $state, $http, $stateParams, $translate, $log, toaster, urlPrefix, loadCus) {
	
	$scope.customers = loadCus.customers;
	$scope.formData.isDetailMode = false;
	
	$translate('sale.header_panel').then(function (msg) {
		$scope.$parent.headerTitle = msg;
	});
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/customer/searchCus', {
			ref: $scope.formData.ref,
			status: $scope.formData.status
		}).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.customers = data.data.customers;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clearSearchForm = function() {
		$scope.formData.ref = null;
		$scope.formData.status = null;
		$scope.search();
	}
	
});