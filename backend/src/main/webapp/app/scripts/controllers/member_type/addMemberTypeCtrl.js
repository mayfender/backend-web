angular.module('sbAdminApp').controller('AddMemberTypeCtrl', function($rootScope, $scope, $base64, $http, $translate, urlPrefix) {
	
	console.log('testing AddMemberTypeCtrl');
	
	$scope.$parent.headerTitle = 'เพิ่มประเภทสมาชิก';
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	$scope.persisBtn = 'บันทึก';
	
	
	
});