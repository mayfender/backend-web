angular.module('sbAdminApp').controller('AddMemberTypeCtrl', function($rootScope, $scope, $base64, $http, $translate, urlPrefix) {
	
	console.log('testing AddMemberTypeCtrl');
	
	$scope.$parent.headerTitle = 'เพิ่มประเภทสมาชิก';
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	$scope.persisBtn = 'บันทึก';
	
	
	$scope.save = function() {
		$http.post(urlPrefix + '/restAct/memberType/save', {
			userNameShow: $scope.user.userNameShow,
			userName: $scope.user.userName,
			password: $base64.encode($scope.user.password),
			authority: $scope.user.roles[0].authority,
			status: $scope.user.enabled
		}).then(function(data) {
			if(data.data.statusCode != 9999) {			
				if(data.data.statusCode == 2001) {
					$translate('message.err.username_show_same').then(function (msg) {
						$scope.existingUserShowErrMsg = msg;
					});
				}else if(data.data.statusCode == 2000) {
					$translate('message.err.username_same').then(function (msg) {
						$scope.existingUserErrMsg = msg;
					});
				}else{
					$rootScope.systemAlert(data.data.statusCode);
				}
				
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Save User Success');
			$scope.formData.currentPage = 1;
			$scope.formData.status = null;
			$scope.formData.role = "";
			$scope.formData.userName = null;
			$state.go('dashboard.user.search', {
				'itemsPerPage': $scope.itemsPerPage, 
				'currentPage': 1,
				'status': $scope.formData.status, 
				'role': $scope.formData.role, 
				'userName': $scope.formData.userName
			});
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	
	
});