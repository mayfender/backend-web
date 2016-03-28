angular.module('sbAdminApp').controller('SearchReceiptCtrl', function($rootScope, $scope, $http, $state, $translate, loadServiceData, urlPrefix, roles) {	
	
	$scope.data = loadServiceData.serviceDatas;	
	$scope.totalItems = loadServiceData.totalItems;
	$scope.maxSize = 5;
	
	$scope.$parent.isShowUpdateBtn = true;
	$scope.$parent.headerTitle = 'แสดง' + $state.params.txt;
	$scope.$parent.iconBtn = 'fa-plus-square';
	$scope.$parent.url = 'add';
	
	
	$scope.openStart = function($event) {
	    $event.preventDefault();
	    $event.stopPropagation();

	    $scope.formData.openedStart = true;
	}
	$scope.openEnd = function($event) {
	    $event.preventDefault();
	    $event.stopPropagation();

	    $scope.formData.openedEnd = true;
	}
	
	$scope.print = function(id) {
		$http.get(urlPrefix + '/restAct/serviceData/print?id=' + id).then(function(data) {
			if(data.data.statusCode != 9999) {
				if(data.data.statusCode == 5000) {
					$rootScope.systemAlert(data.data.statusCode, 'กรุณาตรวจสอบ printer');
				} else if(data.data.statusCode == 5001) {
					$rootScope.systemAlert(data.data.statusCode, 'ไม่พบ printer กรุณาตรวจสอบ');
				} else {
					$rootScope.systemAlert(data.data.statusCode);					
				}
				return;
			}
			
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}

	$scope.search = function() {
		
		$http.post(urlPrefix + '/restAct/serviceData/findServiceData', {
			serviceTypeId: $state.params.serviceTypeId,
			currentPage: $scope.formData.currentPage,
	    	itemsPerPage: $scope.itemsPerPage,
	    	docNo: $scope.formData.docNo,
	    	dateTimeStart: $scope.formData.dateTimeStart,
	    	dateTimeEnd: $scope.formData.dateTimeEnd && $scope.formData.dateTimeEnd.setHours(23, 59, 59),
	    	status: $scope.formData.status
		}).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.data = data.data.serviceDatas;
			$scope.totalItems = data.data.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.editItem = function(id) {
		$state.go('dashboard.receipt.add', {id: id});
	}
	
	$scope.deleteItem = function(id) {
		var deleteUser = confirm('ยืนยันการลบข้อมูล');
	    if(!deleteUser) return;
	    
		$http.post(urlPrefix + '/restAct/serviceData/delete', {
			id: id,
			serviceTypeId: $state.params.serviceTypeId,
			currentPage: $scope.formData.currentPage,
	    	itemsPerPage: $scope.itemsPerPage,
	    	docNo: $scope.formData.docNo,
	    	dateTimeStart: $scope.formData.dateTimeStart,
	    	dateTimeEnd: $scope.formData.dateTimeEnd && $scope.formData.dateTimeEnd.setHours(23, 59, 59),
	    	status: $scope.formData.status
		}).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'ลบข้อมูลสำเร็จ');
			$scope.data = data.data.serviceDatas;
			$scope.totalItems = data.data.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.pageChanged = function() {
		console.log('test');
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	
	$scope.clearSearchForm = function() {
		$scope.formData.dateTimeStart = null;
		$scope.formData.dateTimeEnd = null;
		$scope.formData.docNo = null;
		$scope.formData.status = null;
		
		$scope.search();
	}
	
});
