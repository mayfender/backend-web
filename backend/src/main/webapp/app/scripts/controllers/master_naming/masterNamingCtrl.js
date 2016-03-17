angular.module('sbAdminApp').controller('MasterNamingCtrl', function($rootScope, $scope, $base64, $http, $translate, $stateParams, urlPrefix, loadPosition) {
	
	$scope.headerLabel = $stateParams.val;
	$scope.panelLabel = 'แสดง' + $stateParams.val;
	
	$scope.statuses = [{value: 0, text: 'เปิด'}, {value: 1, text: 'ปิด'}]; 
	$scope.datas = loadPosition.namingDetails;
	
	
	$scope.addItem = function(d) {
		$scope.inserted = {
			name: '',
			status: 0
	    };
		$scope.datas.push($scope.inserted);
	};
	
	$scope.saveItem = function(data, d, index) {
		 /*var msg;
		 if(!d.id) {
			 msg = 'Save Menu Type Success';			 
		 } else {
			 msg = 'Update Menu Type Success';
		 }
		 
		 return $http.post(urlPrefix + '/restAct/menuType/saveAndUpdate', {
			id: d.id,
			name: data.name,
			isEnabled: (data.isEnabled == 1 || data.isEnabled == true) ? true : false,
			iconColor: d.iconColorNew
		 }).then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, msg);
			
			$scope.menuTypes[index].id = data.data.id;			
			d.iconColor = d.iconColorNew;
		 }, function(response) {
			 $rootScope.systemAlert(response.status);
		 });*/
	};
	
	
	$scope.removeItem = function(index, d) {
		/*var deleteUser = confirm('Are you sure you want to delete this Item?');
	    if(!deleteUser) return;
	    
		return $http.get(urlPrefix + '/restAct/menuType/deleteMenuType?id=' + d.id).then(function(data) {
			if(data.data.statusCode != 9999) {			
				if(data.data.statusCode == 5000) {
					$rootScope.systemAlert(data.data.statusCode, msg_err_delete_mType);
				} else if(data.data.statusCode == 5001) {
					$rootScope.systemAlert(data.data.statusCode, msg_err_delete_mType_parent);
				} else {					
					$rootScope.systemAlert(data.data.statusCode);
				}
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Delete Menu Type Success');
			
			$scope.cancel(index);
		 }, function(response) {
			 $rootScope.systemAlert(response.status);
		 });		*/
	};
	
	
	
	$scope.cancel = function(index) {
		$scope.datas.splice(index, 1);
	}
	
	$scope.checkValue= function(data) {
		if (data == null || data == '') {
			return err_msg;
		}
	};
	
	
});