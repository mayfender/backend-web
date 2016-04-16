angular.module('sbAdminApp').controller('AddRegisterCtrl', function($rootScope, $stateParams, $scope, $state, $base64, $http, $translate, $filter, urlPrefix, roles_customer, loadData) {
	
	console.log(loadData);
	
	$scope.format = "dd/MM/yyyy";
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	$scope.isEdit = false;
	$scope.rolesConstant = roles_customer;
	$scope.memberTypes = loadData.memberTyps;
	$scope.prefixNames = loadData.namingDetails;
	
	$scope.todayDate = new Date(loadData.todayDate);
	
	var isChangedImg = false;
	$('.datepicker').datepicker();
	focus();
	
	if($stateParams.regId) {
		$scope.data = loadData.registration;
		
		if($scope.data.birthday) {			
			$scope.data.birthday = new Date($scope.data.birthday);
			$scope.data.birthday.setFullYear($scope.data.birthday.getFullYear() + 543); 
		}
		$scope.data.expireDate = new Date($scope.data.expireDate);
		$scope.data.registerDate = new Date($scope.data.registerDate);
		
		if($scope.data.imgBase64) {			
			$scope.$parent.imageSource = 'data:image/JPEG;base64,' + $scope.data.imgBase64;
			delete $scope.data['imgBase64'];
		} else {
			$scope.$parent.imageSource = null;
		}
		
		$scope.memberId = $scope.data.memberId;
		delete $scope.data['memberId'];
		
		var memberType = $scope.memberTypes.filter(function( obj ) {
			return obj.memberTypeId == $scope.data.memberTypeId;
		})[0];
		if(memberType) {
			$scope.memberPrice = $filter('number')(memberType.memberPrice, 2);				
		}else{
			$scope.data.memberTypeId = "";
		}
		
		$scope.persisBtn = 'แก้ใข';		
		$scope.$parent.headerTitle = 'แก้ใขข้อมูลสมาชิก     [เลขที่สมาชิก: ' + $scope.memberId + ']';
		$scope.isEdit = true;
		$scope.isPassRequired = false;
	} else {
		$scope.$parent.headerTitle = 'ลงทะเบียนสมาชิก';
		$scope.persisBtn = 'บันทึก';		
		$scope.data = {authen:{status:1}, registerDate: new Date(), prefixName: {}};
		$scope.$parent.imageSource = null;
		$scope.isPassRequired = true;
	}
	
	$scope.save = function() {
		
		var result = isCorrectPassword();
		if(!result) return;
		
		$scope.data.authen.password = $base64.encode($scope.password);
		$scope.data.imgContent = $scope.imgUpload && $scope.imgUpload.base64;
		$scope.data.imgName = $scope.imgUpload && $scope.imgUpload.filename;
		
		if($scope.data.birthday) {			
			$scope.data.birthday.setFullYear($scope.data.birthday.getFullYear() - 543); 
		}
		
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
		
		if($scope.data.birthday) {			
			$scope.data.birthday.setFullYear($scope.data.birthday.getFullYear() - 543); 
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
		/*if($scope.isEdit) {
			console.log('print');
		} else {
			$scope.save();
		}*/
	
		$http.get(urlPrefix + '/restAct/pdfExport/getRegistrationReceipt?id=1', {responseType: 'arraybuffer'}).then(function(data) {			
			var file = new Blob([data.data], {type: 'application/pdf'});
	        var fileURL = URL.createObjectURL(file);
	        window.open(fileURL);
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
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
	
	$scope.selectedMemType = function() {
		var memberType = $scope.memberTypes.filter(function( obj ) {
			return obj.memberTypeId == $scope.data.memberTypeId;
		})[0];
		
		if(memberType) {
			$scope.data.expireDate = angular.copy($scope.todayDate);
			$scope.memberPrice = $filter('number')(memberType.memberPrice, 2);
			
			if(memberType.durationType == 1) {
				
				$scope.data.expireDate.setDate($scope.data.expireDate.getDate() + memberType.durationQty);
				
			} else if(memberType.durationType == 2) {
				
				$scope.data.expireDate = $scope.data.expireDate.calcMYNoRollover(memberType.durationQty, memberType.durationType);			
				
			} else if(memberType.durationType == 3) {
				
				$scope.data.expireDate = $scope.data.expireDate.calcMYNoRollover(memberType.durationQty, memberType.durationType);
				
			}
		} else {
			$scope.data.expireDate = null;
			$scope.memberPrice = null;
		}
	}
	
	//------------------------------: Date Calculation :------------------------------------
	Date.prototype.calcMYNoRollover = function(offset, type){
		var dt = new Date(this);
		
		if(type == 2) {
			dt.setMonth(dt.getMonth() + offset) ;			
		} else if(type == 3) {
			dt.setFullYear(dt.getFullYear() + offset) ;
		}
		
		if (dt.getDate() < this.getDate()) { 
			dt.setDate(0); 
		}
		
		return dt;
	};
	
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
	$scope.openRegisterDate = function($event) {
	    $event.preventDefault();
	    $event.stopPropagation();

	    $scope.registerDatePicker = true;
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
		$("select[name='prefixName']").focus();
	} 
	
	$scope.checkCitizenId = function($event) {
		var c = angular.element("input[name='citizenId']").val();
		if(c.length == 13) {
			$("input[name='fingerId']").focus();			
		}
	}
	
	
});