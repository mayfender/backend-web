angular.module('sbAdminApp').controller('ResultCodeCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, urlPrefix, loadData) {
	
	$scope.products = $localStorage.products;
	$scope.product = $scope.products[0];
	$scope.items = loadData.resultCodes;
	$scope.resultCodeGroups = loadData.resultCodeGroups;
	$scope.statuses = [{value: 1, text: 'เปิด'}, {value: 0, text: 'ปิด'}]; 
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/code/findResultCode', {
			productId: ($scope.product && $scope.product.id) || ($localStorage.setting && $localStorage.setting.currentProduct)
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.items = result.resultCodes;
			$scope.resultCodeGroups = result.resultCodeGroups;
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
        $scope.inserted = {code: '', desc: '', meaning: '', enabled: 1};
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
		var deleteUser = confirm('ยืนยันการลบข้อมูล');
	    if(!deleteUser) return;
	    
	    $http.get(urlPrefix + '/restAct/code/deleteResultCode?id='+id+'&productId='+
	    		($scope.product && $scope.product.id) || ($localStorage.setting && $localStorage.setting.currentProduct)).then(function(data) {
	    			
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
		$http.post(urlPrefix + '/restAct/code/saveResultCode', {
			id: item.id,
			code: data.code,
			desc: data.desc,
			meaning: data.meaning,
			enabled: JSON.parse(data.enabled),
			resultGroupId: data.resultGroupId,
			productId: ($scope.product && $scope.product.id) || ($localStorage.setting && $localStorage.setting.currentProduct)
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//------------------------------: Modal dialog :------------------------------------
    var myModal;
	var isDismissModal;
	$scope.resultCodeGroupModal = function() {		
		if(!myModal) {
			myModal = $('#myModal').modal();			
			myModal.on('hide.bs.modal', function (e) {
				if(!isDismissModal) {
					return e.preventDefault();
				}
				isDismissModal = false;
			});
			myModal.on('hidden.bs.modal', function (e) {
				//
			});
		} else {			
			myModal.modal('show');
		}
	}
	
	$scope.dismissModal = function() {
		isDismissModal = true;
		myModal.modal('hide');
	}
	
	//------------------------------: Editable :----------------------------------------
    $scope.addGroup = function() {
        $scope.inserted = {name: ''};
        $scope.resultCodeGroups.push($scope.inserted);
    };
    
    $scope.cancelNewGroup = function(item) {
    	for(i in $scope.resultCodeGroups) {
    		if($scope.resultCodeGroups[i] == item) {
    			$scope.resultCodeGroups.splice(i, 1);
    		}
    	}
    }

	$scope.removeGroup = function(index, id) {
		$http.get(urlPrefix + '/restAct/resultCodeGroup/delete?id='+id+'&productId='+
	    		($scope.product && $scope.product.id) || ($localStorage.setting && $localStorage.setting.currentProduct)).then(function(data) {
	    
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.resultCodeGroups.splice(index, 1);
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	};
	
	$scope.saveGroup = function(data, item, index) {
		$http.post(urlPrefix + '/restAct/resultCodeGroup/save', {
			id: item.id,
			name: data.name,
			productId: ($scope.product && $scope.product.id) || ($localStorage.setting && $localStorage.setting.currentProduct)
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$scope.cancelNewMenu(item);
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			if(!item.id) {
				item.id = result.id;
			}
		}, function(response) {
			$scope.cancelNewMenu(item);
			$rootScope.systemAlert(response.status);
		});
	}
	
	//------------------------------------------------
	
});