angular.module('sbAdminApp').controller('PositionCtrl', function($rootScope, $scope, $base64, $http, $translate, urlPrefix) {
	
	$scope.users = [
	                {id: 1, name: 'ผู้จัดการ', status: 0},
	                {id: 2, name: 'Admin', status: 0},
	                {id: 3, name: 'ผู้ช่วย', status: 1}
	              ]; 
	
});