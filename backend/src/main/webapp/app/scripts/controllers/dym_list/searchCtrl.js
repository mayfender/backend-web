angular.module('sbAdminApp').controller('SearchCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, urlPrefix, loadData) {
	$scope.items = loadData.dymSearch;
	$scope.currentField = $scope.items && $scope.items[0];
	
	$scope.addItem = function() {
        $scope.inserted = {name: '', enabled: 1};
        $scope.items.push($scope.inserted);
    };
    $scope.addValue = function() {
    	$scope.valInserted = {name: '', enabled: 1};
    	if(!$scope.currentField.values) {
    		$scope.currentField.values = new Array();
    	}
    	$scope.currentField.values.push($scope.valInserted);
    };
    $scope.cancelNewItem = function(item) {
    	for(i in $scope.items) {
    		if($scope.items[i] == item) {
    			$scope.items.splice(i, 1);
    		}
    	}
    }
    $scope.cancelNewValue = function(value) {
    	for(i in $scope.currentField.values) {
    		if($scope.currentField.values[i] == value) {
    			$scope.currentField.values.splice(i, 1);
    		}
    	}
    }
    
    $scope.saveItem = function(data, item, index) {
		console.log(data);
		$http.post(urlPrefix + '/restAct/dymSearch/saveField', {
			id: item.id,
			name: data.name,
			fieldName: data.fieldName,
			order: data.order,
			enabled: JSON.parse(data.enabled),
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$scope.cancelNewItem(item);
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			if(!item.id) {
				item.id = result.id;
				$scope.currentField = item;
			}
		}, function(response) {
			$scope.cancelNewItem(item);
			$rootScope.systemAlert(response.status);
		});
	}
    
    $scope.saveValue = function(data, item, index) {
		$http.post(urlPrefix + '/restAct/dymSearch/saveValue', {
			id: item.id,
			name: data.name,
			value: data.value,
			fieldId: $scope.currentField.id,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$scope.cancelNewValue(item);
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			if(!item.id) {
				item.id = result.id;
			}
		}, function(response) {
			$scope.cancelNewItem(item);
			$rootScope.systemAlert(response.status);
		});
	}
    
    $scope.removeItem = function(index, id) {
		var deleteUser = confirm('ยืนยันการลบข้อมูล');
	    if(!deleteUser) return;
	    
	    $http.get(urlPrefix + '/restAct/dymSearch/deleteField?id='+id+'&productId='+
	    		$rootScope.workingOnProduct.id).then(function(data) {
	    			
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.items.splice(index, 1);
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	};
	
	$scope.removeValue = function(index, id) {
		var deleteUser = confirm('ยืนยันการลบข้อมูล');
	    if(!deleteUser) return;
	    
	    $http.get(urlPrefix + '/restAct/dymSearch/deleteValue?fieldId='+$scope.currentField.id+'&id='+id+'&productId='+ $rootScope.workingOnProduct.id).then(function(data) {
	    			
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.currentField.values.splice(index, 1);
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	};
    
	$scope.gotoVal = function(field) {
		$scope.currentField = field;
	}
	
});