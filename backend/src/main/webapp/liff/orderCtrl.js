angular.module('sbAdminApp').controller('OrderCtrl', function($rootScope, $state, $scope, $timeout, $q, $http, $ngConfirm, $localStorage, $base64, $filter, urlPrefix) {
	
	$scope.orderList = new Array();
	$scope.formData = {};
	$scope.keyword = '';
	$scope.kbs = [
		['1', '4', '7','0'],
		['2', '5', '8','00'],
		['3', '6', '9', '-'],
		['=', 'x', 'ลบ', '-']
	];
	
	const tPredic = new TypePrediction();
	var predicObj = {};
	
	var predicList = new Array();
	predicList.push({type: 4, name: 'ลอย', init: true});
	predicList.push({type: 44, name: 'วิ่งล่าง', init: false});
	predicObj['1'] = predicList;
	
	predicList = new Array();
	predicList.push({type: 23, name: 'บนล่าง', init: true});
	predicList.push({type: 2, name: 'บน', init: false});
	predicList.push({type: 3, name: 'ล่าง', init: false});
	predicList.push({type: 43, name: 'วิ่งบน', init: false});
	predicObj['2'] = predicList;
	
	predicList = new Array();
	predicList.push({type: 1, name: 'บน', init: true});
	predicList.push({type: 132, name: 'เฉพาะโต๊ด', init: false});
	predicObj['3'] = predicList;
	
	predicList = new Array();
	predicList.push({type: 41, name: 'แพ 4', init: true});
	predicList.push({type: 121, name: 'กลับหมด', init: false});
	predicObj['4'] = predicList;
	
	predicList = new Array();
	predicList.push({type: 42, name: 'แพ 5', init: true});
	predicList.push({type: 122, name: 'กลับหมด', init: false});
	predicObj['5'] = predicList;
	
	var currentType;
	var ordObjUpdate;
	var updateIndex;
	//--------------------------------------------------------------------------
	
	$scope.askName = function() {
		$scope.formData.name = null;
		$ngConfirm({
		    title: 'ชื่อผู้ซื้อ',
		    contentUrl: 'askName.html',
		    type: 'blue',
		    typeAnimated: true,
		    scope: $scope,
		    columnClass: 'col-xs-10 col-xs-offset-1',
		    buttons: {
			    close: {
		        	text: 'ยกเลิก',
		        	btnClass: 'btn-red'
		        },
		        send: {
		        	text: 'ส่งข้อมูล',
		        	btnClass: 'btn-green',
		        	action: function(scope, button){
		        		sendOrder();
		        	}
		        }
		    }
		});	
	}
	
	$scope.showOrder = function(group) {
		$('#lps-overlay').css("display", "block");
		$state.go("home.order.showOrder", {createdDateTime: group});
	}
	
	$scope.addToList = function() {
		if(!$scope.keyword) return;
		
		if(ordObjUpdate) {
			ordObjUpdate = null;
		} else {
			var eqIndex = $scope.keyword.indexOf("=");
			if($scope.keyword.indexOf("=") == -1) return;
			
			//currentType
			var lastPriceSet = $scope.keyword.substring(eqIndex + 1);
			if(!lastPriceSet) return;
			
			//---:
			currentType.lastPriceSet = lastPriceSet;
			
			//---:
			$scope.orderList.push({orderNumberSet: $scope.keyword, typeObj: currentType});	
		}
		
		$scope.keyword = '';
		$scope.kbPressed($scope.keyword);
		
		$("#valueBox").animate({ scrollTop: $('#valueBox').prop("scrollHeight")}, 1000);
	}
	
	$scope.changeType = function(pd) {
		for(var x in $scope.predicTypeList) {
			$scope.predicTypeList[x].init = false;
		}
		pd.init = true;
		currentType = pd;
		
		//---:
		showHideOnChangeType();
		
		//---:
		if(ordObjUpdate) {
			ordObjUpdate.typeObj = pd;
		}
	}
	
	$scope.changePriceProb = function(pd) {
		if($scope.keyword.indexOf("=") == -1) {			
			$scope.kbPressed('=' + pd.name);
		} else {			
			$scope.kbPressed(pd.name);
		}
		$scope.addToList();
	}
	
	$scope.kbPressed = function(val) {
		//---: Prevent duplicated sign.
		if(val == "=" || val == "x") {
			if($scope.keyword.indexOf(val) != -1) {
				return;
			}
		}
		if(val == "x") {
			if($scope.keyword.indexOf("=") == -1) {
				return;
			}
		}
		
		//---:
		if(val == 'ลบ') {
			$scope.keyword = $scope.keyword.slice(0, -1);
		} else {
			$scope.keyword += val;
		}
		
		//---:
		if(currentType && (currentType.type == 4 || currentType.type == 41 || currentType.type == 42 || 
							currentType.type == 43 || currentType.type == 44 || currentType.type == 132 ||
							currentType.type == 121 || currentType.type == 122)) { //---: ลอย, แพ 4, แพ 5, วิ่งบน, วิ่งล่าง, เฉพาะโต๊ด, กลับหมด
			var xIndex = $scope.keyword.indexOf("x");
			if(xIndex != -1) {
				$scope.keyword = $scope.keyword.substring(0, xIndex);
				return;
			}
		}
		
		//---: Limit order number length not over 5 digit.
		var orderNumber = tPredic.getOrderNumber($scope.keyword);
		if(orderNumber.length > 5) {
			$scope.keyword = $scope.keyword.slice(0, -1);
			return;
		}
		
		//---:
		getPredict($scope.keyword, orderNumber);
		
		//---:
		if(ordObjUpdate) {
			ordObjUpdate.orderNumberSet = $scope.keyword;
			
			if(!$scope.keyword) {
				$scope.orderList.splice(updateIndex, 1);
				ordObjUpdate = null;
			}
		}
	}
	
	$scope.updateOrdSet = function(ordObj, i) {
		updateIndex = i;
		ordObjUpdate = ordObj;
		
		var orderNumber = tPredic.getOrderNumber(ordObj.orderNumberSet);
		
		$scope.predicTypeList = getPredicTypeList(orderNumber);
		$scope.changeType(ordObj.typeObj);
		
		$scope.keyword = '';
		$scope.kbPressed(ordObj.orderNumberSet);
	}
	
	$scope.clearOrderList = function() {
		var result = window.confirm('ยืนยันการล้างข้อมูล ทั้งหมด !!!');
		if(!result) return;
		
		$scope.orderList = new Array();
		$scope.kbPressed('');
	}
	
	function sendOrder() {
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
			
			$scope.formData.name = null;
			$scope.orderList = new Array();
			
			$scope.showOrder(result.createdDateTime);
		}, function(response) {
			$('#lps-overlay').css("display","none");
			informMessage('ส่งข้อมูลไม่สำเร็จ!!!');
		});
	}
	
	function getPredict(keyword, orderNumber) {
		$scope.predicPriceProbList = new Array();
		$scope.predicTypeList = null;
		$scope.defaultType = null;
		
		if(orderNumber) {
			$scope.predicTypeList = getPredicTypeList(orderNumber);
						
			if(orderNumber.length == 2 || orderNumber.length == 3) {
				var price = tPredic.getPredicPrice(keyword);
				
				if(price) {
					//---: Predict
					if(orderNumber.length == 3) {
						var p = predictProb(keyword, orderNumber);
						if(p) $scope.predicPriceProbList.push(p);
					}
					
					//---: Price
					if(orderNumber.length == 2 || orderNumber.length == 3) {					
						var show = true;
						if(currentType && (currentType.type == 43 || currentType.type == 132)) show = false; //---: วิ่งบน, เฉพาะโต๊ด
						$scope.predicPriceProbList.push({type: 2, name: 'x' + price, show: show});
					}					
				} else {
					//---: Show last price
					if(currentType.lastPriceSet && !ordObjUpdate) {
						$scope.predicPriceProbList.push({type: 3, name: currentType.lastPriceSet, show: true});
					}
				}
			}
		}
	}
	
	function predictProb(keyword, orderNumber) {
		if(keyword.indexOf("x") == -1) {
			var probNum = tPredic.getProbNum(orderNumber);
			if(probNum > 0) {
				var show = true;
				if(currentType && currentType.type == 132) show = false; //---: เฉพาะโต๊ด
				return {type: 1, name: 'x' + probNum, show: show};
			}
		}
	}
	
	function getPredicTypeList(ordNum) {
		var pList;
		if(ordNum.length == 1) {
			pList = predicObj['1'];
		} else if(ordNum.length == 2) {
			pList = predicObj['2'];
		} else if(ordNum.length == 3) {			
			pList = predicObj['3'];
		} else if(ordNum.length == 4) {			
			pList = predicObj['4'];
		} else if(ordNum.length == 5) {
			pList = predicObj['5'];
		}
		
		currentType = $filter('filter')(pList, {'init': true})[0];
		return pList;
	}
	
	function showHideOnChangeType() {
		if(currentType.type == 43 || currentType.type == 132) {  //---: วิ่งบน, เฉพาะโต๊ด
			var xIndex = $scope.keyword.indexOf("x");
			if(xIndex != -1) {
				$scope.keyword = $scope.keyword.substring(0, xIndex);
				
				if(ordObjUpdate) {
					ordObjUpdate.orderNumberSet = $scope.keyword;
				}
			}
			
			$scope.predicPriceProbList[0] && ($scope.predicPriceProbList[0].show = false);
			$scope.predicPriceProbList[1] && ($scope.predicPriceProbList[1].show = false);
		} else {
			$scope.predicPriceProbList[0] && ($scope.predicPriceProbList[0].show = true);
			$scope.predicPriceProbList[1] && ($scope.predicPriceProbList[1].show = true);
		}		
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
	
	var searchParams = new URLSearchParams(window.location.search);
	login(searchParams.get('uid'));
	//-----------------------------------------------------------------
	
});




//-- Classes
class TypePrediction {
	constructor() {}
	
	getOrderNumber(ordNumSet) {
		var eqIndex = ordNumSet.indexOf("=");
		var ordNum;
		
		if(eqIndex == -1) {
			ordNum = ordNumSet.substring(0);
		} else {
			ordNum = ordNumSet.substring(0, eqIndex);
		}
		
		return ordNum;
	}
	
	getPredicPrice(ordNumSet) {
		var eqIndex = ordNumSet.indexOf("=");
		var xIndex = ordNumSet.indexOf("x");
		if(xIndex != -1) return;
		if(eqIndex == -1) return;
		
		var price = ordNumSet.substring(eqIndex + 1);
		if(!price) return;
		
		return price;
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
		return probNum;
	}
	
}