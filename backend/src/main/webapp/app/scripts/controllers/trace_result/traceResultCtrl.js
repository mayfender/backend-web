angular.module('sbAdminApp').controller('TraceResultCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, urlPrefix, loadData) {
	
	console.log(loadData);
	$scope.headers = loadData.headers;
	$scope.users = loadData.users;
	$scope.traceDatas = loadData.traceDatas;	
	$scope.totalItems = loadData.totalItems;
//	$scope.appointAmountTotal = loadData.appointAmountTotal;
	$scope.isDisableNoticePrint = loadData.isDisableNoticePrint;
	$scope.isTraceExportExcel = loadData.isTraceExportExcel;
	$scope.isTraceExportTxt = loadData.isTraceExportTxt;
	
	$scope.actionCodes = loadData.actionCodes;
	$scope.resultCodeGroups = loadData.resultCodeGroups;
	$scope.resultGroup = loadData.resultCodeGroups[0];
	var resultCodesDummy = loadData.resultCodes;
	$scope.resultCodes = $filter('filter')(resultCodesDummy, {resultGroupId: $scope.resultGroup && $scope.resultGroup.id});
	
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;
	$scope.product = $rootScope.products[0];
	$scope.dateColumnNames = [
	                          {col: 'createdDateTime', text:'วันที่ติดตาม'}, 
	                          {col: 'appointDate', text:'วันนัดชำระ'}, 
	                          {col: 'nextTimeDate', text:'วันนัด Call'}
	                          ];
	var lastCol;
	var colToOrder;
	
	function searchCriteria() {
		if($scope.formData.dateTo) {
			$scope.formData.dateTo.setHours(23,59,59);			
		}
		
		var criteria = {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			productId: $rootScope.group4 ? ($rootScope.setting && $rootScope.setting.currentProduct) : $scope.product.id,
			columnName: colToOrder,
			order: $scope.order,
			keyword: $scope.formData.keyword,
			owner: $scope.formData.owner,
			dateColumnName: $scope.formData.dateColumnName,
			dateFrom: $scope.formData.dateFrom,
			dateTo: $scope.formData.dateTo,
			actionCodeId: $scope.actionCodeId,
			resultCodeId: $scope.resultCodeId
		}
		
		return criteria;
	}
	
	$scope.search = function(isNewLoad) {
		$http.post(urlPrefix + '/restAct/traceWork/traceResult', searchCriteria()).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.traceDatas = result.traceDatas;	
			$scope.totalItems = result.totalItems;
			
			if(isNewLoad) {				
				$scope.headers = result.headers;
				$scope.users = result.users;
				
				$scope.actionCodes = result.actionCodes;
				$scope.resultCodeGroups = result.resultCodeGroups;
				$scope.resultGroup = result.resultCodeGroups[0];
				var resultCodesDummy = result.resultCodes;
				$scope.resultCodes = $filter('filter')(resultCodesDummy, {resultGroupId: $scope.resultGroup.id});
			}
//			$scope.appointAmountTotal = result.appointAmountTotal;
			
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.exportResult = function(fileType) {
		var criteria = searchCriteria();
		criteria.isFillTemplate = true;
		criteria.fileType = fileType;
		
		$http.post(urlPrefix + '/restAct/traceResultReport/download', criteria, {responseType: 'arraybuffer'}).then(function(data) {	
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
	
	$scope.exportNotices = function() {
		var criteria = searchCriteria();
		
		$http.post(urlPrefix + '/restAct/traceWork/exportNotices', criteria, {responseType: 'arraybuffer'}).then(function(data) {	
			
			var file = new Blob([data.data], {type: 'application/pdf'});
	        var fileURL = URL.createObjectURL(file);
	        window.open(fileURL);
	        window.URL.revokeObjectURL(fileURL);  //-- Clear blob on client
			
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clearSearchForm = function(isNewLoad) {
		$scope.formData.keyword = null;
		$scope.column = null;
		$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;
		$scope.formData.dateColumnName = null;
		$scope.formData.dateFrom = null;
		$scope.formData.dateTo = null;
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
	
	
	$scope.changeProduct = function(prod) {
		if(prod == $scope.product) return;
		
		$scope.product = prod;
		$scope.clearSearchForm(true);
	}
	
	$scope.changeResultGroups = function(gp) {
		$scope.resultGroup = gp;
		$scope.resultCodes = $filter('filter')(resultCodesDummy, {resultGroupId: gp.id});
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