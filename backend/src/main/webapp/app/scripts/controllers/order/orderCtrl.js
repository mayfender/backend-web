angular.module('sbAdminApp').controller('OrderCtrl', function($rootScope, $state, $scope, $base64, $http, $translate, $localStorage, $ngConfirm, $filter, focus, urlPrefix, loadData) {
	console.log(loadData);
	
	if(!loadData) {
		console.log('loadData is null so should be relogin.');
		$state.go("login", {action: 'logout'});
		return;
	}
	
	var now = new Date($rootScope.serverDateTime);
	$scope.panel = 0;
	$scope.tabActived = 0;
	$scope.periods = loadData.periods;
	$scope.users = loadData.users;
	
	$scope.isDnDable = true;
	$scope.isLoadProgress = false;
	
	$scope.formData = {
		bonSw: false, langSw: false, orderName: null, discount: '10'
	};
	
	$scope.roles = [{id: 3, name: 'ผู้ดูแล'}, {id: 1, name: 'ลูกค้า'}];
	$scope.formData.userRole = null;
	$scope.formData.userSearchId = null;
//	$scope.formData.userRole = $rootScope.group_0 ? null : 3;
//	$scope.formData.userSearchId = $rootScope.group_0 ? null : $rootScope.userId;
	//----------------------------------------------------------------------------
	
	
	if($scope.periods && $scope.periods.length > 0) {
		var p = $scope.periods[0];
		$scope.formData.period = p._id;
		
		$scope.formData.result2 = p.result2;
		$scope.formData.result3 = p.result3;
		
		chkDate(p.periodDateTime);
	}

	$scope.periodModes = [{id: 1, name:'ทั่วไป'}, {id: 2, name:'เพิ่ม'}];
	$scope.periodMode = $scope.periodModes[0];
	$scope.loyGroupTitle = {4: 'ลอย', 41: 'แพ 4', 42: 'แพ 5', 43: 'วิ่งบน', 44: 'วิ่งล่าง'};
	
	$scope.periodModeChange = function(p) {
		$scope.periodMode = p;
	}
	
	$scope.saveOrder = function() {
		init();
		$scope.tabActived = 0;
		$scope.isFormDisable = true;
		$scope.formData.userSearchId = $rootScope.userId;
		
		$http.post(urlPrefix + '/restAct/order/saveOrder', {
			name: $scope.formData.name,
			orderNumber: $scope.formData.orderNumber,
			bon: $scope.formData.bon,
			bonSw: $scope.formData.bonSw,
			lang: $scope.formData.lang,
			langSw: $scope.formData.langSw,
			tod: $scope.formData.tod,
			loy: $scope.formData.loy,
			runBon: $scope.formData.runBon,
			runLang: $scope.formData.runLang,
			tab : $scope.tabActived,
			chkBoxType: $scope.checkBoxType,
			userId: $rootScope.userId,
			periodId: $scope.formData.period,
			dealerId: $rootScope.workingOnDealer.id
		}, {
			ignoreLoadingBar: true
		}).then(function(data) {
			$scope.isFormDisable = false;
			var result = data.data;
			
			if(result.statusCode == 1001) {
				restrictedConfirm($scope.formData.orderNumber, 1);
				clearForm();
				focus('orderNumber');
				return;
			} else if(result.statusCode != 9999) {
				console.log(result);
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			/*if(($scope.orderData.length + 1) == result.orderData.length) {
				$scope.orderData.push(result.orderData[result.orderData.length - 1]);	
				console.log('update last one');			
			} else {
				$scope.orderData = result.orderData;				
				console.log('update all');		
			}*/
			
			$scope.orderData = result.orderData;						
			$scope.totalPriceSum = result.totalPriceSum;
			$scope.totalPriceSumAll = result.totalPriceSumAll;
			$scope.orderNameLst = result.orderNameLst;
			$scope.formData.orderName = $scope.formData.name;
			
			$("#orderDataInput").animate({ scrollTop: $('#orderDataInput').prop("scrollHeight")}, 1000);
			
			if($scope.orderData[$scope.orderData.length-1].isHalfPrice) {
				restrictedConfirm($scope.formData.orderNumber, 2);
			}
			
			clearForm();
			focus('orderNumber');
		}, function(response) {
			$rootScope.systemAlert(response.status);
			$scope.isFormDisable = false;
		});
	}
	
	$scope.addPeriod = function() {
		var periodDateObj = $("input[name='period']").data("DateTimePicker");
		var periodDate = periodDateObj && periodDateObj.date();
		
		if(periodDate == null) return;
		
		$scope.formData.newPeriod = periodDate.toDate();
		$scope.formData.newPeriod.setHours(0,0,0,0);
		
		$http.post(urlPrefix + '/restAct/order/savePeriod', {
			periodDateTime: $scope.formData.newPeriod
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.periods = result.periods;
			$scope.formData.period = $scope.periods[0]._id;
			
			$scope.formData.newPeriod = null;
			$("input[name='period']").data("DateTimePicker").date(null);
			
			$scope.periodMode = $scope.periodModes[0];
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.exportOrder = function() {		
		var p = $filter('filter')($scope.periods, {_id: $scope.formData.period})[0];
		
		$http.post(urlPrefix + '/restAct/order/export',{
			userId: null,
			periodId: $scope.formData.period,
			periodDate: p.periodDateTime,
			dealerId: $rootScope.workingOnDealer.id
		} ,{responseType: 'arraybuffer'}).then(function(data) {	
			
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
	
	$scope.saveResult = function() {
		$http.post(urlPrefix + '/restAct/order/saveResult',{
			result2: $scope.formData.result2,
			result3: $scope.formData.result3,
			periodId: $scope.formData.period
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.showOrdGroup = function(ord) {
		if(!($scope.tabActived == 1 || $scope.tabActived == 2 || $scope.tabActived == 3)) {
			return;	
		}
			
		$http.get(urlPrefix + '/restAct/order/getOrderNumProb?orderNumber=' + ord._id).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.orderGroup = new Array();
			var ordObj, ordName;
			for(var x in $scope.orderData) {
				ordObj = $scope.orderData[x];
				for(var i in result.orderNumberList) {
					ordName = result.orderNumberList[i];
					if(ordObj._id == ordName) {
						$scope.orderGroup.push(ordObj);
						break;
					}
				}
			}
			
			$ngConfirm({
			    title: 'แสดงชุดตัวเลข [ ' + ord._id +' ]',
			    contentUrl: './views/order/orderGroup.html',
			    type: 'blue',
			    scope: $scope,
			    typeAnimated: true,
			    columnClass: 'col-xs-8 col-xs-offset-2',
			    backgroundDismiss: true,
			    buttons: {
			        ok: {
			        	text: 'OK',
			        	btnClass: 'btn-blue',
			        	keys: ['enter'],
			        	action: function(scope, button){
			        		
			        	}
			        }
			    }
			});
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	/*function checkResult() {
		$http.get(urlPrefix + '/restAct/order/checkResult?periodId=' + $scope.formData.period + '&dealerId=' + $rootScope.workingOnDealer.id).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			var lotResult = result.chkResultMap.total
			
			$scope.resultGroup1 = [{
				title: '3 บน', result: lotResult.result3
			}, {
				title: 'โต๊ด', result: lotResult.resultTod
			}, {
				title: '2 บน', result: lotResult.resultBon2
			}, {
				title: '2 ล่าง', result: lotResult.resultLang2
			}];
						
			$scope.resultGroup2 = [{
				title: 'ลอย', result: lotResult.loy,
			},{
				title: 'แพ 4', result: lotResult.pair4
			}, {
				title: 'แพ 5', result: lotResult.pair5
			}, {
				title: 'วิ่งบน', result: lotResult.runBon
			}, {
				title: 'วิ่งล่าง', result: lotResult.runLang
			}];
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}*/
	
	$scope.checkBoxTypeAllFn = function() {
		if($scope.checkBoxTypeAll) {
			$scope.checkBoxType = {
					bon3: true, bon2: true, lang2: true, 
					loy: true, pair4: true, pair5: true, runBon: true, runLang: true
			};			
		} else {
			$scope.checkBoxType = {
					bon3: false, bon2: false, lang2: false, 
					loy: false, pair4: false, pair5: false, runBon: false, runLang: false
			};
		}
		$scope.chkBoxTypeChange();
	}
	
	$scope.chkBoxTypeChange = function() {
		getData();
	}
	
	$scope.changePeriod = function() {
		var p = $filter('filter')($scope.periods, {_id: $scope.formData.period})[0];
		$scope.formData.result2 = p.result2;
		$scope.formData.result3 = p.result3;
		chkDate(p.periodDateTime);
		
		$scope.changeTab($scope.tabActived);
	}
	
	$scope.changeRole = function() {
		$scope.formData.userSearchId = null;
		getGroupUsers();
		getData();
	}
	$scope.changeOrderName = function() {
		getData();
	}
	
	$scope.changeTab = function(tab) {
		$scope.tabActived = tab;
		
		init();
		
		getData();
	}
	
	$scope.chkOrderNumber = function() {
		/*if($scope.formData.orderNumber.length > 3) {
			$scope.formData.bonSw = true;
		} else {
			$scope.formData.bonSw = false;			
		}*/
	}
	
	$scope.comparator = function(actual, expected) {
	    if (!expected) {
	        return true;
	    } else {
            return angular.equals(actual, expected);
	    }
	}
	
	var jc;
	$scope.updateDell = function(ord) {
		$scope.editDelete = angular.copy(ord);
		$scope.editDelete.oldName = ord.name;
		
		jc = $ngConfirm({
		    title: 'แก้ไขชื่อ / ลบข้อมูล',
		    contentUrl: './views/order/editDelete.html',
		    type: 'blue',
		    typeAnimated: true,
		    scope: $scope,
		    columnClass: 'col-xs-8 col-xs-offset-2',
		    buttons: {
		        edit: {
		        	show: false,
		            text: 'แก้ใขชื่อ',
		            btnClass: 'btn-blue',
		            action: function(scope, button){
		            	updateDell(scope.editDelete._id, scope.editDelete.name);
		            }
		        },
		        delete: {
		        	show: true,
		        	text: 'ลบ',
		        	btnClass: 'btn-red',
		        	action: function(scope, button){
		        		updateDell(scope.editDelete._id);
		        	}
		        },
		        close: {
		        	text: 'ยกเลิก',
		        	action: function(scope, button){
		        		
		            }
		        }
		    }
		});	
	}
	
	function restrictedConfirm(ordNum, type) {
		var title, msg;
		if(type == 1) {
			title = 'แจ้งเลขปิด';
			msg = 'เป็นเลขที่ ปิด';
		} else {
			title = 'แจ้งเลขจ่ายครึ่งราคา';			
			msg = 'เป็นเลขที่ จ่ายครึ่งราคา';
		}
		
		$ngConfirm({
		    title: title,
		    content: '<strong>'+ ordNum +' ' + msg + ' !</strong>',
		    type: 'red',
		    typeAnimated: true,
		    columnClass: 'col-xs-8 col-xs-offset-2',
		    buttons: {
		        ok: {
		        	text: 'OK',
		        	btnClass: 'btn-red',
		        	keys: ['enter'],
		        	action: function(scope, button){
		        		
		        	}
		        }
		    }
		});
	}
	
	$scope.checkUpdateDell = function() {
		if($scope.editDelete.name == $scope.editDelete.oldName) {
			jc.buttons.delete.setShow(true);
			jc.buttons.edit.setShow(false);
		} else {
			jc.buttons.delete.setShow(false);
			jc.buttons.edit.setShow(true);
		}
		
		if(!$scope.editDelete.name) {
			jc.buttons.edit.setDisabled(true);
		} else {
			jc.buttons.edit.setDisabled(false);			
		}
	}
	
	function updateDell(id, name) {
		$scope.isLoadProgress = true;
		$http.post(urlPrefix + '/restAct/order/editDelete', {
			orderId: id,
			orderName :$scope.formData.orderName,
			orderNameUpdate: name,
			tab : $scope.tabActived,
			chkBoxType: $scope.checkBoxType,
			userId: $scope.formData.userSearchId,
			periodId: $scope.formData.period,
			dealerId: $rootScope.workingOnDealer.id
		}, {
			ignoreLoadingBar: true
		}).then(function(data) {
			$scope.isLoadProgress = false;
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.orderData = result.orderData;
			$scope.totalPriceSum = result.totalPriceSum;
			$scope.totalPriceSumAll = result.totalPriceSumAll;
			$scope.orderNameLst = result.orderNameLst;
		}, function(response) {
			$scope.isLoadProgress = false;
			$rootScope.systemAlert(response.status);
		});
	}
	
	function clearForm() {
		$scope.formData.orderNumber = null;
		$scope.formData.bon = null;
		$scope.formData.bonSw = false;
		$scope.formData.lang = null;
		$scope.formData.langSw = false;
		$scope.formData.tod = null;
		$scope.formData.loy = null;
		$scope.formData.runBon = null;
		$scope.formData.runLang = null;
	}
	
	function chkDate(periodDateTime) {
		var limitedDateTimeDnD = new Date(periodDateTime);
		limitedDateTimeDnD.setHours(17, 0, 0, 0);
		$scope.isDnDable = now.getTime() > limitedDateTimeDnD.getTime();
	}
	
	function getData() {
		$scope.isLoadProgress = true;
		$scope.orderData = null;
		
		$http.post(urlPrefix + '/restAct/order/getData', {
			tab : $scope.tabActived,
			chkBoxType: $scope.checkBoxType,
			orderName :$scope.formData.orderName,
			userId: $scope.formData.userSearchId,
			userRole: $scope.formData.userRole,
			periodId: $scope.formData.period,
			dealerId: $rootScope.workingOnDealer.id
		}, {
			ignoreLoadingBar: true
		}).then(function(data) {
			$scope.isLoadProgress = false;
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.orderData = result.orderData;
			$scope.totalPriceSum = result.totalPriceSum;
			$scope.totalPriceSumAll = result.totalPriceSumAll;
			$scope.orderNameLst = result.orderNameLst;
		}, function(response) {
			$scope.isLoadProgress = false;
			$rootScope.systemAlert(response.status);
		});
	}
	
	
	//---------------------------
	function getGroupUsers() {
		if($scope.formData.userRole) {
			$scope.groupUsers = $filter('filter')($scope.users, {roleId: $scope.formData.userRole}, true);
		} else {
			$scope.groupUsers = $scope.users;
		}
	}
	
	function init() {
		$scope.checkBoxTypeAll = true;
		$scope.checkBoxType = {
			bon3: true, bon2: true, lang2: true, 
			loy: true, pair4: true, pair5: true, runBon: true, runLang: true
		};
	}
	
	function initDateEl() {		
		$('.dtPicker').each(function() {
			$(this).datetimepicker({
				format: 'DD/MM/YYYY',
				showClear: true,
				showTodayButton: true,
				locale: 'th'
			}).on('dp.hide', function(e){
				
			}).on('dp.change', function(e){
				
			});
		});
	}
	
	init();
	initDateEl();
	getData();
	focus('name');
	getGroupUsers();
	
});