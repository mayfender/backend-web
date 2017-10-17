angular.module('sbAdminApp').controller('PaymentDetailCtrl', function($rootScope, $scope, $stateParams, $state, $base64, $http, $localStorage, $translate, FileUploader, urlPrefix, loadData) {
	
	$scope.paymentDetails = loadData.paymentDetails;
	$scope.headers = loadData.headers;
	$scope.taskDetailHeaders = loadData.taskDetailHeaders;
	$scope.users = loadData.users;
	$scope.totalItems = loadData.totalItems;
	$scope.maxSize = 5;
	
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;	
	
	$scope.$parent.isDetailPage = !$stateParams.isShowPage;
	
	var today = new Date($rootScope.serverDateTime);
	$scope.formData.dateFrom = angular.copy(today);
	$scope.formData.dateTo = angular.copy(today);
	$scope.formData.dateFrom.setHours(0,0,0,0);
	$scope.formData.dateTo.setHours(23,59,59,999);
	
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
			fileId: $stateParams.fileId,
			productId: $rootScope.workingOnProduct.id,
			owner: $scope.formData.owner,
			keyword: $scope.formData.keyword,
			dateFrom: $scope.formData.dateFrom,
			dateTo: $scope.formData.dateTo
		}
		
		return criteria;
	}
	
	$scope.search = function() {
		
		
		$http.post(urlPrefix + '/restAct/paymentDetail/find', searchCriteria()).then(function(data) {
			loadData = data.data;
			
			if(loadData.statusCode != 9999) {
				$rootScope.systemAlert(loadData.statusCode);
				return;
			}
			
			$scope.paymentDetails = loadData.paymentDetails;
			$scope.totalItems = loadData.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.exportResult = function() {
		var criteria = searchCriteria();
		criteria.isFillTemplate = true;
		
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
		
		var today = new Date($rootScope.serverDateTime);
		$scope.formData.dateFrom = angular.copy(today);
		$scope.formData.dateTo = angular.copy(today);
		$scope.formData.dateFrom.setHours(0,0,0,0);
		$scope.formData.dateTo.setHours(23,59,59,999);
		
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
	
	$('.input-daterange input').each(function() {
	    $(this).datepicker($scope.datePickerOptions);
	});
	
	$scope.$parent.gotoSelected = function() {
		$state.go("dashboard.payment.search");
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
	    	
});