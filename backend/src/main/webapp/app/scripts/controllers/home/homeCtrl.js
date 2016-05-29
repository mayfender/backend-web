angular.module('sbAdminApp').controller('HomeCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, urlPrefix) {
	
	console.log("Home page");
	
	console.log($localStorage.products);
	
	$scope.products = $localStorage.products;
	
});