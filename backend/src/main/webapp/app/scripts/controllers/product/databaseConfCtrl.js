angular.module('sbAdminApp').controller('DatabaseConfCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, urlPrefix, toaster) {
	
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	$scope.persisBtn = 'บันทึก';
	$scope.data = $stateParams.data;
	$scope.$parent.headerTitle = 'Database Configuration of [' + $scope.data.productName + ']';		
	
	$scope.update = function() {
		
		delete $scope.data['createdDateTime'];
		console.log($scope.data);
		
		$http.post(urlPrefix + '/restAct/product/updateDatabaseConf', {
				id: $scope.data.id,
				database: $scope.data.database
		}).then(function(data) {
			if(data.data.statusCode != 9999) {				
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Update Success');
			$state.go('dashboard.product.search', {
				'itemsPerPage': $scope.itemsPerPage, 
				'currentPage': $scope.formData.currentPage,
				'enabled': $scope.formData.enabled,
				'productName': $scope.formData.productName
			});
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
});