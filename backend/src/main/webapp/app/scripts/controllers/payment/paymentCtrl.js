angular.module('sbAdminApp').controller('PaymentCtrl', function($rootScope, $state, $scope, $base64, $http, $timeout, $translate, $q, $localStorage, $ngConfirm, $filter, urlPrefix, loadData) {
	$scope.periods = loadData.periods;
	$scope.users = loadData.users;
	$scope.roles = [{id: 3, name: 'ผู้ดูแล'}, {id: 1, name: 'ลูกค้า'}];
	$scope.currPriceData;
	$scope.priceData;
	$scope.sum2 = 0;
	$scope.sumDiscount2 = 0;
	$scope.tabActived = 0;
	$scope.adminSum = 0;
	$scope.formData = {
			period: $scope.periods[0]._id,
			userRole: 3
	};
	$scope.checkBoxType = {
		bon3: true, bon2: true, lang2: true, 
		loy: true, pair4: true, pair5: true, runBon: true, runLang: true
	};
	
	var typeTitleList = [
		{'1':'รวม 3 บน', 'percent':'percentBon3'}, {'5':'รวมโต๊ด', 'percent':'percentTod'}, {'2':'รวม 2 บน', 'percent':'percentBon2'}, 
		{'3':'รวม 2 ล่าง', 'percent':'percentLang2'}, {'4':'รวมลอย', 'percent':'percentLoy'}, {'41':'รวมแพ 4', 'percent':'percentPare4'}, 
		{'42':'รวมแพ 5', 'percent':'percentPare5'}, {'43':'รวมวิ่งบน', 'percent':'percentRunBon'}, {'44':'รวมวิ่งล่าง', 'percent':'percentRunLang'}
	];
	
	
	//---:
	$scope.changeRole = function() {
		$scope.paymentDataList = new Array();
		$scope.sum2 = 0;
		$scope.sumDiscount2 = 0;
		$scope.formData.userSearchId = null;
		$scope.formData.orderName = null;
	}
	
	$scope.changeOrderName = function() {
		if($scope.formData.userRole == 3) {
			if(!$scope.formData.orderName) {
				$scope.paymentDataList = null;
				$scope.sum2 = 0;
				$scope.sumDiscount2 = 0;
				return;	
			}
		} else {
			if(!$scope.formData.userSearchId) return;				
		}
		
		$http.post(urlPrefix + '/restAct/order/getSumPaymentByOne', {
			orderName: $scope.formData.orderName && $scope.formData.orderName.name,
			userId: $scope.formData.userSearchId && $scope.formData.userSearchId.id,
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
			
			$scope.paymentDataList = new Array();
			$scope.sum2 = 0;
			$scope.sumDiscount2 = 0;
			
			/*if($scope.formData.userRole == 3) {
				if(!$scope.formData.orderName) return;	
			} else {
				if(!$scope.formData.userSearchId) return;				
			}*/
			
			var typeObj, value, discount;
			for(var i in typeTitleList) {
				typeObj = typeTitleList[i];
				value = result.totalPriceSumAllMap[Object.keys(typeObj)[0]];
				
				$scope.paymentDataList.push({
					title: typeObj[Object.keys(typeObj)[0]],
					value: value,
					percent: typeObj.percent
				});
				
				$scope.sum2 += value;
				$scope.sumDiscount2 += discount;
			}
			
			//---:
			if($scope.formData.orderName) {
				$scope.formData.priceList = $scope.formData.orderName.price;				
			}
			if($scope.formData.userSearchId) {
				$scope.formData.priceList = $scope.formData.userSearchId.price;				
			}
			$scope.changePriceList();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.changePeriod = function() {
		$scope.formData.orderName = null;
		$scope.formData.userSearchId = null;		
		
		getSumPaymentAll();
	}
	
	$scope.changeTab = function(tab) {
		$scope.tabActived = tab;
	}
	
	$scope.changePrice = function(obj) {
		$http.post(urlPrefix + '/restAct/order/changePrice', {
			userId: obj.id,
			name: obj.name,
			priceId: obj.price,
			isCustomer: obj.isCustomer,
			dealerId: $rootScope.workingOnDealer.id
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
	
	$scope.changePriceList = function() {
		$scope.currPriceData = $filter('filter')($scope.priceList, {id: $scope.formData.priceList}, true)[0];		
		
		//---: Get first SendRound because all SendRound is the same percentage.
		var firstKey = Object.keys($scope.currPriceData.priceData)[0];
		$scope.priceData = $scope.currPriceData.priceData[firstKey];
		
		//---:
		payDiscountCal();
	}
	
	function payDiscountCal() {
		$scope.sumDiscount2 = 0;
		var obj;
		for(var i in $scope.paymentDataList) {
			obj = $scope.paymentDataList[i];
			obj.discount = ($scope.priceData[obj.percent] / 100) * obj.value;
			$scope.sumDiscount2 += obj.discount;
		}
	}
	
	function getPriceList() {
		$http.get(urlPrefix + '/restAct/receiver/getPriceList?dealerId=' + $rootScope.workingOnDealer.id + '&enabled=true').then(function(data){
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.priceList = result.priceList;
			$scope.currPriceData = $scope.priceList[0];
			
			//---:
			$scope.formData.priceList = $scope.currPriceData.id;
			$scope.changePriceList();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	function getSumPaymentAll() {
		$scope.isLoadProgress = true;
		$http.post(urlPrefix + '/restAct/order/getSumPaymentAll', {
			periodId: $scope.formData.period,
			dealerId: $rootScope.workingOnDealer.id
		},{
			ignoreLoadingBar: true
		}).then(function(data) {
			$scope.isLoadProgress = false;
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.paymentAdminData = result.paymentData['admin'];
			$scope.paymentCustomerData = result.paymentData['customer'];
			
			$scope.paymentAllData = $scope.paymentAdminData.concat($scope.paymentCustomerData);
			
			$scope.sum1 = result.paymentData['adminSum'] + result.paymentData['customerSum'];
			$scope.sumDiscount1 = result.paymentData['adminSumDiscount'] + result.paymentData['customerSumDiscount'];
			
			//---:
			var orderObj;
			for(var i in $scope.paymentAdminData) {
				orderObj = $scope.paymentAdminData[i];
				orderObj.desc = parseInt(i)+1 + '. ' + orderObj.name;
			}
			for(var i in $scope.paymentCustomerData) {
				orderObj = $scope.paymentCustomerData[i];
				orderObj.desc = parseInt(i)+1 + '. ' + orderObj.name;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
			$scope.isLoadProgress = false;
		});
	}
	
	//---:
	getPriceList();
	getSumPaymentAll();
	
});