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
			restrict: 'A',
			link: function($scope, $element, $attr) {
//				console.log($attr.ngModel);
				console.log($scope.data.paidDate);
		    }
	    };
		
		
		
		
		
		
		/*return {
	        require: 'ngModel',
	        scope: {
	            modelValue: '=ngModel'
	        },
	        link: function (scope, element, attrs, ngModel, controller) {
	        	console.log('test');*/
	        	
//	        	scope.viewValue = 'test';
//	        	ngModel.$render(); 
//	        	scope.modelValue = $dateParser(new Date(scope.modelValue), "dd/MM/yyyy");
//	        	console.log('test');
//	        	console.log(angular.element(element).val() + ' --');
//	        	console.log($dateParser(new Date(scope.modelValue), "dd/MM/yyyy"));
		  /*  }
	    };*/
	});


