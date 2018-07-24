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
	        controller:function($rootScope, $window, $scope, $http, $state, $localStorage, $sce, urlPrefix){
	        	console.log('header');
	        	
	        	$scope.productsSelect = $rootScope.products;
	        	var isStamped = false;
	        	
	        	$scope.changeProduct = function(product) {
	        		
	        		if(product == null || $rootScope.workingOnProduct == product) return;
	        		
	        		$rootScope.workingOnProduct = product; 
	        		$state.go('dashboard.home');
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
							        
//							        accessTimeStamp();
							        
							        if ((lastMinuteVal != time) && (time % 59 == 0) && time != 0) {
							        	//--: Every 1 hour here.
							        	refreshClock(1);
							            lastMinuteVal = time;
							        }
								 }
							}
	        		});
	        	} else {
	        		var time;
	        		
	        		if($rootScope.workingTime < 0) {
	        			time = Math.abs($rootScope.workingTime);
	        		} else {
	        			time = $rootScope.workingTime;
	        		}
	        		
	        		clock = $('.clock').FlipClock(time, {
	        			countdown: true,
	        			callbacks: {
	    		        	stop: function() {
	    		        		if($rootScope.workingTime < 0) {
	    		        			setTimeout(function() { 
	    		        				refreshClock(2, true);
	    		        			}, 2000);
	    		        		} else {	    		        			
	    		        			$scope.$apply(function () {
	    		        	            $rootScope.isOutOfWorkingTime = true;
	    		        	        });
	    		        		}
	    		        	},
	    		        	interval: function () {
        			            var time = this.factory.getTime().time;
        			            
//        			            accessTimeStamp();
        			            
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
	        		
	        		
	        		
	        		
    			function refreshClock(mode, isRestart) {
    				$http.post(urlPrefix + '/refreshClock', {'token': $localStorage.token[$rootScope.username]}).then(function(data) {
    					
    					var data = data.data;
    					$rootScope.serverDateTime = data.serverDateTime;
    					$rootScope.isOutOfWorkingTime = data.isOutOfWorkingTime;
    					
    					if(mode == 1) {
    						clock.setTime(new Date($rootScope.serverDateTime));
    					} else {
    						$rootScope.workingTime = data.workingTime;
    						var time;
    						
    						if($rootScope.workingTime < 0) {
    		        			time = Math.abs($rootScope.workingTime);
    		        		} else {
    		        			time = $rootScope.workingTime;
    		        		}
    						
    						clock.setTime(time);
    						if(isRestart) {
    							clock.start();
    						}
    					}
    				}, function(response) {
    					console.log(response);
    				});
    			}
        		
	        		
	        	
        			
    			/*----------------------------- Sip Phone -------------------------------*/
    			//--: Have to use localStorage instead of $localStorage because in the setting value using the localStorage in file CtxApp.js 
    			//--: ext. localStorage.setItem('ctxPhone', 'true');
    			
    			var isRegistered = localStorage.ctxPhone	
    			if(!isRegistered && $rootScope.phoneWsServer && $rootScope.phoneRealm && $rootScope.showname && $rootScope.phoneExt && $rootScope.phonePass) {
    				initPhone();
				} else {
					$scope.isPhoneHide = true;
				}
    			
    			function initPhone() {
    				var user = {
						    //  User Name
						    "User" : $rootScope.phoneExt,
						    //  Password
						    "Pass" : $rootScope.phonePass,
						    //  Auth Realm
						    "Realm"   : $rootScope.phoneRealm,
						    // Display Name
						    "Display" : $rootScope.showname,
						    // WebSocket URL
						    "WSServer"  : $rootScope.phoneWsServer
						};
			
					ctxApp(user);
    			}
    			$scope.resetPhone = function() {
    				initPhone();
    			}
    			/*----------------------------- Sip Phone -------------------------------*/
        		
    		
    			function accessTimeStamp() {
    				if(!$rootScope.group6) return;
    				
	            	var diffMs = Math.abs(new Date() - $rootScope.lastTimeAccess);
	            	var diffMins = Math.floor((diffMs/1000)/60);
	            	var timeLimited = 1;
	            	var params;
	            	
	            	if(diffMins == timeLimited && !isStamped) {
	            		console.log("Over time !!!");
	            		$rootScope.saveRestTimeOut({action: 'start', timeLimited: timeLimited});
	            		isStamped = true;
	            	} else if(isStamped && diffMins == 0) {
	            		console.log("Reactive again !!!");
	            		$rootScope.saveRestTimeOut({action: 'end'});
	            		isStamped = false;
	            	}
    			}
    			
    			$rootScope.saveRestTimeOut = function(params, callBack) {
    				params.productId = $rootScope.workingOnProduct.id;
            		params.userId = $rootScope.userId;
            		params.deviceId = $localStorage.deviceId;
            		
            		$http.post(urlPrefix + '/restAct/accessManagement/saveRestTimeOut', params, {ignoreUpdateLastTimeAccess: true}).then(function(data) {
    					
    					callBack && callBack();
    					
    				}, function(response) {
    					console.log(response);
    				});
    			}
    			
	        }
    	}
	});