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
	        controller:function($rootScope, $window, $scope, $http, $state, $filter, $timeout, $localStorage, $sce, $q, urlPrefix){
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
    			$scope.chatting = {keyword: ''};
    			$scope.chatting.groups = [
      	    			                {showname: 'Company Group (56)', firstName: 'PT Siam', msg: 'สบายดีมั้ย', status: 1}, 
      	    			                {showname: 'Port Group (15)', firstName: 'SCB', msg: 'สวัสครับ', status: 0}
      	    			                ];
    			/*$scope.chatting.items = [
     	    			                {showname: 'akachai', firstName: 'เอกชัย สมคิด', msg: 'น่าเล่น อยากให้หมาว่สยน้ำเป็นจัง มันจะได้สนุก ไปเรียนว่ายนำดีกว่า', status: 1}, 
     	    			                {showname: 'Duangporn', firstName: 'ดวงพร', msg: 'สวัสครับ', status: 0},
     	    			                {showname: 'Krung', firstName: 'กรุงไทย มีผล', msg: 'ไม่อยู่', status: 1},
     	    			                {showname: 'Wannapha', firstName: 'วรรณภา มัสมัน', msg: 'ชมพู่เมื่อส่ง mail ไป แนบ file script ให้ช่วย run อีกตัว    run ได้เลยไม่ต้อง stop app', status: 0},
     	    			                {showname: 'Jompol', firstName: 'จุมพล', msg: 'ได้เลย', status: 1},
     	    			                {showname: 'Somsri', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
     	    			                {showname: 'Somsri2', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
     	    			                {showname: 'Somsri3', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
     	    			                {showname: 'Somsri4', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1},
     	    			                {showname: 'Somsri5', firstName: 'สมศรี', msg: 'กลับก่อนนะ', status: 1}
     	    			                ];*/
    			$scope.chatting.messages = [
    			                   {msg: 'สวัสดีครับ คุณ ศราวุธ', msgTime: '11:05', isMe: false},
    			                   {msg: 'สวัสดีครับ เป็นไงบ้างครับ', msgTime: '11:10', isMe: true},
    			                   {msg: 'ก็สบายดีครับ', msgTime: '11:20', isMe: false},
    			                   {msg: 'มีอะไรให้ช่วยมั้ยครับ', msgTime: '11:25', isMe: true},
    			                   {msg: 'ผมอยากจะทดลองใช้ระบบ DMS ครับ', msgTime: '11:30', isMe: false}
    			                   ];
    			
    			function getLastChatFriend() {
    				var deferred = $q.defer();
    				var result = $http.post(urlPrefix + '/restAct/chatting/getLastChatFriend').then(function(data) {
    					var data = data.data;
    					if(data.statusCode != 9999) {
    		    			$rootScope.systemAlert(data.statusCode);
    		    		}
    					console.log(data);
    					return data.mapData;
		        	 }, function(response) {
    					console.log(response);
		        	 });
    				deferred.resolve(result);
    		        return deferred.promise;
    			}
    			$scope.chatting.getItems = function(index, count) {
    		        var deferred = $q.defer();
    		        var start = index;
    		        var end = index + count - 1;
		        	var item, result = [];
		        	
			        if (end > -1 && start <= end) {
			        	 start = ((start < 0 ? 0 : start) + 1);
			        	 var currentPage = 1;
			        	 
			        	 if(start > 1) {
			        		 currentPage = Math.ceil(start / count);
			        	 }
			        	 console.log('currentPage: ' + currentPage);
			        	 result = $http.get(urlPrefix + '/restAct/chatting/getFriends?currentPage=' + currentPage + '&itemsPerPage=' + count + '&keyword=' + $scope.chatting.keyword).then(function(data) {
        					var data = data.data;
        					if(data.statusCode != 9999) {
        		    			$rootScope.systemAlert(data.statusCode);
        		    			return [];
        		    		}
        					return data.friends;
			        	 }, function(response) {
        					console.log(response);
			        	 });
			        }
			        deferred.resolve(result);
    		        return deferred.promise;
    			}
    			$scope.chatting.friendSource = {
    				get: function(descriptor, callback) {
    					var index = descriptor.index;
    					var count = descriptor.count;
    					console.log('index: ' + index +', count: ' + count);
    					
    					/*
    					 * Chat History
    					var start = index;
    					var end = Math.min(index + count - 1, 1);
    					console.log('start: ' + start +', end: ' + end);
    					
    					var result = [];
    					if (start <= end) {
    						for (var i = start; i <= end; i++) {
    							var serverDataIndex = (-1) * i + 1;
    							console.log(serverDataIndex);
    							var item = $scope.chatting.items[serverDataIndex];
    							console.log(item);
    							if (item) {
    								result.push(item);
    							}
    						}
    				        callback(result);
    					}*/
    					
				        
    					if($scope.chatting.tab == 1 || $scope.chatting.tab == 3) {
    						var start = index;
    						var end = index + count - 1;
    						var item, items, result = [];
    						if (end > -1 && start <= end) {
    							if($scope.chatting.keyword) {
    								if($scope.chatting.tab == 1) {
    									items = $filter('filter')($scope.chatting.items, filterOr);    									
    								} else {
    									items = $filter('filter')($scope.chatting.groups, filterOr);
    								}
    							} else {
    								if($scope.chatting.tab == 1) {
    									if(!$scope.chatting.items) {
	    									getLastChatFriend().then(function(result) {
	    										$scope.chatting.items = result;
	    		    							callback(result);
	    		    						}); 
    									} else {
    										items = $scope.chatting.items;    									    										
    									}
    								} else {
    									items = $scope.chatting.groups;
    								}
    							}
    							if(items) {
						        	for (var i = start; i <= end; i++) {
						        		if (item = items[i]) {
						        			result.push(item);
						        		}
						            }
    							}
    						}
				        	callback(result);
    					} else {
    						$scope.chatting.getItems(index, count).then(function(result) {
    							callback(result);
    						});    						
    					}
					}
    			 };
    			$scope.chatting.chkEnter = function(e) {
    				 if (e.ctrlKey && e.keyCode == 13) {
    					 $scope.chatting.sendMsg();
    				 }
    			}
    			$scope.chatting.changeTab = function(tab) {
    				if($scope.chatting.tab == tab) return;
    				$scope.chatting.keyword = '';
    				$scope.chatting.tab = tab;
    				$scope.chatting.adapter.reload(0);
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
    			function filterOr(val) {
    				return val.showname.toLowerCase().includes($scope.chatting.keyword.toLowerCase()) || val.firstName.toLowerCase().includes($scope.chatting.keyword.toLowerCase());
    			}
    			
    				
	        } // end Ctrl
    	}
	});