angular.module('sbAdminApp').controller('KitchenSearchCtrl', function($rootScope, $scope, $state, $http, $stateParams, $translate, $log, toaster, urlPrefix, loadOrder) {
	
	console.log(loadOrder);
	
	$scope.ordersStart = loadOrder.ordersStart;
	$scope.ordersDoing = loadOrder.ordersDoing;
	$scope.ordersFinished = loadOrder.ordersFinished;
	$scope.ordersStartDummy = loadOrder.ordersStart;
	$scope.stateOrdersStart = 0;
	$scope.checkOrderMenuStartIds = [];
	$scope.checkOrderMenuDoingIds = [];
	$scope.checkOrderMenuFinishedIds = [];
	
	var queueHeaderMsg, queueHeaderSameMsg;
	$translate('kichen.panel.header_queue').then(function (msg) {
		queueHeaderMsg = msg;
		$scope.headerQueue = queueHeaderMsg;
	});
	$translate('kichen.panel.header_queue_same').then(function (msg) {
		queueHeaderSameMsg = msg;
	});
	
	
	$scope.checkToDo = function(id) {
		$scope.stateOrdersStart = 1;
		$scope.ordersStartDummy = [];
		$scope.headerQueue = queueHeaderSameMsg;
		
		for(i in $scope.ordersStart) {
			var order = $scope.ordersStart[i];
			
			if(order.menu.id == id) {
				$scope.checkOrderMenuStartIds.push(order.id);
				$scope.ordersStartDummy.push(order);
			}
		}
	}
	
	$scope.reState = function() {
		$scope.headerQueue = queueHeaderMsg;
		$scope.stateOrdersStart = 0;
		$scope.ordersStartDummy = $scope.ordersStart;
		$scope.checkOrderMenuStartIds.splice(0,$scope.checkOrderMenuStartIds.length);
	}
	
	$scope.changeStatusTo = function(ids, status) {
		console.log(ids);
		
		$http.get(urlPrefix + '/restAct/order/changeOrderStatus?ids=' + ids + '&status=' + status).then(function(data) {
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}	    		
    		
    		var data = data.data;
    		
    		$scope.ordersStart = data.ordersStart;
    		$scope.ordersDoing = data.ordersDoing;
    		$scope.ordersFinished = data.ordersFinished;
    		
    		$scope.reState();
    		$scope.checkOrderMenuDoingIds = [];
    		$scope.checkOrderMenuFinishedIds = [];
    		
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	
});