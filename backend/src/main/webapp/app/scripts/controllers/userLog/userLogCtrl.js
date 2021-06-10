angular.module('sbAdminApp').controller('UserLogCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, $state, $ngConfirm, FileUploader, urlPrefix, loadData) {
		
	$scope.rolesConstant = [
		{authority:'ROLE_ADMIN', name:'Admin', id: 3},
		{authority:'ROLE_MANAGER', name:'Manager', id: 4},
		{authority:'ROLE_SUPERVISOR', name:'Supervisor', id: 2},
		{authority:'ROLE_USER', name:'User', id: 1}
	];
	$scope.formData = {currentPage: 1};
	$scope.maxSize = 5;
	$scope.itemsPerPage = 10;
	
	var today = new Date($rootScope.serverDateTime);
	
	$scope.getUsers = function() {
		$scope.users = new Array();
		if($scope.formData.role) {			
			for(var x in $scope.allUsers) {
				if($scope.allUsers[x].authorities[0].authority != $scope.formData.role.authority) continue;
				$scope.users.push($scope.allUsers[x]);
			}
		}
		$scope.getLog();
	}
	
	$scope.getLog = function() {
		var dateFrom = $("input[name='dateFrom']").data("DateTimePicker").date();
		var dateTo = $("input[name='dateTo']").data("DateTimePicker").date();
		
		if(dateFrom) {
			$scope.formData.dateFrom = dateFrom.toDate();
			$scope.formData.dateFrom.setSeconds(0);
			$scope.formData.dateFrom.setMilliseconds(0);
		} else {
			$scope.logs = null;
			return;
		}
		if(dateTo) {
			$scope.formData.dateTo = dateTo.toDate();
			$scope.formData.dateTo.setSeconds(59);
			$scope.formData.dateTo.setMilliseconds(999);
		} else {
			$scope.logs = null;
			return;	
		}
		
		$http.post(urlPrefix + '/restAct/userLog/getLog', {
			userGroup: $scope.formData.role && $scope.formData.role.id,
			currentPage: $scope.formData.currentPage,
	    	itemsPerPage: $scope.itemsPerPage,
			dateFrom: $scope.formData.dateFrom,
			dateTo: $scope.formData.dateTo,
			userId: $scope.formData.userId,
			actionName: $scope.formData.actionName,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			$scope.logs = result.logs;
			$scope.totalItems = result.totalItems;
			$scope.actionList = result.actionList;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.acionDesc = function(details) {
		$scope.details = JSON.stringify(JSON.parse(details), null, 2);
		$ngConfirm({
    		title: 'Action Details',
    		icon: 'fa fa-info-circle',
    		closeIcon: true,
    		backgroundDismiss: true,
    		columnClass: 'col-xs-8 col-xs-offset-2',
    		contentUrl: './views/userLog/details.html',
    		scope: $scope,
    		buttons: {
    			OK: {
    				text: 'ปิด',
    				action: function() {
    					
    				}
    			} 
    		}
    	});
	}
	
	function initDate() {
		$scope.formData.dateFrom = angular.copy(today);
		$scope.formData.dateFrom.setHours(0,0,0,0);
		
		$scope.formData.dateTo = angular.copy(today);
		$scope.formData.dateTo.setHours(23,59,0,0);
		
		$("input[name='dateFrom']").data("DateTimePicker").date($scope.formData.dateFrom);
		$("input[name='dateTo']").data("DateTimePicker").date($scope.formData.dateTo);
	}
	
	//----:
	var isInit = true;
	$('.input-daterange .dtPicker').each(function() {
		$(this).datetimepicker({
			format: 'DD/MM/YYYY HH:mm',
			showClear: false,
			showTodayButton: true,
			locale: 'th'
		}).on('dp.hide', function(e){
			
		}).on('dp.change', function(e){
			if($(e.target).attr('name') == 'dateFrom') {
				console.log('dateFrom change');
				
				var dateTo = $("input[name='dateTo']").data("DateTimePicker");
				if(!dateTo.date() || !e.date) {
					$scope.logs = null;
					return;
				}
				
				dateTo.date(e.date.hours(dateTo.date().hours()).minutes(dateTo.date().minutes()));
			} else if($(e.target).attr('name') == 'dateTo') {
				console.log('dateTo change');
				
				var dateTo = e.date;
				if(!dateTo) {
					$scope.logs = null;
					return;
				}
				
				var dateFrom = $("input[name='dateFrom']").data("DateTimePicker");
				
				if(dateTo.isBefore(dateFrom.date())) {
					dateFrom.date(dateTo.hours(dateFrom.date().hours()).minutes(dateFrom.date().minutes()));
				}
			}
			
			if(!isInit) {
				$scope.getLog();			
			}
			isInit = false;
		});
	});
	
	
	
	//----:
	initDate();
	
	//----:
	$scope.totalItems = loadData.totalItems;
	$scope.allUsers = loadData.users;
	$scope.logs = loadData.logs;
	$scope.actionList = loadData.actionList;
	$scope.allUsersMap = {};
	
	for(var x in $scope.allUsers) {
		$scope.allUsersMap[$scope.allUsers[x].id] = $scope.allUsers[x];
	}
	
});