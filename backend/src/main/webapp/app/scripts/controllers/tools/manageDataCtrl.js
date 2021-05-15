angular.module('sbAdminApp').controller('ManageDataCtrl', function($rootScope, $scope, $stateParams, $state, $base64, $http, $localStorage, $translate, $filter, FileUploader, urlPrefix) {
	$scope.$parent.isShowBack = true;
	$scope.$parent.titlePanel = $stateParams.desc;
	$scope.tabActived = 1;
	$scope.btnTxt = 'เริ่มย้ายข้อมูล';
	
	$scope.modules = [
		{
			tbName: 'traceWork',
			title: '1. ผลการติดตาม'
		},
		{
			tbName: 'traceWorkOld',
			title: '2. ผลการติดตามเก่าจากผู้ว่าจ้าง', 
			fields: [
				{field: 'createdDateTime', 
				condName: [
					{id: 1, title: 'มากกว่า'}
				],
				condVal: [
						{id: 1, title: '1 เดือน'},
						{id: 2, title: '3 เดือน'},
						{id: 3, title: '6 เดือน'},
						{id: 4, title: '12 เดือน'}
					]
				}
			]
		}
	];
	
	$scope.formData = {};
	
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