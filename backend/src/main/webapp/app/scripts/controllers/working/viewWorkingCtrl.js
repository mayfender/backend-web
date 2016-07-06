angular.module('sbAdminApp').controller('ViewWorkingCtrl', function($rootScope, $stateParams, $scope, $state, $filter, $http, urlPrefix, loadData) {
	
	console.log(loadData);
	
	$scope.taskDetail = loadData.taskDetail;
	$scope.groupDatas = loadData.groupDatas;
	$scope.lastGroupActive = $scope.groupDatas[0];
	$scope.lastGroupActive.btnActive = true;
	var othersGroupDatas;
	var relatedData;
	var relatedDetail = new Array();
	$scope.fieldName = $filter('orderBy')(loadData.colFormMap[$scope.groupDatas[0].id], 'detOrder');
	
	$scope.changeTab = function(group) {
		if($scope.groupDatas.length == 1) return;
		var fields;
		
		if(group.menu) {
			relatedData = loadData.relatedData[group.menu];
			$scope.taskDetail = relatedData.othersData;
			fields = relatedData.othersColFormMap[group.id];
		} else {
			$scope.taskDetail = loadData.taskDetail;
			fields = loadData.colFormMap[group.id];
		}
		
		$scope.fieldName = $filter('orderBy')(fields, 'detOrder');			
		$scope.lastGroupActive.btnActive = false;
		$scope.lastGroupActive = group;
		group.btnActive = true;
	}
	
	for(x in loadData.relatedData) {
		relatedData = loadData.relatedData[x];
		othersGroupDatas = relatedData.othersGroupDatas;
		
		for(i in othersGroupDatas) {
			othersGroupDatas[i].menu = x;
		}
		
		$scope.groupDatas = $scope.groupDatas.concat(othersGroupDatas);		
	}
	
});