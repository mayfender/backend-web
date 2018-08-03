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
	        controller:function($rootScope, $window, $scope, $http, $state, $filter, $timeout, $localStorage, $sce, urlPrefix){
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
    			$scope.items = [
    			                {name: 'akachai', fullName: 'เอกชัย สมคิด', msg: 'น่าเล่น อยากให้หมาว่สยน้ำเป็นจัง มันจะได้สนุก ไปเรียนว่ายนำดีกว่า', status: 1}, 
    			                {name: 'Duangporn', fullName: 'ดวงพร', msg: 'สวัสครับ', status: 0},
    			                {name: 'Krung', fullName: 'กรุงไทย มีผล', msg: 'ไม่อยู่', status: 1},
    			                {name: 'Wannapha', fullName: 'วรรณภา มัสมัน', msg: 'ชมพู่เมื่อส่ง mail ไป แนบ file script ให้ช่วย run อีกตัว    run ได้เลยไม่ต้อง stop app', status: 0},
    			                {name: 'Jompol', fullName: 'จุมพล', msg: 'ได้เลย', status: 1},
    			                {name: 'Somsri', fullName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1}
    			                ];
    			$scope.messages = [
    			                   {msg: 'สวัสดีครับ คุณ\nศราวุธ', msgTime: '11:05', isMe: false},
    			                   {msg: 'สวัสดีครับ เป็นไงบ้างครับ', msgTime: '11:10', isMe: true},
    			                   {msg: 'ก็สบายดีครับ', msgTime: '11:20', isMe: false},
    			                   {msg: 'มีอะไรให้ช่วยมั้ยครับ', msgTime: '11:25', isMe: true},
    			                   {msg: 'ผมอยากจะทดลองใช้ระบบ DMS ครับ', msgTime: '11:30', isMe: false}
    			                   ];
    			
    			$scope.chkEnter = function(e) {
    				 if (e.ctrlKey && e.keyCode == 13) {
    					 $scope.sendMsg();
    				 }
    			}
    			$scope.changeTab = function(tab) {
    				if($scope.tab == tab) return;
    				
    				$scope.tab = tab;
    				if(tab == 1) {
    					$scope.items = [
    	    			                {name: 'akachai', fullName: 'เอกชัย สมคิด', msg: 'น่าเล่น อยากให้หมาว่สยน้ำเป็นจัง มันจะได้สนุก ไปเรียนว่ายนำดีกว่า', status: 1}, 
    	    			                {name: 'Duangporn', fullName: 'ดวงพร', msg: 'สวัสครับ', status: 0},
    	    			                {name: 'Krung', fullName: 'กรุงไทย มีผล', msg: 'ไม่อยู่', status: 1},
    	    			                {name: 'Wannapha', fullName: 'วรรณภา มัสมัน', msg: 'ชมพู่เมื่อส่ง mail ไป แนบ file script ให้ช่วย run อีกตัว    run ได้เลยไม่ต้อง stop app', status: 0},
    	    			                {name: 'Jompol', fullName: 'จุมพล', msg: 'ได้เลย', status: 1},
    	    			                {name: 'Somsri', fullName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1}
    	    			                ];
    				} else if(tab == 2) {
    					$scope.items = [
    	    			                {name: 'akachai', fullName: 'เอกชัย สมคิด', msg: 'สบายดีมั้ย', status: 1}, 
    	    			                {name: 'Duangporn', fullName: 'ดวงพร', msg: 'สวัสครับ', status: 0},
    	    			                {name: 'Krung', fullName: 'กรุงไทย มีผล', msg: 'ไม่อยู่', status: 1},
    	    			                {name: 'Wannapha', fullName: 'วรรณภา มัสมัน', msg: 'ไม่มี', status: 0},
    	    			                {name: 'Jompol', fullName: 'จุมพล', msg: 'ได้เลย', status: 1},
    	    			                {name: 'Somsri', fullName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    	    			                {name: 'Jompol', fullName: 'จุมพล', msg: 'ไม่เอาอะไรแล้ว', status: 1},
    	    			                {name: 'Jomkhan', fullName: 'จอบขวัญ', msg: 'พรุ่งนี้ไม่มา', status: 1},
    	    			                {name: 'Samrit', fullName: 'สัมริด', msg: 'ขอกลับเร็ววันนี้', status: 1},
    	    			                {name: 'Komkrit', fullName: 'คมกริด', msg: 'จริงเลอ', status: 1},
    	    			                {name: 'Somrak', fullName: 'สมรัก', msg: 'จริงเลอ', status: 1},
    	    			                {name: 'Sman', fullName: 'สมาน', msg: 'จริงเลอ', status: 1},
    	    			                {name: 'Jitrapab', fullName: 'จิตรภาพ', msg: 'จริงเลอ', status: 1}
    	    			                ];
    				} else if(tab == 3) {
    					$scope.items = [
    	    			                {name: 'Company Group (56)', fullName: 'PT Siam', msg: 'สบายดีมั้ย', status: 1}, 
    	    			                {name: 'Port Group (15)', fullName: 'SCB', msg: 'สวัสครับ', status: 0}
    	    			                ];
    				}
    			}
    			$scope.sendMsg = function() {
    				if(!$scope.$$childTail.chatMsg) return;
    				
    				$scope.messages.push({msg: $scope.$$childTail.chatMsg, msgTime: $filter('date')(new Date(), 'HH:mm'), isMe: true});
    				$scope.$$childTail.chatMsg = null;
    				
    				var chtMsg = $('#chat-messages');
    				chtMsg.animate({scrollTop: chtMsg[0].scrollHeight}, 'slow');
    			}
    			$scope.goChat = function(e, data) {
    				$scope.isChatPage = true;
    				var el = $(e.currentTarget);
    				var childOffset = el.offset();
			        var parentOffset = el.parent().parent().offset();
			        var childTop = childOffset.top - parentOffset.top;
			        var clone = el.find('img').eq(0).clone();
			        var top = childTop + 12 + "px";
			        var chtMsg = $('#chat-messages');
			        
			        $(clone).css({'top': top}).addClass("floatingImg").appendTo("#chatbox");
			        
			        setTimeout(function(){$("#profile p").addClass("animate");$("#profile").addClass("animate");}, 100);
		        	setTimeout(function(){
		        		chtMsg.addClass("animate");
		                $('.cx, .cy').addClass('s1');
		                setTimeout(function(){$('.cx, .cy').addClass('s2');}, 100);
		                setTimeout(function(){$('.cx, .cy').addClass('s3');}, 200);         
		            }, 150);                                                        
		            
		            $('.floatingImg').animate({
		                'width': "68px",
		                'left':'108px',
		                'top':'20px'
		            }, 200);
		            
		            $("#profile p").html(data.name);
		            $("#profile span").html(data.fullName);         
		            
		            $(".message").not(".right").find("img").attr("src", $(clone).attr("src"));                                  
		            $('#friendslist').fadeOut();
		            $('#chatview').fadeIn();
		            
		            $('#close').unbind("click").click(function(){
		            	$scope.$apply(function () {
		            		$scope.isChatPage = false;		            		
		            	});
		            	
		                $("#chat-messages, #profile, #profile p").removeClass("animate");
		                $('.cx, .cy').removeClass("s1 s2 s3");
		                $('.floatingImg').animate({
		                    'width': "40px",
		                    'top': top,
		                    'left': '12px'
		                }, 200, function(){$('.floatingImg').remove()});                
		                
		                setTimeout(function(){
		                    $('#chatview').fadeOut();
		                    $('#friendslist').fadeIn();             
		                }, 50);
		            });
		            
		            chtMsg.animate({scrollTop: chtMsg[0].scrollHeight}, 'slow');
    			} // end goChat
    			
    				
	        } // end Ctrl
    	}
	});