angular.module('sbAdminApp').controller('RenewalCtrl', function($rootScope, $scope, $state, $base64, $http, $translate, $filter, urlPrefix, loadRegistered) {
	
	$scope.datas = loadRegistered.registereds;
	$scope.totalItems = loadRegistered.totalItems;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.popup = {};
	$scope.data = {};
	$scope.format = "dd/MM/yyyy";
	
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
	
	$scope.renewRegister = function() {
		
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
			
			$scope.popup.name = obj.firstname + ' ' + obj.lastname;
			$scope.popup.registerDate = obj.registerDate;
			$scope.popup.expireDate = new Date(obj.expireDate);
			$scope.popup.expireDate.setHours(00,00,00);
			$scope.popup.period = obj.period;
			$scope.popup.status = obj.status;
			$scope.data.regId = obj.regId;
			$scope.data.memberTypeId = obj.memberTypeId;
			
			if($scope.popup.expireDate.getTime() > $scope.todayDate.getTime()) {
				$scope.todayDate = angular.copy($scope.popup.expireDate);
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