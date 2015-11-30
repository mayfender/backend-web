angular.module('sbAdminApp').controller('MenuTypeCtrl', function($rootScope, $scope, $state, $http, $stateParams, $translate, $log, toaster, urlPrefix, loadMenuType) {
	
	$scope.menuTypes = loadMenuType.menuTypes;
	
	
	$scope.addMenuType = function() {
		$scope.inserted = {
			id: $scope.menuTypes.length + 1,
			name: ''
	    };
		$scope.menuTypes.push($scope.inserted);
	};
	
	$scope.saveUser = function(data, mt) {
		 //$scope.user not updated yet
		 //angular.extend(data, {id: id});
		 $log.log(data);
		 //return $http.post('/saveUser', data);
	};
	
	$scope.cancel = function(index) {
		$scope.removeMenuType(index);
	}
	
	$scope.checkName = function(data) {
		 
	};
	
	
	$scope.removeMenuType = function(index) {
	    $scope.menuTypes.splice(index, 1);
	  };
	
});