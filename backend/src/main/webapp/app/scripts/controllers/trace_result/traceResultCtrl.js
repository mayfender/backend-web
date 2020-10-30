angular.module('sbAdminApp').controller('TraceResultCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, $ngConfirm, FileUploader, urlPrefix, loadData) {
	
	$scope.headers = loadData.headers;
	$scope.users = loadData.users;
	$scope.traceDatas = loadData.traceDatas;	
	$scope.totalItems = loadData.totalItems;
	$scope.uploadTemplates = loadData.uploadTemplates;
	$scope.createdByLog = loadData.createdByLog;
	
//	$scope.appointAmountTotal = loadData.appointAmountTotal;
	$scope.isDisableNoticePrint = loadData.isDisableNoticePrint;
	$scope.isTraceExportExcel = loadData.isTraceExportExcel;
	$scope.isTraceExportTxt = loadData.isTraceExportTxt;
	$scope.onApi = loadData.onApi;
	$scope.dymList = loadData.dymList;
	$scope.dymSearch = loadData.dymSearch;
	
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;
	
	$scope.dateColumnNames = [
	                          {col: 'createdDateTime', text:'วันที่ติดตาม'}, 
	                          {col: 'appointDate', text:'วันนัดชำระ'}, 
	                          {col: 'nextTimeDate', text:'วันนัด Call'}
	                          ];
	$scope.apiUploadStatuses = [
	                          {id: 1, text:'Success'},
	                          {id: 2, text:'Fail'}
	                          ];
	
	$scope.formData.dateColumnName = $stateParams.dateColumnName;
	
	var today = new Date($rootScope.serverDateTime);
	$scope.column = $stateParams.columnName;
	$scope.order = $stateParams.order;
	var colToOrder = angular.copy($scope.column);
	var lastCol = angular.copy($scope.column);
	
	initGroup();
	
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
			columnName: colToOrder,
			order: $scope.order,
			keyword: $scope.formData.keyword,
			owner: $scope.formData.owner,
			dateColumnName: $scope.formData.dateColumnName,
			dateFrom: $scope.formData.dateFrom,
			dateTo: $scope.formData.dateTo,
			actionCodeId: $scope.actionCodeId,
			resultCodeId: $scope.resultCodeId,
			isHold: $scope.formData.isHold,
			codeName: $scope.formData.codeName,
			codeValue: $scope.formData.codeValue,
			dymSearchFiedName: $scope.formData.dymSearchFieldName && $scope.formData.dymSearchFieldName.fieldName,
			dymSearchFiedVal: $scope.formData.dymSearchValue,
			apiUploadStatus: $scope.formData.apiUploadStatus
		}
		
		return criteria;
	}
	
	$scope.upload = function() {
		$ngConfirm({
			 title: 'Upload ข้อมูลผลการติดตาม',
			 closeIcon: true,
			 contentUrl: './views/trace_result/uploadForm.html',
			 icon: 'fa fa-cloud-upload',
			 scope: $scope,
			 buttons: {
				 close: {
					 text: 'ปิด',
					 action: function(){
//						 exportResultProceed(templateId, fileType, isLastOnly, isNoTrace, false);
					 }
				 }
			 }
		 });
		
		
	}
	
	$scope.search = function(isNewLoad) {
		$scope.isLoading = true;
		
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
				
				$scope.isDisableNoticePrint = result.isDisableNoticePrint;
				$scope.isTraceExportExcel = result.isTraceExportExcel;
				$scope.isTraceExportTxt = result.isTraceExportTxt;
				$scope.dymList = result.dymList;
				$scope.uploadTemplates = result.uploadTemplates;
			}
//			$scope.appointAmountTotal = result.appointAmountTotal;
			
//			clearState();
			
			$scope.isLoading = false;
		}, function(response) {
			$rootScope.systemAlert(response.status);
			$scope.isLoading = false;
		});
	}
	
	$scope.exportResult = function(templateId, fileType, isLastOnly, isNoTrace) {
		$ngConfirm({
			 title: false,
			 closeIcon: true,
			 content: 'แสดงรายงานทุกบัญชี รวมถึงบัญชีที่ยุติการติดตาม',
			 buttons: {
				 yes: {
					 text: 'รวม',
					 action: function(){
						 exportResultProceed(templateId, fileType, isLastOnly, isNoTrace, false);
					 }
				 },
				 no: {
					 text: 'ไม่รวม',
					 btnClass: 'btn-green',
					 action: function(){
						 exportResultProceed(templateId, fileType, isLastOnly, isNoTrace, true);
					 }
		        }
			 }
		 });
	}
	
	function exportResultProceed(templateId, fileType, isLastOnly, isNoTrace, isActiveOnly) {
		var criteria = searchCriteria();
		criteria.isFillTemplate = true;
		criteria.fileType = fileType;
		criteria.isLastOnly = isLastOnly;
		criteria.isNoTrace = isNoTrace; 
		criteria.id = templateId; 
		criteria.isActiveOnly = isActiveOnly;
		
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
	
	$scope.updateTaskDetail = function() {
		var isDelete = confirm('ยืนยันการ update ข้อมูล');
	    if(!isDelete) return;
	    
		var criteria = searchCriteria();
		
		$http.post(urlPrefix + '/restAct/traceWork/updateTaskDetail', criteria).then(function(data) {	
			
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Update Success');
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.clearSearchForm = function(isNewLoad) {
		$scope.formData.keyword = null;
		$scope.column = 'createdDateTime';
		$scope.order = 'desc';
		colToOrder = 'createdDateTime';
		$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;
		$scope.formData.dateColumnName = $stateParams.dateColumnName;
		
		initDate();
		
		$scope.formData.codeName = null;
		$scope.formData.codeValue = null;
		$scope.codeNameChange();
		
		$scope.actionCodeId = null;
		$scope.resultCodeId = null;
		
		$scope.formData.dymSearchFieldName = null;
		$scope.formData.dymSearchValue = null;
		
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
			$("input[name='dateFrom']").data("DateTimePicker").date(null);
			$("input[name='dateTo']").data("DateTimePicker").date(null);
		} else {
			initDate();
		}
	}
	
	
	$scope.changeProduct = function(prod) {
		if(prod == $rootScope.workingOnProduct) return;
		
		$rootScope.workingOnProduct = prod;
		$scope.clearSearchForm(true);
	}
	
	$scope.goToTask = function(id) {
		$state.go('dashboard.working.search.view', {id: id, parentId: id, productId: $rootScope.workingOnProduct.id, fromPage: 'trace'});
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
	
	var isShow = false;
	var lastIndex;
	$scope.getHis = function(el, index, id, event) {
		if(lastIndex == index) {
			if(isShow) {
				remoteGetHis(el, index, id);
			} else {
				$(".his_" + lastIndex).remove();		
			}
			isShow ? isShow = false : isShow = true;
		} else {
			if(lastIndex != null) {
				$(".his_" + lastIndex).remove();	
			}
			remoteGetHis(el, index, id);
			isShow = false;
		}
		lastIndex = index;
		
		event.preventDefault();
		event.stopPropagation();
	}
	
	function remoteGetHis(el, index, id) {
		var productId = $rootScope.group4 ? ($rootScope.setting && $rootScope.setting.currentProduct) : $rootScope.workingOnProduct.id;
		
		$http.get(urlPrefix + '/restAct/traceWork/getHis?productId=' + productId + "&id=" + id).then(function(data) {	
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			if(result.traceWorkHises.length > 0) {
				var traceObj;
				var html;
				var subHtml;
				for(x in result.traceWorkHises) {
					traceObj = result.traceWorkHises[x]
					var list;
					subHtml = '';
					var code;
					
					for(i in $scope.dymList) {
						list = $scope.dymList[i];
						
						if(traceObj['link_' + list.fieldName][0]) {
							code = (traceObj['link_' + list.fieldName][0].code || traceObj['link_' + list.fieldName][0].meaning || '');
						} else {
							code = '';
						}
						subHtml += "<td style='border: 0;text-align: center;'>" + code + "</td>";
					}
					
					html = "<tr class='his_" + index + "' style='white-space: nowrap; background-color: #D2D2D2;border: 0;'>" +
					"<td style='border: 0;' colspan='3' align='right'><i class='fa fa-clock-o'></i> " + $filter('date')(traceObj.createdDateTime, 'dd/MM/yyyy hh:mm:ss') + "</td>" +
					"<td style='border: 0;vertical-align: middle; white-space: nowrap;'>" + $filter('date')(traceObj.resultText, 'dd/MM/yyyy') + "</td>" +
					"<td style='border: 0;text-align: center;'>" + (traceObj.nextTimeDate ? $filter('date')(traceObj.nextTimeDate, 'dd/MM/yyyy') : '') + "</td>" +
					"<td style='border: 0;text-align: center;'>" + (traceObj.appointDate ? $filter('date')(traceObj.appointDate, 'dd/MM/yyyy') : '') + "</td>" +
					"<td style='border: 0;text-align: right;'>" + (traceObj.appointAmount ? $filter('number')(traceObj.appointAmount, 2) : '') +"</td>" +
					subHtml +
					"<td style='border: 0;text-align: center;'>" + (traceObj.tel || '') + "</td>" +
					"<td style='border: 0;'></td>" +
					"<td style='border: 0;' colspan='100%'></td>" +
					"</tr>";
					
					$(html).insertAfter($(el).closest('tr')).hide().show('slow');
				}
			} else {
				html = "<tr class='his_" + index + "' style='white-space: nowrap; background-color: #D2D2D2;border: 0;'>" +
				"<td style='border: 0;' colspan='3' align='right'><i class='fa fa-clock-o'></i> Not found updated history</td>" +
				"<td style='border: 0;' colspan='100%'></td>" +
				"</tr>";
				
				$(html).insertAfter($(el).closest('tr')).hide().show('slow');
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
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
	
	//-------------------------------: Context Menu :----------------------------------
	function toggleHold(obj) {
		var results = isHoldToggle(obj);
		
		$http.post(urlPrefix + '/restAct/traceWork/updateHold', {
			isHolds: results,
			productId: $rootScope.group4 ? ($rootScope.setting && $rootScope.setting.currentProduct) : $rootScope.workingOnProduct.id
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				isHoldToggle();
				return;
			}
		}, function(response) {
			isHoldToggle();
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.contextMenuSelected = function(selected) {
		var selectedData = $filter('filter')($scope.traceDatas, {selected: true});
		
		if(selectedData.length == 0) {
			alert('กรุณาเลือกอย่างน้อย 1 รายการ');
			return;
		}
		
		switch(selected) {
			case 1: {
				toggleHold(selectedData);
				break;
			}
		}
	}
	
	
	//---------------------------------------: File Upload :---------------------------------------
	$scope.uploader = new FileUploader({
        url: urlPrefix + '/restAct/traceWork/traceUpload', 
        headers:{'X-Auth-Token': $localStorage.token[$rootScope.username]}, 
        formData: [{productId: $rootScope.workingOnProduct.id}]
    });
	

    // CALLBACKS
	$scope.uploader.onWhenAddingFileFailed = function(item /*{File|FileLikeObject}*/, filter, options) {
        console.info('onWhenAddingFileFailed', item, filter, options);
    };
    $scope.uploader.onAfterAddingFile = function(fileItem) {
        console.info('onAfterAddingFile', fileItem);
        console.log(fileItem);
        fileItem.upload();
        itemFile = fileItem;
    };
    $scope.uploader.onAfterAddingAll = function(addedFileItems) {
        console.info('onAfterAddingAll', addedFileItems);
    };
    $scope.uploader.onBeforeUploadItem = function(item) {
    	console.log('1');
    	return;
    	
    	/*$scope.statusMsg = 'กำลังดำเนินการ กรุณารอ...';
    	confirmObj = $ngConfirm({
    		title: 'จ่ายงาน/แก้ใข/ดึงข้อมูล',
    		icon: 'fa fa-spinner fa-spin',
    		closeIcon: false,
    		type: 'orange',
    		scope: $scope,
    		content: '<strong>{{statusMsg}}</strong>',
    		buttons: {
    			OK: {
    				disabled: true,
    				text: '...',
    				btnClass: 'btn-orange',
    				action: function() {
    					$('#uploadFile').val('');
    				}
    			} 
    		}
    	});*/
    	
        console.info('onBeforeUploadItem', item);
    };
    $scope.uploader.onProgressItem = function(fileItem, progress) {
        console.info('onProgressItem', fileItem, progress);
    };
    $scope.uploader.onProgressAll = function(progress) {
        console.info('onProgressAll', progress);
    };
    $scope.uploader.onSuccessItem = function(fileItem, response, status, headers) {
        console.info('onSuccessItem', fileItem, response, status, headers);
    };
    $scope.uploader.onErrorItem = function(fileItem, response, status, headers) {
        console.info('onErrorItem', fileItem, response, status, headers);
        $rootScope.systemAlert(-1, ' ', fileItem.file.name + ' ไม่สามารถนำเข้าได้ กรุณาตรวจสอบรูปแบบไฟล์');
    };
    $scope.uploader.onCancelItem = function(fileItem, response, status, headers) {
        console.info('onCancelItem', fileItem, response, status, headers);
    };
    $scope.uploader.onCompleteItem = function(fileItem, response, status, headers) {
        console.info('onCompleteItem', fileItem, response, status, headers);
        $('#uploadFile').val('');
        
       /* if(response.statusCode == 9999) {			
    		$scope.statusMsg = updatedMsg;
    		confirmObj.setIcon('fa fa-info-circle');
    		confirmObj.buttons.OK.setDisabled(false);
    		confirmObj.buttons.OK.setText('OK');
        	
        } else {
        	$rootScope.systemAlert(response.statusCode);
        	$scope.statusMsg = 'ดำเนินการไม่สำเร็จ กรุณาตรวจสอบไฟล์';
    		confirmObj.setIcon('fa fa-info-circle');
    		confirmObj.buttons.OK.setDisabled(false);
    		confirmObj.buttons.OK.setText('OK');
        }*/
    };
    $scope.uploader.onCompleteAll = function() {
        console.info('onCompleteAll');
    };
	//---------------------------------------: File Upload :---------------------------------------
	
    
    
    
    
    
    
    
    
    
//	var lastRowSelected;
//	var lastIndex;
	/*$scope.rowSelect = function(data, index, e) {
		//--: right click
		if(e.which == 3) {
			return;
		}
		
		var isPressedCtrl = window.event.ctrlKey;
		var isPressedshift = window.event.shiftKey;
		
		if(isPressedCtrl) {
			lastRowSelected = data;
			lastIndex = index;
			
			if(data.selected) {
				data.selected = false;			
				$scope.countSelected--;
				if($scope.countSelected == 0) lastRowSelected = null;
			} else {
				data.selected = true;
				$scope.countSelected++;
			}
		} else if(isPressedshift && lastRowSelected) {
			if(lastIndex > index) {
				lastRowSelected = data;
				
				for (; index < lastIndex; index++) { 
					if($scope.traceDatas[index].selected) continue;
					
					$scope.traceDatas[index].selected = true;
					$scope.countSelected++;
				}
			} else if(lastIndex < index) {
				lastRowSelected = data;
				
				for (; lastIndex <= index; lastIndex++) { 
					if($scope.traceDatas[lastIndex].selected) continue;
					
					$scope.traceDatas[lastIndex].selected = true;
					$scope.countSelected++;
				}
			} else {				
				console.log('Nothing to do.');
			}
		}
	}	*/
	
	/*function clearState() {
		lastRowSelected = null;
		lastIndex = null;
		$scope.countSelected = 0;
	}*/
	
	function isHoldToggle(obj) {
		var result = [];
		
		for(i in obj) {
			if(obj[i].isHold) {
				obj[i].isHold = false;
				result.push({id: obj[i]._id, isHold: obj[i].isHold});
			} else {
				obj[i].isHold = true;
				result.push({id: obj[i]._id, isHold: obj[i].isHold});
			}
			obj[i].selected = false;
		}
		
		return result;
	}
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

	angular.element(document).ready(function () {
		$('[data-submenu]').submenupicker();
	});
	
	
	//---------------------------------
	initDate();
	
});