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
	
	
	//---:
	$scope.changeRole = function() {
		$scope.formData.userSearchId = null;
		getGroupUsers();
		getData();
	}
	
	$scope.changeOrderName = function() {
		getData();
	}
	
	$scope.changePeriod = function() {
		getData();
	}
	
	$scope.changePriceList = function() {
		$scope.currPriceData = $filter('filter')($scope.priceList, {id: $scope.formData.priceList}, true)[0];		
		
		//---: Get first SendRound because all SendRound is the same percentage.
		var firstKey = Object.keys($scope.currPriceData.priceData)[0];
		$scope.priceData = $scope.currPriceData.priceData[firstKey];
	}
	
	//---:
	function getData() {
		$scope.isLoadProgress = true;
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
			$scope.isLoadProgress = false;
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
			$scope.isLoadProgress = false;
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