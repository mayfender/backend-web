'use strict';
/**
 * @ngdoc function
 * @name sbAdminApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the sbAdminApp
 */
angular.module('sbAdminApp').controller('ChartCtrl', function ($rootScope, $scope, $timeout, $http, $translate, urlPrefix, findAllYears) {
	
	Chart.defaults.global.colours = [
	   '#46BFBD', // green
	   '#4D5360',  // dark grey
       '#F7464A', // red
       '#97BBCD', // blue
       '#FDB45C', // yellow
       '#949FB1', // grey
       '#DCDCDC' // light grey	
    ];
	
	var titles = {};	
	var units = {};
	var year = findAllYears.result.years[0];
	var labels;
	var labelsLineInit = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', 
					      '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', 
					      '21', '22', '23', '24', '25', '26', '27', '28', '29', '30', '31'];
	var dataLineInit = [[0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	           	    	 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	           	    	 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]];
	
	$translate('chart.header_panel.vehicle').then(function (headlineVehicle) {
		titles.vehicle = headlineVehicle;
		$scope.title = headlineVehicle;
	});
	$translate('chart.header_panel.money').then(function (headlineMoney) {
		titles.money = headlineMoney;
	});
	$translate('chart.unit.vehicle').then(function (unitVehicle) {
		units.vehicle = unitVehicle;
		$scope.unit = unitVehicle;
	});
	$translate('chart.unit.money').then(function (unitMoney) {
		units.money = unitMoney;
	});
	
	
	if($translate.use() == 'en') {
		labels = {months: {'Jan.':'01', 'Feb.':'02', 'Mar.':'03', 'Apr.':'04', 'May.':'05', 'June.':'06', 
	           			   'July.':'07', 'Aug.':'08', 'Sept.':'09', 'Oct.':'10', 'Nov.':'11', 'Dec.':'12'}};
	}else if($translate.use() == 'th'){		
		labels = {months: {'ม.ค.':'01', 'ก.พ.':'02', 'มี.ค.':'03', 'เม.ย.':'04', 'พ.ค.':'05', 'มิ.ย.':'06', 
				           'ก.ค.':'07', 'ส.ค.':'08', 'ก.ย.':'09', 'ต.ค.':'10', 'พ.ย.':'11', 'ธ.ค.':'12'}};
	}
	
	var keys = [];
	for(var k in labels.months) keys.push(k);
	
	$scope.line = {
    	labels: labelsLineInit,
	    
   	    data: dataLineInit,
	   	
   	    onClick: function (points, evt) {}
    };
		
	$scope.bar = {
		labels: keys,
		data: [findAllYears.result.monthValues || [0,0,0,0,0,0,0,0,0,0,0,0]],
		
	    onClick: function (points, evt) {
	    	var point = points[0];
	    	if(point == null || point.value == null || point.value == 0) return;
	    	
	    	$scope.line.colours = [{
	    		fillColor: point.fillColor, 
	    		strokeColor: point.strokeColor,
	    		pointColor: point.strokeColor,
	            pointStrokeColor: "#fff",
	            pointHighlightFill: "#fff",
	            pointHighlightStroke: point.strokeColor,
	    		
	    	}];
	    	
	    	var action;
	    	if($scope.title == titles.money) 
	    		action = 'reportMoneyDay';
	    	else 
	    		action = 'reportVehicleDay';
	    	
	    	var monthNum = labels.months[point.label];
	    	
	    	$http.get(urlPrefix + '/restAct/report/' + action + '?year=' + year + '&month=' + monthNum)
    		.then(function(data) {
        		if(data.data.statusCode != 9999) {
        			$rootScope.systemAlert(data.data.statusCode);
        			return;
        		}	    		
        		
        		$scope.line.data = [data.data.result.values];
        		
        		var numDays = new Date(year, monthNum, 0).getDate();
        		var days = [];
        		for (var i = 1; i <= numDays; i++) days.push(i);
        		$scope.line.labels = days;
        		
    	    }, function(response) {
    	    	$rootScope.systemAlert(response.status);
    	    });
	    }
	};
	
	$scope.donut = {
		labels: findAllYears.result.years,
		data: findAllYears.result.values,
    	
    	onClick: function (points, evt) {
    		var point = points[0];
	    	if(point == null || point.value == null || point.value == 0) return;
    		
    		var fillColor = point.fillColor;
    		var indexOf = fillColor.lastIndexOf(',') + 1;
    		var fillColorResult = fillColor.substring(0, indexOf) + '0.2)';
    		year = point.label;
    		$scope.bar.colours = [{ fillColor: fillColorResult, strokeColor: fillColor }];
    		
    		var action;
	    	if($scope.title == titles.money) 
	    		action = 'reportMoneyMonth';
	    	else 
	    		action = 'reportVehicleMonth';
    		
    		$http.get(urlPrefix + '/restAct/report/' + action + '?year=' + year)
    		.then(function(data) {
        		if(data.data.statusCode != 9999) {
        			$rootScope.systemAlert(data.data.statusCode);
        			return;
        		}	    		
        		
        		$scope.bar.data = [data.data.result.values];
        		$scope.line.data = dataLineInit;
        		$scope.line.labels = labelsLineInit;
    	    }, function(response) {
    	    	$rootScope.systemAlert(response.status);
    	    });
  	    }
    };
	
	$scope.chartVehicle = function() {
		if($scope.title == titles.vehicle) return;
		
		$scope.title = titles.vehicle;
		$scope.unit = units.vehicle;
		
		$http.get(urlPrefix + '/restAct/report/reportVehicleYear')
		  .then(function(data){
      		if(data.data.statusCode != 9999) {
      			$rootScope.systemAlert(data.data.statusCode);
      		}
      		
      		setChangeModeResult(data.data);
		  }, function(response) {
			  $rootScope.systemAlert(response.status);
        });
	}
	
	$scope.chartMoney = function() {
		if($scope.title == titles.money) return;
		
		$scope.title = titles.money;
		$scope.unit = units.money;
			
		$http.get(urlPrefix + '/restAct/report/reportMoneyYear')
		  .then(function(data){
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    		}
    		
      		setChangeModeResult(data.data);
		  }, function(response) {
			  $rootScope.systemAlert(response.status);
      });
	}
	
	function setChangeModeResult(data) {
		year = data.result.years[0];
		$scope.donut.labels = data.result.years;
  		$scope.donut.data = data.result.values;
  		$scope.bar.data = [data.result.monthValues || [0,0,0,0,0,0,0,0,0,0,0,0]];
  		$scope.line.data = dataLineInit;
  		$scope.line.labels = labelsLineInit;
  		$scope.bar.colours = [Chart.defaults.global.colours[0]];
  		$scope.line.colours = [Chart.defaults.global.colours[0]];
	}
	
    
});