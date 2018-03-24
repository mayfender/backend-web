angular.module('sbAdminApp').controller('AddProductCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, urlPrefix, roles, toaster) {
	
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	$scope.persisBtn = 'บันทึก';
	
	if($stateParams.data) { //-- Initial edit module		
		$scope.$parent.headerTitle = 'แก้ใขโปรดักส์';		
		$scope.isEdit = true;
		$scope.data = {};
		$scope.data.id = $stateParams.data.id;
		$scope.data.productName = $stateParams.data.productName;
		$scope.data.enabled = $stateParams.data.enabled;
		$scope.data.isTraceExportExcel = $stateParams.data.productSetting.isTraceExportExcel;
		$scope.data.isTraceExportTxt = $stateParams.data.productSetting.isTraceExportTxt;
		$scope.data.traceDateRoundDay = $stateParams.data.productSetting.traceDateRoundDay;
		$scope.data.noticeFramework = $stateParams.data.productSetting.noticeFramework;
		$scope.data.pocModule = $stateParams.data.productSetting.pocModule || 0;
		$scope.data.createdByLog = $stateParams.data.productSetting.createdByLog || 0;
		$scope.data.autoUpdateBalance = $stateParams.data.productSetting.autoUpdateBalance || 0;
		$scope.data.paymentRules = $stateParams.data.productSetting.paymentRules;
		$scope.data.discountColumnName = $stateParams.data.productSetting.discountColumnName;
	} else { // Initial for create module
		
		$scope.$parent.headerTitle = 'เพิ่มโปรดักส์';
		$scope.data = {};
		$scope.data.enabled = 1;
		$scope.data.noticeFramework = 1;
		$scope.data.isTraceExportExcel = true;
		$scope.data.isTraceExportTxt = false;
		$scope.data.pocModule = 0;
		$scope.data.autoUpdateBalance = 0;
		$scope.data.createdByLog = 0;
	}
	
	$scope.clear = function() {
		setNull();
	}
	
	$scope.update = function() {
		
		delete $scope.data['createdDateTime'];
		
		console.log($scope.data);
		
		$http.post(urlPrefix + '/restAct/product/updateProduct', $scope.data).then(function(data) {
			if(data.data.statusCode != 9999) {				
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Update Success');
			$state.go('dashboard.product.search', {
				'itemsPerPage': $scope.itemsPerPage, 
				'currentPage': $scope.formData.currentPage,
				'enabled': $scope.formData.enabled,
				'productName': $scope.formData.productName
			});
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.save = function() {
		$http.post(urlPrefix + '/restAct/product/saveProduct', $scope.data).then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Save Success');
			$scope.formData.currentPage = 1;
			$scope.formData.enabled = null;
			$scope.formData.productName = null;
			$state.go('dashboard.product.search', {
				'itemsPerPage': $scope.itemsPerPage, 
				'currentPage': 1,
				'enabled': $scope.formData.enabled,
				'productName': $scope.formData.productName
			});
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	function setNull() {
		$scope.user.reTypePassword = null;
		$scope.user.userName = null;
		$scope.user.password = null;
		$scope.autoGen = false;
		$scope.user.roles[0].authority = "";
		$scope.user.enabled = 1;
	} 
	
});