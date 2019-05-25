angular.module('sbAdminApp').controller('SmsCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, $timeout, $ngConfirm, FileUploader, urlPrefix, loadData) {
	console.log(loadData);
	
	$scope.totalItems = loadData.totalItems;
	$scope.smses = loadData.smses;
	$scope.headers = loadData.headers;
	$scope.status = [{name: 'รอส่ง', val: 0}, {name: 'ส่งแล้ว', val: 1}, {name: 'ส่งไม่สำเร็จ', val: 2}];	
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;
	$scope.formData.status = 0;
	
	$scope.chk = {}
	$scope.chk.selected = new Set();
	
	var today = new Date($rootScope.serverDateTime);
	
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
			productId: $rootScope.workingOnProduct.id,
			dateFrom: $scope.formData.dateFrom,
			dateTo: $scope.formData.dateTo,
			status: $scope.formData.status
		}
		
		return criteria;
	}
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/sms/get', searchCriteria()).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.smses = result.smses;	
			$scope.totalItems = result.totalItems;
			chkSelected();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.remove = function() {
		$ngConfirm({
			 title: 'ยืนยันการลบข้อมูล',
			 content: 'คุณแน่ใจว่าต้องการลบข้อมูล ?',
			 buttons: {
				 yes: {
					 text: 'ลบ',
					 btnClass: 'btn-red',
					 action: function(scope, button) {
						 var params = searchCriteria();
						 params.ids = Array.from($scope.chk.selected);
						 
						 $http.post(urlPrefix + '/restAct/sms/remove', params).then(function(data) {
							var result = data.data;
								
							if(result.statusCode != 9999) {
								$rootScope.systemAlert(result.statusCode);
								return;
							}
							
							$scope.smses = result.smses;
							$scope.totalItems = result.totalItems;
							$scope.chk.selected = new Set();
							$scope.chk.selectorAll = false;
							
							$rootScope.systemAlert(result.statusCode, 'ลบข้อมูลเรียบร้อยแล้ว');
						}, function(response) {
							//
						}); 
					 }
				 },
				 no: {
					 text: 'ยกเลิก'
				 }
			 }
		 });
	}
	
	$scope.changeStatus = function() {
		$scope.chk.selected = new Set();
		$scope.search();
	}
	
	//---------------------------------: Check Box :----------------------------------------
	$scope.chk.smsSelect = function(e, data) {
		e.stopPropagation();
		
		if (e.target.checked === undefined) {
			setTimeout(function(){ 
				$(e.currentTarget).children().click();
			}, 10);
		} else {
			if(e.target.checked) {
				$scope.chk.selected.add(data._id);
			} else {
				$scope.chk.selected.delete(data._id);
			}
		}
	}
	$scope.chk.selectAll = function(e) {
		for(var x in $scope.smses) {
			if(e.target.checked) {
				$scope.smses[x].selector = true;
				$scope.chk.selected.add($scope.smses[x]._id);
			} else {
				$scope.smses[x].selector = false;
				$scope.chk.selected.delete($scope.smses[x]._id);				
			}
		}
	}
	function chkSelected() {
		if($scope.chk.selected.size == 0) {
			return;
		}
		
		$scope.chk.selectorAll = false;
		for(var x in $scope.smses) {
			if($scope.chk.selected.has($scope.smses[x]._id)) {
				$scope.smses[x].selector = true;
			}
		}
	}
	
	//---------------------------------------------------------------------
	$scope.clearSearchForm = function(isNewLoad) {
		$scope.formData.currentPage = 1;
		$scope.formData.status = 0;
		
		initDate();
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
	
	function initDate() {
		$scope.formData.dateFrom = angular.copy(today);
		$scope.formData.dateFrom.setHours(0,0,0,0);
		
		$scope.formData.dateTo = angular.copy(today);
		$scope.formData.dateTo.setHours(23,59,0,0);
		
		$("input[name='dateFrom']").data("DateTimePicker").date($scope.formData.dateFrom);
		$("input[name='dateTo']").data("DateTimePicker").date($scope.formData.dateTo);
	}
	
	initDate();
	
});