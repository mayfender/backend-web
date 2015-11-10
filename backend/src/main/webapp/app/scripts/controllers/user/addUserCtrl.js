angular.module('sbAdminApp').controller('AddUserCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, urlPrefix, toaster) {
	
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	
	if($stateParams.user) { //-- Initial edit module
		$translate('user.header.panel.edit_user').then(function (editUser) {
			$scope.$parent.headerTitle = editUser;
		});
		$translate('user.addpage.update_btn').then(function (updateBtn) {
			$scope.persisBtn = updateBtn;
		});
		
		$scope.user = $stateParams.user;
		$scope.isEdit = true;
	} else {                // Initial for create module
		$translate('user.header.panel.add_user').then(function (addUser) {
			$scope.$parent.headerTitle = addUser;
		});
		$translate('user.addpage.save_btn').then(function (saveBtn) {
			$scope.persisBtn = saveBtn;
		});
		
		$scope.user = {};
		$scope.user.roles = [{}];
		$scope.user.enabled = 1;
	}
	
	$scope.clear = function() {
		setNull();
	}
	
	$scope.update = function() {
		$http.post(urlPrefix + '/restAct/user/updateUser', {
			id: $scope.user.id,
			userName: $scope.user.userName,
			authority: $scope.user.roles[0].authority,
			status: $scope.user.enabled
		}).then(function(data) {
			if(data.data.statusCode != 9999) {				
				if(data.data.statusCode == 2000) {
					$scope.existingUserErrMsg = "Username already exists";
				}else{
					$rootScope.systemAlert(data.data.statusCode);
				}
				
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Update User Success');
			$state.go('dashboard.user.search');
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.save = function() {
		var result = confirmPassword();
		
		if(!result && !$scope.autoGen) {
			$scope.notMatchRepassErrMsg = "Must match the previous entry";
			return;
		}
		
		$http.post(urlPrefix + '/restAct/user/saveUser', {
			userName: $scope.user.userName,
			password: $base64.encode($scope.user.password),
			authority: $scope.user.roles[0].authority,
			status: $scope.user.enabled
		}).then(function(data) {
			if(data.data.statusCode != 9999) {				
				if(data.data.statusCode == 2000) {
					$scope.existingUserErrMsg = "Username already exists";
				}else{
					$rootScope.systemAlert(data.data.statusCode);
				}
				
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Save User Success');
			$state.go('dashboard.user.search');
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.autoGenEvent = function() {
		if($scope.autoGen){
			$scope.user.userName = 'gen_' + Math.floor(Date.now() / 1000);
			$scope.user.password = '1234';    	
			$scope.user.roles[0].authority = "";
			$scope.existingUserErrMsg = null;
			$scope.notMatchRepassErrMsg = null;
		}else{
			setNull();
		}    			
	}
	
	function setNull() {
		$scope.user.reTypePassword = null;
		$scope.user.userName = null;
		$scope.user.password = null;
		$scope.autoGen = false;
		$scope.user.roles[0].authority = "";
		$scope.user.enabled = 1;
	} 
	
	function confirmPassword() {
		return ($scope.user.password == $scope.user.reTypePassword);
	}
	
});