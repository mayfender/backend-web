angular.module('sbAdminApp').controller('AddCtrl', function($rootScope, $scope, $http, $state, $stateParams, $translate, $localStorage, loadData, urlPrefix, roles2, roles3) {	
	
	console.log('test Add');
	
	
	//---: Main CTRL
	$scope.main.headerTitle = 'Add Customer';
	$scope.main.iconBtn = 'fa-long-arrow-left';
	$scope.main.page = 2;	
	//---/ Main CTRL
	$scope.formData = {package: 1};
	$scope.packages = [{id: 1, name: 'เช่า'}, {id: 2, name: 'ซื้อขาด'}];
	$scope.data = $stateParams.data;
	
	
	$scope.customers = [
		{id:'1', name: 'Krungsri AY', enabled: true, createdDateTime: new Date()},
		{id:'2', name: 'Kasikorn', enabled: true, createdDateTime: new Date()},
		{id:'3', name: 'เงินติดล้อ', enabled: true, createdDateTime: new Date()},
		{id:'4', name: 'ธนชาต', enabled: true, createdDateTime: new Date()},
		{id:'5', name: 'ออมสิน', enabled: false, createdDateTime: new Date()}
		];
	
	if($scope.data) {
		$scope.main.headerTitle = 'Edit Customer';
		console.log($scope.data);
		
		$scope.formData.name = $scope.data.name;
		$scope.formData.code = $scope.data.code;
		$scope.formData.package = $scope.data.package;
		$scope.formData.enabled = $scope.data.enabled;
	} else {
		$scope.formData.enabled = true;		
	}
	
});
