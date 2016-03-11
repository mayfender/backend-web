angular.module('sbAdminApp').controller('AddReceiptCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, urlPrefix, roles, toaster) {
	
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	$scope.$parent.headerTitle = 'ทำรายการ (' + $scope.serviceTypeText +')';
	$scope.persisBtn = "บันทึก";
	
	
});