angular.module('sbAdminApp').controller('ViewWorkingCtrl', function($rootScope, $stateParams, $scope, $state, $filter, $http, urlPrefix, loadData) {
	
	console.log(loadData);
	
	$scope.taskDetail = loadData.taskDetail;
	$scope.groupDatas = loadData.groupDatas;
	var othersGroupDatas;
	var relatedData;
	var relatedDetail = new Array();
	var lastGroupActive = $scope.groupDatas[0];
	lastGroupActive.btnActive = true;
	$scope.fieldName = $filter('orderBy')(loadData.colFormMap[$scope.groupDatas[0].id], 'detOrder');
	$scope.tabActionMenus = [{name: 'ผลการติดตาม', url: './views/working/tab_1.html', btnActive: true}, 
	                         {name: 'เบอร์ติดต่อ', url: './views/working/tab_2.html'}, 
	                         {name: 'ประวัติการนัดชำระ', url: './views/working/tab_3.html'}, 
	                         {name: 'payment', url: './views/working/tab_4.html'}, 
	                         {name: 'บัญชีพ่วง', url: './views/working/tab_5.html'},
	                         {name: 'ข้อมูลงาน', url: './views/working/tab_6.html'}];
	$scope.lastTabActionMenuActive = $scope.tabActionMenus[0];
	
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
		lastGroupActive.btnActive = false;
		lastGroupActive = group;
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
	
	$scope.changeTabAction = function(menu) {
		$scope.lastTabActionMenuActive.btnActive = false;
		$scope.lastTabActionMenuActive = menu;
		menu.btnActive = true;
	}
	
});