angular.module('sbAdminApp').controller('PaymentCtrl', function($rootScope, $state, $scope, $base64, $http, $timeout, $translate, $q, $localStorage, $ngConfirm, $filter, urlPrefix, loadData) {
	$scope.periods = loadData.periods;
	$scope.users = loadData.users;
	$scope.roles = [{id: 3, name: 'ผู้ดูแล'}, {id: 1, name: 'ลูกค้า'}];
	$scope.currPriceData;
	$scope.priceData;
	$scope.sum = 0;
	$scope.sumDiscount = 0;
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
		$scope.sum = 0;
		$scope.sumDiscount = 0;
		$scope.formData.userSearchId = null;
		$scope.formData.orderName = null;
		getGroupUsers();
		getData();
	}
	
	$scope.changeOrderName = function() {
		$http.post(urlPrefix + '/restAct/order/getSumPaymentByOne', {
			orderName: $scope.formData.orderName,
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
			
			$scope.paymentDataList = new Array();
			$scope.sum = 0;
			$scope.sumDiscount = 0;
			
			if($scope.formData.userRole == 3) {
				if(!$scope.formData.orderName) return;	
			} else {
				if(!$scope.formData.userSearchId) return;				
			}
			
			var typeObj, value, discount;
			for(var i in typeTitleList) {
				typeObj = typeTitleList[i];
				value = result.totalPriceSumAllMap[Object.keys(typeObj)[0]];
				discount = ((100 - $scope.priceData[typeObj.percent]) / 100) * value;
				
				$scope.paymentDataList.push({
					title: typeObj[Object.keys(typeObj)[0]],
					value: value,
					percent: typeObj.percent,
					discount: discount
				});
				
				$scope.sum += value;
				$scope.sumDiscount += discount;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.changePeriod = function() {
		$scope.formData.orderName = null;
		$scope.formData.userSearchId = null;		
		
		$scope.changeOrderName();
		getData();
		getSumPaymentAll();
	}
	
	$scope.changeTab = function(tab) {
		$scope.tabActived = tab;
	}
	
	$scope.changePriceList = function() {
		$scope.currPriceData = $filter('filter')($scope.priceList, {id: $scope.formData.priceList}, true)[0];		
		
		//---: Get first SendRound because all SendRound is the same percentage.
		var firstKey = Object.keys($scope.currPriceData.priceData)[0];
		$scope.priceData = $scope.currPriceData.priceData[firstKey];
		
		//---:
		payDiscountCal();
	}
	
	//---:
	function getData() {
		$http.post(urlPrefix + '/restAct/order/getData', {
			tab : 44, 
			orderName :$scope.formData.orderName,
			userId: $scope.formData.userSearchId,
			userRole: $scope.formData.userRole,
			periodId: $scope.formData.period,
			dealerId: $rootScope.workingOnDealer.id
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.orderNameLst = new Array();
			var orderObj;
			for(var i in result.orderNameLst) {
				orderObj = result.orderNameLst[i];
				$scope.orderNameLst.push({id: orderObj, name: parseInt(i)+1 + '. ' + orderObj});
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	function getGroupUsers() {
		if($scope.formData.userRole) {
			$scope.groupUsers = angular.copy($filter('filter')($scope.users, {roleId: $scope.formData.userRole}, true));
			
			var groupObj;
			for(var i in $scope.groupUsers) {
				groupObj = $scope.groupUsers[i];
				groupObj.showname = parseInt(i)+1 + '. ' + groupObj.showname;
			}
		} else {
			$scope.groupUsers = $scope.users;
		}
	}
	
	function payDiscountCal() {
		$scope.sumDiscount = 0;
		var obj;
		for(var i in $scope.paymentDataList) {
			obj = $scope.paymentDataList[i];
			obj.discount = ((100 - $scope.priceData[obj.percent]) / 100) * obj.value;
			$scope.sumDiscount += obj.discount;
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
			
			$scope.paymentAllData = result.paymentData['admin'];
			$scope.adminSum = result.paymentData['adminSum'];
			
//			console.log(result.paymentData);
		}, function(response) {
			$rootScope.systemAlert(response.status);
			$scope.isLoadProgress = false;
		});
	}
	
	//---:
	getData();
	getGroupUsers();
	getPriceList();
	getSumPaymentAll();
	
});