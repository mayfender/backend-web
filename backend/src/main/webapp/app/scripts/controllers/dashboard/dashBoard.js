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
	var barCustom = {
		tooltips: {
	        callbacks: {
	            label: function(tooltipItem, data) {
	                return tooltipItem.yLabel && tooltipItem.yLabel.toLocaleString(undefined, {maximumFractionDigits:2});
	            }
	        }
	    },
		 plugins: {
	            datalabels: {
	            	rotation: 270,
	            	color: 'black',
	                labels: {
	                    title: {
	                        font: {
	                            weight: 'bold',
	                        	size: 10,
	                        }
	                    }
	                },
//	            	anchor: 'end',
	                formatter: function(value, context) {
	                    return value.toLocaleString(undefined, {maximumFractionDigits:2});
	                }
	            }
		 }
	};
	
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
	
	//-- Merge 2 object.
	angular.merge($scope.bar.options, barCustom);
	
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
	
	//-- Merge 2 object.
	angular.merge($scope.bar2.options, barCustom);
	
	$scope.formData = {isAll: true};
	
	var today = new Date($rootScope.serverDateTime);
	$scope.colors = ['#ED402A', '#00A39F', '#A0B421', '#F0AB05'];
	$scope.colors2 = ['#F0AB05', '#A0B421', '#ED402A', '#00A39F'];
	
	$scope.traceCount = function() {
		var dateFrom = $("input[name='dateFrom']").data("DateTimePicker").date();
		var dateTo = $("input[name='dateTo']").data("DateTimePicker").date();
		
		if(dateFrom) {
			$scope.formData.dateFrom = dateFrom.toDate();
			$scope.formData.dateFrom.setSeconds(0);
			$scope.formData.dateFrom.setMilliseconds(0);
		} else {
			$scope.formData.dateFrom = null;
		}
		if(dateTo) {
			$scope.formData.dateTo = dateTo.toDate();
			$scope.formData.dateTo.setSeconds(59);
			$scope.formData.dateTo.setMilliseconds(999);
		} else {
			$scope.formData.dateTo = null;			
		}
		
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
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.payment = function() {
		if($rootScope.group6) return;
		
		var dateFrom = $("input[name='dateFrom_payment']").data("DateTimePicker").date();
		var dateTo = $("input[name='dateTo_payment']").data("DateTimePicker").date();
		
		if(dateFrom) {
			$scope.formData.dateFromPayment = dateFrom.toDate();
			$scope.formData.dateFromPayment.setSeconds(0);
			$scope.formData.dateFromPayment.setMilliseconds(0);
		} else {
			$scope.formData.dateFromPayment = null;
		}
		if(dateTo) {
			$scope.formData.dateToPayment = dateTo.toDate();
			$scope.formData.dateToPayment.setSeconds(59);
			$scope.formData.dateToPayment.setMilliseconds(999);
		} else {
			$scope.formData.dateToPayment = null;			
		}
		
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
	function initDateEl() {
		$('.input-daterange .dtPicker').each(function() {
			$(this).datetimepicker({
				format: 'DD/MM/YYYY HH:mm',
				showClear: true,
				showTodayButton: true,
				locale: 'th'
			}).on('dp.hide', function(e){
				
			}).on('dp.change', function(e){
				if($scope.isNotUseDateRelate) {
					$scope.isNotUseDateRelate = false;
					return;
				}
				
				if($(e.target).attr('name') == 'dateFrom') {
					console.log('dateFrom change');
					
					var dateTo = $("input[name='dateTo']").data("DateTimePicker");
					if(!dateTo.date() || !e.date) return;
					
					$scope.isNotUseDateRelate = true;
					dateTo.date(e.date.hours(dateTo.date().hours()).minutes(dateTo.date().minutes()));
					$scope.traceCount();
				} else if($(e.target).attr('name') == 'dateTo') {
					console.log('dateTo change');
					
					if($scope.isNotUseDateRelate) {
						$scope.isNotUseDateRelate = false;
						return;
					}
					
					var dateTo = e.date;
					if(!dateTo) return;
					
					var dateFrom = $("input[name='dateFrom']").data("DateTimePicker");
					
					if(dateTo.isBefore(dateFrom.date())) {
						$scope.isNotUseDateRelate = true;
						dateFrom.date(dateTo.hours(dateFrom.date().hours()).minutes(dateFrom.date().minutes()));
					}
					$scope.traceCount();
				} else if($(e.target).attr('name') == 'dateFrom_payment') {
					console.log('dateFrom pay change');
					
					var dateTo = $("input[name='dateTo_payment']").data("DateTimePicker");
					if(!dateTo.date() || !e.date) return;
					
					$scope.isNotUseDateRelate = true;
					dateTo.date(e.date.hours(dateTo.date().hours()).minutes(dateTo.date().minutes()));
					$scope.payment();
					console.log('t1');
				} else if($(e.target).attr('name') == 'dateTo_payment') {
					console.log('dateTo pay change');
					
					if($scope.isNotUseDateRelate) {
						$scope.isNotUseDateRelate = false;
						return;
					}
					
					var dateTo = e.date;
					if(!dateTo) return;
					
					var dateFrom = $("input[name='dateFrom_payment']").data("DateTimePicker");
					
					if(dateTo.isBefore(dateFrom.date())) {
						$scope.isNotUseDateRelate = true;
						dateFrom.date(dateTo.hours(dateFrom.date().hours()).minutes(dateFrom.date().minutes()));
					}
					$scope.payment();
					console.log('t1');
				}
			});
		});
	}
	
	$scope.initDate = function() {
		initDateEl();
		
		if(!$scope.formData.dateFrom) {			
			$scope.formData.dateFrom = angular.copy(today);
			$scope.formData.dateFrom.setHours(0,0,0,0);
		}
		if(!$scope.formData.dateTo) {
			$scope.formData.dateTo = angular.copy(today);
			$scope.formData.dateTo.setHours(23,59,0,0);			
		}
		$("input[name='dateFrom']").data("DateTimePicker").date($scope.formData.dateFrom);
		$("input[name='dateTo']").data("DateTimePicker").date($scope.formData.dateTo);
		
		//-----------------------: Payment
		$scope.formData.dateFromPayment = angular.copy(today);
		$scope.formData.dateFromPayment.setHours(0,0,0,0);
		$scope.formData.dateToPayment = angular.copy(today);
		$scope.formData.dateToPayment.setHours(23,59,0,0);
		$("input[name='dateFrom_payment']").data("DateTimePicker").date($scope.formData.dateFromPayment);
		$("input[name='dateTo_payment']").data("DateTimePicker").date($scope.formData.dateToPayment);
	}
	
});
