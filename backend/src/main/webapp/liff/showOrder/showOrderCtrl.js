angular.module('sbAdminApp').controller('ShowOrderCtrl', function($rootScope, $state, $stateParams, $scope, $timeout, $q, $http, $ngConfirm, $localStorage, $base64, urlPrefix) {
	
	console.log('ShowOrderCtrl');
	
	$scope.formData = {
		group: $stateParams.createdDateTime
	};
	$scope.checkBoxType = {
		bon3: true, bon2: true, lang2: true, 
		loy: true, pair4: true, pair5: true, runBon: true, runLang: true
	};
	
	$scope.selectItem = function(item, i) {
		$scope.index = i + 1;
		if(item._id == $scope.itemIdSelected) {			
			$scope.itemIdSelected = null;
		} else {			
			$scope.itemIdSelected = item._id;
		}
	}
	
	$scope.remove = function(type) {
		var result = window.confirm('ยืนยันการลบข้อมูล !!!');
		if(!result) return;
		
		$('#lps-overlay').css("display", "block");
		
		var params = {
			tab : 0,
			chkBoxType: $scope.checkBoxType,
			userId: $rootScope.userId,
			periodId: $rootScope.period['_id'],
			dealerId: $rootScope.workingOnDealer.id,
			createdDateTime: $scope.formData.group,
			deviceId: 2
		};
		
		if(type == 1) {
			params.orderId = $scope.itemIdSelected;
		} else {
			params.deleteGroup = $scope.formData.group;
		}
		
		$http.post(urlPrefix + '/restAct/order/editDelete', params).then(function(data) {
			$('#lps-overlay').css("display", "none");
			var result = data.data;
			if(result.statusCode != 9999) {
				informMessage('ลบข้อมูลไม่สำเร็จ');
				return;
			}

			$scope.orderData = result.orderData;
			$scope.createdDateGroup = result.createdDateGroup;
			$scope.orderNameLst = result.orderNameLst;
			$scope.totalPriceSumAll = result.totalPriceSumAll;
			
			$scope.itemIdSelected = null;
		}, function(response) {
			$('#lps-overlay').css("display", "none");
			informMessage('ลบข้อมูลไม่สำเร็จ');
		});
	}
	
	$scope.refreshData = function() {
		getData();
		$scope.itemIdSelected = null;
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
				informMessage('Server Error!!!');
				return;
			}
			
			$scope.orderData = result.orderData;
			$scope.createdDateGroup = result.createdDateGroup;
			$scope.orderNameLst = result.orderNameLst;
			$scope.totalPriceSumAll = result.totalPriceSumAll;
			
		}, function(response) {
			$('#lps-overlay').css("display", "none");
			informMessage('Server Error!!!');
		});
	}
	
	function informMessage(msg) {
		$ngConfirm({
		    title: 'แจ้งเตือน',
		    content: msg,
		    type: 'blue',
		    typeAnimated: true,
		    scope: $scope,
		    columnClass: 'col-xs-10 col-xs-offset-1',
		    buttons: {
			    OK: {
		        	text: 'OK',
		        	btnClass: 'btn-red'
		        }
		    }
		});	
	}
	
	
	
	getData();
	
});
