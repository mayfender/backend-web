angular.module('sbAdminApp').controller('PaymentCtrl', function($rootScope, $state, $scope, $base64, $http, $timeout, $translate, $q, $localStorage, $ngConfirm, $filter, urlPrefix, loadData) {
	console.log(loadData);
	console.log('PaymentCtrl');
	
	$scope.periods = loadData.periods;
	$scope.users = loadData.users;
	$scope.roles = [{id: 3, name: 'ผู้ดูแล'}, {id: 1, name: 'ลูกค้า'}];
	$scope.currPriceData;
	$scope.priceData;
	$scope.formData = {
			period: $scope.periods[0]._id,
			userRole: 3
	};
	$scope.checkBoxType = {
		bon3: true, bon2: true, lang2: true, 
		loy: true, pair4: true, pair5: true, runBon: true, runLang: true
	};
	
	var typeTitleList = [
		{'1':'รวม 3', 'percent':'percentBon3'}, {'2':'รวม 2 บน', 'percent':'percentBon2'}, {'3':'รวม 2 ล่าง', 'percent':'percentLang2'}, 
		{'4':'รวมลอย', 'percent':'percentLoy'}, {'41':'รวมแพ 4', 'percent':'percentPare4'}, {'42':'รวมแพ 5', 'percent':'percentPare5'}, 
		{'43':'รวมวิ่งบน', 'percent':'percentRunBon'}, {'44':'รวมวิ่งล่าง', 'percent':'percentRunLang'}, {'5':'รวมโต๊ด', 'percent':'percentTod'}
	];
	
	
	//---:
	$scope.changeRole = function() {
		$scope.paymentDataList = new Array();
		$scope.formData.userSearchId = null;
		getGroupUsers();
		getData();
	}
	
	$scope.changeOrderName = function() {		
		$scope.paymentDataList = new Array();
		
		if($scope.formData.userRole == 3) {
			if(!$scope.formData.orderName) return;	
		} else {
			if(!$scope.formData.userSearchId) return;				
		}
		
		$http.post(urlPrefix + '/restAct/order/getSumPayment', {
			orderName :$scope.formData.orderName,
			userId: $scope.formData.userSearchId,
			userRole: $scope.formData.userRole,
			periodId: $scope.formData.period,
			dealerId: $rootScope.workingOnDealer.id
		}, {
			ignoreLoadingBar: true
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			var typeObj;
			for(var i in typeTitleList) {
				typeObj = typeTitleList[i];
				
				$scope.paymentDataList.push({
					title: typeObj[Object.keys(typeObj)[0]],
					value: result.totalPriceSumAllMap[Object.keys(typeObj)[0]],
					percent: typeObj.percent
				});
			}
			
			//---:
			payCalculate();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	function payCalculate() {
		$scope.sum = 0;
		$scope.sumDiscount = 0;
		var obj;
		for(var i in $scope.paymentDataList) {
			obj = $scope.paymentDataList[i];
			obj.discount = ((100 - $scope.priceData[obj.percent]) / 100) * obj.value;
			
			$scope.sum += obj.value;
			$scope.sumDiscount += obj.discount;
		}
	}
	
	$scope.changePeriod = function() {
		getData();
	}
	
	$scope.changePriceList = function() {
		$scope.currPriceData = $filter('filter')($scope.priceList, {id: $scope.formData.priceList}, true)[0];		
		
		//---: Get first SendRound because all SendRound is the same percentage.
		var firstKey = Object.keys($scope.currPriceData.priceData)[0];
		$scope.priceData = $scope.currPriceData.priceData[firstKey];
		
		//---:
		payCalculate();
	}
	
	//---:
	function getData() {
		$scope.orderData = null;
		
		$http.post(urlPrefix + '/restAct/order/getData', {
			tab : 0,
			chkBoxType: $scope.checkBoxType,
			orderName :$scope.formData.orderName,
			userId: $scope.formData.userSearchId,
			userRole: $scope.formData.userRole,
			periodId: $scope.formData.period,
			dealerId: $rootScope.workingOnDealer.id
		}, {
			ignoreLoadingBar: true
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.orderData = result.orderData;
			$scope.totalPriceSum = result.totalPriceSum;
			$scope.totalPriceSumAll = result.totalPriceSumAll;
			$scope.orderNameLst = result.orderNameLst;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	function getGroupUsers() {
		if($scope.formData.userRole) {
			$scope.groupUsers = $filter('filter')($scope.users, {roleId: $scope.formData.userRole}, true);
		} else {
			$scope.groupUsers = $scope.users;
		}
	}
	
	function getPriceList() {
		$http.get(urlPrefix + '/restAct/receiver/getPriceList?dealerId=' + $rootScope.workingOnDealer.id + '&enabled=true').then(function(data){
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			console.log(result.priceList);
			
			$scope.priceList = result.priceList;
			$scope.currPriceData = $scope.priceList[0];
			
			//---:
			$scope.formData.priceList = $scope.currPriceData.id;
			$scope.changePriceList();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	//---:
	getData();
	getGroupUsers();
	getPriceList();
	
});