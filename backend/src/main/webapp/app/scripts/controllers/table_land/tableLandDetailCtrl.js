angular.module('sbAdminApp').controller('TableLandDetailCtrl', function($rootScope, $scope, $state, $http, $stateParams, $translate, $log, toaster, urlPrefix, loadOrders) {
	
	console.log(loadOrders.orders);
	
	$scope.orders = loadOrders.orders;
	$scope.formData.isEditMode = true;
	
	$translate('table_land.header_panel.detail').then(function (msg) {
		$scope.$parent.headerTitle = msg + ' 12';
	});
	
});