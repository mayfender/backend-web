'use strict';

/**
 * @ngdoc directive
 * @name izzyposWebApp.directive:adminPosHeader
 * @description
 * # adminPosHeader
 */
angular.module('sbAdminApp')
.directive('tableland',function() {
	return {
		templateUrl:'scripts/directives/table_land/table.html',
		restrict:'E',
		replace:true,
		scope: {
	    'model': '=',
	    'tablename': '@',
	    'colour': '@',
	    'type':'@',
	    'goto':'@'
		}
		
	}
});
