angular.module('sbAdminApp').controller('OrderCtrl', function($rootScope, $scope, $base64, $http, $translate, $ngConfirm, urlPrefix, loadData) {
	console.log('Order');
	
//	$scope.periods = [{id: '1', name: '01/01/2020'},{id: '2', name: '15/01/2020'},{id: '3', name: '01/02/2020'}];
	console.log(loadData.periods);
	
	$scope.periods = loadData.periods;
	$scope.formData = {};
	if($scope.periods.length > 0) {
		$scope.formData.period = $scope.periods[0]._id;
	}
//	$scope.periodModes = [{id: 1, name:'ข้อมูล'}, {id: 2, name:'เพิ่ม'}, {id: 3, name:'แก้ใข'}];
	$scope.periodModes = [{id: 1, name:'ข้อมูล'}, {id: 2, name:'เพิ่ม'}];
	$scope.periodMode = $scope.periodModes[0];
	
	$scope.periodModeChange = function(p) {
		$scope.periodMode = p;
		
		/*if($scope.periodMode.id == 3) {
			$("input[name='period']").data("DateTimePicker").date(null);
		}*/
	}
	
	$scope.addPeriod = function() {
		var periodDateObj = $("input[name='period']").data("DateTimePicker");
		var periodDate = periodDateObj && periodDateObj.date();
		
		if(periodDate == null) return;
		
		$scope.formData.newPeriod = periodDate.toDate();
		$scope.formData.newPeriod.setHours(0,0,0,0);
		
		$http.post(urlPrefix + '/restAct/order/savePeriod', {
			periodDateTime: $scope.formData.newPeriod,
			userId: $rootScope.userId
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