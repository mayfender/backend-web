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
	        controller:function($rootScope, $scope, $http, $state, $localStorage, $sce, urlPrefix){
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
	        	
	        	
	        	
	        	
	        	
	        	
	        	
	        	
	        	//----------------------------------------------------------------------------
	        	$scope.provinceId;
	        	$scope.amphureId;
	        	$scope.districtId;
	        	$scope.provinceOptions = [];
	        	$scope.amphureOptions = [];
	        	$scope.districtOptions = [];
	        	var commonCfg = {
        			valueField: 'id',
	        		maxItems: 1,
	        	};
	        	
	        	$scope.provinceConfig = angular.copy(commonCfg)
	        	$scope.provinceConfig.placeholder = 'จังหวัด';
	        	$scope.provinceConfig.labelField = 'provinceName',
	        	$scope.provinceConfig.searchField = 'provinceName',
        		$scope.provinceConfig.onChange = function(val){
	        		if(!val) {
	        			console.log('onChange val empty');
	        			$scope.amphureOptions = [];
        				$scope.$apply();
	        			return;
	        		}
        			findAmphure(val)
        		};
        		$scope.provinceConfig.onType = function(val){        		
        			if(!val) {
        				console.log('onType val empty');
        				$scope.provinceOptions = [];
        				$scope.amphureOptions = [];
        				$scope.$apply();
        				return;
        			}
        			findProvince(val);
        		};
        		
        		$scope.amphureConfig = angular.copy(commonCfg)
        		$scope.amphureConfig.placeholder = 'อำเภอ';
        		$scope.amphureConfig.labelField = 'amphurName',
	        	$scope.amphureConfig.searchField = 'amphurName',
        		$scope.amphureConfig.onChange = function(val){
        			console.log(val);
        		};
        		
        		$scope.districtConfig = angular.copy(commonCfg)
        		$scope.districtConfig.placeholder = 'ตำบล';
        		$scope.districtConfig.onChange = function(val){
        			console.log(val);
        		};
        		
        		function findProvince(val) {
	        		$http.get(urlPrefix + '/restAct/thaiRegion/findProvince?provinceName='+val).then(function(data) {
	        			var result = data.data;
	        			
	        			if(result.statusCode != 9999) {				
	        				$rootScope.systemAlert(result.statusCode);
	        				return;
	        			}
	        			
	        			$scope.provinceOptions = result.provinces;
	        		}, function(response) {
	        			$rootScope.systemAlert(response.status);
	        		});
        		}
        		function findAmphure(val) {
	        		$http.get(urlPrefix + '/restAct/thaiRegion/findAmphure?provinceId='+val).then(function(data) {
	        			var result = data.data;
	        			
	        			if(result.statusCode != 9999) {				
	        				$rootScope.systemAlert(result.statusCode);
	        				return;
	        			}
	        			
	        			$scope.amphureOptions = result.amphures;
	        		}, function(response) {
	        			$rootScope.systemAlert(response.status);
	        		});
        		}
	            //----------------------------------------------------------------------------
	        		
	        		
	        		
	        		
	        		
	        		
	        		
	        	
	        }
    	}
	});


