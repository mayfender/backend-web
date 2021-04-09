angular.module('sbAdminApp').controller('LottoResultCtrl', function($rootScope, $state, $scope, $http, $timeout, $filter, urlPrefix, loadData) {
	console.log(loadData);
	console.log('LottoResultCtrl');
	$scope.periods = loadData.periods;
	$scope.formData = {};
	$scope.colors = ['rgb(126 239 126)', 'rgb(243 124 124)', 'rgb(239 217 65)', 'rgb(107 205 243)', 'rgb(249 161 70)'];
	
	if($scope.periods && $scope.periods.length > 0) {
		var p = $scope.periods[0];
		$scope.formData.period = p._id;
		$scope.formData.result2 = p.result2;
		$scope.formData.result3 = p.result3;
	}
	
	$scope.changePeriod = function() {
		var p = $filter('filter')($scope.periods, {_id: $scope.formData.period})[0];
		$scope.formData.result2 = p.result2;
		$scope.formData.result3 = p.result3;
		checkResult();
	}
	
	function checkResult() {
		$http.get(urlPrefix + '/restAct/order/checkResult?periodId=' + $scope.formData.period + 
				'&dealerId=' + $rootScope.workingOnDealer.id + 
				'&userId=' + $rootScope.userId).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				alert(result.statusCode);
				return;
			}
			
			$scope.haveWin = false;
			$scope.lottoResult = result.chkResultList;
			$scope.titles = [
				{title: '3 ตัวบน', field: 'result3_price', sum: 0, show: false},
				{title: '3 ตัวโต๊ด', field: 'resultTod_todPrice', sum: 0, show: false},
				{title: '2 ตัวบน', field: 'resultBon2_price', sum: 0, show: false},
				{title: '2 ตัวล่าง', field: 'resultLang2_price', sum: 0, show: false},
				{title: 'ลอย', field: 'loy_price', sum: 0, show: false},
				{title: 'แพ 4', field: 'pair4_price', sum: 0, show: false},
				{title: 'แพ 5', field: 'pair5_price', sum: 0, show: false},
				{title: 'วิ่งบน', field: 'runBon_price', sum: 0, show: false},
				{title: 'วิ่งล่าง', field: 'runLang_price', sum: 0, show: false},
				{title: 'ปักบน', field: 'resultPugBon_price', sum: 0, show: false},
				{title: 'ปักล่าง', field: 'resultPugLang_price', sum: 0, show: false}
			];
			$scope.resultFormated = new Array();
			var dummyResult, lotto, title, chkExit, objDummy;
			for(var i in $scope.lottoResult) {
				lotto = $scope.lottoResult[i];
				
				//----
				for(var k in $scope.titles) {
					title = $scope.titles[k];
					dummyResult = lotto[title.field];
					
					if(dummyResult > 0) {
						$scope.haveWin = true;
						title.show = true;
						title.sum += dummyResult;
						
						chkExit = $filter('filter')($scope.resultFormated, {name: lotto.name}, true)[0];
						if(chkExit == null) {
							objDummy = {name: lotto.name, isCustomer: lotto.isCustomer};
							objDummy[title.field] = dummyResult;
							$scope.resultFormated.push(objDummy);
						} else {
							chkExit[title.field] = dummyResult;
						}
					}
				}
			}
			
			
			
		}, function(response) {
			alert(response.status);
		});
	}
	
	
	checkResult();
	
});