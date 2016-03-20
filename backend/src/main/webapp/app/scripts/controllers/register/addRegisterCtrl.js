angular.module('sbAdminApp').controller('AddRegisterCtrl', function($rootScope, $stateParams, $scope, $state, $base64, $http, $translate, urlPrefix, roles_customer, loadData) {
	
	console.log(loadData);
	
	$scope.format = "dd-MM-yyyy";
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	$scope.isEdit = false;
	$scope.rolesConstant = roles_customer;
	$scope.memberTypes = loadData.memberTyps;
	var isChangedImg;
	focus();
	
	if($stateParams.regId) {
		$scope.data = loadData.registration;
		$scope.persisBtn = 'แก้ใข';		
		$scope.$parent.headerTitle = 'แก้ใขข้อมูลสมาชิก     [เลขที่สมาชิก: ' + $scope.data.memberId + ']';
		$scope.isEdit = true;
	} else {
		$scope.$parent.headerTitle = 'ลงทะเบียนสมาชิก';
		$scope.persisBtn = 'บันทึก';		
		$scope.data = {authen:{status:1}};
		$scope.$parent.imageSource = null;
	}
	
	$scope.save = function() {
		var result = confirmPassword();
		
		if(!result) {
			$rootScope.systemAlert(-1, ' ', 'รหัสผ่านไม่เหมือนกัน');
			$scope.password = null;
			$scope.rePassword = null;
			$("input[name='password']").focus();
			return;
		}
		
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
	
	/*$scope.update = function() {
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
	}*/
	
	$scope.clear = function() {
		$scope.data.prefixName = null;
		$scope.data.firstname = null;
		$scope.data.lastname = null;
		$scope.data.citizenId = null;
		$scope.data.fingerId = null;
		$scope.data.birthday = null;
		$scope.password = null;
		$scope.rePassword = null;
		$scope.data.memberTypeId = null;
		$scope.data.expireDate = null;
		$scope.data.conTelNo = null;
		$scope.data.conMobileNo = null;
		$scope.data.conEmail = null;
		$scope.data.conLineId = null;
		$scope.data.conFacebook = null;
		$scope.data.conAddress = null;
		$scope.data.authen.userName = null;
		$scope.data.authen.authority = null;
		$scope.data.authen.status = null;
		
		$scope.imgUpload = null;
		$('#imgUpload').attr('src', null);
		
		focus();
	}
	
	function confirmPassword() {
		return ($scope.password == $scope.rePassword);
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