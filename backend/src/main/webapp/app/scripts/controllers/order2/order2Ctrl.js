angular.module('sbAdminApp').controller('Order2Ctrl', function($rootScope, $state, $scope, $base64, $http, $timeout, $translate, $q, $localStorage, $ngConfirm, $filter, urlPrefix, loadData) {
	console.log(loadData);
	console.log('Order2Ctrl');
	
	$scope.keyword = '';
	
	$scope.kbs = [
		['1', '4', '7','0'],
		['2', '5', '8','00'],
		['3', '6', '9', 'ลบ'],
		['=', 'x', '-']
		
		/*['-', '1','5','9'],
		['ลบ', '2','6','0'],
		['x', '3','7', '00'],
		['=', '4','8']*/
	];
	
	
	$scope.kbPressed = function(val) {
		if(val == 'ลบ') {
			$scope.keyword = $scope.keyword.slice(0, -1);
		} else {
			if($scope.keyword.length == 2) { 
				$scope.keyword += ',';
			} else if($scope.keyword.length > 2){
				var index = $scope.keyword.lastIndexOf(",") + 1
				var dummy = $scope.keyword.substring(index);
				
				console.log(dummy);
				
				if(dummy.length == 2) {
					$scope.keyword += ',';			
				}
			}
			
			$scope.keyword += val;
		}
		
		$('#valueBox').animate({scrollLeft: '+=1500'}, 500);		
	}
	
	$scope.clear = function() {
		$scope.keyword = '';
	}
	
	
});