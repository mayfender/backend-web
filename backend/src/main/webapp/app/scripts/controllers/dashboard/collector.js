'use strict';
angular.module('sbAdminApp').controller('Collector', function($rootScope, $scope, $http, $filter, $state, urlPrefix) {
	$scope.collectors = [];
	$scope.column = 'showname';
	$scope.order = 'asc';
	var lastCol = $scope.column;
	
	$scope.traceCount = function() {
		$http.post(urlPrefix + '/restAct/dashBoard/collectorWork', {
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}			
			
			$scope.collectors = result.collectorWork;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.columnOrder = function(col, from) {
		$scope.column = col;
		var sign;
		
		if(lastCol != $scope.column) {
			$scope.order = null;
		}
		
		if($scope.order == 'desc' || $scope.order == null) {			
			$scope.order = 'asc';
			sign = '+';
		} else if($scope.order == 'asc') {
			$scope.order = 'desc';
			sign = '-';
		}
		
		lastCol = $scope.column;
		$scope.collectors = $filter('orderBy')($scope.collectors, sign + $scope.column)
	}
	
	
	//------------------------
	$scope.traceCount();
});
