'use strict';

/**
 * @ngdoc directive
 * @name izzyposWebApp.directive:adminPosHeader
 * @description
 * # adminPosHeader
 */
angular.module('sbAdminApp')
	.directive('header2',function(){
		return {
        templateUrl:'scripts/directives/header2/header.html',
        restrict: 'E',
        replace: true,
    	}
	});


