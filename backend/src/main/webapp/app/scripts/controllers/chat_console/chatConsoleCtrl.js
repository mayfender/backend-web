angular.module('sbAdminApp').controller('ChatConsoleCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, $state, urlPrefix) {
	$scope.chatLogs = new Array(); 
	
	var lRegisterToken = {
			ns: jws.NS_BASE + ".plugins.debtalert",
			type: 'registerChatConsole'
	};
	$rootScope.lWSC.sendToken(lRegisterToken);
	
	$scope.pringConsole = function(data) {
		if($scope.chatLogs.length == 20) {
			$scope.chatLogs.shift();
		}
		
		$scope.$apply(function () {
			$scope.chatLogs.push({
				author: data.authorName,
				msg: data.msg
			});
		});
	}
	
	//-----------------------------------------------------
	$scope.$on('$destroy', function() {
		var lRegisterToken = {
				ns: jws.NS_BASE + ".plugins.debtalert",
				type: 'unRegisterChatConsole'
		};
		$rootScope.lWSC.sendToken(lRegisterToken);
    });

});