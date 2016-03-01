angular.module('sbAdminApp').controller('SearchReceiptCtrl', function($rootScope, $scope, $http, $state, $translate, loadServiceData, urlPrefix, roles) {	
	
	$scope.data = loadServiceData.serviceDatas;	
	$scope.totalItems = loadServiceData.totalItems;
	$scope.maxSize = 5;
	
	$scope.$parent.isShowUpdateBtn = true;
	$scope.$parent.headerTitle = 'แสดง' + $state.params.txt;
	$scope.$parent.iconBtn = 'fa-plus-square';
	$scope.$parent.url = 'add';
	
	if($state.params.type == 1) {
		
	} else if($state.params.type == 2) {
		
	} else if($state.params.type == 3) {
		
	} else if($state.params.type == 4) {
		
	} else if($state.params.type == 5) {
		
	}
	
	
	
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/serviceData/findServiceData', {
			serviceTypeId: $state.params.serviceTypeId,
			currentPage: $scope.formData.currentPage,
	    	itemsPerPage: $scope.itemsPerPage
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
	
	$scope.pageChanged = function() {
		console.log('55');
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	
});
