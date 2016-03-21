angular.module('sbAdminApp').controller('AddReceiptCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, urlPrefix, roles, toaster, loadServiceData) {
	
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	
	if($stateParams.id) {
		$scope.$parent.headerTitle = 'แก้ใขรายการ (' + $scope.serviceTypeText +')';
		$scope.persisBtn = "แก้ใข";
		$scope.criteria = loadServiceData.serviceData;
		$scope.isEdit = true;
	} else {
		$scope.criteria = {};
		$scope.persisBtn = "บันทึก";
		$scope.$parent.headerTitle = 'เพิ่มรายการ (' + $scope.serviceTypeText +')';
	}
	
	
	$scope.save = function() {
		$scope.criteria.serviceTypeId = $scope.serviceTypeId;
		
		$http.post(urlPrefix + '/restAct/serviceData/save', $scope.criteria).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$state.go('dashboard.receipt.search', {
				'itemsPerPage': $scope.itemsPerPage, 
				'currentPage': 1,
				'serviceTypeId': $scope.serviceTypeId,
				'status': $scope.formData.status,
				'docNo': $scope.formData.docNo,
		    	'dateTimeStart': $scope.formData.dateTimeStart,
		    	'dateTimeEnd': $scope.formData.dateTimeEnd,
		    	'txt': $scope.serviceTypeText
			});
			
			$rootScope.systemAlert(data.data.statusCode, 'บันทึกข้อมูลสำเร็จ');
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.update = function() {
		$http.post(urlPrefix + '/restAct/serviceData/update', $scope.criteria).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$state.go('dashboard.receipt.search', {
				'itemsPerPage': $scope.$parent.itemsPerPage, 
				'currentPage': $scope.$parent.formData.currentPage,
				'serviceTypeId': $scope.serviceTypeId,
				'status': $scope.$parent.formData.status,
				'docNo': $scope.$parent.formData.docNo,
		    	'dateTimeStart': $scope.$parent.formData.dateTimeStart,
		    	'dateTimeEnd': $scope.$parent.formData.dateTimeEnd,
		    	'txt': $scope.serviceTypeText
			});
			
			$rootScope.systemAlert(data.data.statusCode, 'บันทึกข้อมูลสำเร็จ');
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	
	
	
});