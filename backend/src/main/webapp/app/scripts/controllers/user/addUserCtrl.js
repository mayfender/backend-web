angular.module('sbAdminApp').controller('AddUserCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, $filter, $localStorage, urlPrefix, roles, roles2, toaster, loadProductsSelect) {
	
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	$scope.productsSelect = loadProductsSelect.products;
	$scope.isShowProducts = true;
	var isChangedImg = false;
	
	if($localStorage.authorities[0].authority == 'ROLE_ADMIN') {		
		$scope.rolesConstant = roles2;		
	} else {
		$scope.rolesConstant = roles;		
	}
	
	console.log(roles);
	
	$translate('user.addpage.save_btn').then(function (saveBtn) {
		$scope.persisBtn = saveBtn;
	});
	
	$scope.authoritySelected = function() {
		console.log($scope.user.authorities[0].authority);
		if($scope.user.authorities[0].authority == 'ROLE_SUPERADMIN' || $scope.user.authorities[0].authority == 'ROLE_MANAGER') {
			$scope.isShowProducts = false;
			$scope.user.products = null;
		} else {
			$scope.isShowProducts = true;
		}
	}
	
	if($stateParams.user) { //-- Initial edit module
		$translate('user.header.panel.edit_user').then(function (editUser) {
			$scope.$parent.headerTitle = editUser;
		});
		
		$scope.user = $stateParams.user;
		$scope.isEdit = true;
		$scope.authoritySelected();
	} else {                // Initial for create module
		$translate('user.header.panel.add_user').then(function (addUser) {
			$scope.$parent.headerTitle = addUser;
		});
		
		$scope.user = {};
		$scope.user.authorities = [{}];
		$scope.user.enabled = true;
	}
	
	$scope.clear = function() {
		setNull();
	}
	
	$scope.update = function() {
		$http.post(urlPrefix + '/restAct/user/updateUser', {
			id: $scope.user.id,
			showname: $scope.user.showname,
			username: $scope.user.username,
			password: $scope.user.password && $base64.encode($scope.user.password),
			authority: $scope.user.authorities[0].authority,
			enabled: $scope.user.enabled,
			productIds: $scope.user.products
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
			
			$rootScope.systemAlert(data.data.statusCode, 'Update User Success');
			$state.go('dashboard.user.search', {
				'itemsPerPage': $scope.itemsPerPage, 
				'currentPage': $scope.formData.currentPage,
				'status': $scope.formData.status, 
				'role': $scope.formData.role, 
				'userName': $scope.formData.userName,
				'product': $scope.formData.product
			});
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
			showname: $scope.user.showname,
			username: $scope.user.username,
			password: $base64.encode($scope.user.password),
			authority: $scope.user.authorities[0].authority,
			enabled: $scope.user.enabled,
			productIds: $scope.user.products
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
			$scope.formData.product = null;
			
			$state.go('dashboard.user.search', {
				'itemsPerPage': $scope.itemsPerPage, 
				'currentPage': 1,
				'status': $scope.formData.status, 
				'role': $scope.formData.role, 
				'userName': $scope.formData.userName,
				'product': $scope.formData.product
			});
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.autoGenEvent = function() {
		if($scope.autoGen){
			var genName = 'gen_' + Math.floor(Date.now() / 1000);
			$scope.user.userNameShow = genName;
			$scope.user.userName = genName;
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
	
	
	$scope.preview = function(element) {		
		isChangedImg = true;
		
		if (element.files && element.files[0]) {
			$scope.currentFile = element.files[0];
			var reader = new FileReader();
	
			reader.onload = function(event) {
				$scope.imageSource = event.target.result;	
			}
			
			// when the file is read it triggers the onload event above.
			reader.readAsDataURL(element.files[0]);
			$scope.$apply();
		} else {
			$scope.imgUpload = null;
			$('#imgUpload').attr('src', null);
		}	
	}
	
});