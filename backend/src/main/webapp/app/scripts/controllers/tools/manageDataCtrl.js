angular.module('sbAdminApp').controller('ManageDataCtrl', function($rootScope, $scope, $stateParams, $state, $base64, $http, $localStorage, $translate, $filter, FileUploader, urlPrefix) {
	$scope.$parent.isShowBack = true;
	$scope.$parent.titlePanel = $stateParams.desc;
	$scope.tabActived = 1;
	$scope.btnTxt = 'เริ่มย้ายข้อมูล';
	
	$scope.checkData = function() {
		$http.post(urlPrefix + '/restAct/tools/manageData', {
			operationId: 1,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			var map = result.map;
			
			$scope.inTraceWorkAll = map.inTraceWorkAll;
			$scope.inTraceWorkSystem = map.inTraceWorkSystem;
			$scope.inTraceWorkOld = map.inTraceWorkOld;
			$scope.inTraceWorkOldAll = map.inTraceWorkOldAll;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.moveTraceData = function() {
		$http.post(urlPrefix + '/restAct/tools/manageData', {
			operationId: 2,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.btnTxt = 'ย้ายข้อมูลสำเร็จ';
			$scope.checkData();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
});