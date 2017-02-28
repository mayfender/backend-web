angular.module('sbAdminApp').controller('DymListListCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, $filter, $localStorage, urlPrefix, roles, roles2, roles3, toaster, loadData) {
	
	$scope.$parent.product = $rootScope.products[0];
	$scope.$parent.headerTitle = 'แสดง dynamic list';
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.isShowProd = true;
	
	$scope.items = loadData.dymList;
	$scope.statuses = [{value: 1, text: 'เปิด'}, {value: 0, text: 'ปิด'}]; 
	
	$scope.saveItem = function(data, item, index) {
		console.log(data);
		$http.post(urlPrefix + '/restAct/dymList/saveList', {
			id: item.id,
			name: data.name,
			enabled: JSON.parse(data.enabled),
			productId: ($scope.product && $scope.product.id) || ($rootScope.setting && $rootScope.setting.currentProduct)
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
	    		($scope.product && $scope.product.id) || ($rootScope.setting && $rootScope.setting.currentProduct)).then(function(data) {
	    			
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
    
    $scope.gotoDet = function(id) {
    	$state.go('dashboard.dymList.list.listDet', {id: id});
    }
	
});