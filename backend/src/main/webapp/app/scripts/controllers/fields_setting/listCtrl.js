angular.module('sbAdminApp').controller('FieldsSettingListCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, $filter, $localStorage, urlPrefix, roles, roles2, roles3, toaster, loadData) {
	
	console.log(loadData);
	
	$scope.$parent.headerTitle = 'แสดง Fields Setting';
	$scope.$parent.iconBtn = 'fa-long-arrow-left';	
	$scope.items = loadData.fieldSettingList;
	$scope.statuses = [{value: true, text: 'Enable'}, {value: false, text: 'Disable'}]; 
	
	
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/dymList/findList', {
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.items = result.dymList;
		}, function(response) {
			$scope.cancelNewItem(item);
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.saveItem = function(data, item, index) {
		console.log(data);
		$http.post(urlPrefix + '/restAct/dymList/saveList', {
			id: item.id,
			name: data.name,
			columnName: data.columnName,
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
	
	$scope.removeItem = function(index, id) {
		var deleteUser = confirm('ยืนยันการลบข้อมูล');
	    if(!deleteUser) return;
	    
	    $http.get(urlPrefix + '/restAct/dymList/deleteList?id='+id+'&productId='+
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
	
	$scope.addItem = function() {
        $scope.inserted = {name: '', enabled: 1};
        $scope.items.push($scope.inserted);
    };
    
    $scope.cancelNewItem = function(item) {
    	for(i in $scope.items) {
    		if($scope.items[i] == item) {
    			$scope.items.splice(i, 1);
    		}
    	}
    }
    
    $scope.gotoSetting1 = function(id) {
    	$state.go('dashboard.fieldsSetting.list.upload', {id: id});
    }
    
});