angular.module('sbAdminApp').controller('ManageNoticeCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, FileUploader, urlPrefix, loadData) {
	
	$scope.totalItems = loadData.totalItems;
	$scope.headers = loadData.headers;
	$scope.users = loadData.users;
	$scope.noticeToPrints = loadData.noticeToPrints;	
	
	$scope.isDisableNoticePrint = loadData.isDisableNoticePrint;
	$scope.isTraceExportExcel = loadData.isTraceExportExcel;
	$scope.isTraceExportTxt = loadData.isTraceExportTxt;
	$scope.dymList = loadData.dymList;
	
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;
	
	var dateFrom = new Date($rootScope.serverDateTime);
	dateFrom.setHours(0,0,0,0);
	$scope.formData.dateFrom = dateFrom;
	
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
			dateFrom: $scope.formData.dateFrom,
			dateTo: $scope.formData.dateTo
		}
		
		return criteria;
	}
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/noticeManager/findToPrint', searchCriteria()).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.noticeToPrints = result.noticeToPrints;	
			$scope.totalItems = result.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.deleteItem = function(id) {
		var isConfirm = confirm('ยืนยันการลบข้อมูล');
	    if(!isConfirm) return;
	    
		var params = searchCriteria();
		params.id = id;
		
		$http.post(urlPrefix + '/restAct/noticeManager/deleteToPrint', params).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.noticeToPrints = result.noticeToPrints;	
			$scope.totalItems = result.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.printNotice = function(id) {
		$http.get(urlPrefix + '/restAct/noticeManager/printNotice?id='+id+'&productId='+$rootScope.workingOnProduct.id, 
				{responseType: 'arraybuffer'}).then(function(data) {	
					
			var file = new Blob([data.data], {type: 'application/pdf'});
	        var fileURL = URL.createObjectURL(file);
	        window.open(fileURL);
	        window.URL.revokeObjectURL(fileURL);  //-- Clear blob on client
			
	        $scope.dismissModal();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clearSearchForm = function(isNewLoad) {
		$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;
		$scope.formData.currentPage = 1;
		$scope.formData.dateFrom = null;
		$scope.formData.keyword = null;
		$scope.formData.dateTo = null;
		
		colToOrder = $stateParams.columnName;
		$scope.order = $stateParams.order;
		$scope.column = colToOrder;
		lastCol = colToOrder;
		
		var dateFrom = new Date($rootScope.serverDateTime);
		dateFrom.setHours(0,0,0,0);
		$scope.formData.dateFrom = dateFrom;
		
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
	
});