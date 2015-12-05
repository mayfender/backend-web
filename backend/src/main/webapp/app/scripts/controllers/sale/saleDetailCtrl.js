angular.module('sbAdminApp').controller('SaleDetailCtrl', function($rootScope, $scope, $state, $http, $stateParams, $translate, $log, toaster, urlPrefix, loadOrders) {
	
	$scope.orders = loadOrders.orders;
	$scope.formData.isDetailMode = true;
	var err_msg;
	$translate('sale.header_panel.detail').then(function (msg) {
		$scope.$parent.headerTitle = msg;
	});
	
	$translate('sale_detail.table').then(function (msg) {
		$scope.$parent.headerTitle += ' ' + msg + ' ' +$stateParams.tableDetail; 
	});
	
	$translate('sale_detail.ref').then(function (msg) {
		$scope.$parent.headerTitle += ' ' + msg + ' ' +$stateParams.ref;
	});
	$translate('message.err.empty').then(function (mgs) {
		err_msg = mgs;
	});
	
	$scope.deleteOrder = function(id) {
		var isDelete = confirm('Are you sure you want to cancel this Order?');
	    if(!isDelete) return;
		
		$http.get(urlPrefix + '/restAct/order/deleteByOrderId?id=' + id).then(function(data) {
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}	    		
    		
    		$rootScope.systemAlert(data.data.statusCode, 'Cancel Order Success');
    		for(i in $scope.orders) {
    			var order = $scope.orders[i];
    			if(order.id == id) {
    				order.isCancel = true;
    				break;
    			}
    		}
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	$scope.updateAmount = function(amount, id, e) {
		if (amount == null || amount == '') {
			return err_msg;
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