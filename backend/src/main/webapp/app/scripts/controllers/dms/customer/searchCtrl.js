angular.module('sbAdminApp').controller('SearchCtrl', function($rootScope, $scope, $http, $state, $translate, $localStorage, loadData, urlPrefix, roles2, roles3) {	
	
	console.log('test');
	$scope.maxSize = 5;
	$scope.itemsPerPage = 100;
	$scope.formData = {currentPage: 1};
	
	$scope.customers = [
		{id:'1', companyName: 'บริษัท ฟิลอส จำกัด', comCode:'BKK-0001-00001', package: 'เช่า', enabled: true, createdDateTime: new Date()},
		{id:'2', companyName: 'บริษัท แฟร์ กรุ๊ป พลัส จำกัด', comCode:'BKK-0002-00002', package: 'ซื้อขาด', enabled: true, createdDateTime: new Date()},
		{id:'3', companyName: 'หจก.กรรณกร 8998', comCode:'BKK-0003-00003', package: 'ซื้อขาด', enabled: true, createdDateTime: new Date()},
		{id:'4', companyName: 'บริษัท บีซี รัตนทรัพย์ จำกัด', comCode:'BKK-0004-00004', package: 'เช่า', enabled: true, createdDateTime: new Date()},
		{id:'5', companyName: 'บริษัท วีซี แอสเซ็ท จำกัด', comCode:'BKK-0005-00005', package: 'เช่า', enabled: true, createdDateTime: new Date()}
		];
	
	$scope.totalItems = $scope.customers.length;
	
});
