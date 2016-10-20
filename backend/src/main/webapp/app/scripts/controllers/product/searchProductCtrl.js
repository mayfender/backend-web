angular.module('sbAdminApp').controller('SearchProductCtrl', function($rootScope, $scope, $http, $state, $translate, urlPrefix, loadProducts) {	
	
	$scope.maxSize = 5;
	$scope.$parent.url = 'add';
	$scope.$parent.iconBtn = 'fa-plus-square';
	$scope.data = {};
	$scope.data.products = loadProducts.products;
	$scope.totalItems = loadProducts.totalItems;
	$scope.$parent.headerTitle = 'แสดงโปรดักส์';
	var myModal;
	var productId;
	
	$scope.timesCfg = {
		format: 'HH:mm',
		step: '30m'
	};
	$scope.startTimesCfg = {
		minTime: '07:00',
		maxTime: '12:00'
	};
	$scope.endTimesCfg = {
		minTime: '13:00',
		maxTime: '19:00'
	};
	
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
	$scope.importPaymentSetting = function(id, productName) {
		$state.go('dashboard.product.importPaymentConf', {id: id, productName: productName});
	}
	
	$scope.workingTimeSetting = function(id) {
		productId = id;
		
		$http.get(urlPrefix + '/restAct/product/getWorkingTime?productId=' + productId).then(function(data) {
			var result = data.data;
			
    		if(result.statusCode != 9999) {
    			$rootScope.systemAlert(result.statusCode);
    			return;
    		}	    		
    		
    		if(result.startTimeH != null && result.startTimeM != null) {
    			$scope.startTime = new Date();
    			$scope.startTime.setHours(result.startTimeH, result.startTimeM);
    		} else {
    			$scope.startTime = null;
    		}
    		
    		if(result.endTimeH != null && result.endTimeM != null) {
    			$scope.endTime = new Date();
    			$scope.endTime.setHours(result.endTimeH, result.endTimeM);    			
    		} else {
    			$scope.endTime = null;
    		}
    		
    		if(!myModal) {
    			myModal = $('#myModal').modal();			
    			myModal.on('hide.bs.modal', function (e) {
    				if(!isDismissModal) {
    					return e.preventDefault();
    				}
    				isDismissModal = false;
    			});
    			myModal.on('hidden.bs.modal', function (e) {
    				//
    			});
    		} else {			
    			myModal.modal('show');
    		}
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	$scope.updateWorkingTime = function() {
		$http.post(urlPrefix + '/restAct/product/updateWorkingTime', {
			productId: productId,
			startTimeH: $scope.startTime && $scope.startTime.getHours(),
			startTimeM: $scope.startTime && $scope.startTime.getMinutes(),
			endTimeH: $scope.endTime &&  $scope.endTime.getHours(),
			endTimeM: $scope.endTime && $scope.endTime.getMinutes()
		}).then(function(data) {
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}	    		
    		
    		$rootScope.systemAlert(data.data.statusCode, 'Update Success');
    		$scope.dismissModal();
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	$scope.dismissModal = function() {
		if(!myModal) return;
		
		isDismissModal = true;
		myModal.modal('hide');
	}
	
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	
	
});
