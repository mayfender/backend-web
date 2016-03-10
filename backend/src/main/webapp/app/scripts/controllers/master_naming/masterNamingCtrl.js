angular.module('sbAdminApp').controller('MasterNamingCtrl', function($rootScope, $scope, $base64, $http, $translate, $stateParams, $filter, urlPrefix, loadPosition) {
	
	$scope.headerLabel = $stateParams.val;
	$scope.panelLabel = 'แสดง' + $stateParams.val;
	$scope.statuses = [{value: 0, text: 'เปิด'}, {value: 1, text: 'ปิด'}]; 
	$scope.datas = loadPosition.namingDetails;
	$scope.criteria = {};
	
	
	//-----------------------------------: Search Feature :-----------------------------------------
	$scope.search = function() {
		$scope.criteria.masterNamingId = $stateParams.id;
		
		$http.post(urlPrefix + '/restAct/masterNaming/findDetail',
    		$scope.criteria
    	).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.datas = data.data.namingDetails;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clearSearchForm = function() {
		$scope.criteria.displayValue = null;
		$scope.criteria.status = null;
		$scope.search();
	}
	
	//-----------------------------------: Add Edit Delete Features :-----------------------------------------
	$scope.addItem = function(d) {
		$scope.inserted = {
			status: 0
	    };
		$scope.datas.push($scope.inserted);
	};
	
	$scope.saveAndUpdateItem = function(data, d, index) {
		 var msg;
		 if(!d.namingDetId) {
			 msg = 'บันทึกข้อมูลสำเร็จ';			 
		 } else {
			 msg = 'แก้ใขข้อมูลสำเร็จ';
		 }
		 
		 return $http.post(urlPrefix + '/restAct/masterNaming/save', {
			masterNamingDetailId: d.namingDetId,
			masterNamingId: $stateParams.id,
			displayValue: data.displayValue,
			status: data.status
		 }).then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, msg);
			$scope.datas[index].namingDetId = data.data.namingDetId;
		 }, function(response) {
			 $rootScope.systemAlert(response.status);
		 });
	};
	
	$scope.removeItem = function(index, d) {
		var deleteItem = confirm('คุณต้องการลบข้อมูล ?');
	    if(!deleteItem) return;
	    
		return $http.get(urlPrefix + '/restAct/masterNaming/delete?id=' + d.namingDetId).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'ลบข้อมูลสำเร็จ');
			$scope.cancel(index);
		 }, function(response) {
			 $rootScope.systemAlert(response.status);
		 });		
	};
	
	$scope.cancel = function(index) {
		$scope.datas.splice(index, 1);
	}
	
	$scope.checkValue= function(data) {
		if (data == null || data == '') {
			return 'กรุณากรอกข้อมูล';
		}
	};
	
});