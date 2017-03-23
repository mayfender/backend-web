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
	//			legend: { display: true },
				scales: {
			        yAxes: [{
			            ticks: {
			                beginAtZero:true
			            }
			        }]
				}
			}
	};
	
	$scope.formData = {};
	
	var today = new Date($rootScope.serverDateTime);
	$scope.formData.dateFrom = angular.copy(today);
	$scope.formData.dateTo = angular.copy(today);
	
	$scope.formData.dateFrom.setHours(0,0,0);
	$scope.formData.dateTo.setHours(23,59,59);
	
	$scope.colors = ['#ED402A', '#00A39F', '#A0B421', '#F0AB05'];
	
	var dateConf = {
    	format: 'dd/mm/yyyy',
	    autoclose: true,
	    todayBtn: true,
	    clearBtn: true,
	    todayHighlight: true,
	    language: 'th-en'
	}
	
	
	
	
	$scope.search = function() {
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
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.dateFromChange = function() {
		$scope.formData.dateTo = angular.copy($scope.formData.dateFrom);
		$("#dateTo_traceCount").datepicker('update', $filter('date')($scope.formData.dateTo, 'dd/MM/yyyy'));
		
		$scope.search();
	}
	
	
	
	
	
	
	
	
	
	
	
	/*$scope.line = {
		    labels: ['January', 'February', 'March', 'April', 'May', 'June', 'July'],
		    series: ['Series A', 'Series B'],
		    data: [
		      [65, 59, 80, 81, 56, 55, 40],
		      [28, 48, 40, 19, 86, 27, 90]
		    ],
		    onClick: function (points, evt) {
		      console.log(points, evt);
		    }
	    };*/
	
	
	
	
	
	
	
	
	
	$('#dateFrom_traceCount').datepicker(dateConf);
	$('#dateTo_traceCount').datepicker(dateConf);
	
	$scope.search();
	
});
