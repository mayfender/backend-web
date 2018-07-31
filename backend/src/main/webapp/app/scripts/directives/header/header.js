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
	        controller:function($rootScope, $window, $scope, $http, $state, $timeout, $localStorage, $sce, urlPrefix){
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
    			
    			//--------------------------: Chatting :------------------------------
    			$scope.mayfender = function() {
    				console.log('test');
    				if(document.getElementById("chatbox").style.width == '30px') {
    					document.getElementById("chatbox").style.width = "290px";
    					
    				} else {    					
    					document.getElementById("chatbox").style.width = "30px";
    				}
    			}
    			
    			angular.element(document).ready(function () {
    				$timeout(function() {
	    				
    					var preloadbg = document.createElement("img");
    					preloadbg.src = "https://s3-us-west-2.amazonaws.com/s.cdpn.io/245657/timeline1.png";
	    				  
    					$("#searchfield").focus(function(){
    						if($(this).val() == "Search contacts..."){
    							$(this).val("");
	    				    }
    					});
    					$("#searchfield").focusout(function(){
    						if($(this).val() == ""){
    							$(this).val("Search contacts...");       
    						}
	    				});
	    				    
    					$("#sendmessage input").focus(function(){
    						if($(this).val() == "Send message..."){
    							$(this).val("");
    						}
	    				});
    					$("#sendmessage input").focusout(function(){
    						if($(this).val() == ""){
    							$(this).val("Send message...");
	    				    }
	    				});
	    				        
	    				    
    					$(".friend").each(function(){       
    						$(this).click(function(){
    							var childOffset = $(this).offset();
	    				        var parentOffset = $(this).parent().parent().offset();
	    				        var childTop = childOffset.top - parentOffset.top;
	    				        var clone = $(this).find('img').eq(0).clone();
	    				        var top = childTop+12+"px";
	    				            
	    				        $(clone).css({'top': top}).addClass("floatingImg").appendTo("#chatbox");                                    
	    				            
	    				        setTimeout(function(){$("#profile p").addClass("animate");$("#profile").addClass("animate");}, 100);
	    				        	setTimeout(function(){
	    				                $("#chat-messages").addClass("animate");
	    				                $('.cx, .cy').addClass('s1');
	    				                setTimeout(function(){$('.cx, .cy').addClass('s2');}, 100);
	    				                setTimeout(function(){$('.cx, .cy').addClass('s3');}, 200);         
	    				            }, 150);                                                        
	    				            
	    				            $('.floatingImg').animate({
	    				                'width': "68px",
	    				                'left':'108px',
	    				                'top':'20px'
	    				            }, 200);
	    				            
	    				            var name = $(this).find("p strong").html();
	    				            var email = $(this).find("p span").html();                                                      
	    				            $("#profile p").html(name);
	    				            $("#profile span").html(email);         
	    				            
	    				            $(".message").not(".right").find("img").attr("src", $(clone).attr("src"));                                  
	    				            $('#friendslist').fadeOut();
	    				            $('#chatview').fadeIn();
	    				        
	    				            
	    				            $('#close').unbind("click").click(function(){               
	    				                $("#chat-messages, #profile, #profile p").removeClass("animate");
	    				                $('.cx, .cy').removeClass("s1 s2 s3");
	    				                $('.floatingImg').animate({
	    				                    'width': "40px",
	    				                    'top':top,
	    				                    'left': '12px'
	    				                }, 200, function(){$('.floatingImg').remove()});                
	    				                
	    				                setTimeout(function(){
	    				                    $('#chatview').fadeOut();
	    				                    $('#friendslist').fadeIn();             
	    				                }, 50);
	    				            });
	    				        });
	    				    }); 
    					}, 1000); // end timeout
    		    });
    			
	        } // end Ctrl
    	}
	});