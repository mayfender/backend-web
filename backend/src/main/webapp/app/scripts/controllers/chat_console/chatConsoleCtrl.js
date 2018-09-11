angular.module('sbAdminApp').controller('ChatConsoleCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, $state, urlPrefix) {
	console.log('Chat console.');
	
	var lRegisterToken = {
			ns: jws.NS_BASE + ".plugins.debtalert",
			type: 'registerChatConsole'
	};
	$rootScope.lWSC.sendToken(lRegisterToken);
	
	
	
	
	
	//-----------------------------------------------------
	$scope.$on('$destroy', function() {
		var lRegisterToken = {
				ns: jws.NS_BASE + ".plugins.debtalert",
				type: 'unRegisterChatConsole'
		};
		$rootScope.lWSC.sendToken(lRegisterToken);
    });

});