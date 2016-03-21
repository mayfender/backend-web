angular.module('sbAdminApp').controller('AddRegisterCtrl', function($rootScope, $stateParams, $scope, $state, $base64, $http, $translate, $filter, urlPrefix, roles_customer, loadData) {
	
	$scope.format = "dd-MM-yyyy";
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	$scope.isEdit = false;
	$scope.rolesConstant = roles_customer;
	$scope.memberTypes = loadData.memberTyps;
	var isChangedImg = false;
	focus();
	
	if($stateParams.regId) {
		$scope.data = loadData.registration;
		
		if($scope.data.birthday) {			
			$scope.data.birthday = new Date($scope.data.birthday);
		}
		$scope.data.expireDate = new Date($scope.data.expireDate);
		
		if($scope.data.imgBase64) {			
			$scope.$parent.imageSource = 'data:image/JPEG;base64,' + $scope.data.imgBase64;
			delete $scope.data['imgBase64'];
		} else {
			$scope.$parent.imageSource = null;
		}
		
		$scope.memberId = $scope.data.memberId;
		delete $scope.data['memberId'];
		
		$scope.persisBtn = 'แก้ใข';		
		$scope.$parent.headerTitle = 'แก้ใขข้อมูลสมาชิก     [เลขที่สมาชิก: ' + $scope.memberId + ']';
		$scope.isEdit = true;
		$scope.isPassRequired = false;
	} else {
		$scope.$parent.headerTitle = 'ลงทะเบียนสมาชิก';
		$scope.persisBtn = 'บันทึก';		
		$scope.data = {authen:{status:1}};
		$scope.$parent.imageSource = null;
		$scope.isPassRequired = true;
	}
	
	$scope.save = function() {
		
		var result = isCorrectPassword();
		if(!result) return;
		
		$scope.data.authen.password = $base64.encode($scope.password);
		$scope.data.imgContent = $scope.imgUpload && $scope.imgUpload.base64;
		$scope.data.imgName = $scope.imgUpload && $scope.imgUpload.filename;
		
		$http.post(urlPrefix + '/restAct/registration/saveRegistration',
			$scope.data
		).then(function(data) {
			if(data.data.statusCode != 9999) {			
				if(data.data.statusCode == 2000) {
					$rootScope.systemAlert(-1, ' ', 'Username ซ้ำกรุณาลองใหม่อีกครั้ง');
					$scope.data.authen.userName = null;
					$("input[name='userName']").focus();
				}else{
					$rootScope.systemAlert(data.data.statusCode);
				}
				
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'บันทึกข้อมูลสำเร็จ');
			$scope.formData.firstname = null;
			$scope.formData.isActive = null;
			$scope.formData.currentPage = 1;
			
			$state.go('dashboard.register.search', {
				'firstname': $scope.formData.firstname, 
				'isActive': $scope.formData.isActive,
				'currentPage': $scope.formData.currentPage,
				'itemsPerPage': $scope.formData.itemsPerPage
			});
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.update = function() {
		var result = isCorrectPassword();
		if(!result) return;
		
		if($scope.password) {			
			$scope.data.authen.password = $base64.encode($scope.password);
		}
		
		$scope.data.isChangedImg = isChangedImg;
		if(isChangedImg) {			
			$scope.data.imgContent = $scope.imgUpload && $scope.imgUpload.base64;
			$scope.data.imgName = $scope.imgUpload && $scope.imgUpload.filename;
		}
		
		$http.post(urlPrefix + '/restAct/registration/updateRegistration',
			$scope.data
		).then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'แก้ใขข้อมูลสำเร็จ');
			
			$state.go('dashboard.register.search', {
				'currentPage': $scope.formData.currentPage,
				'itemsPerPage': $scope.formData.itemsPerPage,
				'firstname': $scope.formData.firstname,
				'isActive': $scope.formData.isActive
			});
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.print = function() {
		if($scope.isEdit) {
			console.log('print');
		} else {
			$scope.save();
		}
	}
	
	$scope.clear = function() {
		$scope.data = {regId: $scope.data.regId};
		$scope.data.authen = {status: 1};
		$scope.password = null;
		$scope.rePassword = null;
		
		$scope.imgUpload = null;
		$('#imgUpload').attr('src', null);
		
		focus();
	}
	
	function isCorrectPassword() {
		if($scope.password != $scope.rePassword) {
			$rootScope.systemAlert(-1, ' ', 'รหัสผ่านไม่เหมือนกัน');
			$scope.password = null;
			$scope.rePassword = null;
			$("input[name='password']").focus();
			return false;
		} else {
			return true;
		}
	}
	
	//------------------------------: Calendar :------------------------------------
	$scope.openBirthday = function($event) {
	    $event.preventDefault();
	    $event.stopPropagation();

	    $scope.birthdayPicker = true;
	}
	$scope.openExpireDate = function($event) {
	    $event.preventDefault();
	    $event.stopPropagation();

	    $scope.expireDatePicker = true;
	}
	//------------------------------------------------------------------
	
	
	$scope.preview = function(element) {		
		isChangedImg = true;
		
		if (element.files && element.files[0]) {
			$scope.currentFile = element.files[0];
			var reader = new FileReader();
	
			reader.onload = function(event) {
				$scope.$parent.imageSource = event.target.result;	
			}
			// when the file is read it triggers the onload event above.
			reader.readAsDataURL(element.files[0]);
			$scope.$apply();
		} else {
			$scope.imgUpload = null;
			$('#imgUpload').attr('src', null);
		}	
	}
	
	function focus() {
		$("input[name='prefixName']").focus();
	} 
	
});