angular.module('sbAdminApp').controller('ReportMenuCtrl', function($rootScope, $scope, $state, $http, $stateParams, $translate, $log, $filter, $stomp, $sce, toaster, urlPrefix, loadReport) {
	
	$scope.reportDate = $stateParams.reportDate;
	$scope.menus = loadReport.menus;
	$scope.$parent.title = 'แสดงจำนวนรายการอาหารที่สั่ง [' + $filter('date')($stateParams.reportDate, 'dd/MM/yyyy') + ']';
	$scope.$parent.isMenu = true;
	
});