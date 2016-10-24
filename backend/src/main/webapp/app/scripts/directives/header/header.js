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
	        	
	        	
	        	//----------------------: FlipClock :---------------------------------
	        	var clock;
	        	var lastMinuteVal;
	        	if($rootScope.workingTime == null) {
	        		clock = $('.clock').FlipClock(new Date($rootScope.serverDateTime), {
	        			clockFace: 'TwentyFourHourClock',
							callbacks: {
								 interval: function () {
							        var time = this.factory.getTime().time.getMinutes();
							        
							        if ((lastMinuteVal != time) && (time % 59 == 0) && time != 0) {
							        	//--: Every 1 hour here.
							        	refreshClock(1);
							            lastMinuteVal = time;
							        }
								 }
							}
	        		});
	        	} else {
	        		clock = $('.clock').FlipClock($rootScope.workingTime, {
	        			countdown: true,
	        			callbacks: {
	    		        	stop: function() {
	    		        		$localStorage.token = null;
	    		        		$state.go("login");
	    		        	},
	    		        	interval: function () {
        			            var time = this.factory.getTime().time;
        			            if ((time != 0) && (time % 3600 == 0)) {
        			            	//--: Every 1 hour here.
        			            	refreshClock(2);
        			            }
	        				 }
	    		        }
	        		});	        		
	        	}
	        	//----------------------: FlipClock :---------------------------------
	        	
	        	
	        	
	        	
	        	//----------------------------------------------------------------------------
	        	$scope.provinceId;
	        	$scope.amphureId;
	        	$scope.districtId;
	        	$scope.provinceOptions = [];
	        	$scope.amphureOptions = [];
	        	$scope.districtOptions = [];
	        	var selectizeProvince, selectizeAmphure, selectizeDistrict;
	        	var commonCfg = {
        			valueField: 'id',
	        		maxItems: 1,
	        	};
	        	
	        	$scope.provinceConfig = angular.copy(commonCfg)
	        	$scope.provinceConfig.placeholder = 'จังหวัด';
	        	$scope.provinceConfig.labelField = 'provinceName',
	        	$scope.provinceConfig.searchField = 'provinceName',
	        	$scope.provinceConfig.onInitialize = function(selectize){
        			selectizeProvince = selectize;
	        	};
        		$scope.provinceConfig.onChange = function(val){
	        		if(!val) {
	        			console.log('onChange val empty');
	        			selectizeProvince.clearOptions();
	        			selectizeAmphure.clearOptions();
        				selectizeDistrict.clearOptions();
	        			return;
	        		}
        			findData('amphure');
        			selectizeAmphure.open();
        		};
        		$scope.provinceConfig.onType = function(val){        		
        			if(!val) {
        				console.log('onType val empty');
        				selectizeProvince.clearOptions();
        				selectizeAmphure.clearOptions();
        				selectizeDistrict.clearOptions();
        				return;
        			}
        			findData('province', val);
        		};
        		
        		$scope.amphureConfig = angular.copy(commonCfg)
        		$scope.amphureConfig.placeholder = 'อำเภอ';
        		$scope.amphureConfig.labelField = 'amphurName',
	        	$scope.amphureConfig.searchField = 'amphurName',
	        	$scope.amphureConfig.onInitialize = function(selectize){
        			selectizeAmphure = selectize;
	        	};
        		$scope.amphureConfig.onChange = function(val){
        			if(!val) {
        				selectizeDistrict.clearOptions();
        				return;
        			}
        			findData('district');
        			selectizeDistrict.open();
        		};
        		
        		$scope.districtConfig = angular.copy(commonCfg)
        		$scope.districtConfig.placeholder = 'ตำบล';
        		$scope.districtConfig.valueField = 'districtCode',
        		$scope.districtConfig.labelField = 'districtName',
	        	$scope.districtConfig.searchField = 'districtName',
	        	$scope.districtConfig.onInitialize = function(selectize){
        			selectizeDistrict = selectize;
	        	};
        		$scope.districtConfig.onChange = function(val){
        			if(!val) return;
        			findData('zipcode');
        		};
        		
        		function findData(type, provinceName) {
        			var url;
        			
        			if(type == 'province') {
        				url = urlPrefix + '/restAct/thaiRegion/findProvince?provinceName=' + provinceName;
        			} else if(type == 'amphure') {
        				url = urlPrefix + '/restAct/thaiRegion/findAmphure?provinceId=' + $scope.provinceId;
        			} else if(type == 'district') {
        				url = urlPrefix + '/restAct/thaiRegion/findDistrict?provinceId=' + $scope.provinceId + '&amphureId=' + $scope.amphureId;
        			} else if(type == 'zipcode') {
        				url = urlPrefix + '/restAct/thaiRegion/findZipcode?districtCode=' + $scope.districtId;
        			}
        			
	        		$http.get(url).then(function(data) {
	        			var result = data.data;
	        			
	        			if(result.statusCode != 9999) {				
	        				$rootScope.systemAlert(result.statusCode);
	        				return;
	        			}
	        			
	        			if(type == 'province') {
	        				$scope.provinceOptions = result.provinces;
	        			} else if(type == 'amphure') {
	        				$scope.amphureOptions = result.amphures;
	        			} else if(type == 'district') {
	        				$scope.districtOptions = result.districts;
	        			} else if(type == 'zipcode') {
	        				$scope.zipcode = result.zipcode || 'ไม่พบข้อมูล';
	        			}
	        		}, function(response) {
	        			$rootScope.systemAlert(response.status);
	        		});
        		}
        		
	            //----------------------------------------------------------------------------
	        		
	        		
	        		
	        		
        			function refreshClock(mode) {
        				$http.post(urlPrefix + '/refreshClock', {'token': $localStorage.token}).then(function(data) {
        					
        					var data = data.data;
        					$rootScope.serverDateTime = data.serverDateTime;
        					
        					if(mode == 1) {
        						clock.setTime(new Date($rootScope.serverDateTime));
        					} else {
        						$rootScope.workingTime = data.workingTime;
        						clock.setTime($rootScope.workingTime);
        					}
        				}, function(response) {
        					console.log(response);
        				});
        			}
	        		
	        		
	        	
	        }
    	}
	});


