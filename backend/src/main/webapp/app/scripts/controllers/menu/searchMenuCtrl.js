angular.module('sbAdminApp').controller('SearchMenuCtrl', function($rootScope, $scope, $state, $http, $window, $stateParams, $window, $base64, $translate, $log, toaster, urlPrefix, loadAllMenu) {
	
	$log.log(loadAllMenu);
	$scope.$parent.iconBtn = 'fa-plus-square';
	$scope.$parent.url = 'add';
	$scope.menus = loadAllMenu.menus;
	$scope.totalItems = loadAllMenu.totalItems;
	
	$translate('menu.header_panel_search').then(function (msg) {
		$scope.$parent.headerTitle = msg;
	});
	
	$scope.getImage = function(id) {
		$http.get(urlPrefix + '/restAct/menu/getImage?id=' + id).then(function(data){
			if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}
			
			$scope.imgBase64 = data.data.imgBase64;
			$scope.imgName = data.data.imgName;
			$scope.imgType = data.data.imgType;
			
			$('#myModal').modal();
		}, function(response) {
			$rootScope.systemAlert(response.status);
	    });
	}
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/menu/searchMenu', {
			name: $scope.formData.name,
			status: $scope.formData.status,
			isRecommented: $scope.formData.isRecommented,
			currentPage: $scope.formData.currentPage,
	    	itemsPerPage: $scope.itemsPerPage
		}).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.menus = data.data.menus;
			$scope.totalItems = data.data.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clearSearchForm = function() {
		$scope.formData.name = null;
		$scope.formData.status = null;
		$scope.formData.isRecommented = null;
		$scope.search();
	}
	
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	
	
});