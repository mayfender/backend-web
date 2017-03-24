'use strict';
/**
 * @ngdoc function
 * @name sbAdminApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the sbAdminApp
 */
angular.module('sbAdminApp').controller('DashBoard', function($rootScope, $scope, $http, $filter, urlPrefix) {
	
	$scope.bar = {
			options :{
				scales: {
			        yAxes: [{
			            ticks: {
			                beginAtZero:true
			            }
			        }]
				}
			}
	};
	
	$scope.bar2 = {
			options :{
				legend: { display: true },
				scales: {
			        yAxes: [{
			            ticks: {
			                beginAtZero:true
			            }
			        }]
				}
			}
	};
	
	
	
	
	
	
	
	
	
	
	$scope.bar2.labels = ['2006', '2007', '2008', '2009', '2010', '2011', '2012'];
	$scope.bar2.series = ['ยอดจ่าย', 'จำนวนบัญชี'];

	$scope.bar2.data = [
	    [65, 59, 80, 81, 56, 55, 40],
	    [28, 48, 40, 19, 86, 27, 90]
	];
	
	
	
	
	
	
	
	
	
	
	$scope.formData = {};
	
	var today = new Date($rootScope.serverDateTime);
	$scope.formData.dateFrom = angular.copy(today);
	$scope.formData.dateTo = angular.copy(today);
	
	$scope.formData.dateFrom.setHours(0,0,0);
	$scope.formData.dateTo.setHours(23,59,59);
	
	$scope.colors = ['#ED402A', '#00A39F', '#A0B421', '#F0AB05'];
	$scope.colors2 = ['#F0AB05', '#A0B421', '#ED402A', '#00A39F'];
	
	var dateConf = {
    	format: 'dd/mm/yyyy',
	    autoclose: true,
	    todayBtn: true,
	    clearBtn: true,
	    todayHighlight: true,
	    language: 'th-en'
	}
	
	
	
	
	$scope.traceCount = function() {
		$scope.formData.dateTo.setHours(23,59,59);
		
		$http.post(urlPrefix + '/restAct/dashBoard/traceCount', {
			dateFrom: $scope.formData.dateFrom,
			dateTo: $scope.formData.dateTo,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}			
			
			var traceResult;
			var labels = new Array();
			var datas = new Array();
			var x;
				
			for(x in result.traceCount) {
				traceResult = result.traceCount[x];
				labels.push(traceResult.showname);
				datas.push(traceResult.traceNum);
			}
			
			$scope.bar.labels = labels;
			$scope.bar.data = [datas];
			
			$scope.payment();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.payment = function() {		
		$http.post(urlPrefix + '/restAct/dashBoard/payment', {
			dateFrom: $scope.formData.dateFrom,
			dateTo: $scope.formData.dateTo,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}			
			
			console.log(result);
			
			var payment;
			var labels = new Array();
			var datas = new Array();
			var dataObj = {labels: new Array(), series: {seriesName: new Array(), subDatas: new Array()}};
			var test = {};
			var x;
				
			for(x in result.payment) {
				payment = result.payment[x];
				dataObj.labels.push(payment.showname);						
				
				if(dataObj.series.seriesName.length == 0) {
					console.log('add seriesName');
					
					for(var objKey in payment) {
						if(objKey == 'showname') continue;
						
						dataObj.series.seriesName.push(objKey);
					}
				}
				
				for(var objKey in payment) {
					if(objKey == 'showname') continue;
					
					if(!test[objKey]) {
						console.log('init : ' + objKey)
						test[objKey] = new Array();
					}
					
					test[objKey].push(payment[objKey]);
					console.log(test[objKey]);
					
				}
				
			}
			
			dataObj.series.subDatas.push(test[objKey]);				
			console.log(dataObj);
			
//			$scope.bar.labels = labels;
//			$scope.bar.data = [datas];
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	
	
	
	$scope.dateFromChange = function() {
		$scope.formData.dateTo = angular.copy($scope.formData.dateFrom);
		$("#dateTo_traceCount").datepicker('update', $filter('date')($scope.formData.dateTo, 'dd/MM/yyyy'));
		
		$scope.traceCount();
	}
	
	
	
	
	
	
	

	
	
	
	
	
	
	
	$('#dateFrom_traceCount').datepicker(dateConf);
	$('#dateTo_traceCount').datepicker(dateConf);
	
	$scope.traceCount();
	
});
