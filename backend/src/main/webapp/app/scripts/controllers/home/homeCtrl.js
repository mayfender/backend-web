angular.module('sbAdminApp').controller('HomeCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, $state, urlPrefix) {
	var jWebSocketClient;
	
	if($rootScope.authority == 'ROLE_SUPERVISOR') {
		$scope.position = 'Supervisor';
	} else if ($rootScope.authority == 'ROLE_USER') {
		$scope.position = 'Collector';		
	} else if ($rootScope.authority == 'ROLE_ADMIN') {
		$scope.position = 'Admin';				
	} else if ($rootScope.authority == 'ROLE_MANAGER') {
		$scope.position = 'Manager';				
	}
	
	$scope.broardCast = function() {
		  var lRes = jWebSocketClient.broadcastText(
		    "",   				// broadcast to all clients (not limited to a certain pool)
		    'My name is May.'  // broadcast this message
		  );
		  if( lRes.code != 0 ) {
		    console.log('Error to broardcast');
		  }
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//-------------------------------------------------------
	angular.element(document).ready(function () {
		if( jws.browserSupportsWebSockets() ) {
			jWebSocketClient = new jws.jWebSocketJSONClient();
		} else {
		  // Optionally disable GUI controls here
		  var lMsg = jws.MSG_WS_NOT_SUPPORTED;
		  console.log('1');
		  alert( lMsg );
		}
		
		
		var lURL = 'ws://localhost:8787/jWebSocket/jWebSocket';
		var gUsername = 'root';
		var lPassword = 'root';
		
		console.log( "Connecting to " + lURL + " and logging in as '" + gUsername + "'..." );
		var lRes = jWebSocketClient.logon( lURL, gUsername, lPassword, {
		  // OnOpen callback
		  OnOpen: function( aEvent ) {
			  console.log('jWebSocket connection established');
		  },
		  // OnMessage callback
		  OnMessage: function( aEvent, aToken ) {
			  console.log('jWebSocket ['+ aToken.type + '] token received, full message: ##' + aEvent.data);
		  },
		  // OnClose callback
		  OnClose: function( aEvent ) {
			  console.log('jWebSocket connection closed.');
		  }
		});
		
		
		
		
		
		
		
		
		
		
		
		
    });
	
});