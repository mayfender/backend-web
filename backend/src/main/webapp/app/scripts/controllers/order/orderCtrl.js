angular.module('sbAdminApp').controller('OrderCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, $ngConfirm, $filter, urlPrefix, loadData) {
	console.log(loadData);
	
	$scope.tabActived = 1;
	$scope.periods = loadData.periods;
	$scope.totalPriceSumAll = loadData.totalPriceSumAll;
	$scope.orderNameLst = loadData.orderNameLst;
	$scope.formData = {
		bonSw: false, langSw: false, orderName: null, discount: '10'
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
			
			getSumOrder();
			$scope.orderNameLst = result.orderNameLst;
			$scope.totalPriceSumAll = result.totalPriceSumAll;
			
			clearForm();
		}, function(response) {
			$rootScope.systemAlert(response.status);
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
	
	function getSumOrder(tab) {
		$http.post(urlPrefix + '/restAct/order/getSumOrder', {
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
			
			console.log(result);
			$scope.orderData = result.orderData; 
			$scope.totalPriceSum = result.totalPriceSum;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	function getSumOrderTotal(tab) {
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
			
			$scope.result3 = result.result3;
			$scope.resultBon2 = result.resultBon2;
			$scope.resultLang2 = result.resultLang2;
			$scope.resultTod = result.resultTod;
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
		
		if($scope.tabActived == 6) {
			checkResult();			
		} else {
			getSumOrder();			
		}
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
	
	
	
	
	
	
	
	
//	$scope.mayfender = 'may';
	
	/*$scope.addPeriod = function() {
		$ngConfirm({
		    title: 'เพิ่มงวดใหม่',
		    contentUrl: './views/order/addPeriod.html',
		    type: 'blue',
		    typeAnimated: true,
		    scope: $scope,
		    columnClass: 'col-xs-8 col-xs-offset-2',
		    buttons: {
		        save: {
		            text: 'บันทึก',
		            btnClass: 'btn-blue',
		            action: function(){
		            	
		            }
		        },
		        close: {
		        	text: 'ยกเลิก',
		        	action: function(){
		            	
		            }
		        }
		    },
		    onReady: function() {
		    	console.log('9999');
		    	initDateEl();
		    }
		});
		
	}*/
	
	
	
	
	
	
	
	
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
	getSumOrder('1');
	
});