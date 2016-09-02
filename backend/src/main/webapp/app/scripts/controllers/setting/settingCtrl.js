angular.module('sbAdminApp').controller('SettingCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, $state, urlPrefix, loadData) {
	console.log(loadData);
	
	var setting = loadData.setting;
	$scope.companyName = setting && setting.companyName;
	
	$scope.update = function() {
		$http.post(urlPrefix + '/restAct/setting/update', {
			companyName: $scope.companyName
		}).then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.companyName = $scope.companyName;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
});