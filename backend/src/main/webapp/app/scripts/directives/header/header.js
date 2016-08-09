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
	        controller:function($rootScope, $scope, $http, $state, $localStorage, urlPrefix){
	        	
	        	if($localStorage.authorities[0].authority != 'ROLE_SUPERADMIN') {
	        		$scope.productsSelect = $rootScope.products;	        		
	        	}
	        	
	        	$scope.changeProduct = function(id) {
	        		
	        		if(id == null || $scope.currentProduct == id) return;
	        		
	        		$http.post(urlPrefix + '/restAct/user/updateUserSetting', {
	        			username: $localStorage.username,
	        			currentProduct: id
	        		}).then(function(data) {
	        			if(data.data.statusCode != 9999) {				
	        				$rootScope.systemAlert(data.data.statusCode);
	        				return;
	        			}
	        			
	        			if(!$localStorage.setting) $localStorage.setting = {};
	        				
	        			$scope.currentProduct = id;
	        			$localStorage.setting.currentProduct = $scope.currentProduct;
	        			
	        			$state.go('dashboard.home');
	        		}, function(response) {
	        			$rootScope.systemAlert(response.status);
	        		});
	        	}
	        	
	        	if($localStorage.setting && $localStorage.setting.currentProduct) {
	        		if($scope.productsSelect.length == 1) {
		        		if($scope.productsSelect[0].id != $localStorage.setting.currentProduct) {
		        			$scope.changeProduct($scope.productsSelect[0].id);
		        			return;
		        		}
	        		}
	        		
	        		$scope.currentProduct = $localStorage.setting.currentProduct;
	        	} else {
	        		console.log('aa');
        			$scope.changeProduct($scope.productsSelect && $scope.productsSelect[0].id);
	        	}
	        }
    	}
	});


