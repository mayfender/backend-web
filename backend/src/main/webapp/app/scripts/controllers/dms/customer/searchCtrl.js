angular.module('sbAdminApp').controller('SearchCtrl', function($rootScope, $scope, $http, $state, $translate, $localStorage, loadData, urlPrefix, roles2, roles3) {	
	
	//---: Main CTRL
	$scope.main.headerTitle = 'Customer List';
	$scope.main.iconBtn = 'fa-plus-square';
	$scope.main.page = 1;
	//---/ Main CTRL
	
	$scope.maxSize = 5;
	$scope.itemsPerPage = 10;
	$scope.formData = {currentPage: 1};
	$scope.customers = loadData.customers;
	$scope.totalItems = loadData.totalItems;
	
	
	//---:
	$scope.deleteData = function(id) {
		var isConfirmed = confirm('ยืนยันการลบข้อมูล');
	    if(!isConfirmed) return;
	    
		$scope.search(id);
	}
	
	$scope.editData = function(data) {
		$state.go('dashboard.dmsCustomer.add', {data: data});
	}
	
	$scope.search = function(deletedId) {
		$http.post(urlPrefix + '/restAct/dms/getCustomers',{
			deletedId: deletedId,
			currentPage: 1,
	    	itemsPerPage: 10,
	    	name: $scope.formData.name,
	    	enabled: $scope.formData.enabled
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			$scope.customers = result.customers;
			$scope.totalItems = result.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});		
	}
	
	
	
	
	//-------------------------------------------------------
	angular.element(document).ready(function () {
		$("input[name='name']").typeWatch({
			  wait: 750, // 750ms
			  highlight: true,
			 /* captureLength: 3,*/
			  callback: function(value) {
				  $scope.search();
			  }
		});
    });
	
});
