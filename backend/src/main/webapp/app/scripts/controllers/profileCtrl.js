angular.module('sbAdminApp').controller('ProfileCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, urlPrefix, loadData) {
	
	$scope.data = {};
	$scope.data.password = "";
	$scope.data.reTypePassword = "";
	$scope.data.usernameShow = $localStorage.showname;
	$scope.data.username = $localStorage.username;
	$scope.data.role = $localStorage.authorities[0].authority;
	
	var isChangedImg = false;
	var user = loadData.user;
	
	$scope.data.firstName = user.firstName;
	$scope.data.lastName = user.lastName;
	$scope.data.phoneNumber = user.phoneNumber;
	$scope.titleShow = user.title;
	
	$scope.nameTitles = ['นาย', 'นาง','นางสาว'];
	
	if(user.imgData && user.imgData.imgContent) {			
		$scope.imageSource = 'data:image/JPEG;base64,' + user.imgData.imgContent;
	} else {
		$scope.imageSource = null;
	}
	
	$scope.updateProfile = function() {
		var result = confirmPassword();
		
		if(!result) {
			$scope.notMatchRepassErrMsg = "Must match the previous entry";
			return;
		}
		
		$http.post(urlPrefix + '/restAct/user/updateProfile', {
			oldUserNameShow: $localStorage.showname,
			oldUserName: $localStorage.username,
			newUserNameShow: $scope.data.usernameShow,
			newUserName: $scope.data.username,
			password: $scope.data.password && $base64.encode($scope.data.password),
			firstName: $scope.data.firstName,
			lastName: $scope.data.lastName,
			phoneNumber: $scope.data.phoneNumber,
			imgContent: isChangedImg ? ($scope.data.imgUpload && $scope.data.imgUpload.base64) : null,
			imgName: isChangedImg ? ($scope.data.imgUpload && $scope.data.imgUpload.filename) : null,
			isChangedImg: isChangedImg,
			title: $scope.titleShow
		}).then(function(data) {
			if(data.data.statusCode != 9999) {
				if(data.data.statusCode == 2000) {
					$translate('message.err.username_same').then(function (msg) {
						$scope.existingUserErrMsg = msg;
					});
				}else{
					$rootScope.systemAlert(data.data.statusCode);
				}
				
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Update Profile Success');
			$localStorage.showname = $scope.$parent.showname = $scope.data.usernameShow;
			$localStorage.username = $scope.data.username;
			$scope.data.password = "";
			$scope.data.reTypePassword = "";
			
			if(isChangedImg && $scope.data.imgUpload && $scope.data.imgUpload.base64) {
				$rootScope.photoSource = 'data:image/JPEG;base64,' + $scope.data.imgUpload.base64;					
			} else {
				$rootScope.photoSource = null;				
			}
		}, function(response) {
			$rootScope.systemAlert(data.data.statusCode);
		});
	}
	
	function confirmPassword() {
		return ($scope.data.password == $scope.data.reTypePassword);
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
			$scope.data.imgUpload = null;
			$('#imgUpload').attr('src', null);
		}	
	}
	
	//----------------------------------------------------
	
	$scope.changeTitle = function(title) {
		$scope.titleShow = title;
	}
	
});