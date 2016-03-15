angular.module('sbAdminApp').controller('AddRegisterCtrl', function($rootScope, $stateParams, $scope, $state, $base64, $http, $translate, urlPrefix, roles_customer, loadData) {
	
	console.log(loadData);
	
	$scope.format = "dd-MM-yyyy";
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	$scope.isEdit = false;
	$scope.rolesConstant = roles_customer;
	$scope.memberTypes = loadData.memberTyps;
	var isChangedImg;
	
	if($stateParams.data) {
		$scope.persisBtn = 'แก้ใข';		
		$scope.$parent.headerTitle = 'แก้ใขข้อมูลสมาชิก';
		$scope.data = $stateParams.data;
		$scope.isEdit = true;
	} else {
		$scope.$parent.headerTitle = 'ลงทะเบียนสมาชิก';
		$scope.persisBtn = 'บันทึก';		
		$scope.data = {authen:{status:0}};
		$scope.$parent.imageSource = null;
	}
	
	$scope.save = function() {
		console.log($scope.data);
		
		$http.post(urlPrefix + '/restAct/registration/saveRegistration',
			$scope.data
		).then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'บันทึกข้อมูลสำเร็จ');
			/*$scope.formData.status = null;
			$scope.formData.durationType = null;
			$scope.formData.memberTypeName = null;
			
			$state.go('dashboard.register.search', {
				'status': $scope.formData.status, 
				'durationType': $scope.formData.durationType,
				'memberTypeName': $scope.formData.memberTypeName
			});*/
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
		$scope.data.memberTypeName = null;
		$scope.data.durationType = null;
		$scope.data.durationQty = null;
		$scope.data.memberPrice = null;
		$scope.data.status = 0;
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
	
});