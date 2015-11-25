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
			$('#myModal').modal();
		}, function(response) {
			$rootScope.systemAlert(response.status);
	    });
	}
	
	
	
	
});