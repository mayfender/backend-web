angular.module('sbAdminApp').controller('SaleDetailCtrl', function($rootScope, $scope, $state, $http, $stateParams, $translate, $log, toaster, urlPrefix, loadOrders) {
	
	$scope.totalPrice = loadOrders.totalPrice;
	$scope.orders = loadOrders.orders;
	$scope.formData.isDetailMode = true;
	$scope.cusStatus = $stateParams.status;
	
	var errMsg, confirm_cancel_msg, unconfirmCancelMsg;
	$translate('sale.header_panel.detail').then(function (msg) {
		$scope.$parent.headerTitle = msg;
	});
	$translate('sale_detail.table').then(function (msg) {
		$scope.checkBillHeader = ' ' + msg + ' ' +$stateParams.tableDetail; 
	});	
	$translate('sale_detail.ref').then(function (msg) {
		$scope.checkBillHeader += ' ' + msg + ' ' +$stateParams.ref;
	});
	$translate('message.err.empty').then(function (msg) {
		errMsg = msg;
	});
	$translate('message.alert.confirm_cancel').then(function (msg) {
		confirmCancelMsg = msg;
	});
	$translate('message.alert.confirm_uncancel').then(function (msg) {
		unconfirmCancelMsg = msg;
	});
	
	$scope.cancelOrder = function(id, name) {
		var isDelete = confirm(confirmCancelMsg + ' ' +name);
	    if(!isDelete) return;
		
		$http.get(urlPrefix + '/restAct/order/cancelOrder?id=' + id).then(function(data) {
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}	    		
    		
    		$rootScope.systemAlert(data.data.statusCode, 'Cancel Order Success');
    		for(i in $scope.orders) {
    			var order = $scope.orders[i];
    			if(order.id == id) {
    				order.isCancel = true;
    				$scope.totalPrice = $scope.totalPrice - (order.menu.price * order.amount);
    				break;
    			}
    		}
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	$scope.uncancelOrder = function(id, name) {
		var isDelete = confirm(unconfirmCancelMsg + ' ' + name);
	    if(!isDelete) return;
		
		$http.get(urlPrefix + '/restAct/order/uncancelOrder?id=' + id).then(function(data) {
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}	    		
    		
    		$rootScope.systemAlert(data.data.statusCode, 'Uncancel Order Success');
    		for(i in $scope.orders) {
    			var order = $scope.orders[i];
    			if(order.id == id) {
    				order.isCancel = false;
    				$scope.totalPrice = $scope.totalPrice + (order.menu.price * order.amount);
    				break;
    			}
    		}
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	$scope.updateAmount = function(amount, id, e) {
		if (amount == null || amount == '') {
			return errMsg;
		}
		
		$http.post(urlPrefix + '/restAct/order/updateOrder', {
			id: id,
			amount: amount
		}).then(function(data) {
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}	    		
    		
    		$rootScope.systemAlert(data.data.statusCode, 'Update Order Success');
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	
});