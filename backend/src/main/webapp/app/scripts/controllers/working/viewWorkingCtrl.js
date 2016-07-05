angular.module('sbAdminApp').controller('ViewWorkingCtrl', function($rootScope, $stateParams, $scope, $state, $filter, $http, urlPrefix, loadData) {
	
	$scope.taskDetail = loadData.taskDetail;
	$scope.groupDatas = loadData.groupDatas;
	$scope.lastGroupActive = $scope.groupDatas[0];
	$scope.lastGroupActive.btnActive = true;
	
	$scope.fieldName = $filter('orderBy')(loadData.colFormMap[$scope.groupDatas[0].id], 'detOrder');
	
	$scope.changeTab = function(group) {
		group.btnActive = true;
		$scope.lastGroupActive.btnActive = false;
		$scope.lastGroupActive = group;
		$scope.fieldName = $filter('orderBy')(loadData.colFormMap[group.id], 'detOrder');
	}
	
});