angular.module('sbAdminApp').controller('TableManageCtrl', function($rootScope, $scope, $state, $http, $stateParams, $translate, $log, toaster, urlPrefix, loadTables) {
	
	var err_msg;
	var msg_err_delete_mType;
	$scope.tables = loadTables.tables;
	
	$translate('message.err.empty').then(function (mgs) {
		err_msg = mgs;
	});
	$translate('message.err.cann_delete_table_land').then(function (msg) {
		msg_err_delete_mType = msg;
	});
	
	$scope.addTable = function() {
		$scope.inserted = {
			name: ''
	    };
		$scope.tables.push($scope.inserted);
	};
	
	$scope.saveTable = function(data, mt, index) {
		 var msg;
		 if(!mt.id) {
			 msg = 'Save Table Success';			 
		 } else {
			 msg = 'Update Table Success';
		 }
		 
		 return $http.post(urlPrefix + '/restAct/table/saveAndUpdate', {
			id: mt.id,
			name: data.name
		 }).then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, msg);
			
			$scope.tables[index].id = data.data.id;			
		 }, function(response) {
			 $rootScope.systemAlert(response.status);
		 });
	};
	
	$scope.cancel = function(index) {
		$scope.tables.splice(index, 1);
	}
	
	$scope.checkName = function(data) {
		if (data == null || data == '') {
			return err_msg;
		}
	};
	
	$scope.removeTable = function(index, mt) {
		var deleteUser = confirm('Are you sure you want to delete this Item?');
	    if(!deleteUser) return;
	    
		return $http.get(urlPrefix + '/restAct/table/deleteTable?id=' + mt.id).then(function(data) {
			if(data.data.statusCode != 9999) {			
				if(data.data.statusCode == 5000) {
					$rootScope.systemAlert(data.data.statusCode, msg_err_delete_mType);
				} else {					
					$rootScope.systemAlert(data.data.statusCode);
				}
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Delete Table Success');
			
			$scope.cancel(index);
		 }, function(response) {
			 $rootScope.systemAlert(response.status);
		 });		
	};
	
});