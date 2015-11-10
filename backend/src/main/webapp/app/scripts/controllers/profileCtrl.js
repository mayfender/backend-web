angular.module('sbAdminApp').controller('ProfileCtrl', function($rootScope, $scope, $base64, $http, urlPrefix) {
	
	$scope.data = {};
	$scope.data.role = $rootScope.principal.authorities[0].authority;
	$scope.data.username = $rootScope.principal.username;
	$scope.data.password = "";
	$scope.data.reTypePassword = "";
	
	$scope.updateProfile = function() {
		var result = confirmPassword();
		
		if(!result) {
			$scope.notMatchRepassErrMsg = "Must match the previous entry";
			return;
		}
		
		console.log("Continue to call backend");
		$http.post(urlPrefix + '/restAct/user/updateProfile', {
			oldUserName: $rootScope.principal.username,
			newUserName: $scope.data.username,
			password: $scope.data.password && $base64.encode($scope.data.password)
		}).then(function(data) {
			if(data.data.statusCode != 9999) {
				if(data.data.statusCode == 2000) {
					$scope.existingUserErrMsg = "Username already exists";
				}else{
					$rootScope.systemAlert(data.data.statusCode);
				}
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Update Profile Success');
			$rootScope.principal.username = $scope.data.username;
			$scope.data.password = "";
			$scope.data.reTypePassword = "";
			$scope.existingUserErrMsg = null;
		}, function(response) {
			$rootScope.systemAlert(data.data.statusCode);
		});
	}
	
	function confirmPassword() {
		return ($scope.data.password == $scope.data.reTypePassword);
	}
	
});