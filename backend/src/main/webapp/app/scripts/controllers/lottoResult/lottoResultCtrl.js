angular.module('sbAdminApp').controller('LottoResultCtrl', function($rootScope, $state, $scope, $base64, $http, $timeout, $translate, $q, $localStorage, $ngConfirm, $filter, urlPrefix, loadData) {
	console.log(loadData);
	console.log('LottoResultCtrl');
	$scope.periods = loadData.periods;
	$scope.formData = {};
	
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
		$http.get(urlPrefix + '/restAct/order/checkResult?periodId=' + $scope.formData.period + '&dealerId=' + $rootScope.workingOnDealer.id).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.lottoResult = result.chkResultList;
			$scope.bon3Sum = 0;
			$scope.todSum = 0;
			$scope.bon2Sum = 0;
			$scope.lang2Sum = 0;
			$scope.loySum = 0;
			$scope.pair4Sum = 0;
			$scope.pair5Sum = 0;
			$scope.runBonSum = 0;
			$scope.runLangSum = 0;
			var lotto;
			for(var i in $scope.lottoResult) {
				lotto = $scope.lottoResult[i];
				
				$scope.bon3Sum += lotto.result3_price ? lotto.result3_price : 0;
				$scope.todSum += lotto.resultTod_todPrice ? lotto.resultTod_todPrice : 0;
				$scope.bon2Sum += lotto.resultBon2_price ? lotto.resultBon2_price : 0;
				$scope.lang2Sum += lotto.resultLang2_price ? lotto.resultLang2_price : 0;
				$scope.loySum += lotto.loy_price ? lotto.loy_price : 0;
				$scope.pair4Sum += lotto.pair4_price ? lotto.pair4_price : 0;
				$scope.pair5Sum += lotto.pair5_price ? lotto.pair5_price : 0;
				$scope.runBonSum += lotto.runBon_price ? lotto.runBon_price : 0;
				$scope.runLangSum += lotto.runLang_price ? lotto.runLang_price : 0;
			}
			
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	
	checkResult();
	
});