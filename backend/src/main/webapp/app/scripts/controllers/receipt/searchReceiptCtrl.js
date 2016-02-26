angular.module('sbAdminApp').controller('SearchReceiptCtrl', function($rootScope, $scope, $http, $state, $translate, urlPrefix, roles) {	
	
	console.log($state.params);
	
	$scope.$parent.isShowUpdateBtn = true;
	
	if($state.params.type == 1) {
		$scope.$parent.headerTitle = 'แสดงธนาณัติ EMS';
	} else if($state.params.type == 2) {
		$scope.$parent.headerTitle = 'แสดงชำระค่าบริการ';
	} else if($state.params.type == 3) {
		$scope.$parent.headerTitle = 'แสดงธนาณัติออนไลน์';
	} else if($state.params.type == 4) {
		$scope.$parent.headerTitle = 'แสดงชำระค่างวดรถยนต์';
	} else if($state.params.type == 5) {
		$scope.$parent.headerTitle = 'แสดงโอนเงินเข้าบัญชีธนาคาร';
	}
	
	
});
