angular.module('sbAdminApp').controller('RenewalCtrl', function($rootScope, $scope, $state, $base64, $http, $translate, $filter, urlPrefix, loadRegistered) {
	
	$scope.datas = loadRegistered.registereds;
	$scope.totalItems = loadRegistered.totalItems;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.popup = {};
	$scope.data = {};
	$scope.format = "dd/MM/yyyy";
	
	$('.datepicker').datepicker({
	    format: 'dd/mm/yyyy',
	    autoclose: true,
	    todayBtn: true,
	    todayHighlight: true,
	    language: 'th-en'
	});
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/registration/findRenewal',
			$scope.formData
		).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.datas = data.data.registereds;
			$scope.totalItems = data.data.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clearSearchForm = function() {
		$scope.formData.firstname = null;
		$scope.formData.isActive = null;
		$scope.search();
	}
	
	$scope.renewRegister = function(mode) {
		
		$scope.data.currentPage = $scope.formData.currentPage;
		$scope.data.itemsPerPage = $scope.formData.itemsPerPage;		
		$scope.data.firstname = $scope.formData.firstname;
		$scope.data.isActive = $scope.formData.isActive;
		
		$http.post(urlPrefix + '/restAct/renewal/renewal',
			$scope.data
		).then(function(data) {
			var data = data.data;
			
			if(data.statusCode != 9999) {
				$rootScope.systemAlert(data.statusCode);
				return;
			}
			
			$scope.datas = data.registereds;
			$scope.totalItems = data.totalItems;
			$scope.dismissModal();
			
			if(mode == 2) {
				callPrint($scope.data.regId);
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
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
	
	$scope.updateStatus = function(status) {
		
		$scope.data.currentPage = $scope.formData.currentPage;
		$scope.data.itemsPerPage = $scope.formData.itemsPerPage;		
		$scope.data.firstname = $scope.formData.firstname;
		$scope.data.isActive = $scope.formData.isActive;
		$scope.data.status = status;
		
		$http.post(urlPrefix + '/restAct/renewal/updateStatus', $scope.data).then(function(data) {
				var data = data.data;
				
				if(data.statusCode != 9999) {
					$rootScope.systemAlert(data.statusCode);
					return;
				}
				
				$scope.datas = data.registereds;
				$scope.totalItems = data.totalItems;
				$scope.dismissModal();
			}, function(response) {
				$rootScope.systemAlert(response.status);
			});
	}
	
	
	var myModal;
	var isDismissModal;
	$scope.renewPopup = function(obj) {
		$http.get(urlPrefix + '/restAct/renewal/prepareData').then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.memberTypes = data.data.memberTypes;
			$scope.todayDate = new Date(data.data.todayDate);
			$scope.todayDateOnly = angular.copy(data.data.todayDate);
			
			$scope.popup.name = obj.firstname + ' ' + obj.lastname;
//			$scope.popup.registerDate = obj.registerDate;
			$scope.popup.expireDate = new Date(obj.expireDate);
			$scope.popup.expireDate.setHours(00,00,00);
			$scope.popup.period = obj.period;
			$scope.popup.enabled = obj.enabled;
			$scope.data.regId = obj.regId;
			$scope.data.memberTypeId = obj.memberTypeId;
			$scope.data.payType = 1;
			
			if($scope.popup.expireDate.getTime() >= $scope.todayDate.getTime()) {
				var expireDateDummy = angular.copy($scope.popup.expireDate);
				$scope.data.registerDate = expireDateDummy.setDate(expireDateDummy.getDate() + 1);
				
				$scope.todayDate = new Date($scope.data.registerDate);
			} else {
				$scope.data.registerDate = $scope.todayDate;		
			}
			
			//--
			$scope.selectedMemType();
			
			if(!myModal) {
				myModal = $('#myModal').modal();			
				myModal.on('hide.bs.modal', function (e) {
					if(!isDismissModal) {
						return e.preventDefault();
					}
					isDismissModal = false;
				});
			} else {			
				myModal.modal('show');
			}	
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.dismissModal = function() {
		isDismissModal = true;
		myModal.modal('hide');
	}
	
	$scope.selectedMemType = function() {
		var memberType = $scope.memberTypes.filter(function( obj ) {
			return obj.memberTypeId == $scope.data.memberTypeId;
		})[0];
		
		if(memberType) {
			$scope.data.expireDate = angular.copy($scope.todayDate);
			$scope.data.price = memberType.memberPrice;
			
			if(memberType.durationType == 1) {
				
				$scope.data.expireDate.setDate($scope.data.expireDate.getDate() + memberType.durationQty);
				
			} else if(memberType.durationType == 2) {
				
				$scope.data.expireDate = $scope.data.expireDate.calcMYNoRollover(memberType.durationQty, memberType.durationType);			
				
			} else if(memberType.durationType == 3) {
				
				$scope.data.expireDate = $scope.data.expireDate.calcMYNoRollover(memberType.durationQty, memberType.durationType);
				
			}
			
			$('.datepicker').datepicker('update', $filter('date')($scope.data.expireDate, 'dd/MM/yyyy'));
		} else {
			$scope.data.expireDate = null;
			$scope.data.price = null;
			
			$('.datepicker').datepicker('update', $filter('date')($scope.todayDateOnly, 'dd/MM/yyyy'));
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
	
	
	$scope.openExpireDate = function($event) {
	    $event.preventDefault();
	    $event.stopPropagation();

	    $scope.expireDatePicker = true;
	}
	
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	
});