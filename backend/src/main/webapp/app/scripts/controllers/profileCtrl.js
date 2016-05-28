angular.module('sbAdminApp').controller('ProfileCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, urlPrefix) {
	
	$scope.data = {};
	$scope.data.password = "";
	$scope.data.reTypePassword = "";
	$scope.data.usernameShow = $localStorage.showname;
	$scope.data.username = $localStorage.username;
	$scope.data.role = $localStorage.authorities[0].authority;
	
	$scope.updateProfile = function() {
		var result = confirmPassword();
		
		if(!result) {
			$scope.notMatchRepassErrMsg = "Must match the previous entry";
			return;
		}
		
		$http.post(urlPrefix + '/restAct/user/updateProfile', {
			oldUserNameShow: $localStorage.showname,
			oldUserName: $localStorage.username,
			newUserNameShow: $scope.data.usernameShow,
			newUserName: $scope.data.username,
			password: $scope.data.password && $base64.encode($scope.data.password)
		}).then(function(data) {
			if(data.data.statusCode != 9999) {
				if(data.data.statusCode == 2000) {
					$translate('message.err.username_same').then(function (msg) {
						$scope.existingUserErrMsg = msg;
					});
				}else{
					$rootScope.systemAlert(data.data.statusCode);
				}
				
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Update Profile Success');
			$localStorage.showname = $scope.$parent.showname = $scope.data.usernameShow;
			$localStorage.username = $scope.data.username;
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