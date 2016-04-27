'use strict';

/**
 * @ngdoc directive
 * @name izzyposWebApp.directive:adminPosHeader
 * @description
 * # adminPosHeader
 */
angular.module('sbAdminApp')
.directive('money',function() {
	return {
		templateUrl:'scripts/directives/report/money.html',
		restrict:'E',
		replace:true,
		scope: {
	    'model': '=',
	    'dateValue': '@',
	    'moneyValue': '@',
	    'goto':'@'
		}
		
	}
});
