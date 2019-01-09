angular.module('sbAdminApp').controller('ChatConsoleCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, $state, urlPrefix) {
	
	$http.get(urlPrefix + '/restAct/chatting/getChatHis').then(function(data) {	
		var result = data.data;
		
		if(result.statusCode != 9999) {
			$rootScope.systemAlert(result.statusCode);
			return;
		}
		
		$scope.chatData = result.mapData;
	}, function(response) {
		$rootScope.systemAlert(response.status);
	});
	
	
	
	
	
	
	//-----------------------------------------------------
	/*$scope.$on('$destroy', function() {
		var lRegisterToken = {
				ns: jws.NS_BASE + ".plugins.debtalert",
				type: 'unRegisterChatConsole'
		};
		$rootScope.lWSC.sendToken(lRegisterToken);
    });*/

});