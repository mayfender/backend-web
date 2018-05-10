angular.module('sbAdminApp').controller('PayTypeCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, urlPrefix, loadData) {
	var dummy = loadData.productSetting.payTypes || new Array();
	$scope.payTypes = new Array();
	
	for(var x in dummy) {
		$scope.payTypes.push({name: dummy[x]});
	}
	
	$scope.addItem = function() {
        $scope.payTypeInserted = {name: '', isTemp: true};
        $scope.payTypes.push($scope.payTypeInserted);
    };
	
	$scope.removeItem = function(index) {
		var deleteUser = confirm('ยืนยันการลบข้อมูล');
	    if(!deleteUser) return;
	    
	    $scope.payTypes.splice(index, 1);
	    $scope.updatePayType();
	};
	
	$scope.cancelNewItem = function(item) {
    	for(i in $scope.payTypes) {
    		if($scope.payTypes[i] == item) {
    			$scope.payTypes.splice(i, 1);
    		}
    	}
    }
	
	$scope.updatePayType = function() {
		$http.post(urlPrefix + '/restAct/product/updatePayType', {
			payTypes: $scope.payTypes,
			id: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
});