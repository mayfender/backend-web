angular.module('sbAdminApp').controller('HomeCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, $state, urlPrefix) {
	var auth = new String(angular.copy($rootScope.authority));
	console.log(auth);
	console.log($rootScope.authority);
	
	if($rootScope.authority == 'ROLE_SUPERVISOR') {
		$scope.position = 'หัวหน้างาน คอลเลคเตอร์';
	} else if ($rootScope.authority == 'ROLE_USER') {
		$scope.position = 'คอลเลคเตอร์';		
	} else if ($rootScope.authority == 'ROLE_ADMIN') {
		$scope.position = 'แอดมิน';				
	} else if ($rootScope.authority == 'ROLE_MANAGER') {
		$scope.position = 'ผู้จัดการ';				
	}
	
});