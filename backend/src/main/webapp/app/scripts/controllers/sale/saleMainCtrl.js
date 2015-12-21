angular.module('sbAdminApp').controller('SaleMainCtrl', function($rootScope, $scope, $state, $http, $stateParams, $translate, $log, $stomp, toaster, urlPrefix) {
	
	$scope.customers;
	$scope.formData = {isDetailMode: false};
	
	$scope.gotoSelected = function() {        		
		$state.go("dashboard.sale.search", {
			ref: $scope.formData.ref,
			status: $scope.formData.status
		});
	}
	
	//------------------: Websocket :--------------------
	var order;
	function initWebsocket() {
		$log.log('initWebsocket');
		$stomp.connect(urlPrefix + '/websocketHandler')
	    .then(function (frame) {
	    	$log.log('Websocket connect success');
	    	
	    	subWebsocket();
	    });
	}
	
	function subWebsocket() {
		$log.log('subWebsocket');
		order = $stomp.subscribe('/topic/newCus', function (payload, headers, res) {	       
			if($scope.formData.status != 0) {
				$scope.customers.push(payload);
				$scope.$apply();				
			}
		});
	}
	
	function unsubscribe() {
		if(order) {
			$log.log('unsubscribe order');
			order.unsubscribe();
		}
	}
	
	function disconnWebsocket() {
		$log.log('disconnWebsocket');
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