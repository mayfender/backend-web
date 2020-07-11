angular.module('sbAdminApp').controller('ManageOrderCtrl', function($rootScope, $state, $scope, $base64, $http, $translate, $localStorage, $ngConfirm, $filter, urlPrefix, loadData) {
	console.log('ManageOrder');	
	
	var now = new Date($rootScope.serverDateTime);
	$scope.periods = loadData.periods;
	$scope.orderNameLst = loadData.orderNameLst;
	
	$scope.receiverList = new Array();
	$scope.receiverInactiveList = new Array();
	
	var receiverObj;
	for(var i = 0; i < loadData.receiverList.length; i++) {
		receiverObj = loadData.receiverList[i];
		
		if(i < 2) {
			$scope.receiverList.push(receiverObj);
		} else {
			$scope.receiverInactiveList.push(receiverObj);
		}
	}
	
	$scope.receiverChangeIndex = null;
	$scope.isLoadProgress = false;
	$scope.panel = 0;
	$scope.tabActived = 0;
	$scope.isDnDable = true;
	$scope.restricted = {isSettingShow:false};
	
	$scope.orderData = {};
	$scope.totalPriceSum = {};
	$scope.totalPriceSumAll = {};
	
	//--------------------------------------------------
	$scope.moveOrderData = {};
	$scope.moveOrderData.operators = [
//		{id: 1, name: 'น้อยกว่าเท่ากับ'},
		{id: 2, name: 'มากกว่า'},
		{id: 3, name: 'ทั้งหมด'}
	];
	$scope.moveOrderData.prices = [50, 100, 200, 300, 400, 500];
	//--------------------------------------------------
	
	$scope.orderType = [
		{id: 0, name: 'รายการซื้อ'},
		{id: 1, name: 'รวม 3'},
		{id: 2, name: 'รวม 2 บน'},
		{id: 3, name: 'รวม 2 ล่าง'},
		{id: 4, name: 'รวมลอย'},
		{id: 41, name: 'รวมแพ 4'},
		{id: 42, name: 'รวมแพ 5'},
		{id: 43, name: 'รวมวิ่งบน'},
		{id: 44, name: 'รวมวิ่งล่าง'},
		{id: 5, name: 'รวมโต๊ด'},
		{id: 6, name: 'เช็คผล'}
	];
	
	$scope.formData = {
		bonSw: false, langSw: false, orderName: null, discount: '10'
	};
	
	$scope.formData.orderType = 0;
	$scope.formData.orderTypeRestricted = 1;
	
	if($scope.periods && $scope.periods.length > 0) {
		var p = $scope.periods[0];
		$scope.formData.period = p._id;
		
		$scope.formData.result2 = p.result2;
		$scope.formData.result3 = p.result3;
		
		chkDate(p.periodDateTime);
	}
	
	$scope.changeReceiver = function(rcLst, index) {
		$scope.receiverChangeIndex = index;
		var rc = $scope.receiverList[index];
		var rcLst;
		var rcDummy = {};
		
		Object.assign(rcDummy, rc);
		Object.assign(rc, rcLst);
		Object.assign(rcLst, rcDummy);
		
		getData(rc.id, index);
	}
	
	$scope.exportOrder = function(receiverId) {		
		var p = $filter('filter')($scope.periods, {_id: $scope.formData.period})[0];
		
		$http.post(urlPrefix + '/restAct/order/export',{
			userId: $rootScope.userId,
			periodId: $scope.formData.period,
			periodDate: p.periodDateTime,
			receiverId: receiverId
		} ,{responseType: 'arraybuffer'}).then(function(data) {	
			
			var a = document.createElement("a");
			document.body.appendChild(a);
			a.style = "display: none";
			
			var fileName = decodeURIComponent(data.headers('fileName'));
			var file = new Blob([data.data]);
	        var url = URL.createObjectURL(file);
	        
	        a.href = url;
	        a.download = fileName;
	        a.click();
	        a.remove();
	        
	        window.URL.revokeObjectURL(url); //-- Clear blob on client
			
			}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	function checkResult() {
		$http.get(urlPrefix + '/restAct/order/checkResult?periodId=' + $scope.formData.period + '&isAllReceiver=true').then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.resultGroup1 = {};
			$scope.resultGroup2 = {};
			var chk;
			for (var key in result.chkResultMap) {
				chk = result.chkResultMap[key]
				
				$scope.resultGroup1[key] = [{
					title: '3 บน', result: chk.result3
				}, {
					title: 'โต๊ด', result: chk.resultTod
				}, {
					title: '2 บน', result: chk.resultBon2
				}, {
					title: '2 ล่าง', result: chk.resultLang2
				}];
				
				$scope.resultGroup2[key] = [{
					title: 'ลอย', result: chk.loy,
				},{
					title: 'แพ 4', result: chk.pair4
				}, {
					title: 'แพ 5', result: chk.pair5
				}, {
					title: 'วิ่งบน', result: chk.runBon
				}, {
					title: 'วิ่งล่าง', result: chk.runLang
				}];
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	function getOrderNameByPeriod() {
		$http.get(urlPrefix + '/restAct/order/getOrderNameByPeriod?periodId=' + $scope.formData.period + '&userId=' + $rootScope.userId, {
			ignoreLoadingBar: true
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.orderNameLst = result.orderNameLst;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.dropCallback = function(index, item, external, type, receiverId) {
		$scope.isLoadProgress = true;
		var receiverIds = new Array();
		for(var x = 0; x < $scope.receiverList.length; x++) {
			receiverIds.push($scope.receiverList[x].id);
		}
		
		$http.post(urlPrefix + '/restAct/order/moveToReceiver', {
			orderId: item._id,
			receiverId: receiverId,
			tab: $scope.formData.orderType,
			chkBoxType: $scope.checkBoxType,
			orderName :$scope.formData.orderName,
			receiverIds: receiverIds,
			userId: $rootScope.userId,
			periodId: $scope.formData.period
		}, {
			ignoreLoadingBar: true
		}).then(function(data) {
			$scope.isLoadProgress = false;
			var result = data.data;
			
			if(result.statusCode == 1001) {
				restrictedConfirm();
			} else if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			} else {
				$rootScope.systemAlert(result.statusCode, 'Move Success');				
			}
			
			var dataObj;
			for (var key in result.dataMap) {
				dataObj = result.dataMap[key];
				$scope.orderData[key] = dataObj.orderData;
				$scope.totalPriceSum[key] = dataObj.totalPriceSum;
				$scope.totalPriceSumAll[key] = dataObj.totalPriceSumAll;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
			$scope.isLoadProgress = false;
		});
		return item;
	};
	
	$scope.checkBoxTypeAllFn = function() {
		if($scope.chkbox.checkBoxTypeAll) {
			$scope.checkBoxType = {
					bon3: true, bon2: true, lang2: true, 
					loy: true, pair4: true, pair5: true, runBon: true, runLang: true
			};			
		} else {
			$scope.checkBoxType = {
					bon3: false, bon2: false, lang2: false, 
					loy: false, pair4: false, pair5: false, runBon: false, runLang: false
			};
		}
		$scope.chkBoxTypeChange();
	}
	
	$scope.chkBoxTypeChange = function() {
		getData();
	}
	
	$scope.changePeriod = function() {
		var p = $filter('filter')($scope.periods, {_id: $scope.formData.period})[0];
		$scope.formData.result2 = p.result2;
		$scope.formData.result3 = p.result3;
		chkDate(p.periodDateTime);
		
		$scope.changeTab($scope.tabActived);
		getOrderNameByPeriod();
	}
	
	$scope.changeOrderName = function() {
		getData();
	}
	
	$scope.changePercent = function() {
		console.log($scope.formData.discount);
	}
	
	$scope.changeTab = function(tab) {
		$scope.tabActived = tab;
		initCheckBox();
		
		if($scope.tabActived == 6) {
			checkResult();
		} else {
			getData();
		}
	}
	
	$scope.tagManage = function(index) {
		$http.post(urlPrefix + '/restAct/order/updateRestricted', {
			receiverId: $scope.receiverList[index].id,
			noPriceOrds: $scope.noPrice[index],
			halfPriceOrds: $scope.halfPrice[index],
			periodId: $scope.formData.period
		}, {
			ignoreLoadingBar: true
		}).then(function(data) {
			$scope.isLoadProgress = false;
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
			$scope.isLoadProgress = false;
		});
	}
	
	$scope.chkOrderNumber = function() {
		/*if($scope.formData.orderNumber.length > 3) {
			$scope.formData.bonSw = true;
		} else {
			$scope.formData.bonSw = false;			
		}*/
	}
	
	$scope.moveOrder= function(index) {
		$scope.moveOrderData.operator = 2;
		$scope.moveOrderData.price = 50;
		
		if($scope.formData.orderType == 1) {
			$scope.typeMess = 'เลข 3 ตัว';
		} else if($scope.formData.orderType == 2) {
			$scope.typeMess = 'เลข 2 ตัวบน';
		} else if($scope.formData.orderType == 3) {
			$scope.typeMess = 'เลข 2 ตัวล่าง';			
		} else if($scope.formData.orderType == 4) {
			$scope.typeMess = 'เลขลอย';
		}
		
		if(index == 0) {
			$scope.moveFrom = $scope.receiverList[index];
			$scope.moveTo = $scope.receiverList[1];
		} else {
			$scope.moveFrom = $scope.receiverList[index];
			$scope.moveTo = $scope.receiverList[0];
		}
		
		$ngConfirm({
		    title: 'เงื่อนไขการย้าย',
		    contentUrl: './views/manageOrder/moveCondition.html',
		    type: 'blue',
		    typeAnimated: true,
		    scope: $scope,
		    columnClass: 'col-xs-8 col-xs-offset-2',
		    buttons: {
		        save: {
		            text: 'ดำเนินการ',
		            btnClass: 'btn-blue',
		            action: function(scope, button){
		            	$scope.isLoadProgress = true;
		            	$http.post(urlPrefix + '/restAct/order/moveToReceiverWithCond', {
		            		operator: scope.moveOrderData.operator,
	            			price: scope.moveOrderData.price,
	            			tab: scope.formData.orderType,
	            			moveFromId: scope.moveFrom.id,
            				moveToId: scope.moveTo.id,
            				userId: $rootScope.userId,
            				periodId: $scope.formData.period
		        		}, {
		        			ignoreLoadingBar: true
		        		}).then(function(data) {
		        			$scope.isLoadProgress = false;
		        			var result = data.data;
		        			if(result.statusCode != 9999) {
		        				$rootScope.systemAlert(result.statusCode);
		        				return;
		        			}
		        			
		        			var orderObj;
		        			for (var key in result.dataMap) {
		        				orderObj = result.dataMap[key];
		        				$scope.orderData[key] = orderObj.orderData; 
		        				$scope.totalPriceSum[key] = orderObj.totalPriceSum;
		        			}
		        			
		        			console.log(result.movedNum);
		        			
		        			$rootScope.systemAlert(result.statusCode, 'Move Success');
		        		}, function(response) {
		        			$rootScope.systemAlert(response.status);
		        			$scope.isLoadProgress = false;
		        		});
		            }
		        },
		        close: {
		        	text: 'ยกเลิก',
		        	action: function(scope, button){
		            	
		            }
		        }
		    }
		});
	}
	
	$scope.comparator = function(actual, expected) {
	    if (!expected) {
	        return true;
	    } else {
            return angular.equals(actual, expected);
	    }
	}
	
	function chkDate(periodDateTime) {
		var limitedDateTimeDnD = new Date(periodDateTime);
		limitedDateTimeDnD.setHours(15, 0, 0, 0);
		$scope.isDnDable = now.getTime() > limitedDateTimeDnD.getTime();
	}
	
	function getData(recvId, index) {
		$scope.isLoadProgress = true;
		var receiverIds = new Array();
		var restrictedMapIndex = {};
		
		if(recvId) {
			$scope.orderData[recvId] = null;
			receiverIds.push(recvId);		
			restrictedMapIndex[recvId] = index;
		} else {
			$scope.orderData = {};
			for(var x = 0; x < $scope.receiverList.length; x++) {
				receiverIds.push($scope.receiverList[x].id);
				restrictedMapIndex[$scope.receiverList[x].id] = x;
			}
		}
		
		$http.post(urlPrefix + '/restAct/order/getData', {
			tab: $scope.formData.orderType,
			chkBoxType: $scope.checkBoxType,
			orderName :$scope.formData.orderName,
			receiverIds: receiverIds,
			userId: $rootScope.userId,
			periodId: $scope.formData.period
		}, {
			ignoreLoadingBar: true
		}).then(function(data) {
			$scope.isLoadProgress = false;
			$scope.receiverChangeIndex = null;
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			var dataObj;
			for (var key in result.dataMap) {
				dataObj = result.dataMap[key];
				$scope.orderData[key] = dataObj.orderData;
				$scope.totalPriceSum[key] = dataObj.totalPriceSum;
				$scope.totalPriceSumAll[key] = dataObj.totalPriceSumAll;
			}
			
			//--------------------: Restricted Number :-------------------------------
			if(recvId) {
				$scope.noPrice[index] = [];
				$scope.halfPrice[index] = [];
			} else {
				init();
			}
			
			if(result.restrictedOrder) {
				var restrictedOrderObj;
				for (var key in result.restrictedOrder) {
					restrictedOrderObj = result.restrictedOrder[key];
					
					if(restrictedOrderObj.noPrice) {
						$scope.noPrice[restrictedMapIndex[key]] = restrictedOrderObj.noPrice;						
					}
					
					if(restrictedOrderObj.halfPrice) {						
						$scope.halfPrice[restrictedMapIndex[key]] = restrictedOrderObj.halfPrice;
					}
				}
			}
		}, function(response) {
			$scope.isLoadProgress = false;
			$scope.receiverChangeIndex = null;
			$rootScope.systemAlert(response.status);
		});
	}
	
	function restrictedConfirm() {
		$ngConfirm({
		    title: 'แจ้งเลขปิด',
		    content: '<strong>เป็นเลขปิด ไม่สามารถย้ายได้ !</strong>',
		    type: 'red',
		    typeAnimated: true,
		    columnClass: 'col-xs-8 col-xs-offset-2',
		    buttons: {
		        ok: {
		        	text: 'OK',
		        	btnClass: 'btn-red',
		        	keys: ['enter'],
		        	action: function(scope, button){
		        		
		        	}
		        }
		    }
		});
	}
	
	function initCheckBox() {
		$scope.chkbox = {checkBoxTypeAll: true};
		$scope.checkBoxType = {
			bon3: true, bon2: true, lang2: true, 
			loy: true, pair4: true, pair5: true, runBon: true, runLang: true
		};
	}
	
	function init() {
		$scope.noPrice = {0: [], 1: []};
		$scope.halfPrice = {0: [], 1: []};
	}
	
	//---------------------------
	init();
	initCheckBox();
	getData();
	
});