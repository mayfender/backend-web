angular.module('sbAdminApp').controller('OrderCtrl', function($rootScope, $scope, $base64, $http, $translate, $ngConfirm, urlPrefix, loadData) {
	console.log('Order');
	
	$scope.tabActived = 1;
	$scope.periods = loadData.periods;
	$scope.formData = {bonSw: false, langSw: false};
	if($scope.periods.length > 0) {
		$scope.formData.period = $scope.periods[0]._id;
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
		console.log($scope.formData);
		
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
	
	$scope.chkOrderNumber = function() {
		if($scope.formData.orderNumber != null) {
			$scope.orderNumStr = '' + $scope.formData.orderNumber;			
		} else {
			$scope.orderNumStr = '';
		}
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
	
});