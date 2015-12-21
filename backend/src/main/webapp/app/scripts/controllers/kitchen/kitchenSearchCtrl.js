angular.module('sbAdminApp').controller('KitchenSearchCtrl', function($rootScope, $scope, $state, $http, $stateParams, $translate, $log, $stomp, toaster, urlPrefix, loadOrder) {
	
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
	
	
//------------------: Websocket :--------------------
	var obj;
	function initWebsocket() {
		$stomp.connect(urlPrefix + '/websocketHandler')
	    .then(function (frame) {	    	
	    	subWebsocket();
	    });
	}
	
	function subWebsocket() {
		obj = $stomp.subscribe('/topic/order', function (payload, headers, res) {	       
			$scope.ordersStart.push(payload);
			$scope.$apply();
		});
	}
	
	function unsubscribe() {
		if(obj) {
			obj.unsubscribe();
		}
	}
	
	function disconnWebsocket() {
        $stomp.disconnect().then(function(data){
        	$log.log('disconnection success');
        }, function(response){
        	$log.log('disconnection error');
        });
	}
	
	//------------------: Event call back :------------------------
	$scope.$on('$destroy', function () { 
		$log.log('Destroy');
		disconnWebsocket();
	});
	
	
	
	initWebsocket();
});