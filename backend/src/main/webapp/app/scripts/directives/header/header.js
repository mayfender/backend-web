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
    			$rootScope.jws = {chatting: {}};
    			$scope.chatting = {keyword: ''};
    			$scope.chatting.groups = [
      	    			                {showname: 'Company Group (56)', firstName: 'PT Siam', msg: 'สบายดีมั้ย', status: 1}, 
      	    			                {showname: 'Port Group (15)', firstName: 'SCB', msg: 'สวัสครับ', status: 1}
      	    			                ];
    			
    			function getLastChatFriend() {
    				var deferred = $q.defer();
    				var result = $http.get(urlPrefix + '/restAct/chatting/getLastChatFriend').then(function(data) {
    					var data = data.data;
    					if(data.statusCode != 9999) {
    		    			$rootScope.systemAlert(data.statusCode);
    		    		}
    					return data.mapData;
		        	 }, function(response) {
    					console.log(response);
		        	 });
    				deferred.resolve(result);
    		        return deferred.promise;
    			}
    			function getChatMsg(id) {
    				var deferred = $q.defer();
    				
					/*var start = index;
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
					
					
    				
    				/*var result = $http.get(urlPrefix + '/restAct/chatting/getChatMsg?currentPage=' + currentPage + '&itemsPerPage=' + count).then(function(data) {*/
    				var result = $http.get(urlPrefix + '/restAct/chatting/getChatMsg?id=' + id + '&tab=' + $scope.chatting.tab).then(function(data) {
    					var data = data.data;
    					if(data.statusCode != 9999) {
    		    			$rootScope.systemAlert(data.statusCode);
    		    		}
    					return data;
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
    									getLastChatFriend().then(function(result) {
    										console.log(result);
    										$scope.chatting.items = result;
    		    							callback(result);
    		    						});
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
    				
    				$http.post(urlPrefix + '/restAct/chatting/sendMsg', {
    					message: $scope.chatting.chatMsg,
    					chattingId: $scope.chatting.chattingId,
    					friendId: $scope.chatting.currentChatting.id
    				}).then(function(data) {
    					var data = data.data;
    					if(data.statusCode != 9999) {
    		    			$rootScope.systemAlert(data.statusCode);
    		    		}
						
    					if(!$scope.chatting.messages) $scope.chatting.messages = [];
    					if(data.chattingId) $scope.chatting.chattingId = data.chattingId;
    					
    					$scope.chatting.messages.push({body: $scope.chatting.chatMsg, createdDateTime: $filter('date')(new Date(data.createdDateTime), 'HH:mm'), isMe: true});
    					$scope.chatting.chatMsg = null;
    					scrollToBottom();
		        	 }, function(response) {
    					console.log(response);
		        	 });
    			}
    			$scope.chatting.goChat = function(e, data) {
    				$scope.chatting.currentChatting = data;
    				console.log(data);
    				
    				getChatMsg(data['_id'] || data['id']).then(function(result) {
						console.log(result);
						$scope.chatting.messages = result.mapData;
						$scope.chatting.mapImg = result.mapImg;
						$scope.chatting.chattingId = result.chattingId;
					});
    				
    				$scope.chatting.isChatPage = true;
    				var el = $(e.currentTarget);
    				var childOffset = el.offset();
			        var parentOffset = el.parent().parent().offset();
			        var childTop = childOffset.top - parentOffset.top;
			        var clone = el.find('img').eq(0).clone();
			        var chtMsg = $('#chat-messages');
			        
			        setTimeout(function(){$("#profile p").addClass("animate");$("#profile").addClass("animate");}, 100);
		        	setTimeout(function(){
		        		chtMsg.addClass("animate");
		                $('.cx, .cy').addClass('s1');
		                setTimeout(function(){$('.cx, .cy').addClass('s2');}, 100);
		                setTimeout(function(){$('.cx, .cy').addClass('s3');}, 200);         
		            }, 150);                                                        
		            
		            $("#profile p").html(data.showname);
		            $("#profile span").html(data.firstName + (data.lastName ? ' ' + data.lastName : ''));         
		            
		            $(".message").not(".right").find("img").attr("src", $(clone).attr("src"));                                  
		            $('#friendslist').fadeOut();
		            $('#chatview').fadeIn();
		            
		            $('#close').unbind("click").click(function(){
		            	$scope.$apply(function () {
		            		$scope.chatting.isChatPage = false;
		            		$scope.chatting.chatMsg = null;
		            		if($scope.chatting.tab == 1) {		            			
		            			$scope.chatting.adapter.reload(0);
		            		}
		            	});
		            	
		                $("#chat-messages, #profile, #profile p").removeClass("animate");
		                $('.cx, .cy').removeClass("s1 s2 s3");
		                
		                setTimeout(function(){
		                    $('#chatview').fadeOut();
		                    $('#friendslist').fadeIn();             
		                }, 50);
		            });
		            
		            setTimeout(function(){		            	
		            	chtMsg.animate({scrollTop: chtMsg[0].scrollHeight}, 'slow');
		            	$('#inputMsg').focus();
		            }, 500);
    			} // end goChat
    			function filterOr(val) {
    				return val.showname.toLowerCase().includes($scope.chatting.keyword.toLowerCase()) || val.firstName.toLowerCase().includes($scope.chatting.keyword.toLowerCase());
    			}
    			function scrollToBottom() {
    				var chtMsg = $('#chat-messages');
					chtMsg.animate({scrollTop: chtMsg[0].scrollHeight}, 'slow');
					$('#inputMsg').focus();
    			}
    			
    			$rootScope.jws.chatting.callback = function(data) {
    				if('checkStatusResp' == data.type) {					
    					for(var i in data.friendActive) {
    						$filter('filter')($scope.chatting.items, {username: data.friendActive[i]})[0].status = 1;
    					}
    				} else if('sendMsgResp' == data.type) {
    					if($scope.chatting.isChatPage) {
	    					if(!$scope.chatting.mapImg) {
	    						getThumbnail(data.author);
							} else if(!$scope.chatting.mapImg[data.author]) {
								getThumbnail(data.author);
							}
    					}
    					
    					$scope.$apply(function () { 
    						if(!$scope.chatting.messages) {
    							$scope.chatting.messages = new Array();
    						}
    						
    						if(data.chattingId) $scope.chatting.chattingId = data.chattingId;
    						
    						$scope.chatting.messages.push({body: data.msg, author: data.author, createdDateTime: $filter('date')(new Date(data.createdDateTime), 'HH:mm')});
    						scrollToBottom();
    					});
    				}
    			}
    			
    			function getThumbnail(authorId) {
    				console.log('getThumbnail');
    				var result = $http.get(urlPrefix + '/restAct/chatting/getThumbnail?userId=' + authorId).then(function(data) {
    					var data = data.data;
    					if(data.statusCode != 9999) {
    		    			$rootScope.systemAlert(data.statusCode);
    		    		}
    					
    					if(!$scope.chatting.mapImg) $scope.chatting.mapImg = {};
    					$scope.chatting.mapImg[authorId] = {imgContent: data.thumbnail};
		        	 }, function(response) {
    					console.log(response);
		        	 });
    			}
    			
    				
	        } // end Ctrl
    	}
	});