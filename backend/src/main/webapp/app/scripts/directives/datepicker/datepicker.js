'use strict';

/**
 * @ngdoc directive
 * @name izzyposWebApp.directive:adminPosHeader
 * @description
 * # adminPosHeader
 */
angular.module('sbAdminApp')
	.directive('myDatepicker',function(){
		
		function link(scope, element, attrs, controller) {
			var options;
			
			scope.$watch(attrs.myDatepicker, function(value) {
				options = value;
				element.datepicker(options);
	        });
			
	    }
		
		return {
	        require: 'ngModel',
	        link: link
	    };
	});


