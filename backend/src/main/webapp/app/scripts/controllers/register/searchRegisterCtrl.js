angular.module('sbAdminApp').controller('SearchRegisterCtrl', function($rootScope, $scope, $state, $base64, $http, $translate, urlPrefix, loadRegistered) {
	
	$scope.datas = loadRegistered.registereds;
	$scope.totalItems = loadRegistered.totalItems;
	$scope.maxSize = 5;
	$scope.$parent.headerTitle = 'แสดงสมาชิก';
	$scope.$parent.iconBtn = 'fa-plus-square';
	$scope.$parent.url = 'add';
	
	$scope.exportRegistered = function() {
		$scope.$parent.formData.reportType = 3;
		
		$http.post(urlPrefix + '/restAct/fileServer/getFileByRegister', $scope.$parent.formData, {responseType: 'arraybuffer'}).then(function(data) {			
			var a = document.createElement("a");
			document.body.appendChild(a);
			a.style = "display: none";
			
			var file = new Blob([data.data], {type: 'application/vnd.ms-excel'});
	        var url = URL.createObjectURL(file);
	        
	        a.href = url;
	        a.download = 'registration.xlsx';
	        a.click();
	        a.remove();
	        
	        window.URL.revokeObjectURL(url); //-- Clear blob on client
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/registration/findRegistered',
				$scope.$parent.formData
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
		$scope.$parent.formData.firstname = null;
		$scope.$parent.formData.isActive = null;
		$scope.$parent.formData.memberId = null;
		$scope.$parent.formData.expireDate = null;
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.edit = function(regId) {
		$state.go('dashboard.register.add', {regId: regId});
	}
	
	$scope.deleteItem = function(id) {
		
		var deleteUser = confirm('คุณต้องการลบข้อมูล ?');
	    if(!deleteUser) return;
	    
	    $scope.$parent.formData.regId = id;
		
		$http.post(urlPrefix + '/restAct/registration/deleteRegistration',
			$scope.$parent.formData
		).then(function(data) {
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}	    		
    		
    		$rootScope.systemAlert(data.data.statusCode, 'ลบข้อมูลสำเร็จ');
    		$scope.datas = data.data.registereds;
			$scope.totalItems = data.data.totalItems;
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	
});