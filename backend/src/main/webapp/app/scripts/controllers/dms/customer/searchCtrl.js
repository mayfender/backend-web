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
	
	/*$scope.customers = [
		{id:'1', name: 'บริษัท ฟิลอส จำกัด', code:'BKK-0001-00001', package: 1, enabled: true, createdDateTime: new Date()},
		{id:'2', name: 'บริษัท แฟร์ กรุ๊ป พลัส จำกัด', code:'BKK-0002-00002', package: 2, enabled: true, createdDateTime: new Date()},
		{id:'3', name: 'หจก.กรรณกร 8998', code:'BKK-0003-00003', package: 2, enabled: false, createdDateTime: new Date()},
		{id:'4', name: 'บริษัท บีซี รัตนทรัพย์ จำกัด', code:'BKK-0004-00004', package: 1, enabled: true, createdDateTime: new Date()},
		{id:'5', name: 'บริษัท วีซี แอสเซ็ท จำกัด', code:'BKK-0005-00005', package: 1, enabled: true, createdDateTime: new Date()}
		];*/
	

	//---:
	$scope.editData = function(data) {
		$state.go('dashboard.dmsCustomer.add', {data: data});
	}
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/dms/getCustomers',{
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
