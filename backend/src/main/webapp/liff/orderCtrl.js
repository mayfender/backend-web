angular.module('sbAdminApp').controller('OrderCtrl', function($rootScope, $state, $scope, $timeout, $q, $http, $ngConfirm, $localStorage, $base64, urlPrefix) {
	
	console.log('OrderCtrl');
	
	$scope.setNumberDigit = null;
	$scope.isValidToNext = false;
	$scope.prediction = {};
	$scope.lineData = {};
	$scope.keyword = '';
	$scope.ordObjUpdate = null;
	$scope.orderList = new Array();
	$scope.formData = {};
	
	const prediction = new Prediction();
	
	$scope.kbs = [
		['1', '4', '7','0'],
		['2', '5', '8','00'],
		['3', '6', '9', '-'],
		['=', 'x', 'ลบ', '-']
	];
	$scope.formData = {};
	$scope.formData.functionId = '1';
	$scope.functions = [{id: '1', name: 'ทั่วไป'}, {id: '2', name: 'ชุด'}, {id: '3', name: 'ก๊อบปี้วาง'}];
	var startPredictIndex = 0;
	
	$scope.sendOrder = function() {
		$('#lps-overlay').css("display","block");
		$http.post(urlPrefix + '/restAct/order/saveOrder2', {
			name: $scope.formData.name || $rootScope.showname,
			orderList: $scope.orderList,
			userId: $rootScope.userId,
			periodId: $rootScope.period['_id'],
			dealerId: $rootScope.workingOnDealer.id
		}).then(function(data) {
			$('#lps-overlay').css("display","none");
			var result = data.data;
			if(result.statusCode != 9999) {
				informMessage('ส่งข้อมูลไม่สำเร็จ!!!');
				return;
			}
			
			$scope.orderList = new Array();
			$scope.formData.name = null;
			
			$scope.showOrder(result.createdDateTime);
		}, function(response) {
			$('#lps-overlay').css("display","none");
			informMessage('ส่งข้อมูลไม่สำเร็จ!!!');
		});
	}
	
	$scope.showOrder = function(group) {
		$state.go("home.order.showOrder", {createdDateTime: group});
	}
	
	$scope.nextStep = function() {
		if($scope.formData.functionId == 1) {
			$scope.save();
		} else if($scope.formData.functionId == 2) {
			//
		} else if($scope.formData.functionId == 3) {
			// 
		}
	}
	
	$scope.changeFunction = function() {
		$scope.keyword = '';
		$scope.setNumberDigit = null;
	}
	
	$scope.predictSet = function(value, t) {
		if(t == 1 && ($scope.prediction.orderNumberLen == 4 || $scope.prediction.orderNumberLen == 5)) {
			$scope.type = 12;
		}
		if($scope.ordObjUpdate) {
			$scope.keyword += value;			
			$scope.ordObjUpdate.orderNumberSet = $scope.keyword;
			if($scope.type) {
				$scope.ordObjUpdate.type = $scope.type;
			}
			
			$scope.keyword = '';
			$scope.ordObjUpdate = null;
		} else {
			$scope.keyword += value + ',';
			var orderResult = prediction.orderListManage($scope.keyword);
			if(orderResult != null) {
				addOrd(orderResult);
			}
		}
		$scope.type = null;
	}
	
	//---------------------: Order waiting to send:---------------------------
	var index;
	$scope.updateOrdSet = function(ordObj, i) {
		index = i;
		$scope.ordObjUpdate = ordObj;
		$scope.keyword = ordObj.orderNumberSet;
		$scope.type = ordObj.type;
	}
	$scope.removeOrdSet = function() {
		$scope.orderList.splice(index, 1);
		$scope.keyword = '';
		$scope.ordObjUpdate = null;
		$scope.type = null;
	}
	//---------------------: Order waiting to send:---------------------------
	
	$scope.setType = function(type) {
		$scope.type = type;
		if($scope.ordObjUpdate) {
			$scope.ordObjUpdate.type = type;
		}
	}
	
	function setType(order) {
		if($scope.type) {
			order.type = $scope.type;
		} else {
			if($scope.currentProbNum == 1) {
				order.type = 4;				
			} else if($scope.currentProbNum == 2) {
				order.type = 23;
			} else if($scope.currentProbNum == 3) {
				order.type = null;				
			} else if($scope.currentProbNum == 4) {
				order.type = 41;
			} else if($scope.currentProbNum == 5) {
				order.type = 42;
			}			
		}
	}
	
	function addOrd(order) {
		if(order != ',') {
			setType(order);
			$scope.orderList.push(order);			
		}
		
		$scope.keyword = '';
		$scope.ordObjUpdate = null;
		$scope.type = null;
	}
	
	$scope.kbPressed = function(val) {
		
		if(val == ',' && !$scope.keyword) return;
		
		if(val == 'ลบ') {
			$scope.keyword = $scope.keyword.slice(0, -1);
			if($scope.ordObjUpdate) {
				$scope.ordObjUpdate.orderNumberSet = $scope.keyword;
			}
		} else {
			if($scope.formData.functionId == '1') {
				$scope.keyword += val;
				if($scope.ordObjUpdate) {
					if(val == ',') {
						addOrd(val);
					} else {
						$scope.ordObjUpdate.orderNumberSet = $scope.keyword;						
					}
				} else {
					var orderResult = prediction.orderListManage($scope.keyword);
					if(orderResult != null) {
						addOrd(orderResult);
					}
				}
			} else if($scope.formData.functionId == '2') {
				if(!$scope.setNumberDigit) {					
					$scope.keyword += val;
					if($scope.keyword.length == 2) {
						askDigit();
					}
					return;
				}
				
				if($scope.keyword.length == $scope.setNumberDigit) {
					$scope.keyword += ',';
				} else if($scope.keyword.length > $scope.setNumberDigit){
					var index = $scope.keyword.lastIndexOf(",") + 1
					var dummy = $scope.keyword.substring(index);
					
					if(dummy.length == $scope.setNumberDigit) {
						$scope.keyword += ',';			
					}
				}
				$scope.keyword += val;				
			} else if($scope.formData.functionId == '3') {
				
			}
		}
	}
	
	$scope.clearOrderList = function() {
		var result = window.confirm('ยืนยันการล้างข้อมูล ทั้งหมด !!!');
		if(!result) return;
		$scope.orderList = new Array();
	}
	
	$scope.$watch('keyword', function(newValue, oldValue, scope) {
		 if($scope.formData.functionId == 1) {
			 if($scope.keyword) {
				 $scope.isValidToNext = true;
			 } else {
				 $scope.isValidToNext = false;				 
			 }			 
			 $scope.prediction = prediction.getPredict($scope.keyword);
			 
			 if($scope.prediction.orderNumber) {
				 $scope.currentProbNum = $scope.prediction.orderNumber.length;
				 if($scope.ordObjUpdate) {
					 $scope.type = null;
					 setType($scope.ordObjUpdate);					 
				 }
			 }
		 } else if($scope.formData.functionId == 2) {
			 if($scope.setNumberDigit) {
				 $scope.isValidToNext = true;
			 } else {
				 $scope.isValidToNext = false;				 
			 }
		 } else if($scope.formData.functionId == 3) {
			// 
		 }
		 
//		 $('#valueBox').animate({scrollLeft: '+=1500'}, 500);
		 $("#valueBox").animate({ scrollTop: $('#valueBox').prop("scrollHeight")}, 1000);
	});
	
	function askDigit() {
		$ngConfirm({
		    title: 'เลือกเลขชุด',
		    content: "",
		    type: 'blue',
		    typeAnimated: true,
		    scope: $scope,
		    columnClass: 'col-xs-10 col-xs-offset-1',
		    buttons: {
			    digit2: {
		        	text: 'เลข 2 ตัว',
		        	btnClass: 'btn-red',
		        	action: function(scope, button){
		        		$scope.setNumberDigit = 2;
		        	}
		        },
		        digit3: {
		        	text: 'เลข 3 ตัว',
		        	btnClass: 'btn-green',
		        	action: function(scope, button){
		        		$scope.setNumberDigit = 3;
		        	}
		        }
		    }
		});	
	}
	
	function informMessage(msg) {
		$ngConfirm({
		    title: 'แจ้งเตือน',
		    content: msg,
		    type: 'blue',
		    typeAnimated: true,
		    scope: $scope,
		    columnClass: 'col-xs-10 col-xs-offset-1',
		    buttons: {
			    OK: {
		        	text: 'OK',
		        	btnClass: 'btn-red'
		        }
		    }
		});	
	}
	
	
	//-----------------------------------------------------------------
	function login(lineUserId) {		
		authenticate(lineUserId, function() {
	        if (!$scope.authenticated) {
	        	window.location.href = 'https://www.notfound.com';
	        }
	        $('#lps-overlay').css("display","none");
	   });
	}
	
	var authenticate = function(lineUserId, callback) {
	    $http.post(urlPrefix + '/loginByLineUserId', {'lineUserId': $base64.encode(lineUserId)}).
	    then(function(data) {
	    	
	    	var userData = data.data;
	    	$scope.isDisabled = userData.isDisabled;
	    	
	    	if($scope.isDisabled) {
	    		return
	    	}
	    	
		    if (userData.token) {
		    	$localStorage.token = {};
		    	
		    	//[Local Storage]
		    	$localStorage.token[userData.username] = userData.token;
		    	$rootScope.showname = userData.showname;
		    	$rootScope.username = userData.username;
		    	$rootScope.userId = userData.userId;
		    	$rootScope.period = userData.period;
		    	$rootScope.dealers = userData.dealers;
		    	$rootScope.authority = userData.authorities[0].authority;
		    	
		    	if($rootScope.authority == 'ROLE_SUPERADMIN') {
		    		$rootScope.dealers.unshift({id: null, name:'--: Select Dealer :--'});
		    	}
		    	$rootScope.workingOnDealer = $rootScope.dealers && $rootScope.dealers[0];
		    	
		    	$rootScope.serverDateTime = userData.serverDateTime;
		    	$rootScope.firstName = userData.firstName;
		    	$rootScope.lastName = userData.lastName;
		    	$rootScope.title = userData.title;
		    	$rootScope.backendVersion = userData.version;
		    	
		        $scope.authenticated = true;
		    } else {
		    	$scope.authenticated = false;
		    }
		    callback && callback();
	    }, function(response) {
	    	$scope.authenticated = false;
	    	callback && callback();
	    });
	}
	//-----------------------------------------------------------------
	
	function runApp() {
		return $q(function(resolve, reject) {
			liff.getProfile().then(profile => {
				resolve(profile);
			}).catch(err => reject(err));
		});
    }
	
	$scope.$watch('$viewContentLoaded', 
		function() {
			$timeout(function() {
				console.log('Init Line Login.');
			    liff.init({ liffId: "1654799308-zj2ewgpV" }, () => {
			    	if (liff.isLoggedIn()) {
			    		runApp().then(function(profile) {
							if(profile.userId) {
								login(profile.userId)
							} else {
								window.location.href = 'https://www.notfound.com';
							}
			        	}, function(err) {
			        		window.location.href = 'https://www.notfound.com';
			        		console.error(err)
			        	});
			      	} else {
			        	liff.login();
			     	}
			    }, err => console.error(err.code, error.message));
		},0);
	});
});


//-- Classes
class Prediction {
	constructor() {
//		this.orderNumberSetObj = new Array();
	}
	
	lastOrderNumber(val) {
		this.lastDigit = val.substring(val.length - 1, val.length);
		var commaIndex = val.lastIndexOf(",") + 1;
		if(this.lastDigit == '=') {
			this.orderNumber = val.substring(commaIndex, val.length - 1);
			this.lastEqualIndex = val.lastIndexOf("=");
			return this.orderNumber;
		} else {
			this.orderNumber = null;
		}
		return this.orderNumber;
	}
	
	getProbNum(orderNumber) {
		if(!orderNumber) return 0;
		
		var text = '', ch, count = 0;
		for( var x = 0; x < orderNumber.length; x++ ) {
			ch = orderNumber.charAt(x);
			if(text.includes(ch)) {
				count++;
			}
			text += orderNumber.charAt(x);
		}
		
		var probNum = 0;
		if(orderNumber.length == 2) {
			probNum = count == 0 ? 2 : 0;			
		} else if(orderNumber.length == 3) {
			probNum = count == 0 ? 6 : (count == 1 ? 3 : 0);
		} else if(orderNumber.length == 4) {
			probNum = count == 0 ? 24 : (count == 1 ? 12 : (count == 2 ? 4 : 0));
		} else if(orderNumber.length == 5) {
			probNum = count == 0 ? 60 : (count == 1 ? 33 : (count == 2 ? 13 : (count == 3 ? 4 : 0)));
		}
		return {probNum: probNum, orderNumberLen: orderNumber.length};
	}
	
	getPredict(val) {
		var result = {
			orderNumber: this.lastOrderNumber(val)
		};
		
		var eChInd = val.lastIndexOf("=");
		var xChInd = val.lastIndexOf("x");
		var commChInd = val.lastIndexOf(",");
		
		if(eChInd == -1) return result;
		
		this.startPredictIndex = eChInd;
		if(xChInd > this.startPredictIndex) {
			this.startPredictIndex = xChInd;		
		}
		if(commChInd > this.startPredictIndex) {
			this.startPredictIndex = commChInd;			
		}
		
		var index1 = val.indexOf("=", this.startPredictIndex);
		if(index1 != -1) {
			var equalVal = val.substring(index1);
			if(equalVal.length == 1) {
				return result;
			} else if(equalVal) {
				var probNumObj = this.getProbNum(val.substring(commChInd + 1, index1));
				result.orderNumberLen = probNumObj.orderNumberLen;
				result.probNum = probNumObj.probNum;
				result.price = equalVal.substring(1);
			}				
		}
		
		return result;
	}
	
	orderListManage(val, isAdd) {
		this.lastDigit = val.substring(val.length - 1, val.length);
		
		if(this.lastDigit == ',') {
			var textToFind = val.substring(0, val.length - 2);
			var lastCommaIndex = textToFind.lastIndexOf(",") + 1;
			var orderNumberSet = val.substring(lastCommaIndex, val.length - 1);
			
			if(orderNumberSet) {
				if(orderNumberSet.includes(",")) return null;
				return {orderNumberSet: orderNumberSet};
			}
		}
		return null;
	}
	
}