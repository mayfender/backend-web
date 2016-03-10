angular.module('sbAdminApp').controller('AddMemberTypeCtrl', function($rootScope, $stateParams, $scope, $state, $base64, $http, $translate, urlPrefix) {
	
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	$scope.isEdit = false;
	
	if($stateParams.data) {
		console.log($stateParams.data);
		
		$scope.persisBtn = 'แก้ใข';		
		$scope.$parent.headerTitle = 'แก้ใขประเภทสมาชิก';
		$scope.data = $stateParams.data;
		$scope.isEdit = true;
	} else {
		$scope.$parent.headerTitle = 'เพิ่มประเภทสมาชิก';
		$scope.persisBtn = 'บันทึก';		
		$scope.data = {status: 0};
		$scope.isEdit = false;
	}
	
	$scope.save = function() {
		$http.post(urlPrefix + '/restAct/memberType/save',
			$scope.data
		).then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'บันทึกข้อมูลสำเร็จ');
			$scope.formData.status = null;
			$scope.formData.durationType = null;
			$scope.formData.memberTypeName = null;
			
			$state.go('dashboard.memberType.search', {
				'status': $scope.formData.status, 
				'durationType': $scope.formData.durationType,
				'memberTypeName': $scope.formData.memberTypeName
			});
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.update = function() {
		$http.post(urlPrefix + '/restAct/memberType/update',
			$scope.data
		).then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'แก้ใขข้อมูลสำเร็จ');
			
			$state.go('dashboard.memberType.search', {
				'status': $scope.formData.status, 
				'durationType': $scope.formData.durationType,
				'memberTypeName': $scope.formData.memberTypeName
			});
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clear = function() {
		$scope.data.memberTypeName = null;
		$scope.data.durationType = null;
		$scope.data.durationQty = null;
		$scope.data.memberPrice = null;
		$scope.data.status = 0;
	}
	
	
});