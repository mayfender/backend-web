angular.module('sbAdminApp').controller('ProfileCtrl', function($rootScope, $scope, $base64, $http, $translate, urlPrefix, loadProfile) {
	
	$scope.data = {};
	$scope.data.role = $rootScope.principal.authorities[0].authority;
	$scope.data.username = $rootScope.principal.username;
	$scope.data.password = "";
	$scope.data.reTypePassword = "";
	
	if(loadProfile) {
		$scope.data.usernameShow = loadProfile.userNameShow;
	}
	
	$scope.updateProfile = function() {
		var result = confirmPassword();
		
		if(!result) {
			$scope.notMatchRepassErrMsg = "Must match the previous entry";
			return;
		}
		
		$http.post(urlPrefix + '/restAct/user/updateProfile', {
			oldUserNameShow: $rootScope.principal.usernameShow,
			oldUserName: $rootScope.principal.username,
			newUserNameShow: $scope.data.usernameShow,
			newUserName: $scope.data.username,
			password: $scope.data.password && $base64.encode($scope.data.password)
		}).then(function(data) {
			if(data.data.statusCode != 9999) {
				if(data.data.statusCode == 2001) {
					$translate('message.err.username_show_same').then(function (msg) {
						$scope.existingUserShowErrMsg = msg;
					});
				}else if(data.data.statusCode == 2000) {
					$translate('message.err.username_same').then(function (msg) {
						$scope.existingUserErrMsg = msg;
					});
				}else{
					$rootScope.systemAlert(data.data.statusCode);
				}
				
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Update Profile Success');
			$rootScope.principal.usernameShow = $scope.data.usernameShow;
			$rootScope.principal.username = $scope.data.username;
			$scope.data.password = "";
			$scope.data.reTypePassword = "";
		}, function(response) {
			$rootScope.systemAlert(data.data.statusCode);
		});
	}
	
	function confirmPassword() {
		return ($scope.data.password == $scope.data.reTypePassword);
	}
	
});