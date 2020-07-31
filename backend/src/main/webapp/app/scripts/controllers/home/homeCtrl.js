angular.module('sbAdminApp').controller('HomeCtrl', function($rootScope, $state, $scope, $base64, $http, $timeout, $translate, $q, $localStorage, $ngConfirm, $filter, urlPrefix) {
	console.log('Home');
	
	if($rootScope.authority == 'ROLE_SUPERADMIN') {
		$scope.position = 'Super Admin';		
	} else if ($rootScope.authority == 'ROLE_MANAGER') {
		$scope.position = 'Manager';				
	} else if ($rootScope.authority == 'ROLE_ADMIN') {
		$scope.position = 'Admin';				
	} else if($rootScope.authority == 'ROLE_SUPERVISOR') {
		$scope.position = 'Supervisor';
	} else if ($rootScope.authority == 'ROLE_USER') {
		$scope.position = 'Collector';		
	}
	
});