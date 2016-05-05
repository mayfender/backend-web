'use strict';

/**
 * @ngdoc directive
 * @name izzyposWebApp.directive:adminPosHeader
 * @description
 * # adminPosHeader
 */
angular.module('sbAdminApp')
	.directive('selectpage',function() {
    return {
        templateUrl:'scripts/directives/selectpage/selectpage.html',
        restrict: 'E',
        replace: true,
        scope: {control: '='},
        controller: function($scope, $attrs) {
        	
        	$scope.selectPageHolder = {};
        	var result;
        	$scope.control.showModal = function(i) {
        		$scope.selectPageHolder.i = i;
        		
      			if(!$scope.selectPageHolder.myModal) {      				
      				$scope.selectPageHolder.myModal = $('#myModal').modal();			
      				$scope.selectPageHolder.myModal.on('hide.bs.modal', function (e) {
      					if(!$scope.selectPageHolder.isDismissModal) {
      						return e.preventDefault();
      					}
      					$scope.selectPageHolder.isDismissModal = false;
      				});
      				$scope.selectPageHolder.myModal.on('hidden.bs.modal', function (e) {
      					$scope.control.callback($scope.selectPageHolder.i, result);
      				});
      			} else {			
      				$scope.selectPageHolder.myModal.modal('show');
      			}
        	}
      		
      	  	$scope.selectPageHolder.btn = function(r) {
      	  		result = r;
      	  		$scope.selectPageHolder.isDismissModal = true;
      	  		$scope.selectPageHolder.myModal.modal('hide');
      	  	}
        	
        }
    }
  });
