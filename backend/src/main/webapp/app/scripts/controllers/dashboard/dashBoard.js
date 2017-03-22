'use strict';
/**
 * @ngdoc function
 * @name sbAdminApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the sbAdminApp
 */
angular.module('sbAdminApp').controller('DashBoard', function($scope, $position, $stomp) {
	console.log('DashBoard');
	
	$scope.bar = {
		    labels: ['Duangporn', 'Wassana', 'somkiet', 'pagurasee', 'chonticha', 'natali'],
			series: ['Series A'],

			data: [
			   [65, 59, 12, 32, 44, 55]
			]
	    	
	    };
	
	
	
	/*$scope.line = {
		    labels: ['January', 'February', 'March', 'April', 'May', 'June', 'July'],
		    series: ['Series A', 'Series B'],
		    data: [
		      [65, 59, 80, 81, 56, 55, 40],
		      [28, 48, 40, 19, 86, 27, 90]
		    ],
		    onClick: function (points, evt) {
		      console.log(points, evt);
		    }
	    };*/
	
	
	
	
	$('.input-daterange input').each(function() {
	    $(this).datepicker({
	    	format: 'dd/mm/yyyy',
		    autoclose: true,
		    todayBtn: true,
		    clearBtn: true,
		    todayHighlight: true,
		    language: 'th-en'
	    });
	});
	
	
});
