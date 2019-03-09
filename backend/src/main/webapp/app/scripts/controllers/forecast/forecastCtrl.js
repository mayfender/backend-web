angular.module('sbAdminApp').controller('ForecastCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, $timeout, $ngConfirm, FileUploader, urlPrefix, loadData) {
	
	$scope.headers = loadData.headers;
	$scope.users = loadData.users;
	$scope.createdByLog = loadData.createdByLog;
	$scope.forecastDatas = loadData.forecastDatas;	
	$scope.totalItems = loadData.totalItems;
	$scope.uploadTemplates = loadData.uploadTemplates;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;	
	$scope.dymList = loadData.dymList;
	$scope.dymSearch = loadData.dymSearch;
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
	initGroup();
	
	$scope.datePickerOptions = {
		    format: 'dd/mm/yyyy',
		    autoclose: true,
		    todayBtn: true,
		    clearBtn: true,
		    todayHighlight: true,
		    language: 'th-en'
	};
	
	
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
			dateTo: $scope.formData.dateTo,
			codeName: $scope.formData.codeName,
			codeValue: $scope.formData.codeValue,
			dymSearchFiedName: $scope.formData.dymSearchFieldName && $scope.formData.dymSearchFieldName.fieldName,
			dymSearchFiedVal: $scope.formData.dymSearchValue
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
		$ngConfirm({
			 title: false,
			 closeIcon: true,
			 content: 'แสดงรายงานทุกบัญชี รวมถึงบัญชีที่ยุติการติดตาม',
			 buttons: {
				 yes: {
					 text: 'รวม',
					 action: function(){
						 exportResultProceed(templateId, isLastOnly, false);
					 }
				 },
				 no: {
					 text: 'ไม่รวม',
					 btnClass: 'btn-green',
					 action: function(){
						 exportResultProceed(templateId, isLastOnly, true);
					 }
		        }
			 }
		 });
	}
	
	function exportResultProceed(templateId, isLastOnly, isActiveOnly) {
		var criteria = searchCriteria();
		criteria.isFillTemplate = true;
		criteria.isLastOnly = isLastOnly;
		criteria.id = templateId;
		criteria.isActiveOnly = isActiveOnly;
		
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
	
	$scope.paidtDateChange = function(dataObj, dataVal) {		
		console.log(dataObj);
		console.log(dataVal);
		
		$http.post(urlPrefix + '/restAct/forecast/updatePaidAmount', {
			id: dataObj['_id'],
			paidDate: dataObj['paidDate'],
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			dataObj['paidAmount'] = result.paidAmount;
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
		
		$scope.formData.codeName = null;
		$scope.formData.codeValue = null;
		$scope.codeNameChange();
		
		$scope.formData.dymSearchFieldName = null;
		$scope.formData.dymSearchValue = null;
		
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
		if(!$scope.formData.dateColumnName) {
			$scope.formData.dateFrom = null;
			$scope.formData.dateTo = null;
		}
	}
	
	$scope.dateFromChange = function() {
		$scope.formData.dateTo = angular.copy($scope.formData.dateFrom);
		$("#dateTo").datepicker('update', $filter('date')($scope.formData.dateTo, 'dd/MM/yyyy'));
	}
	
	$scope.dateToChange = function() {
		if($scope.formData.dateFrom && $scope.formData.dateTo) {
			if($scope.formData.dateFrom.getTime() > $scope.formData.dateTo.getTime()) {	
				$scope.formData.dateFrom = angular.copy($scope.formData.dateTo);
				$("#dateFrom").datepicker('update', $filter('date')($scope.formData.dateFrom, 'dd/MM/yyyy'));
			}
		}
	}
	
	//---------------------------------: Paging :----------------------------------------
	$scope.pageChanged = function() {
		$scope.search();
		
		$timeout(function() {
			$("i[name='paidDateCancelBtn']").each(function() {
				angular.element($(this)).triggerHandler('click');
			});
		}, 0);
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	//---------------------------------: Paging :----------------------------------------
	

	
	//-------------------------------: /Context Menu :----------------------------------
	
	$('.input-daterange input').each(function() {
	    $(this).datepicker($scope.datePickerOptions);
	});
	
	

	angular.element(document).ready(function () {
		$('[data-submenu]').submenupicker();
	});
	
	//---------------------------------: Dynamic List :----------------------------------------
	$scope.codeNameChange = function() {
		$scope.selectedCodeName = $filter('filter')($scope.dymList, {fieldName: $scope.formData.codeName})[0];
		
		if(!$scope.selectedCodeName) {
			$scope.codeGroups = null;
			return;
		}
		
		if($scope.selectedCodeName.dymListDetGroup) {
			$scope.codeGroups = $scope.selectedCodeName.dymListDetGroup;
		} else {
			$scope.codeGroups = null;
		}
	}
	
	$scope.changeGroup = function(gp) {
		$scope.selectedCodeName.groupSelected = gp;
		$scope.selectedCodeName.dymListDet = $filter('filter')($scope.selectedCodeName.dymListDetDummy || $scope.selectedCodeName.dymListDet, {groupId: gp['_id']});
	}
	
	function initGroup() {
		var list;
		
		for(i in $scope.dymList) {
			list = $scope.dymList[i];
			list.groupSelected = list.dymListDetGroup[0];
			
			if(list.groupSelected) {				
				list.dymListDetDummy = list.dymListDet;
				list.dymListDet = $filter('filter')(list.dymListDetDummy, {groupId: list.groupSelected['_id']});
			}
		}
	}
	
});