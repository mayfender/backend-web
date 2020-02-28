angular.module('sbAdminApp').controller('HomeCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, $state, $timeout, $ngConfirm, urlPrefix) {
	
	if($rootScope.authority == 'ROLE_SUPERVISOR') {
		$scope.position = 'Supervisor';
	} else if ($rootScope.authority == 'ROLE_USER') {
		$scope.position = 'Collector';		
	} else if ($rootScope.authority == 'ROLE_ADMIN') {
		$scope.position = 'Admin';				
	} else if ($rootScope.authority == 'ROLE_MANAGER') {
		$scope.position = 'Manager';				
	}
	
	
	$scope.$watch('$viewContentLoaded', 
    	function() {
	        $timeout(function() {
	        	//-- Set screen to display on mobile.
	        	$('body').css('min-width', '1024px');    	        	
	        }, 0);
		}
	);

});