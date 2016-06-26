angular.module('sbAdminApp').controller('ViewWorkingCtrl', function($rootScope, $stateParams, $scope, $state, $filter, $http, urlPrefix, loadData) {
	
	$scope.fieldName = loadData.fieldName;
	$scope.taskDetail = loadData.taskDetail;
	
	
});