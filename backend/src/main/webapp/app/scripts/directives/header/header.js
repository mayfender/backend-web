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
    			$scope.chatting.privateChatDisabled = $rootScope.workingOnProduct.productSetting.privateChatDisabled;
    			$scope.chatting.groups = [
      	    			                {id: '111111111111111111111111', showname: 'Company Group', firstName: $rootScope.companyName, isGroup: true}, 
      	    			                {id: $rootScope.workingOnProduct.id, showname: 'Port Group', firstName: $rootScope.workingOnProduct.productName, isGroup: true}
      	    			                ];
    			
    			function getLastChatFriend() {
    				var deferred = $q.defer();
    				var result = $http.get(urlPrefix + '/restAct/chatting/getLastChatFriend?productId=' + $rootScope.workingOnProduct.id, {
    					ignoreLoadingBar: true
    				}).then(function(data) {
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
    			function getChatMsg(chattingId, friendId, isGroup) {
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
    				
    				chattingId = chattingId ? chattingId : '';
    				friendId = friendId ? friendId : '';
    				var result = $http.get(urlPrefix + '/restAct/chatting/getChatMsg?chattingId=' + chattingId + '&friendId=' + friendId + '&isGroup=' + isGroup, {
    					ignoreLoadingBar: true
    				}).then(function(data) {
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
			        	 result = $http.get(urlPrefix + '/restAct/chatting/getFriends?currentPage=' + currentPage + '&itemsPerPage=' + count + '&keyword=' + $scope.chatting.keyword, {
			        		 ignoreLoadingBar: true
			        	 }).then(function(data) {
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
    									if($scope.chatting.isLocalReload) {
    										$scope.chatting.isLocalReload = false;
    		    							callback($scope.chatting.items);
    									} else {
	    									getLastChatFriend().then(function(result) {
	    										console.log(result);
	    										$scope.chatting.items = result;
	    										var item, group;
	    										for(var x in $scope.chatting.items) {
	    											item = $scope.chatting.items[x];
	    											if(item.members.length == 1) {
	    												group = $filter('filter')($scope.chatting.groups, {id: item.members[0]})[0];
	    					    						if(group) {
	    					    							item.showname = group.showname;
	    					    							item.firstName = group.firstName;
	    					    							item.status = 1;
	    					    							item.isGroup = true;
	    					    						}
	    											}
	    										}
	    										
	    		    							callback(result);
	    		    						});
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
    				
    				$http.post(urlPrefix + '/restAct/chatting/sendMsg', {
    					message: $scope.chatting.chatMsg,
    					chattingId: $scope.chatting.currentChatting._id,
    					friendId: $scope.chatting.currentChatting.friendId,
    					isGroup: $scope.chatting.currentChatting.isGroup,
    					productId: $rootScope.workingOnProduct.id
    				}, {ignoreLoadingBar: true}).then(function(data) {
    					var data = data.data;
    					if(data.statusCode != 9999) {
    		    			$rootScope.systemAlert(data.statusCode);
    		    		}
						
    					if(!$scope.chatting.messages) $scope.chatting.messages = [];
    					if(data.chattingId) $scope.chatting.currentChatting._id = data.chattingId;
    					
    					var createdDateTime = new Date(data.createdDateTime);
    					
    					if($scope.chatting.tab == 1) {
	    					$scope.chatting.currentChatting.lastMsg = $scope.chatting.chatMsg;
	    					$scope.chatting.currentChatting.updatedDateTime = createdDateTime;
	    					$scope.chatting.items = $filter('orderBy')($scope.chatting.items, '-updatedDateTime');
	    					$scope.chatting.isLocalReload = true;
	    					$scope.chatting.adapter.reload(0);
    					}
    					
    					$scope.chatting.chatMsg = $("<span></span>").html($scope.chatting.chatMsg).emoticonize(true)[0].innerHTML;
    					$scope.chatting.messages.push({_id: data.msgId, body: $scope.chatting.chatMsg, createdDateTime: $filter('date')(createdDateTime, 'HH:mm'), isMe: true});
    					$scope.chatting.chatMsg = null;
    					scrollToBottom();
		        	 }, function(response) {
    					console.log(response);
		        	 });
    			}
    			$scope.chatting.goChat = function(e, data) {
    				console.log(data);
    				$scope.chatting.currentChatting = data;
    				$scope.chatting.currentChatting.unRead = null;
    				$scope.chatting.isChatPage = true;
    				
    				if($scope.chatting.tab == 1) {
    					$scope.chatting.isGroup = data.members.length == 1;    					
    				} else if($scope.chatting.tab == 2) {
    					$scope.chatting.isGroup = false;
    				} else if($scope.chatting.tab == 3) {
    					$scope.chatting.isGroup = true;
    				}
    				
    				getChatMsg(data['_id'], data['id'], data.isGroup).then(function(result) {
						$scope.chatting.messages = result.mapData;
						$scope.chatting.mapImg = result.mapImg;
						var x;
						for(x in $scope.chatting.messages) {
							$scope.chatting.messages[x].body = $("<span></span>").html($scope.chatting.messages[x].body).emoticonize(true)[0].innerHTML;
						}
						
						if(result.chattingId) {
							$scope.chatting.currentChatting._id = result.chattingId;
						} else {
							$scope.chatting.currentChatting.friendId = data['id'];
						}
						console.log($scope.chatting.currentChatting);
					});
    				
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
		            		$scope.chatting.messages = null;
		            		$scope.chatting.mapImg = null;
		            		$scope.chatting.chatMsg = null;
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
    			
    			$scope.chatting.chatHide = function() {
    				if($scope.chatting.isShow) {
	    				if($scope.chatting.tab != 1) {
	    					$scope.chatting.tab = 1;
	    					$scope.chatting.adapter.reload(0);
	    				}
	    				if($scope.chatting.isChatPage) {    					
	    					$timeout(function(){
	    						$('#close').click();
	    					});
	    				}
	    				
	    				$scope.chatting.isShow = false;
    				} else {
    					$scope.chatting.isShow = true;
    					$scope.chatting.isChatBlink = false;
    				}
    			}
    			function filterOr(val) {
    				return val.showname.toLowerCase().includes($scope.chatting.keyword.toLowerCase()) || val.firstName.toLowerCase().includes($scope.chatting.keyword.toLowerCase());
    			}
    			function scrollToBottom() {
    				var chtMsg = $('#chat-messages');
					chtMsg.animate({scrollTop: chtMsg[0].scrollHeight}, 'slow');
					$('#inputMsg').focus();
    			}
    			
    			function read(chattingId) {
    				$http.post(urlPrefix + '/restAct/chatting/read', {
    					chattingId: chattingId
    				}, {ignoreLoadingBar: true}).then(function(data) {
    					var data = data.data;
    					if(data.statusCode != 9999) {
    		    			$rootScope.systemAlert(data.statusCode);
    		    		}
		        	 }, function(response) {
    					console.log(response);
		        	 });
    			}
    			
    			function getThumbnail(authorId) {
    				console.log('getThumbnail');
    				var result = $http.get(urlPrefix + '/restAct/chatting/getThumbnail?userId=' + authorId, {
    					ignoreLoadingBar: true
    				}).then(function(data) {
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
    			
    			$scope.inputMsgUpdateVal = function(val) {
    				$scope.chatting.chatMsg = val;
    			}
    			
    			//---------------------------------: JWS Callback :------------------------------------
    			var notification;
    			$rootScope.jws.chatting.callback = function(data) {
    				if('checkStatusResp' == data.type) {
    					if($scope.chatting.tab == 1) {
	    					$scope.$apply(function () { 
		    					var item;
		    					for(var i in data.friendActive) {
		    						item = $filter('filter')($scope.chatting.items, {userId: data.friendActive[i]})[0];
		    						if(item) item.status = 1;
		    					}
	    					});
    					} else if($scope.chatting.tab == 2) {
    						/*$scope.chatting.adapter.applyUpdates(function (item, scope) {
    							var obj = $filter('filter')(data.friendActive, item.username);
    							if(obj && obj[0]) item.status = 1;
    						});*/
    					}
    				} else if('sendMsgResp' == data.type) {
    					if(!$scope.chatting.isShow) {
    						$scope.chatting.isChatBlink = true;
    						
    						/*if (Notification.permission !== "granted") Notification.requestPermission();
    						else {
    							var item = $filter('filter')($scope.chatting.items, {_id: data.chattingId})[0];
    							var options = {
    									icon: "/backend/app/images/IM.png",
    									body: data.msg
    							};
    							
    							if(notification) notification.close();
    							
    							notification = new Notification(item.showname, options);
    							notification.onclick = function () {
    								$scope.$apply(function () {    									
    									$scope.chatting.isShow = true;
    									$scope.chatting.isChatBlink = false;
    								});
    								this.close();
    							};
    						}*/
    						
    						msgAlert(data.msg);
    					}
    					
    					if($scope.chatting.tab == 1) {
							var item = $filter('filter')($scope.chatting.items, {_id: data.chattingId})[0];
							if(item) {
								$scope.$apply(function () {
									item.lastMsg = data.msg;
									item.updatedDateTime = new Date(data.createdDateTime);
									if(item.unRead) {
										item.unRead++;
									} else {
										item.unRead = 1;
									}
									
									$scope.chatting.items = $filter('orderBy')($scope.chatting.items, '-updatedDateTime');
									$scope.chatting.isLocalReload = true;
								});    								
							}
							$scope.chatting.adapter.reload(0);
						}
    					
    					if($scope.chatting.isChatPage) {
	    					if(!$scope.chatting.mapImg || !$scope.chatting.mapImg[data.author]) {
	    						getThumbnail(data.author);
							}
    					
	    					$scope.$apply(function () {
	    						if(!$scope.chatting.messages) {
	    							$scope.chatting.messages = new Array();
	    						}
	    						
	    						if($scope.chatting.currentChatting._id) {
	    							if($scope.chatting.currentChatting._id == data.chattingId) {
	    								$scope.chatting.currentChatting.unRead = null;
	    								data.msg = $("<span></span>").html(data.msg).emoticonize(true)[0].innerHTML;
		    							$scope.chatting.messages.push({_id: data.msgId, showname: data.authorName, body: data.msg, author: data.author, createdDateTime: $filter('date')(new Date(data.createdDateTime), 'HH:mm')});
		    							scrollToBottom();
		    							console.log(data);
		    							read(data.chattingId);
	    							} else {
	    								msgAlert(data.msg);
	    							}
	    						}
	    					});
    					}
    				} else if('readResp' == data.type) {
    					if($scope.chatting.isChatPage) {
    						if($scope.chatting.currentChatting._id && $scope.chatting.currentChatting._id == data.chattingId) {
    							$scope.$apply(function () {
    								console.log($scope.chatting.messages);
	    							for(var x in data.chatMsgId) {
	    								var msg = $filter('filter')($scope.chatting.messages, {_id: data.chatMsgId[x]})[0];
	    								if(msg) {
	    									if(msg.readCount) {
	    										msg.readCount++;
		    								} else {
		    									msg.readCount = 1;
		    								}
	    								}
	    							}
    							});
    						}
    					}
    				} else if('activeUser' == data.type || 'inActiveUser' == data.type) {
    					console.log(data);
    					if($scope.chatting.tab == 1) {
    						var item = $filter('filter')($scope.chatting.items, {userId: data.userId})[0];
    						if(item) {
    							$scope.$apply(function () {
    								if('activeUser' == data.type) {
    									item.status = 1;
    								} else {
    									item.status = 0;
    								}
    							});
    						}
    					}
    				}
    			}
    			
    			function msgAlert(msg) {
    				var el = $(".notifyjs-bootstrap-success")
					if(el.length >= 3) {			
						el.eq(2).trigger('notify-hide');
					}
					  
					$.notify(msg.length <= 30 ? msg : msg.substring(0, 30) + '...' , {
						position: 'bottom right',
						  	className: 'warn',
						  	autoHide: true,
					});
    			}
    				
	        } // end Ctrl
    	}
	});