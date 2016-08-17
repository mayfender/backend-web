angular.module('sbAdminApp').controller('TraceResultCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, urlPrefix, loadData) {
	
	console.log(loadData);
	$scope.headers = loadData.headers;
	$scope.users = loadData.users;
	$scope.traceDatas = loadData.traceDatas;	
	$scope.totalItems = loadData.totalItems;
	$scope.appointAmountTotal = loadData.appointAmountTotal;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.formData.owner = $rootScope.group4 ? $localStorage.username : null;
	$scope.format = "dd-MM-yyyy";
	$scope.product = $rootScope.products[0];
	$scope.dateColumnNames = [
	                          {col: 'createdDateTime', text:'วันที่ติดตาม'}, 
	                          {col: 'appointDate', text:'วันนัดชำระ'}, 
	                          {col: 'nextTimeDate', text:'วันนัด Call'}
	                          ];
	var lastCol;
	var colToOrder;
	
	$scope.search = function() {
		if($scope.formData.dateTo) {
			$scope.formData.dateTo.setHours(23,59,59);			
		}
		
		$http.post(urlPrefix + '/restAct/traceWork/traceResult', {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			productId: $rootScope.group4 ? ($localStorage.setting && $localStorage.setting.currentProduct) : $scope.product.id,
			columnName: colToOrder,
			order: $scope.order,
			keyword: $scope.formData.keyword,
			owner: $scope.formData.owner,
			dateColumnName: $scope.formData.dateColumnName,
			dateFrom: $scope.formData.dateFrom,
			dateTo: $scope.formData.dateTo
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.traceDatas = result.traceDatas;	
			$scope.totalItems = result.totalItems;
			$scope.appointAmountTotal = result.appointAmountTotal;
			
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clearSearchForm = function() {
		$scope.formData.keyword = null;
		$scope.column = null;
		$scope.formData.owner = $rootScope.group4 ? $localStorage.username : null;
		$scope.formData.dateColumnName = null;
		$scope.formData.dateFrom = null;
		$scope.formData.dateTo = null;
		$scope.search();
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
	
	
	$scope.changeProduct = function(prod) {
		if(prod == $scope.product) return;
		
		$scope.product = prod;
		$scope.search();
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
	
});