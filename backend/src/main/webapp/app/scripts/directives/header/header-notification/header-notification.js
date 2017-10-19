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
	        controller:function($rootScope, $scope, $localStorage){
	        	console.log('headerNotification');
	        	
	        	$rootScope.group0 = $rootScope.authority == 'ROLE_SUPERADMIN';
	        	$rootScope.group1 = ($rootScope.group0 || $rootScope.authority == 'ROLE_ADMIN');
	        	$rootScope.group2 = $rootScope.group1 || $rootScope.authority == 'ROLE_SUPERVISOR';
	        	$rootScope.group3 = $rootScope.group2 || $rootScope.authority == 'ROLE_USER';
	        	$rootScope.group4 = ($rootScope.authority == 'ROLE_SUPERVISOR' || $rootScope.authority == 'ROLE_USER');
	        	$rootScope.group5 = $rootScope.authority == 'ROLE_MANAGER';
	        	$rootScope.group6 = $rootScope.authority == 'ROLE_USER';
	        	
	        	$rootScope.gb_timeFormat = 'HH:mm:ss';
	        	$rootScope.gb_dateFormat = 'dd/MM/yyyy';
	        	$rootScope.gb_dateTimeFormat = 'dd/MM/yyyy HH:mm:ss';
	        	
	        }
    	}
	});


