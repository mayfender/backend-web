angular.module('sbAdminApp').controller('LpsCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, $state, $ngConfirm, urlPrefix) {
	
//	$scope.lp = '';
	$scope.section = 1;
	$scope.keyword = new Array();
	$scope.keyword[0] = '';
	$scope.keyword[1] = '';
	
	var kbsNo = [
		['1','4','7','กขค.'],
		['2','5','8','CLR'],
		['3','6','9', 'logout']
	];
	var kbsLetter = [
		['ก','ง','ญ','ณ','น','ฟ','ว','อ'],
		['ข','จ','ฎ','ด','บ','ภ','ศ','ฮ'],
		['ฃ','ฉ','ฏ','ต','ป','ม','ษ','123.'],
		['ค','ช','ฐ','ถ','ผ','ย','ส','CLR'],
		['ฅ','ซ','ฑ','ท','ฝ','ร','ห'],
		['ฆ','ฌ','ฒ','ธ','พ','ล','ฬ','logout']
		];
	
	$scope.kbs = kbsNo;
	
	$scope.kbPressed = function(val) {
		if(val == 'กขค.' || val == '123.') {
			if($scope.kbs.length == 3) {
				$scope.kbs = kbsLetter;				
			} else {
				$scope.kbs = kbsNo;				
			}
		} else if(val == 'logout') {
			logout();
		} else if(val == 'CLR') {
			$scope.keyword[$scope.section] = $scope.keyword[$scope.section].slice(0, -1);
		} else {			
			$scope.keyword[$scope.section] = $scope.keyword[$scope.section] + val;
		}
	}
	
	$scope.changeSection = function(sect) {
		if(sect == 0) {
			$scope.section = 0;
		} else {			
			$scope.section = 1;
		}
	}
	
	function logout() {
//		login({action: 'logout'});
		$state.go('login', {action: 'logout'});
	}

});