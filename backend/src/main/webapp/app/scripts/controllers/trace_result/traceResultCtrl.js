angular.module('sbAdminApp').controller('TraceResultCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, FileUploader, urlPrefix, loadData) {
	
	$scope.headers = loadData.headers;
	$scope.users = loadData.users;
	$scope.traceDatas = loadData.traceDatas;	
	$scope.totalItems = loadData.totalItems;
	$scope.uploadTemplates = loadData.uploadTemplates;
	
//	$scope.appointAmountTotal = loadData.appointAmountTotal;
	$scope.isDisableNoticePrint = loadData.isDisableNoticePrint;
	$scope.isTraceExportExcel = loadData.isTraceExportExcel;
	$scope.isTraceExportTxt = loadData.isTraceExportTxt;
	$scope.dymList = loadData.dymList;
	
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.formData.owner = $rootScope.group4 ? $rootScope.userId : null;
	
	$scope.dateColumnNames = [
	                          {col: 'createdDateTime', text:'วันที่ติดตาม'}, 
	                          {col: 'appointDate', text:'วันนัดชำระ'}, 
	                          {col: 'nextTimeDate', text:'วันนัด Call'}
	                          ];
	$scope.holdSelectLst = [
	                          {status: true, text:'Hold'},
	                          {status: false, text:'Unhold'}
	                          ];
	$scope.formData.dateColumnName = $stateParams.dateColumnName;
	
	var dateFrom = new Date($rootScope.serverDateTime);
	dateFrom.setHours(0,0,0,0);
	$scope.formData.dateFrom = dateFrom;
	
	$scope.column = $stateParams.columnName;
	$scope.order = $stateParams.order;
	var colToOrder = angular.copy($scope.column);
	var lastCol = angular.copy($scope.column);
	
	initGroup();
	
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
			actionCodeId: $scope.actionCodeId,
			resultCodeId: $scope.resultCodeId,
			isHold: $scope.formData.isHold,
			codeName: $scope.formData.codeName,
			codeValue: $scope.formData.codeValue
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
				
				$scope.isDisableNoticePrint = result.isDisableNoticePrint;
				$scope.isTraceExportExcel = result.isTraceExportExcel;
				$scope.isTraceExportTxt = result.isTraceExportTxt;
				$scope.dymList = result.dymList;
				$scope.uploadTemplates = result.uploadTemplates;
			}
//			$scope.appointAmountTotal = result.appointAmountTotal;
			
			clearState();
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.exportResult = function(templateId, fileType, isLastOnly, isNoTrace) {
		var criteria = searchCriteria();
		criteria.isFillTemplate = true;
		criteria.fileType = fileType;
		criteria.isLastOnly = isLastOnly;
		criteria.isNoTrace = isNoTrace; 
		criteria.id = templateId; 
		
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
		$scope.formData.dateFrom = null;
		$scope.formData.dateTo = null;
		
		$scope.formData.dateColumnName = $stateParams.dateColumnName;
		var dateFrom = new Date($rootScope.serverDateTime);
		dateFrom.setHours(0,0,0,0);
		$scope.formData.dateFrom = dateFrom;
		
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
		if(prod == $rootScope.workingOnProduct) return;
		
		$rootScope.workingOnProduct = prod;
		$scope.clearSearchForm(true);
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
	$scope.getHis = function(el, index, id) {
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
					
					for(i in $scope.dymList) {
						list = $scope.dymList[i];
						subHtml += "<td style='border: 0;text-align: center;'>" + (traceObj['link_' + list.fieldName][0].code || traceObj['link_' + list.fieldName][0].meaning || '') + "</td>";
					}
					
					console.log(subHtml);
					
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
	
	var lastRowSelected;
	var lastIndex;
	$scope.rowSelect = function(data, index, e) {
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
	}	
	
	function clearState() {
		lastRowSelected = null;
		lastIndex = null;
		$scope.countSelected = 0;
	}
	
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
	
	

	angular.element(document).ready(function () {
		$('[data-submenu]').submenupicker();
	});
	
});