angular.module('sbAdminApp').controller('SearchReceiptCtrl', function($rootScope, $scope, $http, $state, $translate, urlPrefix, roles) {	
	
	console.log($state.params);
	
	$scope.$parent.isShowUpdateBtn = true;
	
});
