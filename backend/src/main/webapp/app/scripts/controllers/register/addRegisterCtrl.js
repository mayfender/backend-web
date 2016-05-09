angular.module('sbAdminApp').controller('AddRegisterCtrl', function($rootScope, $stateParams, $scope, $state, $base64, $http, $translate, $filter, urlPrefix, roles_customer, loadData) {
	
	$scope.format = "dd/MM/yyyy";
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	$scope.isEdit = false;
	$scope.rolesConstant = roles_customer;
	$scope.memberTypes = loadData.memberTyps;
	$scope.prefixNames = loadData.namingDetails;
	$scope.todayDate = new Date(loadData.todayDate);
	$scope.selectpageObj = {};
	
	var isChangedImg = false;
	focus();
	
	if($stateParams.regId) {
		$scope.data = loadData.registration;
		$scope.zipcodes = loadData.zipcodes;
		
		var zipcodeDummy = $scope.data.zipcode;
		$scope.zipcodes.length == 1 ? $scope.districtName = zipcodeDummy.district.districtName : $scope.districtName = null; 
		$scope.zipcode = zipcodeDummy.zipcode;
		$scope.data.addressId = zipcodeDummy.id;
		$scope.amphur = zipcodeDummy.district.amphur.amphurName;
		$scope.province = zipcodeDummy.district.province.provinceName;
		
		if($scope.data.birthday) {			
			$scope.birthday = new Date($scope.data.birthday);
			$scope.birthday.setFullYear($scope.birthday.getFullYear() + 543); 
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
		delete $scope.data['zipcode'];
		
		/*var memberType = $scope.memberTypes.filter(function( obj ) {
			return obj.memberTypeId == $scope.data.memberTypeId;
		})[0];
		if(memberType) {
			$scope.memberPrice = $filter('number')(memberType.memberPrice, 2);				
		}else{
			$scope.data.memberTypeId = "";
		}*/
		
		$scope.persisBtn = 'บันทึก';		
		$scope.$parent.headerTitle = 'แก้ใขข้อมูลสมาชิก     [เลขที่สมาชิก: ' + $scope.memberId + ']';
		$scope.isEdit = true;
		$scope.isPassRequired = false;
	} else {
		$scope.$parent.headerTitle = 'ลงทะเบียนสมาชิก';
		$scope.persisBtn = 'บันทึก';		
		$scope.data = {authen:{status:1, authority: 'ROLE_MEMBER'}, registerDate: $scope.todayDate, prefixName: {}, payType: 1};
		$scope.$parent.imageSource = null;
		$scope.isPassRequired = true;
		$scope.districtName = ' ';
	}
	
	$scope.save = function(mode) {
		var result = isCorrectPassword();
		if(!result) return;
		
		$scope.data.authen.password = $base64.encode($scope.password);
		$scope.data.imgContent = $scope.imgUpload && $scope.imgUpload.base64;
		$scope.data.imgName = $scope.imgUpload && $scope.imgUpload.filename;
		
		if($scope.birthday) {			
			$scope.data.birthday = angular.copy($scope.birthday);
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
			
			if(mode == 1) {
				callPrint(data.data.regId);
			}
			
			$scope.selectpageObj.msg = 'บันทึกข้อมูลสำเร็จ';
			$scope.selectpageObj.showModal(1);
			
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.update = function(mode) {
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
		
		if($scope.birthday) {			
			$scope.data.birthday = angular.copy($scope.birthday);
			$scope.data.birthday.setFullYear($scope.data.birthday.getFullYear() - 543); 
		}
		
		$http.post(urlPrefix + '/restAct/registration/updateRegistration',
			$scope.data
		).then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			if(mode == 1) {
				callPrint($scope.data.regId);
			} else {
				$scope.selectpageObj.msg = 'แก้ใขข้อมูลสำเร็จ';
				$scope.selectpageObj.showModal(2);				
			}
			
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.selectpageObj.callback = function(i, r) {
		if(i == 1) { // Add
			if(r == 1) {
				$scope.formData.firstname = null;
				$scope.formData.isActive = null;
				$scope.formData.currentPage = 1;
				
				$state.go('dashboard.register.search', {
					'firstname': $scope.formData.firstname, 
					'isActive': $scope.formData.isActive,
					'currentPage': $scope.formData.currentPage,
					'itemsPerPage': $scope.formData.itemsPerPage
				});
			}else{
				$scope.clear();
				$scope.$apply();
			}
		} else if(i == 2) { // Update
			if(r == 1) {
				$state.go('dashboard.register.search', {
					'currentPage': $scope.formData.currentPage,
					'itemsPerPage': $scope.formData.itemsPerPage,
					'firstname': $scope.formData.firstname,
					'isActive': $scope.formData.isActive
				});
			}
		}
	}
	
	//-------------------------------------: Address Interactive :------------------------------------------
	$scope.zipCodeChanged = function() {
		var c = angular.element("input[name='zipcode']").val();
		if(c.length == 5) {
			$http.get(urlPrefix + '/restAct/address/findByZipcode?zipcode=' + c).then(function(data) {			
				var result = data.data;
				if(result.statusCode != 9999) {			
					$rootScope.systemAlert(data.data.statusCode);
					return;
				}
				
				$scope.zipcodes = result.zipcodes;
				
				if($scope.zipcodes.length == 1) {
					$scope.data.addressId = $scope.zipcodes[0].id;
					
					$scope.districtName = $scope.zipcodes[0].district.districtName;
					$scope.amphur = $scope.zipcodes[0].district.amphur.amphurName;
					$scope.province = $scope.zipcodes[0].district.province.provinceName;
				} else {
					$scope.data.addressId = null;
					$scope.districtName = null;
					$scope.amphur = null;
					$scope.province = null;
				}
				
			}, function(response) {
				$rootScope.systemAlert(response.status);
			});
		} else {
			$scope.districts = null;
		}
	}
	
	$scope.selectedDistrict = function() {
		var selectedAddress = $scope.zipcodes.filter(function( obj ) {
			return obj.id == $scope.data.addressId;
		})[0];
		
		if(selectedAddress) {			
			$scope.amphur = selectedAddress.district.amphur.amphurName;
			$scope.province = selectedAddress.district.province.provinceName;
		}else{
			$scope.amphur = null;
			$scope.province = null;
		}
	}
	//-------------------------------------------------------------------------------
	
	function callPrint(id) {
		$http.get(urlPrefix + '/restAct/fileServer/getFileById?id=' + id + '&type=1', {responseType: 'arraybuffer'}).then(function(data) {			
			var file = new Blob([data.data], {type: 'application/pdf'});
	        var fileURL = URL.createObjectURL(file);
	        window.open(fileURL);
	        window.URL.revokeObjectURL(fileURL);  //-- Clear blob on client
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.printRegisterForm = function() {
		$http.get(urlPrefix + '/restAct/fileServer/getFileById?id=' + $scope.data.regId + '&type=2', {responseType: 'arraybuffer'}).then(function(data) {			
			var file = new Blob([data.data], {type: 'application/pdf'});
	        var fileURL = URL.createObjectURL(file);
	        window.open(fileURL);
	        window.URL.revokeObjectURL(fileURL);  //-- Clear blob on client
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.print = function() {
		if(!$scope.isEdit) {
			$scope.save(1);
		} else {
			$scope.update(1);
		}
	}
	
	$scope.clear = function() {
//		$scope.data = {regId: $scope.data.regId};
//		$scope.password = null;
//		$scope.rePassword = null;
		
		$scope.data.authen = {status: 1, authority: 'ROLE_MEMBER'};
		$scope.data.prefixName.namingDetId = null;
		$scope.data.firstname = null;
		$scope.data.lastname = null;
		$scope.data.firstnameEng = null;
		$scope.data.lastnameEng = null;
		$scope.data.citizenId = null;
		$scope.data.fingerId = null;
		$scope.data.memberTypeId = null;
		$scope.data.registerDate = $scope.todayDate;
		$scope.data.expireDate = null;
		$scope.data.price = null;
		$scope.data.payType = 1;
		$scope.data.conTelNo = null;
		$scope.data.conMobileNo1 = null;
		$scope.data.conMobileNo2 = null;
		$scope.data.conMobileNo3 = null;
		$scope.data.conEmail = null;
		$scope.data.conLineId = null;
		$scope.data.conFacebook = null;
		$scope.data.conAddress = null;
		
		$scope.birthday = null;
		$scope.zipcode = null;
		$scope.amphur = null;
		$scope.province = null;
		$scope.districtName = ' ';
		
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
	
	$scope.registerDateChange = function() {
		$scope.selectedMemType();
	}
	
	$scope.selectedMemType = function() {
		var memberType = $scope.memberTypes.filter(function( obj ) {
			return obj.memberTypeId == $scope.data.memberTypeId;
		})[0];
		
		if(memberType) {
			$scope.data.expireDate = angular.copy($scope.data.registerDate);
//			$scope.memberPrice = $filter('number')(memberType.memberPrice, 2);
			$scope.memberPrice = memberType.memberPrice;
			
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
		
		$scope.data.price = $scope.memberPrice;
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