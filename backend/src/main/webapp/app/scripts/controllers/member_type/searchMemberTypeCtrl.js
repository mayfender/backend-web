angular.module('sbAdminApp').controller('SearchMemberTypeCtrl', function($rootScope, $scope, $state, $base64, $http, $translate, urlPrefix, loadMemberType) {
	
	
	$scope.datas = loadMemberType.memberTyps;
	$scope.$parent.headerTitle = 'แสดงประเภทสมาชิก';
	$scope.$parent.iconBtn = 'fa-plus-square';
	$scope.$parent.url = 'add';
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/memberType/findMemberType',
			$scope.$parent.formData
		).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.datas = data.data.memberTyps;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clearSearchForm = function() {
		$scope.$parent.formData.memberTypeName = null;
		$scope.$parent.formData.durationType = null;
		$scope.$parent.formData.isActive = null;
		$scope.search();
	}
	
	$scope.edit = function(data) {
		$state.go('dashboard.memberType.add', {data: data});
	}
	
	$scope.deleteItem = function(id) {
		
		var deleteUser = confirm('คุณต้องการลบข้อมูล ?');
	    if(!deleteUser) return;
	    
	    $scope.$parent.formData.memberTypeId = id;
		
		$http.post(urlPrefix + '/restAct/memberType/delete',
			$scope.$parent.formData
		).then(function(data) {
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}	    		
    		
    		$rootScope.systemAlert(data.data.statusCode, 'ลบข้อมูลสำเร็จ');
    		$scope.datas = data.data.memberTyps;
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	
});