angular.module('sbAdminApp').controller('SearchMemberTypeCtrl', function($rootScope, $scope, $base64, $http, $translate, urlPrefix, loadMemberType) {
	
	
	$scope.datas = loadMemberType.memberTyps;
	$scope.$parent.headerTitle = 'แสดงประเภทสมาชิก';
	$scope.$parent.iconBtn = 'fa-plus-square';
	$scope.$parent.url = 'add';
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/memberType/findMemberType',
			$scope.$parent.formData
		).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.datas = data.data.memberTyps;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clearSearchForm = function() {
		$scope.$parent.formData.memberTypeName = null;
		$scope.$parent.formData.durationType = null;
		$scope.$parent.formData.status = null;
		$scope.search();
	}
	
	
});