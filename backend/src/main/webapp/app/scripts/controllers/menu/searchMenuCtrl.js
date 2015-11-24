angular.module('sbAdminApp').controller('SearchMenuCtrl', function($rootScope, $scope, $state, $http, $window, $stateParams, $window, $base64, $translate, toaster, urlPrefix) {
	
	$scope.$parent.iconBtn = 'fa-plus-square';
	$scope.$parent.url = 'add';
	$scope.menus = [{}, {}]
	
	$translate('menu.header_panel_search').then(function (msg) {
		$scope.$parent.headerTitle = msg;
	});
	
});