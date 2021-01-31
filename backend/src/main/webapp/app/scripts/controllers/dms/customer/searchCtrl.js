angular.module('sbAdminApp').controller('SearchCtrl', function($rootScope, $scope, $http, $state, $translate, $localStorage, loadData, urlPrefix, roles2, roles3) {	
	
	console.log('test');
	
	//---: Main CTRL
	$scope.main.headerTitle = 'Customer List';
	$scope.main.iconBtn = 'fa-plus-square';
	$scope.main.page = 1;
	//---/ Main CTRL
	
	$scope.maxSize = 5;
	$scope.itemsPerPage = 100;
	$scope.formData = {currentPage: 1};
	
	$scope.customers = [
		{id:'1', companyName: 'บริษัท ฟิลอส จำกัด', comCode:'BKK-0001-00001', package: 1, enabled: true, createdDateTime: new Date()},
		{id:'2', companyName: 'บริษัท แฟร์ กรุ๊ป พลัส จำกัด', comCode:'BKK-0002-00002', package: 2, enabled: true, createdDateTime: new Date()},
		{id:'3', companyName: 'หจก.กรรณกร 8998', comCode:'BKK-0003-00003', package: 2, enabled: false, createdDateTime: new Date()},
		{id:'4', companyName: 'บริษัท บีซี รัตนทรัพย์ จำกัด', comCode:'BKK-0004-00004', package: 1, enabled: true, createdDateTime: new Date()},
		{id:'5', companyName: 'บริษัท วีซี แอสเซ็ท จำกัด', comCode:'BKK-0005-00005', package: 1, enabled: true, createdDateTime: new Date()}
		];
	
	$scope.totalItems = $scope.customers.length;

	//---:
	$scope.editUser = function(data) {
		$state.go('dashboard.dmsCustomer.add', {data: data});
	}
	
});
