angular.module('sbAdminApp').controller('MenuTypeCtrl', function($rootScope, $scope, $state, $http, $stateParams, $translate, $log, toaster, urlPrefix, loadMenuType) {
	
	$log.log(loadMenuType);
	
	$scope.menuTypes = loadMenuType.menuTypes;
	
	
	$scope.addMenuType = function() {
		$scope.inserted = {
			name: ''
	    };
		$scope.menuTypes.push($scope.inserted);
	};
	
	$scope.saveMenuType = function(data, mt) {
		 $log.log(data);
		 $log.log(mt);
		 var msg;
		 
		 if(!mt.id) {
			 msg = 'Save User Success';			 
		 } else {
			 msg = 'Update User Success';
		 }
		 
		 return $http.post(urlPrefix + '/restAct/menuType/saveAndUpdate', {
			id: mt.id,
			name: data.name
		 }).then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, msg);
		 }, function(response) {
			 $rootScope.systemAlert(response.status);
		 });
	};
	
	$scope.cancel = function(index) {
		$scope.removeMenuType(index);
	}
	
	$scope.checkName = function(data) {
		$log.log(data);
	};
	
	
	$scope.removeMenuType = function(index) {
	    $scope.menuTypes.splice(index, 1);
	  };
	
});