angular.module('sbAdminApp').controller('PaymentCtrl', function($rootScope, $state, $scope, $base64, $http, $timeout, $translate, $q, $localStorage, $ngConfirm, $filter, urlPrefix, loadData) {
	$scope.periods = loadData.periods;
	$scope.users = loadData.users;
	$scope.roles = [{id: 3, name: 'ผู้ดูแล'}, {id: 1, name: 'ลูกค้า'}];
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
		{'42':'รวมแพ 5', 'percent':'percentPare5'}, {'43':'รวมวิ่งบน', 'percent':'percentRunBon'}, {'44':'รวมวิ่งล่าง', 'percent':'percentRunLang'},
		{'60,61,62':'รวมปักบน', 'percent':'percentPugBon'}, {'63,64':'รวมปักล่าง', 'percent':'percentPugLang'}
	];
	
	
	//---:
	$scope.changeRole = function() {
		$scope.paymentDataList = new Array();
		$scope.sum2 = 0;
		$scope.sumDiscount2 = 0;
		$scope.formData.userSearchId = null;
		$scope.formData.orderName = null;
		$scope.formData.priceList = null;
	}
	
	$scope.changeOrderName = function(from) {
		var isBlocked = false;
		if($scope.formData.userRole == 3) {
			if(!$scope.formData.orderName) isBlocked = true;
		} else {
			if(!$scope.formData.userSearchId) isBlocked = true;
		}
		
		if(isBlocked) {
			$scope.paymentDataList = null;
			$scope.formData.priceList = null;
			$scope.sum2 = 0;
			$scope.sumDiscount2 = 0;
			return;
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
			
			var typeObj, value, discount, key, keyArr;
			for(var i in typeTitleList) {
				typeObj = typeTitleList[i];
				key = Object.keys(typeObj)[0];
				value = 0;
				
				if(key.indexOf(',') != -1) {
					keyArr = key.split(",");
					
					for(var i in keyArr) {
						value += result.totalPriceSumAllMap[keyArr[i]];
					}
				} else {
					value = result.totalPriceSumAllMap[key];					
				}
				
				$scope.paymentDataList.push({
					title: typeObj[key],
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
			$scope.changePriceList(from);
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
			
			if(obj.isCustomer) {
				$scope.formData.userRole = 1;
				$scope.formData.userSearchId = $filter('filter')($scope.paymentCustomerData, {id: obj.id}, true)[0];
				$scope.formData.orderName = null;
			} else {
				$scope.formData.userRole = 3;
				$scope.formData.orderName = $filter('filter')($scope.paymentAdminData, {name: obj.name}, true)[0];
				$scope.formData.userSearchId = null;
			}
			
			$scope.changeOrderName(1);
			
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.changePriceList = function(from) {	
		var firstPrice = $filter('filter')($scope.priceList, {id: $scope.formData.priceList}, true)[0];
		
		//---: Get first SendRound because all SendRound is the same percentage.
		var firstKey = Object.keys(firstPrice.priceData)[0];
		$scope.priceData = firstPrice.priceData[firstKey];
		
		//---:
		$scope.sumDiscount2 = 0;
		var obj;
		for(var i in $scope.paymentDataList) {
			obj = $scope.paymentDataList[i];
			obj.discount = ($scope.priceData[obj.percent] / 100) * obj.value;
			$scope.sumDiscount2 += obj.discount;
		}
		
		if(from == 1) {
			if($scope.formData.userSearchId) {
				$scope.sumDiscount1 -= $scope.formData.userSearchId.sumDiscount;
				$scope.sumDiscount1 += $scope.sumDiscount2;
				
				$scope.formData.userSearchId.sumDiscount = $scope.sumDiscount2;
			} else {
				$scope.sumDiscount1 -= $scope.formData.orderName.sumDiscount;
				$scope.sumDiscount1 += $scope.sumDiscount2;
				
				$scope.formData.orderName.sumDiscount = $scope.sumDiscount2;
			}
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
			
			//---:
			calSum1();
		}, function(response) {
			$rootScope.systemAlert(response.status);
			$scope.isLoadProgress = false;
		});
	}
	
	function calSum1() {
		$scope.sumDiscount1 = 0;
		$scope.sum1 = 0;
		var orderObj;
		for(var i in $scope.paymentAdminData) {
			orderObj = $scope.paymentAdminData[i];
			orderObj.desc = parseInt(i)+1 + '. ' + orderObj.name;
			
			$scope.sum1 += orderObj.sum;
			$scope.sumDiscount1 += orderObj.sumDiscount;
		}
		for(var i in $scope.paymentCustomerData) {
			orderObj = $scope.paymentCustomerData[i];
			orderObj.desc = parseInt(i)+1 + '. ' + orderObj.name;
			
			$scope.sum1 += orderObj.sum;
			$scope.sumDiscount1 += orderObj.sumDiscount;
		}
	}
	
	//---:
	getPriceList();
	getSumPaymentAll();
	
});