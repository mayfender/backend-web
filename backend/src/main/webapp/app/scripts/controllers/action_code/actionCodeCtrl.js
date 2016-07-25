angular.module('sbAdminApp').controller('ActionCodeCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, urlPrefix) {
	
	$scope.products = $localStorage.products;
	$scope.product = $scope.products[0];
	
	$scope.items = [{id: 1, code: 'aa', desc: 'bb', meaning: 'ccc', enabled: true}, 
	                {id: 2, code: 'df', desc: 'fgdf', meaning: 'dsfdf', enabled: false}
	               ];
	
	$scope.statuses = [
	                   {value: 1, text: 'เปิด'},
	                   {value: 2, text: 'ปิด'}
	                  ]; 
	
	
	
	$scope.changeProduct = function(prod) {
		if(prod == $scope.product) return;
		
		$scope.product = prod;
	}
	
	//------------------------------: Editable :----------------------------------------
	$scope.addItem = function() {
        $scope.inserted = {code: '', desc: '', meaning: '', enabled: true};
        $scope.items.push($scope.inserted);
    };
    
    $scope.cancelNewItem = function(item) {
    	for(i in $scope.items) {
    		if($scope.items[i] == item) {
    			$scope.items.splice(i, 1);
    		}
    	}
    }

	$scope.removeItem = function(index, id) {
	    $http.post(urlPrefix + '/restAct/importMenu/delete', {
			id: id,
			productId: ($scope.product && $scope.product.id) || ($localStorage.setting && $localStorage.setting.currentProduct)
		}).then(function(data) {
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
	
	$scope.saveItem = function(data, item, index) {
		$http.post(urlPrefix + '/restAct/code/saveCode', {
			id: item.id,
			code: data.code,
			desc: data.desc,
			meaning: data.meaning,
			productId: ($scope.product && $scope.product.id) || ($localStorage.setting && $localStorage.setting.currentProduct)
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$scope.removeItem(index);
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			if(!item.id) {
				item.id = result.id;
			}
		}, function(response) {
			$scope.removeItem(index);
			$rootScope.systemAlert(response.status);
		});
	}
	
	
});