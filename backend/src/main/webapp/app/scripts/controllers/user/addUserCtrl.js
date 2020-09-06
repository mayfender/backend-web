angular.module('sbAdminApp').controller('AddUserCtrl', function($rootScope, $scope, $stateParams, $http, $state, $base64, $translate, $filter, $localStorage, urlPrefix, roles2, roles3, toaster, loadData) {
	
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	$scope.nameTitles = ['นาย', 'นาง','นางสาว', 'คุณ'];
	var userLoad = loadData.user;
	var isChangedImg = false;
	
	if($rootScope.workingOnDealer.id == null) {
		$scope.rolesConstant = roles2;
	} else {
		$scope.rolesConstant = roles3;
	}
	
	if($stateParams.user) { //-- Initial edit module
		$scope.$parent.headerTitle = 'Edit';
		$scope.user = $stateParams.user;
		$scope.user.firstName = userLoad.firstName;
		$scope.user.lastName = userLoad.lastName;
		$scope.user.lineUserId = userLoad.lineUserId;
		$scope.titleShow = userLoad.title;
		$scope.isEdit = true;
		
		if(userLoad.imgData && userLoad.imgData.imgContent) {			
			$scope.imageSource = 'data:image/JPEG;base64,' + userLoad.imgData.imgContent;
		} else {
			$scope.imageSource = null;
		}
	} else {                // Initial for create module
		$scope.$parent.headerTitle = 'Add';
		$scope.user = {};
		$scope.user.authorities = [{}];
		$scope.user.enabled = true;
		$scope.titleShow = 'นาย';
	}
	
	$scope.clear = function() {
		setNull();
	}
	
	$scope.update = function() {
		var authority = $scope.user.authorities[0].authority;
		
		console.log($scope.user.imgUpload);
		
		$http.post(urlPrefix + '/restAct/user/updateUser', {
			id: $scope.user.id,
			showname: $scope.user.showname,
			username: $scope.user.username,
			password: $scope.user.password && $base64.encode($scope.user.password),
			authority: $scope.user.authorities[0].authority,
			enabled: $scope.user.enabled,
			dealerId: $rootScope.workingOnDealer.id,
			firstName: $scope.user.firstName,
			lastName: $scope.user.lastName,
			imgContent: isChangedImg ? ($scope.user.imgUpload && $scope.user.imgUpload.base64) : null,
			imgName: isChangedImg ? ($scope.user.imgUpload && $scope.user.imgUpload.filename) : null,
			isChangedImg: isChangedImg,
			title: $scope.titleShow,
			lineUserId: $scope.user.lineUserId
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
				}else if(data.data.statusCode == 2002) {
					$scope.existingLineUserErrMsg = 'ID ซ้ำ';
				}else{
					$rootScope.systemAlert(data.data.statusCode);
				}
				
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Update User Success');
			$state.go('dashboard.user.search', {
				'itemsPerPage': $scope.itemsPerPage, 
				'currentPage': $scope.formData.currentPage,
				'role': $scope.formData.role, 
				'userName': $scope.formData.userName
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
		
		var authority = $scope.user.authorities[0].authority;
		
		$http.post(urlPrefix + '/restAct/user/saveUser', {
			showname: $scope.user.showname,
			username: $scope.user.username,
			password: $base64.encode($scope.user.password),
			authority: authority,
			enabled: $scope.user.enabled,
			dealerId: $rootScope.workingOnDealer.id,
			firstName: $scope.user.firstName,
			lastName: $scope.user.lastName,
			imgContent: $scope.user.imgUpload && $scope.user.imgUpload.base64,
			imgName: $scope.user.imgUpload && $scope.user.imgUpload.filename,
			title: $scope.titleShow,
			lineUserId: $scope.user.lineUserId
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
				}else if(data.data.statusCode == 2002) {
					$scope.existingLineUserErrMsg = 'ID ซ้ำ';					
				}else{
					$rootScope.systemAlert(data.data.statusCode);
				}
				
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Save User Success');
			
			$scope.formData.currentPage = 1;
			$scope.formData.role = "";
			$scope.formData.userName = null;
			
			$state.go('dashboard.user.search', {
				'itemsPerPage': $scope.itemsPerPage, 
				'currentPage': 1,
				'role': $scope.formData.role, 
				'userName': $scope.formData.userName
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
		$scope.user.showname = null;
		$scope.user.username = null;
		$scope.user.password = null;
		$scope.user.reTypePassword = null;
		$scope.user.products = null;
		$scope.user.firstName = null;
		$scope.user.lastName = null;
		$scope.imageSource = null;
		$scope.user.authorities[0].authority = "";
		$scope.user.enabled = true;
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
				$scope.$apply();
			}
			
			// when the file is read it triggers the onload event above.
			reader.readAsDataURL(element.files[0]);
		} else {
			$scope.user.imgUpload = null;
			$('#imgUpload').attr('src', null);
		}	
	}
	
	//----------------------------------------------------
	
	$scope.changeTitle = function(title) {
		$scope.titleShow = title;
	}
	
});