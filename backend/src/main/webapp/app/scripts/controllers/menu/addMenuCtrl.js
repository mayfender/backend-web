angular.module('sbAdminApp').controller('AddMenuCtrl', function($rootScope, $scope, $state, $http, $window, $stateParams, $window, $base64, $translate, toaster, urlPrefix) {
	
	$scope.$parent.iconBtn = 'fa-long-arrow-left';
	$scope.$parent.url = 'search';
	var isEdit = true;
	
	if(!isEdit){
		$translate('menu.header_panel_add').then(function (msg) {
			$scope.$parent.headerTitle = msg;
		});		
	}else{		
		$translate('menu.header_panel_edit').then(function (msg) {
			$scope.$parent.headerTitle = msg;
		});
	}
	
});