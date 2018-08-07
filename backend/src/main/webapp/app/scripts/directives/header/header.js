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
    			$scope.chatting = {};
    			$scope.chatting.items = [
    			                {showname: 'akachai', firstName: 'เอกชัย สมคิด', msg: 'น่าเล่น อยากให้หมาว่สยน้ำเป็นจัง มันจะได้สนุก ไปเรียนว่ายนำดีกว่า', status: 1}, 
    			                {showname: 'Duangporn', firstName: 'ดวงพร', msg: 'สวัสครับ', status: 0},
    			                {showname: 'Krung', firstName: 'กรุงไทย มีผล', msg: 'ไม่อยู่', status: 1},
    			                {showname: 'Wannapha', firstName: 'วรรณภา มัสมัน', msg: 'ชมพู่เมื่อส่ง mail ไป แนบ file script ให้ช่วย run อีกตัว    run ได้เลยไม่ต้อง stop app', status: 0},
    			                {showname: 'Jompol', firstName: 'จุมพล', msg: 'ได้เลย', status: 1},
    			                {showname: 'Somsri', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri2', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri3', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri4', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri5', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri6', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri7', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri8', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri9', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri10', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri11', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri12', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri13', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri14', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri15', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri16', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri17', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri18', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri19', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri20', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri21', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri22', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri23', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri24', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri25', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri26', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri27', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri28', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri29', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri30', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri31', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri32', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri33', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri34', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri35', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri36', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri37', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri38', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri39', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri40', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri41', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri42', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri43', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri44', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri45', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri46', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri47', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri48', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri49', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri50', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri51', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri52', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri53', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri54', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri55', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri56', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri57', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri58', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri59', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri60', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri61', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri62', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri63', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri64', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri65', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri66', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri67', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri68', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri69', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri70', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri71', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri72', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri73', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri74', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri75', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri76', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri77', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri78', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    			                {showname: 'Somsri79', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1}
    			                ];
    			$scope.chatting.messages = [
    			                   {msg: 'สวัสดีครับ คุณ ศราวุธ', msgTime: '11:05', isMe: false},
    			                   {msg: 'สวัสดีครับ เป็นไงบ้างครับ', msgTime: '11:10', isMe: true},
    			                   {msg: 'ก็สบายดีครับ', msgTime: '11:20', isMe: false},
    			                   {msg: 'มีอะไรให้ช่วยมั้ยครับ', msgTime: '11:25', isMe: true},
    			                   {msg: 'ผมอยากจะทดลองใช้ระบบ DMS ครับ', msgTime: '11:30', isMe: false}
    			                   ];
    			
    			
    			 $scope.chatting.friendSource = {
    				get: function(index, count, callback) {
    					console.log(index + ' ' + count + ' '  + callback);
    					var start = index, end = index + count;
    					
    					if(index < 0) {
    						start = 0;
    					}
    					
    					var items = $scope.chatting.items.slice(start, end);
    					console.log('Length: '+ items.length);
    					
					    callback(items);
					}
    			 };
    			$scope.chatting.chkEnter = function(e) {
    				 if (e.ctrlKey && e.keyCode == 13) {
    					 $scope.chatting.sendMsg();
    				 }
    			}
    			$scope.chatting.changeTab = function(tab) {
    				if($scope.chatting.tab == tab) return;
    				
    				$scope.chatting.tab = tab;
    				if(tab == 1) {
    					$scope.chatting.items = [
    	    			                {showname: 'akachai', firstName: 'เอกชัย สมคิด', msg: 'น่าเล่น อยากให้หมาว่สยน้ำเป็นจัง มันจะได้สนุก ไปเรียนว่ายนำดีกว่า', status: 1}, 
    	    			                {showname: 'Duangporn', firstName: 'ดวงพร', msg: 'สวัสครับ', status: 0},
    	    			                {showname: 'Krung', firstName: 'กรุงไทย มีผล', msg: 'ไม่อยู่', status: 1},
    	    			                {showname: 'Wannapha', firstName: 'วรรณภา มัสมัน', msg: 'ชมพู่เมื่อส่ง mail ไป แนบ file script ให้ช่วย run อีกตัว    run ได้เลยไม่ต้อง stop app', status: 0},
    	    			                {showname: 'Jompol', firstName: 'จุมพล', msg: 'ได้เลย', status: 1},
    	    			                {showname: 'Somsri', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    	    			                {showname: 'Somsri2', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    	    			                {showname: 'Somsri3', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    	    			                {showname: 'Somsri4', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    	    			                {showname: 'Somsri5', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    	    			                {showname: 'Somsri6', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    	    			                {showname: 'Somsri7', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    	    			                {showname: 'Somsri8', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    	    			                {showname: 'Somsri9', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    	    			                {showname: 'Somsri10', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    	    			                {showname: 'Somsri11', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
    	    			                {showname: 'Somsri12', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1}
    	    			                ];
    				} else if(tab == 2) {		
    					$http.get(urlPrefix + '/restAct/chatting/getFriends?currentPage=1&itemsPerPage=50').then(function(data) {
        					var data = data.data;
        					if(data.statusCode != 9999) {
        		    			$rootScope.systemAlert(loadData.statusCode);
        		    			return;
        		    		}
        					console.log(data.friends);
        					$scope.chatting.items = data.friends;
        					$scope.chatting.adapter.reload(0);
        				}, function(response) {
        					console.log(response);
        				});
    				} else if(tab == 3) {
    					$scope.chatting.items = [
    	    			                {showname: 'Company Group (56)', firstName: 'PT Siam', msg: 'สบายดีมั้ย', status: 1}, 
    	    			                {showname: 'Port Group (15)', firstName: 'SCB', msg: 'สวัสครับ', status: 0}
    	    			                ];
    				}
    			}
    			$scope.chatting.sendMsg = function() {
    				if(!$scope.chatting.chatMsg) return;
    				
    				$scope.chatting.messages.push({msg: $scope.chatting.chatMsg, msgTime: $filter('date')(new Date(), 'HH:mm'), isMe: true});
    				$scope.chatting.chatMsg = null;
    				
    				var chtMsg = $('#chat-messages');
    				chtMsg.animate({scrollTop: chtMsg[0].scrollHeight}, 'slow');
    				$('#inputMsg').focus();
    			}
    			$scope.chatting.goChat = function(e, data) {
    				$scope.chatting.isChatPage = true;
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
		                'width': "60px",
		                'height': "68px",
		                'left':'108px',
		                'top':'20px'
		            }, 200);
		            
		            $("#profile p").html(data.showname);
		            $("#profile span").html(data.firstName + (data.lastName ? ' ' + data.lastName : ''));         
		            
		            $(".message").not(".right").find("img").attr("src", $(clone).attr("src"));                                  
		            $('#friendslist').fadeOut();
		            $('#chatview').fadeIn();
		            
		            $('#close').unbind("click").click(function(){
		            	$scope.$apply(function () {
		            		$scope.chatting.isChatPage = false;		            		
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
		            $('#inputMsg').focus();
    			} // end goChat
    			
    				
	        } // end Ctrl
    	}
	});