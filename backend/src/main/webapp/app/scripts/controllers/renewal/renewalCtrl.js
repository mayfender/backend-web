angular.module('sbAdminApp').controller('RenewalCtrl', function($rootScope, $scope, $state, $base64, $http, $translate, urlPrefix, loadRegistered) {
	
	console.log(loadRegistered);
	
	$scope.datas = loadRegistered.registereds;
	$scope.totalItems = loadRegistered.totalItems;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/registration/findRenewal',
			$scope.formData
		).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.datas = data.data.registereds;
			$scope.totalItems = data.data.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clearSearchForm = function() {
		$scope.formData.firstname = null;
		$scope.formData.isActive = null;
		$scope.search();
	}
	
	
	
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	
});