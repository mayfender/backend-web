angular.module('sbAdminApp').controller('ShowOrderCtrl', function($rootScope, $state, $stateParams, $scope, $timeout, $q, $http, $ngConfirm, $localStorage, $base64, urlPrefix) {
	
	console.log('ShowOrderCtrl');
	
	$scope.formData = {
		group: $stateParams.createdDateTime
	};
	$scope.checkBoxType = {
		bon3: true, bon2: true, lang2: true, 
		loy: true, pair4: true, pair5: true, runBon: true, runLang: true
	};
	
	$scope.refreshData = function() {
		getData();
	}
	
	function getData() {
		$('#lps-overlay').css("display", "block");
		$http.post(urlPrefix + '/restAct/order/getData', {
			tab : 0,
			chkBoxType: $scope.checkBoxType,
			userId: $rootScope.userId,
			periodId: $rootScope.period['_id'],
			dealerId: $rootScope.workingOnDealer.id,
			deviceId: 2,
			createdDateTime: $scope.formData.group,
			orderName: $scope.formData.name
		}).then(function(data) {
			$('#lps-overlay').css("display", "none");
			var result = data.data;
			if(result.statusCode != 9999) {
				return;
			}
			
			$scope.orderData = result.orderData;
			$scope.createdDateGroup = result.createdDateGroup;
			$scope.orderNameLst = result.orderNameLst;
			$scope.totalPriceSumAll = result.totalPriceSumAll;
			
		}, function(response) {
			$('#lps-overlay').css("display", "none");
		});
	}
	
	
	
	
	getData();
	
});
