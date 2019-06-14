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
			 content: 'คุณแน่ใจว่าต้องการลบข้อมูล <strong>จำนวน {{chk.selected.size}} รายการ ?</strong>',
			 scope: $scope,
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
	
	$scope.sendSms = function() {
		$ngConfirm({
			 title: 'ยืนยันการส่ง SMS',
			 content: 'คุณแน่ใจว่าต้องการส่ง SMS <strong>จำนวน {{totalItems}} รายการ ?</strong>',
			 scope: $scope,
			 buttons: {
				 yes: {
					 text: 'ส่ง',
					 btnClass: 'btn-orange',
					 action: function(scope, button) {
						 $http.post(urlPrefix + '/restAct/sms/sendSms', searchCriteria()).then(function(data) {
							var result = data.data;
								
							if(result.statusCode != 9999) {
								$rootScope.systemAlert(result.statusCode);
								return;
							}
							
							chkSms();
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
	
	$scope.getReport = function() {
		$http.post(urlPrefix + '/restAct/sms/download', searchCriteria(), {responseType: 'arraybuffer'}).then(function(data) {	
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
		chkSms();
		
		$scope.formData.dateFrom = angular.copy(today);
		$scope.formData.dateFrom.setHours(0,0,0,0);
		
		$scope.formData.dateTo = angular.copy(today);
		$scope.formData.dateTo.setHours(23,59,0,0);
		
		$("input[name='dateFrom']").data("DateTimePicker").date($scope.formData.dateFrom);
		$("input[name='dateTo']").data("DateTimePicker").date($scope.formData.dateTo);
	}
	
	function chkSms() {
		var smsResult;
		$http.get(urlPrefix + '/restAct/sms/getSmsSentStatus?productId='+$rootScope.workingOnProduct.id).then(function(data) {
			smsResult = data.data;
			
			if(smsResult.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			chkRetry(smsResult.map);
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	var isFirstRound = true;
	var statusModal;
	function chkRetry(status) {
		if(status) {
			$scope.smsCredit = status.credit;
			$scope.smsCreditUsage = status.creditUsage;
			$scope.smsSuccessAmt = status.success;
			$scope.smsFailAmt = status.fail;
			$scope.smsIsFinished = status.isFinished;
			
			if(!status.isFinished) {
				$scope.smsStatusTitle = 'อยู่ระหว่างการส่ง SMS กรุณารอ...';
				
				if(isFirstRound) {
					isFirstRound = false;
					statusModal = $ngConfirm({
						title: false,
//						closeIcon: false,
						contentUrl: './views/sms/sms_result.html',
						scope: $scope,
					});
				}
				
				setTimeout(function(){ 
					chkSms();
				}, 3000);
				
			} else {
				if(!isFirstRound) {
					$scope.smsStatusTitle = 'ส่ง SMS เสร็จแล้ว';
//					statusModal.setCloseIcon(true);
				}
				isFirstRound = true;				
			}	
		}
	}
	
	initDate();
	
});