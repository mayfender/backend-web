angular.module('sbAdminApp').controller('SettingCtrl', function($rootScope, $scope, $stateParams, $http, $state, urlPrefix, toaster, loadSetting) {
	console.log(loadSetting);
	
	$scope.unlimtedTime = loadSetting.unlimtedTime;
	$scope.beforeHour = loadSetting.beforeHour;
	$scope.minuteToHour = loadSetting.minuteToHour;
	$scope.pricePerTime = loadSetting.pricePerTime;
	$scope.beforeHourPriceRate = loadSetting.beforeHourPriceRate;
	$scope.afterPriceRate = loadSetting.afterPriceRate;
	$scope.parkingSize = loadSetting.parkingSize;
	
	$scope.unlimtedTimeChange = function() {
		
	}
	
	$scope.update = function() {
		$http.post(urlPrefix + '/restAct/setting/updateSetting', {
			unlimtedTime: $scope.unlimtedTime,
			beforeHour: $scope.beforeHour,
			minuteToHour: $scope.minuteToHour,
			pricePerTime: $scope.pricePerTime,
			beforeHourPriceRate: $scope.beforeHourPriceRate,
			afterPriceRate: $scope.afterPriceRate,
			parkingSize: $scope.parkingSize
		}).then(function(data) {
			if(data.data.statusCode != 9999) {				
				if(data.data.statusCode == 2000) {
					$scope.existingUserErrMsg = "Username already exists";
				}else{
					$rootScope.systemAlert(data.data.statusCode);
				}
				
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Update Setting Success');
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
});