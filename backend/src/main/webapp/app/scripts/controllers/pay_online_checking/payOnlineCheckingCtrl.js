angular.module('sbAdminApp').controller('PayOnlineCheckingCtrl', function($rootScope, $scope, $stateParams, $state, $base64, $http, $localStorage, $translate, $filter, FileUploader, urlPrefix, loadData) {
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;
	
	if(loadData.checkMapList) {
		$scope.checkList_1 = loadData.checkMapList['1'] || new Array();
		$scope.checkList_2 = loadData.checkMapList['2'];
		$scope.checkList_3 = loadData.checkMapList['3'];
		if($scope.checkList_3) {
			$.merge($scope.checkList_1, $scope.checkList_3);		
		}
	}
	
	$scope.checkList_his_1 = $scope.checkList_1;
	$scope.checkList_his_3 = $scope.checkList_3;
	
	$scope.headers = loadData.headers;
	$scope.totalItems = loadData.totalItems;
	$scope.maxSize = 5;
	var today = new Date($rootScope.serverDateTime);
	$scope.formData.date = angular.copy(today);
	$scope.formCheckingData = {};

	$scope.goToTask = function(id) {
		$state.go('dashboard.working.search.view', {id: id, productId: $rootScope.workingOnProduct.id});
	}
	
	$scope.getCheckList = function(dateParam) {
		$http.post(urlPrefix + '/restAct/paymentOnlineCheck/getCheckListShow', {
			date: dateParam || today,
			owner: $scope.formData.owner,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			loadData = data.data;
			
			if(loadData.statusCode != 9999) {
				$rootScope.systemAlert(loadData.statusCode);
				return;
			}
			
			if(dateParam) {
				$scope.checkList_his_1 = loadData.checkMapList['1'];
				$scope.checkList_his_3 = loadData.checkMapList['3'];					
			} else {
				$scope.checkList_1 = loadData.checkMapList['1'] || new Array();
				$scope.checkList_2 = loadData.checkMapList['2'];
				$scope.checkList_3 = loadData.checkMapList['3'];
				if($scope.checkList_3) {
					$.merge($scope.checkList_1, $scope.checkList_3);					
				}
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
});