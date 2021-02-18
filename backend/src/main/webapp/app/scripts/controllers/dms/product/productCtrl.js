angular.module('sbAdminApp').controller('ProductCtrl', function($rootScope, $scope, $http, $state, $translate, $localStorage, loadData, urlPrefix, roles2, roles3) {	
	
	//---:
	$scope.groupProducts = loadData.products;
	$scope.formData = {};
	
	//---:
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/dms/getProducts', {
			packageId: $scope.formData.package
		}).then(function(data){
			var result = data.data;
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return $q.reject(data);
    		}
	
    		$scope.groupProducts = result.products;
    	}, function(response) {
    		$rootScope.systemAlert(response.status);
	    });	
	}
	
	
});
