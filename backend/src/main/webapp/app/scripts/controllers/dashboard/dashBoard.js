'use strict';
/**
 * @ngdoc function
 * @name sbAdminApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the sbAdminApp
 */
angular.module('sbAdminApp').controller('DashBoard', function($rootScope, $scope, $http, $filter, $state, urlPrefix) {
	$scope.groupDatas = [{id: 1, name: 'รายงานข้อมูลงาน', btnActive: true}, {id: 2, name: 'รายงานข้อมูลผู้ใช้'}];
	var lastGroupActive = $scope.groupDatas[0];
	
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
					xAxes: [
				        {
			            	stacked : true,
			            	ticks: {beginAtZero:true}
			            }
					],
			        yAxes: [
			            {
			            	stacked : true,
			            	ticks: {beginAtZero:true}
			            }
			        ]
				}
			}
	};
	
	$scope.formData = {isAll: true};
	
	var today = new Date($rootScope.serverDateTime);
	$scope.formData.dateFrom = angular.copy(today);
	$scope.formData.dateTo = angular.copy(today);
	$scope.formData.dateFrom.setHours(0,0,0,0);
	$scope.formData.dateTo.setHours(23,59,59,999);
	
	$scope.formData.dateFromPayment = angular.copy(today);
	$scope.formData.dateToPayment = angular.copy(today);
	$scope.formData.dateFromPayment.setHours(0,0,0,0);
	$scope.formData.dateToPayment.setHours(23,59,59,999);
	
	$scope.colors = ['#ED402A', '#00A39F', '#A0B421', '#F0AB05'];
	$scope.colors2 = ['#F0AB05', '#A0B421', '#ED402A', '#00A39F'];
	
	$scope.dateConf = {
    	format: 'dd/mm/yyyy',
	    autoclose: true,
	    todayBtn: true,
	    clearBtn: false,
	    todayHighlight: true,
	    language: 'th-en'
	}
	
	$scope.traceCount = function(isInit) {
		$scope.formData.dateTo.setHours(23,59,59,999);
		
		$http.post(urlPrefix + '/restAct/dashBoard/traceCount', {
			dateFrom: $scope.formData.dateFrom,
			dateTo: $scope.formData.dateTo,
			isAll: $scope.formData.isAll,
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
			
			if(datas) {
				$scope.bar.sum = datas.reduce(function(acc, val) { return acc + val; }, 0);
			}
			
			if(!$rootScope.group6 && isInit) {
				$scope.payment();
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.payment = function() {
		$scope.formData.dateToPayment.setHours(23,59,59,999);
		
		$http.post(urlPrefix + '/restAct/dashBoard/payment', {
			dateFrom: $scope.formData.dateFromPayment,
			dateTo: $scope.formData.dateToPayment,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}			
			
			var series = new Array();
			$scope.bar2.data = new Array();
			$scope.bar2.sums = new Array();
			var sumLabel;
			
			for(var x in result.datas) {
				if(x == 'paymentNum') {
					series.push('จำนวนบัญชี');
					sumLabel = 'จำนวนบัญชีรวม';
				} else {
					series.push(x);
					sumLabel = x + 'รวม';
				}
				
				if(result.datas[x]) {
					$scope.bar2.sums.push({
						label: sumLabel, 
						value: result.datas[x].reduce(function(acc, val) { return acc + val; }, 0)
					});
				}
				
				$scope.bar2.data.push(result.datas[x]);
			}
			
			$scope.bar2.labels = result.labels;
			$scope.bar2.series = series;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.dateFromChange = function(type) {
		if($scope.isNotUseDateRelate) {
			$scope.isNotUseDateRelate = false;
			return;
		}
		
		$scope.isNotUseDateRelate = true;
		
		if(type == 1) {
			$scope.formData.dateTo = angular.copy($scope.formData.dateFrom);
			$("#dateTo_traceCount").datepicker('update', $filter('date')($scope.formData.dateTo, 'dd/MM/yyyy'));
			
			$scope.traceCount();
		} else if(type == 2) {
			$scope.formData.dateToPayment = angular.copy($scope.formData.dateFromPayment);
			$("#dateTo_payment").datepicker('update', $filter('date')($scope.formData.dateToPayment, 'dd/MM/yyyy'));
			
			$scope.payment();
		}
	}
	
	$scope.dateToChange = function(type) {
		if($scope.isNotUseDateRelate) {
			$scope.isNotUseDateRelate = false;
			return;
		}
		
		if(type == 1) {
			if($scope.formData.dateFrom.getTime() > $scope.formData.dateTo.getTime()) {
				$scope.isNotUseDateRelate = true;
				$scope.formData.dateFrom = angular.copy($scope.formData.dateTo);
				$("#dateFrom_traceCount").datepicker('update', $filter('date')($scope.formData.dateFrom, 'dd/MM/yyyy'));
			}
			$scope.traceCount();
		} else if(type == 2) {
			if($scope.formData.dateFromPayment.getTime() > $scope.formData.dateToPayment.getTime()) {	
				$scope.isNotUseDateRelate = true;
				$scope.formData.dateFromPayment = angular.copy($scope.formData.dateToPayment);
				$("#dateFrom_payment").datepicker('update', $filter('date')($scope.formData.dateFromPayment, 'dd/MM/yyyy'));
			}
			$scope.payment();
		}
	}
	
	$scope.changeTab = function(group) {
		if($scope.groupDatas.length == 1 || lastGroupActive == group) return;
		
		if(group.id == 1) {
			$state.go('dashboard.summaryReport')
		} else if(group.id == 2) {
			$state.go('dashboard.summaryReport.collector')
		}
		
		lastGroupActive.btnActive = false;
		lastGroupActive = group;
		group.btnActive = true;
	}
	
	
	//---------------------------
	$scope.traceCount(true);
	
});
