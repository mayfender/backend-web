'use strict';

/**
 * @ngdoc directive
 * @name izzyposWebApp.directive:adminPosHeader
 * @description
 * # adminPosHeader
 */
angular.module('sbAdminApp')
.directive('sale',function() {
	return {
		templateUrl:'scripts/directives/sale/sale.html',
		restrict:'E',
		replace:true,
		scope: {
	    'model': '=',
	    'tablename': '@',
	    'cusname': '@',
	    'colour': '@',
	    'type':'@',
	    'goto':'@'
		}
		
	}
});
