angular.module('sbAdminApp').controller('AddCtrl', function($rootScope, $scope, $http, $state, $stateParams, $translate, $localStorage, loadData, urlPrefix, roles2, roles3) {	
	
	//---: Main CTRL
	$scope.main.headerTitle = 'Add Customer';
	$scope.main.iconBtn = 'fa-long-arrow-left';
	$scope.main.page = 2;	
	//---/ Main CTRL
	
	$scope.statuses = [{value: true, text: 'On'}, {value: false, text: 'Off'}]; 
	
	if(loadData) {
		$scope.main.headerTitle = 'Edit Customer';
		$scope.customer = loadData.customer;
	} else {
		$scope.customer = {};
		$scope.customer.package = 1;
		$scope.customer.enabled = true;
	}
	
	
	
	//----------------------------------------
	$scope.updateCustomer = function(field, value) {
		if(field == 'package') {
			value = $scope.customer.package;
		} else if(field == 'enabled') {
			value = JSON.parse($scope.customer.enabled);			
		}
		
		$http.post(urlPrefix + '/restAct/dms/updateCustomer',{
			id: $scope.customer._id,
			field : field,
			value : value
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			if(result.id) $scope.customer._id = result.id;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});		
	}
	
	$scope.addItem = function() {
        $scope.inserted = {name: '', enabled: true};
        
        if($scope.customer.products == null) {
        	$scope.customer.products = new Array();
        }
        $scope.customer.products.push($scope.inserted);
    };
    $scope.cancelNewItem = function(item) {
    	for(i in $scope.customer.products) {
    		if($scope.customer.products[i] == item) {
    			$scope.customer.products.splice(i, 1);
    		}
    	}
    }
    $scope.saveItem = function(data, item, index) {
		$http.post(urlPrefix + '/restAct/dms/updateProduct', {
			id: $scope.customer._id,
			productId: item.id,
			name: data.name,
			enabled: JSON.parse(data.enabled)
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$scope.cancelNewItem(item);
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			if(!item.id) {
				item.id = result.id;
				item.createdDateTime = new Date();
			}
		}, function(response) {
			$scope.cancelNewItem(item);
			$rootScope.systemAlert(response.status);
		});
	}
    $scope.removeItem = function(index, id) {
		var isConfirmed = confirm('ยืนยันการลบข้อมูล');
	    if(!isConfirmed) return;
	    
	    $http.get(urlPrefix + '/restAct/dms/removeProduct?id=' + $scope.customer._id + '&productId=' + id).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.customer.products.splice(index, 1);
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	};
	
	
	//-------------------------------------------------------
	angular.element(document).ready(function () {
		$(".data-form").typeWatch({
			  wait: 750, // 750ms
			  highlight: true,
			 /* captureLength: 3,*/
			  callback: function(value) {
				  $scope.updateCustomer(this.name, value);
			  }
		});
    });
	
});
