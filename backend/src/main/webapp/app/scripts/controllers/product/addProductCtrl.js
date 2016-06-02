angular.module('sbAdminApp').controller('AddProductCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, urlPrefix, roles, toaster) {
	
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	$scope.persisBtn = 'บันทึก';
	
	if($stateParams.data) { //-- Initial edit module
		
		$scope.$parent.headerTitle = 'แก้ใขโปรดักส์';		
		$scope.data = $stateParams.data;
		$scope.isEdit = true;
	} else {                // Initial for create module
		
		$scope.$parent.headerTitle = 'เพิ่มโปรดักส์';
		$scope.data = {};
		$scope.data.enabled = 1;
	}
	
	$scope.clear = function() {
		setNull();
	}
	
	$scope.update = function() {
		
		delete $scope.data['createdDateTime'];
		console.log($scope.data);
		
		$http.post(urlPrefix + '/restAct/product/updateProduct', $scope.data).then(function(data) {
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
	
	$scope.save = function() {
		$http.post(urlPrefix + '/restAct/product/saveProduct', $scope.data).then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Save Success');
			$scope.formData.currentPage = 1;
			$scope.formData.enabled = null;
			$scope.formData.productName = null;
			$state.go('dashboard.product.search', {
				'itemsPerPage': $scope.itemsPerPage, 
				'currentPage': 1,
				'enabled': $scope.formData.enabled,
				'productName': $scope.formData.productName
			});
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	function setNull() {
		$scope.user.reTypePassword = null;
		$scope.user.userName = null;
		$scope.user.password = null;
		$scope.autoGen = false;
		$scope.user.roles[0].authority = "";
		$scope.user.enabled = 1;
	} 
	
});