angular.module('sbAdminApp').controller('SearchCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, urlPrefix, loadData) {
	
	$scope.items = loadData.dymSearch;
	$scope.values = new Array();
	var fieldId = $scope.items[0].id;
	
	$scope.addItem = function() {
        $scope.inserted = {name: '', enabled: 1};
        $scope.items.push($scope.inserted);
    };
    $scope.addValue = function() {
    	$scope.valInserted = {name: '', enabled: 1};
    	$scope.values.push($scope.valInserted);
    };
    $scope.cancelNewItem = function(item) {
    	for(i in $scope.items) {
    		if($scope.items[i] == item) {
    			$scope.items.splice(i, 1);
    		}
    	}
    }
    $scope.cancelNewValue = function(value) {
    	for(i in $scope.values) {
    		if($scope.values[i] == value) {
    			$scope.values.splice(i, 1);
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
			fieldId: fieldId, 
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
			$scope.values = new Array();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	};
	
	$scope.removeValue = function(index, id) {
		var deleteUser = confirm('ยืนยันการลบข้อมูล');
	    if(!deleteUser) return;
	    
	    $http.get(urlPrefix + '/restAct/dymSearch/deleteValue?id='+id+'&productId='+ $rootScope.workingOnProduct.id).then(function(data) {
	    			
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.values.splice(index, 1);
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	};
    
	$scope.gotoVal = function(id) {
		fieldId = id;
		$http.get(urlPrefix + '/restAct/dymSearch/getValues?productId='+$rootScope.workingOnProduct.id + '&fieldId='+id).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.values = result.dymSearchValue;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	
	
	
	
	
	
	//-----------: Init
	$scope.gotoVal(fieldId);
	
	
});