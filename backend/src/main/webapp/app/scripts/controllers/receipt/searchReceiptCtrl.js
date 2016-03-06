angular.module('sbAdminApp').controller('SearchReceiptCtrl', function($rootScope, $scope, $http, $state, $translate, loadServiceData, urlPrefix, roles) {	
	
	$scope.data = loadServiceData;	
	$scope.maxSize = 5;
	$scope.$parent.isShowUpdateBtn = true;
	$scope.$parent.headerTitle = 'แสดง' + $state.params.txt;
	$scope.$parent.iconBtn = 'fa-plus-square';
	$scope.$parent.url = 'add';
	
	if($state.params.type == 1) {
		
	} else if($state.params.type == 2) {
		
	} else if($state.params.type == 3) {
		
	} else if($state.params.type == 4) {
		
	} else if($state.params.type == 5) {
		
	}
	
	
});
