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
	        controller:function($scope, $localStorage){
	        	$scope.productsSelect = $localStorage.products;
	        	$scope.selectedProduct = $localStorage.products[0].id;
	        }
    	}
	});


