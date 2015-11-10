'use strict';
/**
 * @ngdoc function
 * @name sbAdminApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the sbAdminApp
 */
angular.module('sbAdminApp').controller('MainCtrl', function($scope, $position, $stomp) {
	var subscription;
	
	$stomp.setDebug(function (args) {
		//console.log(args);
	});
	
	$scope.loginChat = function() {
		console.log('start');
		$stomp.connect('/ricoh/websocketHandler')
	    .then(function (frame) {
	    	console.log('Websocket connect success');
	    	console.log(frame);
	    }, function(response) {
	    	console.log("Websocket cann't connect");
	    	console.log(response);
	    });
	}
	
	$scope.subscribe = function() {
		console.log('start subscribe');
		subscription = $stomp.subscribe('/topic/may', function (payload, headers, res) {
	        $scope.payload = payload;
	       
	        console.log(payload);
	        console.log(headers);
	        console.log(res);
	            
	        }, {"headers": "are awesome"});
	}
	
	$scope.send = function() {
        $stomp.send('/app/may', {
            message: 'body'
        }, {
            priority: 9,
            custom: 42 //Custom Headers
        }).then(function(data){
        	console.log('sent seccess');
        	console.log(data);
        }, function(response){
        	console.log('sent error');
        	console.log(response);        			
        });
	}
	
	$scope.unsubscribe = function() {
		if(subscription) subscription.unsubscribe();
	}
	
	$scope.disconnect = function() {
        $stomp.disconnect().then(function(data){
        	console.log('disconnection success');
        	console.log(data);
        }, function(response){
        	console.log('disconnection error');
        	console.log(response);        	
        });
	}
	
});
