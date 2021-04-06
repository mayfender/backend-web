angular.module('sbAdminApp').controller('ShowOrderCtrl', function($rootScope, $state, $stateParams, $scope, $timeout, $q, $http, $ngConfirm, $localStorage, $base64, urlPrefix) {
	
	$scope.$parent.$parent.isOverTime = $stateParams.isOverTime;
	var itemCDTSelected;
	var sendRoundData = $stateParams.sendRoundData;
	$scope.pageStatus = 1;
	if(sendRoundData) {
		$scope.sendRoundDateTime = sendRoundData.srDateTime;
		$scope.sendRoundMsg = sendRoundData.srMsg;
	}	
	
	$scope.formData = {
		group: $stateParams.createdDateTime
	};
	$scope.checkBoxType = {
		bon3: true, bon2: true, lang2: true, 
		loy: true, pair4: true, pair5: true, 
		runBon: true, runLang: true, 
		pugBon3: true, pugBon2: true, pugBon1: true,
		pugLang2: true, pugLang1: true
	};
	
	$scope.selectItem = function(item, i) {
		$scope.index = i + 1;
		if(item._id == $scope.itemIdSelected) {			
			$scope.itemIdSelected = null;
		} else {			
			$scope.itemIdSelected = item._id;
			itemCDTSelected = item.createdDateTime;
		}
	}
	
	$scope.checkResult = function() {
		if($scope.pageStatus == 1) {
			$scope.pageStatus = 2;
			$state.go("home.order.showOrder.lottoResult", {});
		} else {
			$scope.pageStatus = 1;			
			$state.go("home.order.showOrder", {});
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
			periodDateTime: $rootScope.period.periodDateTime,
			deviceId: 2
		};
		
		if(type == 1) {
			params.orderId = $scope.itemIdSelected;
			params.createdDateTimeDelete = itemCDTSelected;
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

			if(result.notAllowRemove) {
				alert('ไม่สามารถลบได้ เนื่องจากเกินเวลาของรอบส่ง !!!');
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
	
	
	//---:
	getData();
	
	//---:
	if($stateParams.restrictList && Object.keys($stateParams.restrictList).length > 0) {
		var msg = '', msgDesc = '';
		var errCode;
		for (var key in $stateParams.restrictList) {
			errCode = $stateParams.restrictList[key];
			msgDesc = '';
			
			if(errCode == 2) {
				msgDesc = 'บน';
			} else if(errCode == 3) {
				msgDesc = 'ล่าง';				
			} else if(errCode == 4) {
				msgDesc = 'บนล่าง';				
			}
			msg += "ปิด " + key + " " + msgDesc + "<br />";
		}
		informMessage(msg);
	}
	
});
