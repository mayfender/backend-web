'use strict';

/**
 * @ngdoc directive
 * @name izzyposWebApp.directive:adminPosHeader
 * @description
 * # adminPosHeader
 */
angular.module('sbAdminApp')
	.directive('headerNotification',function(){
		return {
	        templateUrl:'scripts/directives/header/header-notification/header-notification.html',
	        restrict: 'E',
	        replace: true,
	        controller:function($scope, $localStorage){
	        	
	        	$scope.group1 = ($scope.authority == 'ROLE_SUPERADMIN' || $scope.authority == 'ROLE_ADMIN');
	        	$scope.group2 = $scope.group1 || $scope.authority == 'ROLE_SUPERVISOR';
	        	$scope.group3 = $scope.group2 || $scope.authority == 'ROLE_USER';
	        	$scope.group4 = ($scope.authority == 'ROLE_SUPERVISOR' || $scope.authority == 'ROLE_USER');
	        	
	        }
    	}
	});


