angular.module('sbAdminApp').controller('LpsCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, $state, $ngConfirm, urlPrefix) {
	
//	$scope.lp = '';
	$scope.section = 1;
	$scope.keyword = new Array();
	$scope.keyword[0] = '';
	$scope.keyword[1] = '';
	
	var kbsNo = [
		['0','4','8'],
		['1','5','9'],
		['2','6','กขค.'],
		['3','7','CLR']
	];
	var kbsLetter = [
		['ก','ง','ญ','ณ','น','ฟ','ว','อ'],
		['ข','จ','ฎ','ด','บ','ภ','ศ','ฮ'],
		['ฃ','ฉ','ฏ','ต','ป','ม','ษ'],
		['ค','ช','ฐ','ถ','ผ','ย','ส'],
		['ฅ','ซ','ฑ','ท','ฝ','ร','ห','123.'],
		['ฆ','ฌ','ฒ','ธ','พ','ล','ฬ','CLR']
		];
	
	$scope.kbs = kbsNo;
	
	$scope.kbPressed = function(val) {
		if(val == 'กขค.' || val == '123.') {
			if($scope.kbs.length == 4) {
				$scope.kbs = kbsLetter;				
			} else {
				$scope.kbs = kbsNo;				
			}
		} else if(val == 'logout') {
			logout();
		} else if(val == 'CLR') {
			$scope.keyword[$scope.section] = $scope.keyword[$scope.section].slice(0, -1);
			
			if($scope.section == 0) {
				findLps();
			}
		} else {
			$scope.keyword[$scope.section] = $scope.keyword[$scope.section] + val;
			
			if($scope.section == 0) {
				findLps();
			}
		}
	}
	
	$scope.changeSection = function(sect) {
		if(sect == 0) {
			$scope.section = 0;
			
			if($scope.keyword[1]) {
				findLps();				
			}
		} else {	
			$scope.section = 1;
			$scope.keyword[0] = '';
			$scope.keyword[1] = '';
			$scope.lpsList = {};
			$scope.isNotFound = false;
			$scope.kbs = kbsNo;
		}	
	}
	
	function findLps() {
		if(!$scope.keyword[0] && !$scope.keyword[1]) {
			$scope.lpsList = {};
			return;
		}
		
		$('#lps-overlay').css("display","block");
		$scope.lpsList = {};
		
		$http.post(urlPrefix + '/restAct/lps/find', {
			lpsGroup: $scope.keyword[0],
			lpsNumber: $scope.keyword[1]
		}, {
			ignoreLoadingBar: true
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$('#lps-overlay').css("display","none");
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.lpsTel = result.lpsTel;
			$scope.fields = result.fields;
			$scope.lpsList = result.lpsList;
			$scope.isNotFound = Object.keys(result.lpsList).length > 0 ? false : true;
			
			$('#lps-overlay').css("display","none");
		}, function(response) {
			$('#lps-overlay').css("display","none");
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.getDetail = function(productName, mapping, detail) {
		$scope.detail = detail;
		$scope.mapping = mapping;
		$scope.productName = productName;
		
		$ngConfirm({
//	    	columnClass: 'col-md-6 col-md-offset-3',
			title: 'Detail ',
			contentUrl: './views/lps/detail.html',
			icon: 'fa fa-info-circle',
			closeIcon: true,
			scope: $scope
		});
	}
	
	function logout() {
		$state.go('login', {action: 'logout'});
	}

});