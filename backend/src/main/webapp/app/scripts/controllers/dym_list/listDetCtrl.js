angular.module('sbAdminApp').controller('DymListDetCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, urlPrefix) {
	
	console.log($stateParams.id);
	
	$scope.product = $rootScope.products[0];
//	$scope.itemDets = loadData.actionCodes;
	$scope.itemDets = [];
	$scope.statuses = [{value: 1, text: 'เปิด'}, {value: 0, text: 'ปิด'}]; 
	$scope.$parent.$parent.isShowBack = true;
	$scope.$parent.$parent.isShowProd = false;
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/code/findActionCode', {
			productId: ($scope.product && $scope.product.id) || ($rootScope.setting && $rootScope.setting.currentProduct)
		}).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.itemDets = data.data.actionCodes;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.changeProduct = function(prod) {
		if(prod == $scope.product) return;
		
		$scope.product = prod;
		$scope.search();
	}
	
	//------------------------------: Editable :----------------------------------------
	$scope.addItem = function() {
        $scope.inserted = {code: '', desc: '', meaning: '', isPrintNotice: false, enabled: 1};
        $scope.itemDets.push($scope.inserted);
    };
    
    $scope.cancelNewItem = function(item) {
    	for(i in $scope.itemDets) {
    		if($scope.itemDets[i] == item) {
    			$scope.itemDets.splice(i, 1);
    		}
    	}
    }

	$scope.removeItem = function(index, id) {
		var deleteUser = confirm('ยืนยันการลบข้อมูล');
	    if(!deleteUser) return;
	    
	    $http.get(urlPrefix + '/restAct/code/deleteActionCode?id='+id+'&productId='+
	    		($scope.product && $scope.product.id) || ($rootScope.setting && $rootScope.setting.currentProduct)).then(function(data) {
	    			
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.itemDets.splice(index, 1);
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	};
	
	$scope.saveItem = function(data, item, index) {
		console.log(data);
		$http.post(urlPrefix + '/restAct/code/saveActionCode', {
			id: item.id,
			code: data.code,
			desc: data.desc,
			meaning: data.meaning,
			isPrintNotice: data.isPrintNotice == null ? false : data.isPrintNotice,
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
	
	
});