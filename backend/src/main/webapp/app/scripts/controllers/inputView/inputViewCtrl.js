angular.module('sbAdminApp').controller('InputViewCtrl', function($rootScope, $templateCache, $state, $scope, $base64, $http, $timeout, $translate, $q, $localStorage, $ngConfirm, $filter, urlPrefix, FileUploader, loadData) {
	console.log(loadData);
	console.log('InputViewCtrl');
	
	//-----
	$scope.baseHost = location.protocol + '//' +  location.host;
//	$scope.orderFile = loadData.orderFile;
	$scope.periodObj = loadData.lastPeriod;
	
	//-----
	var orderFileSum = loadData.orderFileSum;
	var items = Array();
	var openPhotoSwipe;
	var stompClient;
	var pswp;
	
	//----
	function getImageAndFlag(orderFileId) {
		var deferred = $q.defer();
		$http.post(urlPrefix + '/restAct/uploadFile/getImageAndFlag', {
			orderFileId: orderFileId,
			dealerId: $rootScope.workingOnDealer.id,
			periodId: $scope.periodObj['_id']
		}).then(function(data) {
			var result = data.data;
    		if(result.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return $q.reject(data);
			}
    		
    		deferred.resolve(result);
		}, function(response) {
			$rootScope.systemAlert(response.status);
			return $q.reject(data);
		});
		return deferred.promise;
	}
	
	
	/*function prepareItem(passItems) {
		var items;
		var result = {};
		
		if(passItems) items = passItems;
		else      items = Array();
		
		var ordObj, item, isExit; 
		for(var i in $scope.orderFiles) {
			ordObj = $scope.orderFiles[i];
			isExit = false;
			
			if(passItems) {
				for(var j in passItems) {
	    			item = passItems[j];
	    			if(ordObj['_id'] == item.imgId) {
	    				isExit = true;
	    				break;
	    			}
	    		}
			} 
			
			if(!isExit) {
				result.isNew = true;
				items.push({
					imgId: ordObj['_id'],
					src: $scope.baseHost + '/images/' + ordObj.imgPath,
					w: ordObj.fileWidth,
					h: ordObj.fileHeight,
					title: '<h4>' + $filter('date')($scope.periodObj.periodDateTime, 'dd/MM/yyyy') + ' ' + ordObj.customerName + ' (' + ordObj.code + ')</h4>'
//		        title: '<h4>' + ordObj.customerName + ' (' + ordObj.code + ')</h4>'
				});		    			
    		}
		}
		
		result.items = items;
		return result;
	}*/
	
	function toDefaultItem() {
		pswp.currItem.html = prepareDefaultItem()[0].html;
		pswp.currItem.imgId = null;
		pswp.currItem.src = null;
		pswp.currItem.w = null;
		pswp.currItem.h = null;
		pswp.currItem.title = null;
		
		pswp.invalidateCurrItems();
		pswp.updateSize(true);
	}
	
	function prepareDefaultItem() {
		/*var htmlData = "<div class=\"pending-orderfile\">" +
							"<h3>สามารถเรียกดูข้อมูลภาพได้<br /><br />" +
								"จากเครื่องส่งข้อมูล<br /><br />" +
								"ข้อมูลรูปว่าง : <br />" +
								"ข้อมูลรูปกำลังลงข้อมูล : <br />" +
								"ข้อมูลรูปลงแล้ว : <br />" +
								"ข้อมูลรูปลงทั้งหมด : <br />" +
							"</h3>" +
				       "</div>";*/
		
		console.log(orderFileSum);
		var waiting = orderFileSum['0'] ? orderFileSum['0'] : 0;
		var inprogress = orderFileSum['1'] ? orderFileSum['1'] : 0;
		var saved = orderFileSum['2'] ? orderFileSum['2'] : 0;
		
		var htmlData = "<div class=\"pending-orderfile\"><table class=\"table\">" + 
						   "<span style=\"display: inline-block; font-size: 20px; margin-bottom: 30px;\">สามารถเรียกดูข้อมูลภาพได้จาก<br />เครื่องส่งข้อมูล</span>" +
						   "<tbody>" +
							   "<tr>" +
							   		"<td>รอลงข้อมูล</td><td>:&nbsp;&nbsp;&nbsp;<span id=\"ordWaiting\">" + waiting + "</span></td>" +
							   "</tr>" +
							   "<tr>" +
							   		"<td>กำลังลงข้อมูล</td><td>:&nbsp;&nbsp;&nbsp;<span id=\"ordInprogress\">" + inprogress + "</span></td>" +
							   "</tr>" +
							   "<tr>" +
							   		"<td>ลงข้อมูลแล้ว</td><td>:&nbsp;&nbsp;&nbsp;<span id=\"ordSaved\">" + saved + "</span></td>" +
							   "</tr>" +
							   "<tr>" +
							   		"<td>ข้อมูลรูปทั้งหมด</td><td>:&nbsp;&nbsp;&nbsp;<span id=\"ordTotal\">" + (waiting + inprogress + saved) + "</span></td>" +
							   "</tr>" +
						   "</tbody>" +
					   "</table></div>";
		
		var items = Array();
		items.push({
			html: htmlData
		});
		
		return items;
	}
	
	function prepareImgItem(ordObj) {
		var items = Array();
		items.push({
				imgId: ordObj['_id'],
				src: $scope.baseHost + '/images/' + ordObj.imgPath,
				w: ordObj.fileWidth,
				h: ordObj.fileHeight,
				title: '<div class=\'imgTitle\'>' + $rootScope.workingOnDealer.name + ' | ' + $filter('date')($scope.periodObj.periodDateTime, 'dd/MM/yyyy') + ' | ' + $rootScope.showname + 
				'<h4>' +ordObj.customerName + ' (' + ordObj.code + ')</h4></div>'
			});
		return items;
	}
	
	
	//---: Websocket
	function wsConn() {
		stompClient = Stomp.over(new SockJS("/backend/websocketHandler"));
		stompClient.connect({},
			function(frame) {
				stompClient.subscribe("/user/" + $rootScope.username + "/reply", 
						function (greeting) {
							var dataObj = JSON.parse(greeting.body);
							
							if(!$rootScope.username) {
								alert('Username is empty.');
								return;
							}
							if($scope.periodObj['_id'] != dataObj.periodId) {
								alert('Period is not match.');
								return;
							}
							if($rootScope.workingOnDealer.id != dataObj.dealerId) {
								alert('Dealer is not match.');
								return;
							}
							if($rootScope.username != dataObj.userName) {
								alert('Username is not match.');
								return;
							}
							
							if(dataObj.savedOrderFileId) {
								console.log('saved already');
								$('.imgTitle > h4').append(" <span class='bling_me'>Saved</span>");
								return;
							}
							
							if(dataObj.release) {
								console.log('Release.');
								return toDefaultItem();
							}
							
							//-------
							if(!dataObj.orderFileId) {
								console.log('Orderfile is empty.');
								return toDefaultItem();
							}
							
							//-----
							getImageAndFlag(dataObj.orderFileId).then(function(response) {
					    		var ordObj = response.orderFile;
					    		
					    		if(!ordObj) {
					    			alert('Not found Image file.');
					    			return;
					    		}
					    		
					    		var itemData = prepareImgItem(ordObj)[0];
					    		if(!pswp) {						
									console.log('open new photoviewer');
									items.push(itemData);
									openPhotoSwipe();
								} else {
									var isExit = $filter('filter')(pswp.items, {imgId: ordObj['_id']}, true)[0];									
									if(isExit) {
										console.log('OrderFile is the same');
										return;
									}
									pswp.currItem.imgId = itemData.imgId;
									pswp.currItem.src = itemData.src;
									pswp.currItem.w = itemData.w;
									pswp.currItem.h = itemData.h;
									pswp.currItem.title = itemData.title;
									
									pswp.invalidateCurrItems();
						    		pswp.updateSize(true);
								}
		    	            }, function(response) {
		    	                $rootScope.systemAlert(response.status);
		    	            });
				        });
				
				
				//----:
				var dealerSuffixed = $rootScope.workingOnDealer.id.substring($rootScope.workingOnDealer.id.length - 3);
				stompClient.subscribe('/topic/' + dealerSuffixed + '/orderFileSum', 
						function (greeting) {
							console.log(greeting.body);
							var dataObj = JSON.parse(greeting.body);
							
							orderFileSum = dataObj.orderFileSum;
							var waiting = orderFileSum['0'] ? orderFileSum['0'] : 0;
							var inprogress = orderFileSum['1'] ? orderFileSum['1'] : 0;
							var saved = orderFileSum['2'] ? orderFileSum['2'] : 0;
							$("#ordWaiting").html(waiting);
							$("#ordInprogress").html(inprogress);
							$("#ordSaved").html(saved);
							$("#ordTotal").html(waiting + inprogress + saved);
				        });
		}, function(message) {
			$ngConfirm({
				title: 'แจ้งเตือน',
				content: 'ไม่สามารถเชื่อมต่อได้ กรุณาเชื่อมต่ออีกครั้ง',
				type: 'red',
				typeAnimated: true,
				columnClass: 'col-xs-8 col-xs-offset-2',
				buttons: {
					ok: {
						text: 'เชื่อมต่อใหม่',
						btnClass: 'btn-red',
						action: function(scope, button){
							wsConn();
						}
					}
				}
			});
		});
	}
	
	
	
	//----
	var previousItem;
	angular.element(document).ready(function () {
		$timeout(function() {
//			var items = prepareItem().items;
			
			var items;
			if(loadData.orderFile) {
				items = prepareImgItem(loadData.orderFile);
			} else {
				items = prepareDefaultItem();				
			}
			
			openPhotoSwipe = function() {
			    pswpElement = document.querySelectorAll('.pswp')[0];

			    // define options (if needed)
			    var options = {
			       // history & focus options are disabled on CodePen        
			        history: false,
			        focus: false,
			        escKey: false,
			        
			        pinchToClose: false,
			        closeOnScroll: false,
			        closeOnVerticalDrag: false,
			        
			        //---: Default UI Options
			        shareEl: false,
			        closeEl: false,
//			        arrowEl: false,
			        clickToCloseNonZoomable: false,
//			        closeElClasses: ['item', 'caption', 'zoom-wrap', 'ui', 'top-bar'], 
			        closeElClasses: [],

			        showAnimationDuration: 0,
			        hideAnimationDuration: 0
			        
			    };
			    
			    pswp = new PhotoSwipe( pswpElement, PhotoSwipeUI_Default, items, options);
			    pswp.listen('beforeChange', function() {
			    	
			    	/*getNextImage(previousItem && previousItem.imgId, pswp.currItem.imgId).then(function(response) {
			    		if(response.errCode == 500) {
			    			console.log('File is holded please call next image');
			    			pswp.items.splice(pswp.getCurrentIndex(), 1);
			    			pswp.goTo(pswp.getCurrentIndex());
			    			pswp.invalidateCurrItems();
			    			pswp.updateSize(true);
			    			
			    			previousItem = pswp.items[pswp.getCurrentIndex()];
			    		} else {
			    			console.log('File is ready');
			    			previousItem = pswp.currItem;
			    		}
    	            }, function(response) {
    	                $rootScope.systemAlert(response.status);
    	            });*/
			    
			    });
			    pswp.listen('afterChange', function() {
			    	
			    });
			    pswp.listen('gettingData', function(index, item) {
			    	
			    });
			    pswp.listen('close', function() { 
			    	console.log('Close Gallery');
			    	items = new Array();
					pswp = null;
			    });
			    
			    pswp.init();
			};

			//---:
			openPhotoSwipe();
			
			//---:
			wsConn();
			
			
		}, 50);    // End Timeout
    }); // End ready
	
	
	
	//---:
	$scope.$on("$destroy", function(){
		console.log('destroy');
		if(stompClient) {
			stompClient.disconnect(function() {
				console.log("See you next time!");
			});			
		}
    });
    
});