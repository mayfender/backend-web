angular.module('sbAdminApp').controller('SearchProductCtrl', function($rootScope, $scope, $http, $state, $translate, urlPrefix, loadProducts) {	
	
	$scope.maxSize = 5;
	$scope.$parent.url = 'add';
	$scope.$parent.iconBtn = 'fa-plus-square';
	$scope.data = {};
	$scope.data.products = loadProducts.products;
	$scope.totalItems = loadProducts.totalItems;
	$scope.$parent.headerTitle = 'แสดงโปรดักส์';
	
	$scope.deleteUser = function(userId) {
		
		var deleteUser = confirm('Are you sure you want to delete this Item ?');
	    if(!deleteUser) return;
		
		$http.post(urlPrefix + '/restAct/product/deleteProduct', {
			prodId: userId,
	    	enabled: $scope.formData.enabled,
			currentPage: $scope.formData.currentPage,
	    	itemsPerPage: $scope.itemsPerPage,
	    	productName: $scope.formData.productName
		}).then(function(data) {
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}	    		
    		
    		$rootScope.systemAlert(data.data.statusCode, 'Delete Success');
    		$scope.data.products = data.data.products;
    		$scope.totalItems = data.data.totalItems;
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/product/findProduct', {
			enabled: $scope.formData.enabled,
			currentPage: $scope.formData.currentPage,
	    	itemsPerPage: $scope.itemsPerPage,
	    	productName: $scope.formData.productName
		}).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.data.products = data.data.products;
			$scope.totalItems = data.data.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clearSearchForm = function() {
		$scope.formData.enabled = null;
		$scope.formData.productName = null;
		$scope.search();
	}
	
	$scope.editUser = function(data) {
		$state.go('dashboard.product.add', {data: data});
	}
	
	$scope.databaseSetting = function(data) {
		$state.go('dashboard.product.databaseConf', {data: data});
	}
	
	$scope.importTaskSetting = function(id, productName) {
		$state.go('dashboard.product.importConf', {id: id, productName: productName});
	}
	
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	
});
