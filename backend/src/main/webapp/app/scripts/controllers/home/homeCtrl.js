angular.module('sbAdminApp').controller('HomeCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, $state, $ngConfirm, urlPrefix) {
	
	if($rootScope.authority == 'ROLE_SUPERVISOR') {
		$scope.position = 'Supervisor';
	} else if ($rootScope.authority == 'ROLE_USER') {
		$scope.position = 'Collector';		
	} else if ($rootScope.authority == 'ROLE_ADMIN') {
		$scope.position = 'Admin';				
	} else if ($rootScope.authority == 'ROLE_MANAGER') {
		$scope.position = 'Manager';				
	}
	
	
	
	
	
	
	
	
	 $scope.test = function(){
		 $ngConfirm({
			    buttons: {
			        something: function(){
			            // here the key 'something' will be used as the text.
			            $ngConfirm('You clicked on something.');
			        },
			        somethingElse: {
			            text: 'Something else &*', // Some Non-Alphanumeric characters
			            action: function(){
			                $ngConfirm('You clicked on something else');
			            }
			        }
			    }
			})
	
	 }
	
	
	
	
	

});