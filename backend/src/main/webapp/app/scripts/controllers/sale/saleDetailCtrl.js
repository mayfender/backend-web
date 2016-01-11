angular.module('sbAdminApp').controller('SaleDetailCtrl', function($rootScope, $scope, $state, $http, $stateParams, $translate, $log, toaster, urlPrefix, loadOrders) {
	
	$scope.orders = loadOrders.orders;
	$scope.formData.isDetailMode = true;
	$scope.cusStatus = $stateParams.status;
	
	if($scope.cusStatus == 0) {
		$scope.receiveAmount = $stateParams.receiveAmount;
		$scope.change = $stateParams.changeCash;
		$scope.totalPrice = $stateParams.totalPrice;
	} else if($scope.cusStatus == 1){
		$scope.totalPrice = loadOrders.totalPrice;
	}
	
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
		var isDelete = confirm(confirmCancelMsg + ' ' + name);
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
    				var subPrice = 0;
    				
    				for(j in order.subMenus) {
    					var subMenu = order.subMenus[j];
    					if(subMenu.isCancel) continue;
    					subPrice += (subMenu.price * (subMenu.amount || 1));
    				}
    				
    				$scope.totalPrice = $scope.totalPrice - (order.menu.price * order.amount) - subPrice;
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
    				var subPrice = 0;
    				
    				for(j in order.subMenus) {
    					var subMenu = order.subMenus[j];
    					if(subMenu.isCancel) continue;
    					subPrice += (subMenu.price * (subMenu.amount || 1));
    				}
    				
    				$scope.totalPrice = $scope.totalPrice + (order.menu.price * order.amount) + subPrice;
    				break;
    			}
    		}
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	$scope.cancelSubOrder = function(subOrder, orderId) {
		var isDelete = confirm(confirmCancelMsg + ' ' + subOrder.name);
	    if(!isDelete) return;
		
		$http.get(urlPrefix + '/restAct/order/cancelSubOrder?id=' + subOrder.id +"&orderId=" + orderId).then(function(data) {
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}	    		
    		
    		subOrder.isCancel = true;
    		$scope.totalPrice = $scope.totalPrice - (subOrder.price * (subOrder.amount || 1));    		
    		$rootScope.systemAlert(data.data.statusCode, 'Cancel Sub Order Success');
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	$scope.uncancelSubOrder = function(subOrder, orderId) {
		var isDelete = confirm(unconfirmCancelMsg + ' ' + subOrder.name);
	    if(!isDelete) return;
		
		$http.get(urlPrefix + '/restAct/order/uncancelSubOrder?id=' + subOrder.id +"&orderId=" + orderId).then(function(data) {
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}	    		
    		
    		subOrder.isCancel = false;
    		$scope.totalPrice = $scope.totalPrice + (subOrder.price * (subOrder.amount || 1));    		
    		$rootScope.systemAlert(data.data.statusCode, 'Uncancel Sub Order Success');
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	$scope.updateAmount = function(newAmount, id, price, oldAmount, e) {
		if (newAmount == null || newAmount == '') {
			return errMsg;
		}
		
		$http.post(urlPrefix + '/restAct/order/updateOrder', {
			id: id,
			amount: newAmount
		}).then(function(data) {
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}	    		
    		
    		$scope.totalPrice = $scope.totalPrice - (price * oldAmount) + (newAmount * price);
    		
    		$rootScope.systemAlert(data.data.statusCode, 'Update Amount Success');
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	$scope.updateSubAmount = function(newAmount, parentId, id, price, oldAmount, e) {
		if (newAmount == null || newAmount == '') {
			return errMsg;
		}
		
		$http.post(urlPrefix + '/restAct/order/updateSubAmount', {
			parentId: parentId,
			id: id,
			amount: newAmount
		}).then(function(data) {
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}	    		
    		
    		$scope.totalPrice = $scope.totalPrice - (price * oldAmount) + (newAmount * price);
    		
    		$rootScope.systemAlert(data.data.statusCode, 'Update Amount Success');
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	
	/*-----------------------------------------------------------------*/
	var receiveAmount = $('#receiveAmount').focus();
	receiveAmount.focus();
	
	receiveAmount.keydown(function (event) {
	    var keypressed = event.keyCode || event.which;
	    
	    if (keypressed == 13 && $scope.receiveAmount != null && ($scope.receiveAmount >= $scope.totalPrice)) {
	    	$scope.changePopup = $scope.receiveAmount - $scope.totalPrice;
	    	$scope.$apply();
	    	
	    	$http.post(urlPrefix + '/restAct/customer/checkBill', {
	    		id: $stateParams.cusId,
	    		receiveAmount: $scope.receiveAmount,
	    		totalPrice: $scope.totalPrice,
	    		change: $scope.changePopup
			}).then(function(data) {
	    		if(data.data.statusCode != 9999) {
	    			$rootScope.systemAlert(data.data.statusCode);
	    			return;
	    		}	
	    		modalManage();
		    }, function(response) {
		    	$rootScope.systemAlert(response.status);
		    	return;
		    });
	    }
	});	
	
	function modalManage() {
		var myModal = $('#myModal').modal();
    	myModal.keypress(function(event) {
    		var keypressed = event.keyCode || event.which;
    		if (keypressed == 13) {
    			var myModal = $('#myModal');
    			myModal.modal('hide');
    			
    			myModal.on('hidden.bs.modal', function (e) {
    				$scope.gotoSelected();
    			});
    		}
    	});
	}
	
});