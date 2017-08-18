'use strict';
angular.module('sbAdminApp').controller('Collector', function($rootScope, $scope, $http, $filter, $state, urlPrefix) {
	console.log('test collector');

	$scope.collectors = [{name: 'สมพร สังทอง', accountSum: 10000, balanceSum: 5000000}];
});
