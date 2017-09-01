'use strict';

/**
 * @ngdoc directive
 * @name izzyposWebApp.directive:adminPosHeader
 * @description
 * # adminPosHeader
 */
angular.module('sbAdminApp')
	.directive('myDateparser',function($dateParser){
		
		return {
	        require: 'ngModel',
	        scope: {
	            modelValue: '=ngModel'
	        },
	        link: function (scope, element, attrs, ngModel, controller) {
	        	
//	        	scope.viewValue = 'test';
//	        	ngModel.$render(); 
//	        	scope.modelValue = $dateParser(new Date(scope.modelValue), "dd/MM/yyyy");
//	        	console.log('test');
//	        	console.log(angular.element(element).val() + ' --');
//	        	console.log($dateParser(new Date(scope.modelValue), "dd/MM/yyyy"));
		    }
	    };
	});


