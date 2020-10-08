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
    		
    		$scope.search();
    		$rootScope.systemAlert(data.data.statusCode, 'Delete Success');
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/product/findProduct', {
			enabled: $scope.formData.enabled,
			currentPage: $scope.formData.currentPage,
	    	itemsPerPage: $scope.itemsPerPage,
	    	productName: $scope.formData.productName || ($rootScope.workingOnProduct.id && $rootScope.workingOnProduct.productName)
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
    			myModal.on('shown.bs.modal', function (e) {
    				$('.input-daterange .dtPicker').each(function() {
    					$(this).datetimepicker({
    						format: 'HH:mm',
    						useCurrent: false,
    					}).on('dp.hide', function(e){
    						
    					}).on('dp.change', function(e){
    						console.log(e);
    					});
    				});
				});
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
		var normalStartTime = $("input[name='normalStartTime']").data("DateTimePicker").date();
		var normalEndTime = $("input[name='normalEndTime']").data("DateTimePicker").date();
		var satStartTime = $("input[name='satStartTime']").data("DateTimePicker").date();
		var satEndTime = $("input[name='satEndTime']").data("DateTimePicker").date();
		var sunStartTime = $("input[name='sunStartTime']").data("DateTimePicker").date();
		var sunEndTime = $("input[name='sunEndTime']").data("DateTimePicker").date();
		
		$http.post(urlPrefix + '/restAct/product/updateWorkingTime', {
			productId: productId,
			
			normalStartTimeH: normalStartTime && normalStartTime.hours(),
			normalStartTimeM: normalStartTime && normalStartTime.minutes(),
			normalEndTimeH: normalEndTime &&  normalEndTime.hours(),
			normalEndTimeM: normalEndTime && normalEndTime.minutes(),
			
			satStartTimeH: satStartTime && satStartTime.hours(),
			satStartTimeM: satStartTime && satStartTime.minutes(),
			satEndTimeH: satEndTime && satEndTime.hours(),
			satEndTimeM: satEndTime && satEndTime.minutes(),
			
			sunStartTimeH: sunStartTime && sunStartTime.hours(),
			sunStartTimeM: sunStartTime && sunStartTime.minutes(),
			sunEndTimeH: sunEndTime && sunEndTime.hours(),
			sunEndTimeM: sunEndTime && sunEndTime.minutes(),
			
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
	
	$scope.productSetting = function(prod, type) {
		var params = {id: prod.id, updateType: type};
		
		if(type == 1) {
			params.isDisableNoticePrint = prod.productSetting.isDisableNoticePrint;
		} else if(type == 2) {
			params.isHideComment = prod.productSetting.isHideComment;
		} else if(type == 3) {
			params.isHideDashboard = prod.productSetting.isHideDashboard;
		} else if(type == 4) {
			params.isHideAlert = prod.productSetting.isHideAlert;
		} else if(type == 5) {
			params.isDisableBtnShow = prod.productSetting.isDisableBtnShow;
		} else {
			$rootScope.systemAlert('Wrong URL');
		}
		
		$http.post(urlPrefix + '/restAct/product/updateProductSetting', params).then(function(data) {
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
