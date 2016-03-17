angular.module('sbAdminApp').controller('SearchMemberTypeCtrl', function($rootScope, $scope, $base64, $http, $translate, urlPrefix) {
	
	console.log('testing MemberTypeCtrl');
	
	$scope.$parent.headerTitle = 'แสดงประเภทสมาชิก';
	$scope.$parent.iconBtn = 'fa-plus-square';
	$scope.$parent.url = 'add';
	
	$scope.datas = [{memberTypeName: 'aaaa', status: 0}, {memberTypeName: 'bbbb', status: 1}]
	
	
});