angular.module('sbAdminApp').controller('AddCtrl', function($rootScope, $scope, $http, $state, $stateParams, $translate, $localStorage, loadData, urlPrefix, roles2, roles3) {	
	
	//---: Main CTRL
	$scope.main.headerTitle = 'Add Customer';
	$scope.main.iconBtn = 'fa-long-arrow-left';
	$scope.main.page = 2;	
	//---/ Main CTRL
	
	$scope.packages = [{id: 1, name: 'เช่า'}, {id: 2, name: 'ซื้อขาด'}];
	
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
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});		
	}
	
	
	
	
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
