angular.module('sbAdminApp').controller('PaymentDetailCtrl', function($rootScope, $scope, $stateParams, $state, $base64, $http, $localStorage, $translate, $filter, FileUploader, urlPrefix, loadData) {
	
	$scope.paymentDetails = loadData.paymentDetails;
	$scope.headers = loadData.headers;
	$scope.taskDetailHeaders = loadData.taskDetailHeaders;
	$scope.users = loadData.users;
	$scope.dymSearch = loadData.dymSearch;
	$scope.totalItems = loadData.totalItems;
	$scope.maxSize = 5;
	
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;	
	
	$scope.$parent.isDetailPage = true;
	$scope.$parent.isShowPage = $stateParams.isShowPage;
	
	function searchCriteria() {
		var dateFrom = $("input[name='dateFrom']").data("DateTimePicker").date();
		var dateTo = $("input[name='dateTo']").data("DateTimePicker").date();
		
		if(dateFrom) {
			$scope.formData.dateFrom = dateFrom.toDate();
			$scope.formData.dateFrom.setSeconds(0);
			$scope.formData.dateFrom.setMilliseconds(0);
		} else {
			$scope.formData.dateFrom = null;
		}
		if(dateTo) {
			$scope.formData.dateTo = dateTo.toDate();
			$scope.formData.dateTo.setSeconds(59);
			$scope.formData.dateTo.setMilliseconds(999);
		} else {
			$scope.formData.dateTo = null;			
		}
		
		var criteria = {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			fileId: $stateParams.fileId,
			productId: $rootScope.workingOnProduct.id,
			owner: $scope.formData.owner,
			keyword: $scope.formData.keyword,
			dateFrom: $scope.formData.dateFrom,
			dateTo: $scope.formData.dateTo,
			dymSearchFiedName: $scope.formData.dymSearchFieldName && $scope.formData.dymSearchFieldName.fieldName,
			dymSearchFiedVal: $scope.formData.dymSearchValue
		}
		
		return criteria;
	}
	
	$scope.search = function() {
		$scope.isLoading = true;
		
		$http.post(urlPrefix + '/restAct/paymentDetail/find', searchCriteria()).then(function(data) {
			loadData = data.data;
			
			if(loadData.statusCode != 9999) {
				$rootScope.systemAlert(loadData.statusCode);
				return;
			}
			
			$scope.paymentDetails = loadData.paymentDetails;
			$scope.totalItems = loadData.totalItems;
			$scope.isLoading = false;
		}, function(response) {
			$rootScope.systemAlert(response.status);
			$scope.isLoading = false;
		});
	}
	
	$scope.exportResult = function() {
		var criteria = searchCriteria();
		criteria.isFillTemplate = true;
		criteria.pocModule = $rootScope.workingOnProduct.productSetting.pocModule;
		
		$http.post(urlPrefix + '/restAct/paymentReport/download', criteria, {responseType: 'arraybuffer'}).then(function(data) {	
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
		$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;
		
		initDate();
		
		$scope.formData.dymSearchFieldName = null;
		$scope.formData.dymSearchValue = null;
		$scope.search();
	}

	$('.input-daterange .dtPicker').each(function() {
		$(this).datetimepicker({
			format: 'DD/MM/YYYY HH:mm',
			showClear: true,
			showTodayButton: true,
			locale: 'th'
		}).on('dp.hide', function(e){
			
		}).on('dp.change', function(e){
			if($(e.target).attr('name') == 'dateFrom') {
				console.log('dateFrom change');
				
				var dateTo = $("input[name='dateTo']").data("DateTimePicker");
				if(!dateTo.date() || !e.date) return;
				
				dateTo.date(e.date.hours(dateTo.date().hours()).minutes(dateTo.date().minutes()));
			} else if($(e.target).attr('name') == 'dateTo') {
				console.log('dateTo change');
				
				var dateTo = e.date;
				if(!dateTo) return;
				
				var dateFrom = $("input[name='dateFrom']").data("DateTimePicker");
				
				if(dateTo.isBefore(dateFrom.date())) {
					dateFrom.date(dateTo.hours(dateFrom.date().hours()).minutes(dateFrom.date().minutes()));
				}
			}
		});
	});
	
	
	
	$scope.$parent.gotoSelected = function() {
		$state.go("dashboard.payment.search");
	}
	
	$scope.goToTask = function(id) {
		$state.go('dashboard.working.search.view', {id: id, parentId: id, productId: $rootScope.workingOnProduct.id, fromPage: 'payment'});
	}
	
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	
	$scope.$on("$destroy", function() {
		$scope.$parent.isDetailPage = false
    });
	
	if($stateParams.isShowPage) {
		function initDate() {
			var today = new Date($rootScope.serverDateTime);
			$scope.formData.dateFrom = angular.copy(today);
			$scope.formData.dateFrom.setHours(0,0,0,0);
			
			$scope.formData.dateTo = angular.copy(today);
			$scope.formData.dateTo.setHours(23,59,0,0);
			
			$("input[name='dateFrom']").data("DateTimePicker").date($scope.formData.dateFrom);
			$("input[name='dateTo']").data("DateTimePicker").date($scope.formData.dateTo);
		}
	
		initDate();
	}
	    	
});