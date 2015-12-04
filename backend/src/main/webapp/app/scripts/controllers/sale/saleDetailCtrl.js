angular.module('sbAdminApp').controller('SaleDetailCtrl', function($rootScope, $scope, $state, $http, $stateParams, $translate, $log, toaster, urlPrefix, loadOrders) {
	
	$scope.orders = loadOrders.orders;
	$scope.formData.isDetailMode = true;
	
	$translate('sale.header_panel.detail').then(function (msg) {
		$scope.$parent.headerTitle = msg;
	});
	
	$translate('sale_detail.table').then(function (msg) {
		$scope.$parent.headerTitle += ' ' + msg + ' ' +$stateParams.tableDetail; 
	});
	
	$translate('sale_detail.ref').then(function (msg) {
		$scope.$parent.headerTitle += ' ' + msg + ' ' +$stateParams.ref;
	});
	
});