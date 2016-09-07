angular.module('sbAdminApp').controller('HomeCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, $state, urlPrefix) {
	
	if($rootScope.authority == 'ROLE_SUPERVISOR') {
		$scope.position = 'Supervisor';
	} else if ($rootScope.authority == 'ROLE_USER') {
		$scope.position = 'Collector';		
	} else if ($rootScope.authority == 'ROLE_ADMIN') {
		$scope.position = 'Admin';				
	} else if ($rootScope.authority == 'ROLE_MANAGER') {
		$scope.position = 'Manager';				
	}
	
});