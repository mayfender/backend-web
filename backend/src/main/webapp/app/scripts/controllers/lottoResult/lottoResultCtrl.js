angular.module('sbAdminApp').controller('LottoResultCtrl', function($rootScope, $state, $scope, $base64, $http, $timeout, $translate, $q, $localStorage, $ngConfirm, $filter, urlPrefix, loadData) {
	console.log(loadData);
	console.log('LottoResultCtrl');
	$scope.periods = loadData.periods;
	$scope.formData = {};
	$scope.colors = ['rgb(126 239 126)', 'rgb(239 217 65)', 'rgb(243 124 124)', 'rgb(107 205 243)', 'rgb(249 161 70)'];
	
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
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	
	checkResult();
	
});