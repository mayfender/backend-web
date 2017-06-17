angular.module('sbAdminApp').controller('PaymentToUsCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, $state, urlPrefix, loadData) {
	
	console.log('Payment to us ');
	$scope.bankIcon = urlPrefix + '/app/images/krungsri.png';
	$scope.items = [{id: 1, accNo: '333-333333-3'}, {id: 2, accNo: '444-444444-4'}];
	$scope.items = loadData.bankAccs;
	$scope.customerComInfo = loadData.customerComInfo;
	$scope.customerAddress = loadData.customerAddress;
	$scope.customerEmail = loadData.customerEmail;
	
	$scope.saveItem = function(data, item, index) {
		console.log(data);
		$http.post(urlPrefix + '/restAct/contact/saveAccNo', {
			id: item.id,
			accNo: data.accNo
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$scope.cancelNewItem(item);
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			if(!item.id) {
				item.id = result.id;
			}
		}, function(response) {
			$scope.cancelNewItem(item);
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.updateCusCompany = function() {
		$http.post(urlPrefix + '/restAct/contact/updateCusCompany', {
			customerComInfo: $scope.customerComInfo,
			customerAddress: $scope.customerAddress
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.updateCusCompanyEmail = function() {
		$http.post(urlPrefix + '/restAct/contact/updateCusCompanyEmail', {
			customerEmail: $scope.customerEmail
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.removeItem = function(index, id) {
		var deleteUser = confirm('ยืนยันการลบข้อมูล');
	    if(!deleteUser) return;
	    
	    $http.get(urlPrefix + '/restAct/contact/deleteAccNo?id='+id).then(function(data) {
	    			
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.items.splice(index, 1);
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	};
	
	$scope.addItem = function() {
        $scope.inserted = {accNo: ''};
        $scope.items.push($scope.inserted);
    };
    
    $scope.cancelNewItem = function(item) {
    	for(i in $scope.items) {
    		if($scope.items[i] == item) {
    			$scope.items.splice(i, 1);
    		}
    	}
    }

});