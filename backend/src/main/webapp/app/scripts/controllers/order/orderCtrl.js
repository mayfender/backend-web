angular.module('sbAdminApp').controller('OrderCtrl', function($rootScope, $state, $scope, $base64, $http, $translate, $localStorage, $ngConfirm, $filter, urlPrefix, loadData) {
	console.log(loadData);
	
	if(!loadData) {
		console.log('loadData is null so should be relogin.');
		$state.go("login", {action: 'logout'});
		return;
	}
	
	$scope.panel = 0;
	$scope.tabActived = 0;
	$scope.periods = loadData.periods;
	$scope.totalPriceSumAll = loadData.totalPriceSumAll;
	$scope.orderNameLst = loadData.orderNameLst;
	$scope.formData = {
		bonSw: false, langSw: false, orderName: null, discount: '10'
	};
	
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
		
		/*if($scope.periodMode.id == 3) {
			$("input[name='period']").data("DateTimePicker").date(null);
		}*/
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
			getOrderNameByPeriod();
			
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
	
	function getSumOrder() {
		$http.post(urlPrefix + '/restAct/order/getSumOrder', {
			chkBoxType: $scope.checkBoxType,
			tab : $scope.tabActived,
			orderName :$scope.formData.orderName,
			periodId: $scope.formData.period,
			userId: $rootScope.userId
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.orderData = result.orderData; 
			$scope.totalPriceSum = result.totalPriceSum;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	function getSumOrderTotal() {
		$http.post(urlPrefix + '/restAct/order/getSumOrderTotal', {
			orderName :$scope.formData.orderName,
			periodId: $scope.formData.period,
			userId: $rootScope.userId
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.totalPriceSumAll = result.totalPriceSumAll;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.exportOrder = function() {		
		var p = $filter('filter')($scope.periods, {_id: $scope.formData.period})[0];
		
		$http.post(urlPrefix + '/restAct/order/export',{
			userId: $rootScope.userId,
			periodId: $scope.formData.period,
			periodDate: p.periodDateTime
		} ,{responseType: 'arraybuffer'}).then(function(data) {	
					
			/*var file = new Blob([data.data], {type: 'application/pdf'});
	        var fileURL = URL.createObjectURL(file);
	        window.open(fileURL);
	        window.URL.revokeObjectURL(fileURL);  //-- Clear blob on client*/
			
			
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
		$http.get(urlPrefix + '/restAct/order/checkResult?periodId=' + $scope.formData.period).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			console.log(result);
			var lotResult = result.chkResultMap.total
			$scope.result3 = lotResult.result3;
			$scope.resultBon2 = lotResult.resultBon2;
			$scope.resultLang2 = lotResult.resultLang2;
			$scope.resultTod = lotResult.resultTod;
			$scope.resultLoy = lotResult.resultLoy;
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
	
	$scope.chkBoxTypeChange = function() {
		getSumOrder();
	}
	
	$scope.changePeriod = function() {
		var p = $filter('filter')($scope.periods, {_id: $scope.formData.period})[0];
		$scope.formData.result2 = p.result2;
		$scope.formData.result3 = p.result3;
		
		$scope.changeTab($scope.tabActived);
		getSumOrderTotal();
		getOrderNameByPeriod();
	}
	
	$scope.changeOrderName = function() {
		console.log($scope.formData.orderName);
		getSumOrder();
		getSumOrderTotal();	
	}
	
	$scope.changePercent = function() {
		console.log($scope.formData.discount);
	}
	
	$scope.changeTab = function(tab) {
		$scope.tabActived = tab;
		$scope.orderData = null;
		
		if($scope.tabActived == 6) {
			checkResult();			
		} else {
			getSumOrder();
			
			if($scope.tabActived == 0) {
				getSumOrderTotal();
			}
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
	function initDateEl() {		
		$('.dtPicker').each(function() {
			$(this).datetimepicker({
				format: 'DD/MM/YYYY',
				showClear: true,
				showTodayButton: true,
				locale: 'th'
			}).on('dp.hide', function(e){
				
			}).on('dp.change', function(e){
				
			});
		});
	}
	
	initDateEl();
	getSumOrder();
	
});