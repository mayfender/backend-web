'use strict';

/**
 * @ngdoc directive
 * @name izzyposWebApp.directive:adminPosHeader
 * @description
 * # adminPosHeader
 */

angular.module('sbAdminApp')
  .directive('selectPage',['$location',function() {
    return {
      templateUrl:'scripts/directives/selectPage/selectPage.html',
      restrict: 'E',
      replace: true,
      scope: {control: '='},
      link : function (scope, element, attrs) {
    	  console.log('selectPage init');
    	  $scope.mayfender.test = function() {
    		  console.log('Yea!!!!');
    	  }
      },
      controller:function($scope){
    	  
    	  console.log('selectPage');
    	  
    	  $scope.mayfender.test = function() {
    		  console.log('Yea!!!!');
    	  }
    	  
    	  /*$scope.selectPageModal = {};
    	  $scope.selectPageModal.showModal = function() {
    			if(!$scope.selectPageModal.myModal) {
    				$scope.selectPageModal.myModal = $('#myModal').modal();			
    				$scope.selectPageModal.myModal.on('hide.bs.modal', function (e) {
    					if(!$scope.selectPageModal.isDismissModal) {
    						return e.preventDefault();
    					}
    					$scope.selectPageModal.isDismissModal = false;
    				});
    			} else {			
    				$scope.selectPageModal.myModal.modal('show');
    			}
    	  }
    		
    	  $scope.selectPageModal.dismissModal = function() {
    		  $scope.selectPageModal.isDismissModal = true;
    		  $scope.selectPageModal.myModal.modal('hide');
    	  }*/
    	  
        
        
        
      }
    }
  }]);
