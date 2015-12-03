angular.module('sbAdminApp').controller('TableLandCtrl', function($rootScope, $scope, $state, $http, $stateParams, $translate, $log, toaster, urlPrefix, loadTables) {
	
	$scope.tables = loadTables.tables;
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/table/searchTable', {
			name: $scope.formData.tableName,
			status: $scope.formData.status,
		}).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.tables = data.data.tables;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clearSearchForm = function() {
		$scope.formData.tableName = null;
		$scope.formData.status = null;
		$scope.search();
	}
	
});