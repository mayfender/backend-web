angular.module('sbAdminApp').controller('UserLogCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, $state, FileUploader, urlPrefix, loadData) {
	console.log(loadData);
	
	$scope.allUsers = loadData.users;
	$scope.rolesConstant = [
		{authority:'ROLE_ADMIN', name:'Admin'},
		{authority:'ROLE_SUPERVISOR', name:'Supervisor'},
		{authority:'ROLE_USER', name:'User'}
	];
	$scope.formData = {};
	
	$scope.getUsers = function() {
		$scope.users = new Array();
		for(var x in $scope.allUsers) {
			if($scope.allUsers[x].authorities[0].authority != $scope.formData.role) continue;
			$scope.users.push($scope.allUsers[x]);
		}
	}
	
	$scope.getLog = function() {
		if(!$scope.formData.userId) return;
		
		$http.post(urlPrefix + '/restAct/userLog/getLog', {
			userId: $scope.formData.userId,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			$scope.logs = result.logs;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
});