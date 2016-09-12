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
	        	console.log('header');
	        	
	        	if($rootScope.authority != 'ROLE_SUPERADMIN' && $rootScope.authority != 'ROLE_MANAGER') {
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
	        			
	        			if(!$rootScope.setting) $rootScope.setting = {};
	        				
	        			$scope.currentProduct = id;
	        			$rootScope.setting.currentProduct = $scope.currentProduct;
	        			
	        			$state.go('dashboard.home');
	        		}, function(response) {
	        			$rootScope.systemAlert(response.status);
	        		});
	        	}
	        	
	        	if($rootScope.setting && $rootScope.setting.currentProduct) {
	        		if($scope.productsSelect.length == 1) {
		        		if($scope.productsSelect[0].id != $rootScope.setting.currentProduct) {
		        			$scope.changeProduct($scope.productsSelect[0].id);
		        			return;
		        		}
	        		}
	        		
	        		$scope.currentProduct = $rootScope.setting.currentProduct;
	        	} else {
	        		if($scope.productsSelect && $scope.productsSelect.length > 0) {
	        			$scope.changeProduct($scope.productsSelect[0].id);	        			
	        		}
	        	}
	        }
    	}
	});


