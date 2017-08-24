angular.module('sbAdminApp').controller('ForecastCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, FileUploader, urlPrefix, loadData) {
	
	$scope.headers = loadData.headers;
	$scope.users = loadData.users;
	$scope.forecastDatas = loadData.forecastDatas;	
	$scope.totalItems = loadData.totalItems;
	$scope.uploadTemplates = loadData.uploadTemplates;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;	
	$scope.dateColumnNames = [
	                          {col: 'createdDateTime', text:'วันที่บันทึก'}, 
	                          {col: 'appointDate', text:'วันนัดชำระ'}
	                          ];

	$scope.formData.dateColumnName = $stateParams.dateColumnName;
	
	var today = new Date($rootScope.serverDateTime);
	$scope.formData.dateFrom = angular.copy(today);
	$scope.formData.dateTo = angular.copy(today);
	$scope.formData.dateFrom.setHours(0,0,0,0);
	$scope.formData.dateTo.setHours(23,59,59,999);
	
	$scope.column = $stateParams.columnName;
	$scope.order = $stateParams.order;
	var colToOrder = angular.copy($scope.column);
	var lastCol = angular.copy($scope.column);
	
	function searchCriteria() {
		if($scope.formData.dateTo) {
			$scope.formData.dateTo.setHours(23,59,59,999);			
		}
		
		var criteria = {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			productId: $rootScope.workingOnProduct.id,
			columnName: colToOrder,
			order: $scope.order,
			keyword: $scope.formData.keyword,
			owner: $scope.formData.owner,
			dateColumnName: $scope.formData.dateColumnName,
			dateFrom: $scope.formData.dateFrom,
			dateTo: $scope.formData.dateTo
		}
		
		return criteria;
	}
	
	$scope.search = function(isNewLoad) {
		$http.post(urlPrefix + '/restAct/forecast/forecastResult', searchCriteria()).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.forecastDatas = result.forecastDatas;	
			$scope.totalItems = result.totalItems;
			
			if(isNewLoad) {				
				$scope.headers = result.headers;
				$scope.users = result.users;
				
				$scope.uploadTemplates = result.uploadTemplates;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.exportResult = function(templateId, isLastOnly) {
		var criteria = searchCriteria();
		criteria.isFillTemplate = true;
		criteria.isLastOnly = isLastOnly;
		criteria.id = templateId; 
		
		$http.post(urlPrefix + '/restAct/forecastResultReport/download', criteria, {responseType: 'arraybuffer'}).then(function(data) {	
			var a = document.createElement("a");
			document.body.appendChild(a);
			a.style = "display: none";
			
			var fileName = decodeURIComponent(data.headers('fileName'));
			var file = new Blob([data.data]);
	        var url = URL.createObjectURL(file);
	        
	        a.href = url;
	        a.download = fileName;
	        a.click();
	        a.remove();
	        
	        window.URL.revokeObjectURL(url); //-- Clear blob on client
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clearSearchForm = function(isNewLoad) {
		$scope.formData.keyword = null;
		$scope.column = 'createdDateTime';
		colToOrder = 'createdDateTime';
		$scope.order = 'desc';
		$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;
		$scope.formData.dateColumnName = $stateParams.dateColumnName;
		
		var today = new Date($rootScope.serverDateTime);
		$scope.formData.dateFrom = angular.copy(today);
		$scope.formData.dateTo = angular.copy(today);
		$scope.formData.dateFrom.setHours(0,0,0,0);
		$scope.formData.dateTo.setHours(23,59,59,999);
		
		$scope.actionCodeId = null;
		$scope.resultCodeId = null;
		$scope.search(isNewLoad);
	}
	
	$scope.columnOrder = function(col, prefix) {
		$scope.column = col;
		
		if(prefix) {			
			colToOrder = prefix + '.' + col;
		} else {
			colToOrder = col;
		}
		
		if(lastCol != $scope.column) {
			$scope.order = null;
		}
		
		if($scope.order == 'desc') {			
			$scope.order = 'asc';
		} else if($scope.order == 'asc' || $scope.order == null) {
			$scope.order = 'desc';
		}
		
		lastCol = $scope.column;
		$scope.search();
	}
	
	$scope.dateColumnNameChanged = function() {
		$scope.formData.dateColumnName || ($scope.formData.dateFrom = null); ($scope.formData.dateTo = null);
	}
	
	$scope.dateFromChange = function() {
		$scope.formData.dateTo = angular.copy($scope.formData.dateFrom);
		$("#dateTo").datepicker('update', $filter('date')($scope.formData.dateTo, 'dd/MM/yyyy'));
	}
	
	$scope.dateToChange = function() {
		if($scope.formData.dateFrom.getTime() > $scope.formData.dateTo.getTime()) {	
			$scope.formData.dateFrom = angular.copy($scope.formData.dateTo);
			$("#dateFrom").datepicker('update', $filter('date')($scope.formData.dateFrom, 'dd/MM/yyyy'));
		}
	}
	
	//---------------------------------: Paging :----------------------------------------
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	//---------------------------------: Paging :----------------------------------------
	

	
	//-------------------------------: /Context Menu :----------------------------------
	
	$('.input-daterange input').each(function() {
	    $(this).datepicker({
	    	format: 'dd/mm/yyyy',
		    autoclose: true,
		    todayBtn: true,
		    clearBtn: true,
		    todayHighlight: true,
		    language: 'th-en'}
	    );
	});
	
	

	angular.element(document).ready(function () {
		$('[data-submenu]').submenupicker();
	});
	
});