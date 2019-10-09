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
		$scope.data.textLength = $stateParams.data.productSetting.textLength;
		$scope.data.discountFields = $stateParams.data.productSetting.discountFields || new Array();
		$scope.data.userEditable = $stateParams.data.productSetting.userEditable || 0;
		$scope.data.openOfficeHost = $stateParams.data.productSetting.openOfficeHost;
		$scope.data.openOfficePort = $stateParams.data.productSetting.openOfficePort;
		$scope.data.showUploadDoc = $stateParams.data.productSetting.showUploadDoc || 0;
		$scope.data.seizure = $stateParams.data.productSetting.seizure || 0;
		$scope.data.userKYSLaw = $stateParams.data.productSetting.userKYSLaw;
		$scope.data.passKYSLaw = $stateParams.data.productSetting.passKYSLaw;
		$scope.data.userKYS = $stateParams.data.productSetting.userKYS;
		$scope.data.passKYS = $stateParams.data.productSetting.passKYS;
		$scope.data.userKRO = $stateParams.data.productSetting.userKRO;
		$scope.data.passKRO = $stateParams.data.productSetting.passKRO;
		$scope.data.privateChatDisabled = $stateParams.data.productSetting.privateChatDisabled || 0;
		$scope.data.updateEmptyReminderDate = $stateParams.data.productSetting.updateEmptyReminderDate || 0;
		
		$scope.data.smsMessages = $stateParams.data.productSetting.smsMessages || new Array();
		$scope.data.isSmsEnable = $stateParams.data.productSetting.isSmsEnable;
		$scope.data.smsUsername = $stateParams.data.productSetting.smsUsername;
		$scope.data.smsPassword = $stateParams.data.productSetting.smsPassword;
		$scope.data.smsSenderName = $stateParams.data.productSetting.smsSenderName;
		$scope.data.dsf = $stateParams.data.productSetting.dsf || 0;
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
		$scope.data.discountFields = new Array();
		$scope.data.userEditable = 0;
		$scope.data.showUploadDoc = 0;
		$scope.data.seizure = 0;
		$scope.data.privateChatDisabled = 0;
		$scope.data.updateEmptyReminderDate = 0;
		$scope.data.smsMessages = new Array();
		$scope.data.isSmsEnable = false;
		$scope.data.dsf = 0;
	}
	
	$scope.clear = function() {
		setNull();
	}
	
	$scope.update = function() {
		
		delete $scope.data['createdDateTime'];
		
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
	
	$scope.addDiscountField = function() {
		$scope.data.discountFields.push({});
	}
	
	$scope.deleteDiscountField = function(index) {
		$scope.data.discountFields.splice(index, 1);
	}
	
	//--------: SMS
	$scope.addSmsMessage = function() {
		$scope.data.smsMessages.push({});
	}
	
	$scope.deleteSmsMessage = function(index) {
		$scope.data.smsMessages.splice(index, 1);
	}
	
});