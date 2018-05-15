angular.module('sbAdminApp').controller('ManageNoticeCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, $timeout, FileUploader, urlPrefix, loadData) {
	
	$scope.totalItems = loadData.totalItems;
	$scope.headers = loadData.headers;
	$scope.users = loadData.users;
	$scope.noticeToPrints = loadData.noticeToPrints;
	$scope.isDisableNoticePrint = loadData.isDisableNoticePrint;
	$scope.createdByLog = loadData.createdByLog;
	$scope.status = [{name: 'พิมพ์แล้ว', val: true}, {name: 'ยังไม่พิมพ์', val: false}];
	
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;
	
	var today = new Date($rootScope.serverDateTime);
	$scope.formData.dateFrom = angular.copy(today);
	$scope.formData.dateTo = angular.copy(today);
	$scope.formData.dateFrom.setHours(0,0,0,0);
	$scope.formData.dateTo.setHours(23,59,59,999);
	$scope.formData.status = false;
	
	$scope.column = $stateParams.columnName;
	$scope.order = $stateParams.order;
	var colToOrder = angular.copy($scope.column);
	var lastCol = angular.copy($scope.column);
	$scope.isAllChk = false;
	
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
			dateTo: $scope.formData.dateTo,
			status: $scope.formData.status
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
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.printBatchNotice = function() {
		var printConfirm = confirm('ยืนยันการพิมพ์ จำนวน ' + $scope.totalItems + ' รายการ');
	    if(!printConfirm) return;
	    
		$http.post(urlPrefix + '/restAct/noticeManager/printBatchNotice', searchCriteria()).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			download(result.fileName);
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	function download(fileName) {
		$http.get(urlPrefix + '/restAct/noticeXDoc/downloadBatchNotice?fileName=' + fileName, {responseType: 'arraybuffer'}).then(function(data) {	
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
	
	
	//---------------------------------------------------------------------
	$scope.changeStatus = function(status) {
		var ids = getChkIds();
		if(ids.length == 0) return;
		
		$http.post(urlPrefix + '/restAct/noticeManager/changeStatus', {
			ids: ids,
			status: status,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			var noticeItem;
			for(var x in $scope.noticeToPrints) {
				noticeItem = $scope.noticeToPrints[x];
				if(!noticeItem.isChk) continue;
				noticeItem.printStatus = status;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	$scope.trigerAllChk = function() {
		if($scope.isAllChk) {
			$scope.isAllChk = false;
		} else {
			$scope.isAllChk = true;
		}
		
		for(var x in $scope.noticeToPrints) {
			$scope.noticeToPrints[x].isChk = $scope.isAllChk;
		}
	}
	function getChkIds() {
		var ids = new Array();
		var noticeItem;
		for(var x in $scope.noticeToPrints) {
			noticeItem = $scope.noticeToPrints[x];
			if(!noticeItem.isChk) continue;
			
			ids.push(noticeItem['_id']);
		}
		return ids
	}
	//---------------------------------------------------------------------
	
	$scope.clearSearchForm = function(isNewLoad) {
		$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;
		$scope.formData.currentPage = 1;
		$scope.formData.keyword = null;
		
		colToOrder = $stateParams.columnName;
		$scope.order = $stateParams.order;
		$scope.column = colToOrder;
		lastCol = colToOrder;
		
		var today = new Date($rootScope.serverDateTime);
		$scope.formData.dateFrom = angular.copy(today);
		$scope.formData.dateTo = angular.copy(today);
		$scope.formData.dateFrom.setHours(0,0,0,0);
		$scope.formData.dateTo.setHours(23,59,59,999);
		$scope.formData.status = false;
		$scope.isAllChk = false;
		
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
	
});