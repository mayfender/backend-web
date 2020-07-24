'use strict';

/**
 * @ngdoc directive
 * @name izzyposWebApp.directive:adminPosHeader
 * @description
 * # adminPosHeader
 */
angular.module('sbAdminApp')
	.directive('header',function(){
		return {
        templateUrl:'scripts/directives/header/header.html',
        restrict: 'E',
        replace: true,
        controller: function($rootScope, $window, $scope, $http, $state, $filter, $timeout, $localStorage, $sce, $q, urlPrefix){
        		console.log('header');
        	
	        	$scope.changeDealer = function(dealer) {
	        		
	        		if(dealer == null || $rootScope.workingOnDealer == dealer) return;
	        		
	        		$rootScope.workingOnDealer = dealer;
	        		$state.go("dashboard.home");
	        	}
        	}
    	}
	});


