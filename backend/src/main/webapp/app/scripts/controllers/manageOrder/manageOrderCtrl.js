angular.module('sbAdminApp').controller('ManageOrderCtrl', function($rootScope, $state, $scope, $base64, $http, $translate, $localStorage, $ngConfirm, $filter, urlPrefix, loadData) {
	console.log('ManageOrder');	
	
	$scope.panel = 0;
	$scope.tabActived = 0;
	$scope.periods = loadData.periods;
	$scope.orderData = {};
	$scope.totalPriceSum = {};
	$scope.totalPriceSumAll = {};
	
	$scope.result3 = {};
	$scope.resultBon2 = {};
	$scope.resultLang2 = {};
	$scope.resultTod = {};
	$scope.resultLoy = {};
	$scope.receiverList = new Array();
	$scope.receiverInactiveList = new Array();
	
	$scope.orderType = [
		{id: 0, name: 'รายการซื้อ'},
		{id: 1, name: 'สรุป 3'},
		{id: 2, name: 'สรุป 2 บน'},
		{id: 3, name: 'สรุป 2 ล่าง'},
		{id: 4, name: 'สรุปลอย'},
		{id: 5, name: 'สรุปโต๊ด'},
		{id: 6, name: 'เช็คผล'}
		];
	
	
	$scope.totalPriceSumAllMap = loadData.totalPriceSumAllMap;
	$scope.orderNameLst = loadData.orderNameLst;
	$scope.formData = {
		bonSw: false, langSw: false, orderName: null, discount: '10'
	};
	
	$scope.formData.orderType = 0;
	
	$scope.checkBoxType = {
			bon3: true, bon2: true, lang2: true, loy: true
	};
	
	if($scope.periods && $scope.periods.length > 0) {
		var p = $scope.periods[0];
		$scope.formData.period = p._id;
		
		$scope.formData.result2 = p.result2;
		$scope.formData.result3 = p.result3;
	}
//	$scope.periodModes = [{id: 1, name:'ข้อมูล'}, {id: 2, name:'เพิ่ม'}, {id: 3, name:'แก้ใข'}];
	$scope.periodModes = [{id: 1, name:'ทั่วไป'}, {id: 2, name:'เพิ่ม'}];
	$scope.periodMode = $scope.periodModes[0];
	
	$scope.periodModeChange = function(p) {
		$scope.periodMode = p;
	}
	
	$scope.saveOrder = function() {
		$scope.isFormDisable = true;
				
		$http.post(urlPrefix + '/restAct/order/saveOrder', {
			name: $scope.formData.name,
			orderNumber: $scope.formData.orderNumber,
			bon: $scope.formData.bon,
			bonSw: $scope.formData.bonSw,
			lang: $scope.formData.lang,
			langSw: $scope.formData.langSw,
			tod: $scope.formData.tod,
			loy: $scope.formData.loy,
			userId: $rootScope.userId,
			periodId: $scope.formData.period
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.orderNameLst = result.orderNameLst;
			$scope.totalPriceSumAll = result.totalPriceSumAll;
			
			getSumOrder();
			clearForm();
			
			$("#orderDataInput").animate({ scrollTop: $('#orderDataInput').prop("scrollHeight")}, 1000);
			$scope.isFormDisable = false;
		}, function(response) {
			$rootScope.systemAlert(response.status);
			$scope.isFormDisable = false;
		});
	}
	
	$scope.addPeriod = function() {
		var periodDateObj = $("input[name='period']").data("DateTimePicker");
		var periodDate = periodDateObj && periodDateObj.date();
		
		if(periodDate == null) return;
		
		$scope.formData.newPeriod = periodDate.toDate();
		$scope.formData.newPeriod.setHours(0,0,0,0);
		
		$http.post(urlPrefix + '/restAct/order/savePeriod', {
			periodDateTime: $scope.formData.newPeriod
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.periods = result.periods;
			$scope.formData.period = $scope.periods[0]._id;
			
			$scope.formData.newPeriod = null;
			$("input[name='period']").data("DateTimePicker").date(null);
			
			$scope.periodMode = $scope.periodModes[0];
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	function getSumOrder(receiverId) {
		$http.post(urlPrefix + '/restAct/order/getSumOrder', {
			chkBoxType: $scope.checkBoxType,
			tab : $scope.tabActived,
			orderName :$scope.formData.orderName,
			periodId: $scope.formData.period,
			userId: $rootScope.userId,
			receiverId: receiverId
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.orderData[result.receiverId] = result.orderData; 
			$scope.totalPriceSum[result.receiverId] = result.totalPriceSum;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	function getReceiverList() {
		return $http.get(urlPrefix + '/restAct/setting/getReceiverList?enabled=true').then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			if(result.receiverList.length > 2) {
				var obj;
				for(var i = 0; i < result.receiverList.length; i++) {
					obj = result.receiverList[i];
					
					if(i < 2) {
						getSumOrder(obj.id);
						$scope.receiverList.push(obj);
						$scope.totalPriceSumAll[obj.id] = $scope.totalPriceSumAllMap[obj.id];
					} else {
						$scope.receiverInactiveList.push(obj);
					}
				}
			} else {
				$scope.receiverList = result.receiverList;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	var roundCount = 0;
	$scope.changeReceiver = function(rc) {
		
		if($scope.receiverInactiveList.length == 0) return;
		
		var rcLst;
		var rcDummy = {};
		Object.assign(rcDummy, rc);
		
		if(roundCount < $scope.receiverInactiveList.length) {	
			rcLst = $scope.receiverInactiveList[roundCount];
			//--- Call
			getSumOrder(rcLst.id);
			if($scope.tabActived == 0) {
				getSumOrderTotal(rcLst.id);
			}
			
			Object.assign(rc, rcLst);
			Object.assign(rcLst, rcDummy);			
			roundCount++;
		} else {
			roundCount = 0;
			
			rcLst = $scope.receiverInactiveList[roundCount];
			//--- Call
			getSumOrder(rcLst.id);
			if($scope.tabActived == 0) {
				getSumOrderTotal(rcLst.id);
			}
			
			Object.assign(rc, rcLst);
			Object.assign(rcLst, rcDummy);
		}
		
	}
	
	function getSumOrderSet() {
		var id;
		for(var x = 0; x < $scope.receiverList.length; x++) {
			id = $scope.receiverList[x].id;
			getSumOrder(id);
		}
	}
	
	function getSumOrderTotal(receiverId) {
		$http.post(urlPrefix + '/restAct/order/getSumOrderTotal', {
			orderName :$scope.formData.orderName,
			periodId: $scope.formData.period,
			userId: $rootScope.userId,
			receiverId: receiverId
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.totalPriceSumAll[result.receiverId] = result.totalPriceSumAll;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
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
	
	$scope.saveResult = function() {
		$http.post(urlPrefix + '/restAct/order/saveResult',{
			result2: $scope.formData.result2,
			result3: $scope.formData.result3,
			periodId: $scope.formData.period
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
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
			
			console.log(result);
			var chk;
			
			for (var key in result.chkResultMap) {
				console.log("User " + result.chkResultMap[key] + " is #" + key); // "User john is #234"
				
				chk = result.chkResultMap[key]
				$scope.result3[key] = chk.result3;
				$scope.resultBon2[key] = chk.resultBon2;
				$scope.resultLang2[key] = chk.resultLang2;
				$scope.resultTod[key] = chk.resultTod;
				$scope.resultLoy[key] = chk.resultLoy;
			}			
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	function getOrderNameByPeriod() {
		$http.get(urlPrefix + '/restAct/order/getOrderNameByPeriod?periodId=' + $scope.formData.period + '&userId=' + $rootScope.userId).then(function(data) {
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
		$http.post(urlPrefix + '/restAct/order/moveToReceiver', {
			orderId: item._id,
			receiverId: receiverId
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$rootScope.systemAlert(result.statusCode, 'Move Success');
			
			if($scope.tabActived == 0) {
				getSumOrderTotal(receiverId);
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
		return item;
	};
	
	$scope.chkBoxTypeChange = function() {
		getSumOrderSet();
	}
	
	$scope.changePeriod = function() {
		var p = $filter('filter')($scope.periods, {_id: $scope.formData.period})[0];
		$scope.formData.result2 = p.result2;
		$scope.formData.result3 = p.result3;
		
		$scope.changeTab($scope.tabActived);
		getOrderNameByPeriod();
		
		for(var x = 0; x < $scope.receiverList.length; x++) {
			id = $scope.receiverList[x].id;
			getSumOrderTotal(id);	
		}
	}
	
	$scope.changeOrderName = function() {
		getSumOrderSet();
		
		for(var x = 0; x < $scope.receiverList.length; x++) {
			id = $scope.receiverList[x].id;
			console.log(id);
			getSumOrderTotal(id);	
		}
	}
	
	$scope.changePercent = function() {
		console.log($scope.formData.discount);
	}
	
	$scope.changeTab = function(tab) {
		$scope.tabActived = tab;
		$scope.orderData = {};
		
		if($scope.tabActived == 6) {
			checkResult();
		} else {
			getSumOrderSet();
		}
		
		//-- set to default
		$scope.checkBoxType = {
				bon3: true, bon2: true, lang2: true, loy: true
		};
	}
	
	$scope.chkOrderNumber = function() {
		/*if($scope.formData.orderNumber.length > 3) {
			$scope.formData.bonSw = true;
		} else {
			$scope.formData.bonSw = false;			
		}*/
	}
	
	function clearForm() {
		$scope.formData.orderNumber = null;
		$scope.formData.bon = null;
		$scope.formData.bonSw = false;
		$scope.formData.lang = null;
		$scope.formData.langSw = false;
		$scope.formData.tod = null;
		$scope.formData.loy = null;
	}
	
	
	
	
	//---------------------------
	getReceiverList();
	
});