angular.module('sbAdminApp').controller('ImportFingerScanLogCtrl', function($rootScope, $scope, $state, $base64, $http, $translate, urlPrefix) {
	
	console.log('ImportFingerScanLogCtrl');
	
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
//	$scope.totalItems
	
});