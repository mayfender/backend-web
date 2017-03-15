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
		maxTime: '20:00'
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
    		
    		if(result.normalStartTimeH != null && result.normalStartTimeM != null) {
    			$scope.normalStartTime = new Date();
    			$scope.normalStartTime.setHours(result.normalStartTimeH, result.normalStartTimeM);
    		} else {
    			$scope.normalStartTime = null;
    		}
    		
    		if(result.normalEndTimeH != null && result.normalEndTimeM != null) {
    			$scope.normalEndTime = new Date();
    			$scope.normalEndTime.setHours(result.normalEndTimeH, result.normalEndTimeM);    			
    		} else {
    			$scope.normalEndTime = null;
    		}
    		
    		//------
    		
    		if(result.satStartTimeH != null && result.satStartTimeM != null) {
    			$scope.satStartTime = new Date();
    			$scope.satStartTime.setHours(result.satStartTimeH, result.satStartTimeM);
    		} else {
    			$scope.satStartTime = null;
    		}
    		
    		if(result.satEndTimeH != null && result.satEndTimeM != null) {
    			$scope.satEndTime = new Date();
    			$scope.satEndTime.setHours(result.satEndTimeH, result.satEndTimeM);    			
    		} else {
    			$scope.satEndTime = null;
    		}
    		
    		//------
    		
    		if(result.sunStartTimeH != null && result.sunStartTimeM != null) {
    			$scope.sunStartTime = new Date();
    			$scope.sunStartTime.setHours(result.sunStartTimeH, result.sunStartTimeM);
    		} else {
    			$scope.sunStartTime = null;
    		}
    		
    		if(result.sunEndTimeH != null && result.sunEndTimeM != null) {
    			$scope.sunEndTime = new Date();
    			$scope.sunEndTime.setHours(result.sunEndTimeH, result.sunEndTimeM);    			
    		} else {
    			$scope.sunEndTime = null;
    		}
    		
    		$scope.normalWorkingDayEnable = result.normalWorkingDayEnable == null ? false : result.normalWorkingDayEnable;
			$scope.satWorkingDayEnable = result.satWorkingDayEnable == null ? false : result.satWorkingDayEnable; 
			$scope.sunWorkingDayEnable = result.sunWorkingDayEnable == null ? false : result.sunWorkingDayEnable; 
    		
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
			
			normalStartTimeH: $scope.normalStartTime && $scope.normalStartTime.getHours(),
			normalStartTimeM: $scope.normalStartTime && $scope.normalStartTime.getMinutes(),
			normalEndTimeH: $scope.normalEndTime &&  $scope.normalEndTime.getHours(),
			normalEndTimeM: $scope.normalEndTime && $scope.normalEndTime.getMinutes(),
			
			satStartTimeH: $scope.satStartTime && $scope.satStartTime.getHours(),
			satStartTimeM: $scope.satStartTime && $scope.satStartTime.getMinutes(),
			satEndTimeH: $scope.satEndTime &&  $scope.satEndTime.getHours(),
			satEndTimeM: $scope.satEndTime && $scope.satEndTime.getMinutes(),
			
			sunStartTimeH: $scope.sunStartTime && $scope.sunStartTime.getHours(),
			sunStartTimeM: $scope.sunStartTime && $scope.sunStartTime.getMinutes(),
			sunEndTimeH: $scope.sunEndTime &&  $scope.sunEndTime.getHours(),
			sunEndTimeM: $scope.sunEndTime && $scope.sunEndTime.getMinutes(),
			
			normalWorkingDayEnable: $scope.normalWorkingDayEnable,
			satWorkingDayEnable : $scope.satWorkingDayEnable,
			sunWorkingDayEnable: $scope.sunWorkingDayEnable
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
	
	$scope.noticePrintSetting = function(prod) {
		$http.get(urlPrefix + '/restAct/product/noticePrintSetting?productId=' + prod.id + '&isDisableNoticePrint=' + prod.productSetting.isDisableNoticePrint).then(function(data) {
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}	    		
    		
    		$rootScope.systemAlert(data.data.statusCode, 'Update Success');
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
