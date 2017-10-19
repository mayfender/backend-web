angular.module('sbAdminApp').controller('PayOnlineCheckingCtrl', function($rootScope, $scope, $stateParams, $state, $base64, $http, $localStorage, $translate, $filter, FileUploader, urlPrefix, loadData) {
	
	$scope.checkList_1 = loadData.checkList['1'];
	$scope.checkList_2 = loadData.checkList['2'];
	$scope.checkList_3 = loadData.checkList['3'];
	$scope.checkList_his_1 = loadData.checkList['1'];
	$scope.checkList_his_3 = loadData.checkList['3'];
	
	$scope.headers = loadData.headers;
		
	$scope.totalItems = loadData.totalItems;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.maxSize = 5;
	
	var today = new Date($rootScope.serverDateTime);
	$scope.formData.date = angular.copy(today);
	$scope.formCheckingData = {};
	
	$scope.dateConf = {
    	format: 'dd/mm/yyyy',
	    autoclose: true,
	    todayBtn: true,
	    clearBtn: false,
	    todayHighlight: true,
	    language: 'th-en'
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//----------------------------------------------: Check Info Tab :-----------------------------------------------------
	$scope.addContractNo = function() {
		if(!$scope.formCheckingData.contractNo) return;
		
		$http.post(urlPrefix + '/restAct/paymentOnlineCheck/addContractNo', {
			date: today,
			contractNo: $scope.formCheckingData.contractNo,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			loadData = data.data;
			
			if(loadData.statusCode != 9999) {
				$rootScope.systemAlert(loadData.statusCode);
				return;
			}
			
			$rootScope.systemAlert(data.data.statusCode, 'Save Success');
			$scope.formCheckingData.contractNo = null;
			
			$scope.checkList_1 = loadData.checkList['1'];
			$scope.checkList_2 = loadData.checkList['2'];
			$scope.checkList_3 = loadData.checkList['3'];
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.getCheckList = function(dateParam) {
		$http.post(urlPrefix + '/restAct/paymentOnlineCheck/getCheckList', {
			date: dateParam || today,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			loadData = data.data;
			
			if(loadData.statusCode != 9999) {
				$rootScope.systemAlert(loadData.statusCode);
				return;
			}
			
			if(dateParam) {				
				$scope.checkList_his_1 = loadData.checkList['1'];
				$scope.checkList_his_3 = loadData.checkList['3'];
			} else {				
				$scope.checkList_1 = loadData.checkList['1'];
				$scope.checkList_2 = loadData.checkList['2'];
				$scope.checkList_3 = loadData.checkList['3'];
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.deleteChkLstItem = function(id) {	
		var isDelete = confirm('ยืนยันการลบข้อมูล');
	    if(!isDelete) return;
		
		$http.post(urlPrefix + '/restAct/paymentOnlineCheck/deleteChkLstItem', {
			id: id,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			loadData = data.data;
			
    		if(loadData.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}	    		
    		
    		$rootScope.systemAlert(loadData.statusCode, 'ลบข้อมูลสำเร็จ');
    		$scope.checkList_1 = loadData.checkList['1'];
    		$scope.checkList_2 = loadData.checkList['2'];
    		$scope.checkList_3 = loadData.checkList['3'];
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}	
	
	//----------------------------------------------: Check History Tab :-----------------------------------------------------
	$scope.dateChange = function() {
		$scope.getCheckList($scope.formData.date);
	}

	//----------------------------------------------: Upload Data Tab :-----------------------------------------------------
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/paymentOnlineCheck/find', {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			loadData = data.data;
			
			if(loadData.statusCode != 9999) {
				$rootScope.systemAlert(loadData.statusCode);
				return;
			}
			
			$scope.datas = loadData.files;
			$scope.totalItems = loadData.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.deleteItem = function(id) {
		
		var isDelete = confirm('ยืนยันการลบข้อมูล');
	    if(!isDelete) return;
		
		$http.post(urlPrefix + '/restAct/paymentOnlineCheck/deleteFile', {
			id: id,
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}	    		
    		
    		$rootScope.systemAlert(data.data.statusCode, 'ลบข้อมูลสำเร็จ');
    		$scope.datas = data.data.files;
			$scope.totalItems = data.data.totalItems;
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	
	//---------------------------------------------------------------------------------------------------------------------------------
	uploader = $scope.uploader = new FileUploader({
		url: urlPrefix + '/restAct/paymentOnlineCheck/upload', 
        headers:{'X-Auth-Token': $localStorage.token[$rootScope.username]},
        formData: [{productId: $rootScope.workingOnProduct.id}]
    });
	
	 // FILTERS
    uploader.filters.push({
        name: 'customFilter',
        fn: function(item /*{File|FileLikeObject}*/, options) {
            return this.queue.length < 10;
        }
    });
    
    // FILTERS File type
    uploader.filters.push({
        name: 'customFilter',
        fn: function(item /*{File|FileLikeObject}*/, options) {
        	var isValid = item.name.endsWith(".xls") || item.name.endsWith(".xlsx");
        	
        	if(!isValid) {
        		$rootScope.systemAlert(-1, ' ', 'ไฟล์ไม่ถูกต้อง');
        	}
        	
            return isValid;
        }
    });
    
    // FILTERS
    uploader.filters.push({
        name: 'customFilter',
        fn: function(item /*{File|FileLikeObject}*/, options) {
        	// File size have to < 15 MB
            return item.size <= 15000000;
        }
    });

    // CALLBACKS
    uploader.onWhenAddingFileFailed = function(item /*{File|FileLikeObject}*/, filter, options) {
        console.info('onWhenAddingFileFailed', item, filter, options);
    };
    uploader.onAfterAddingFile = function(fileItem) {
        console.info('onAfterAddingFile', fileItem);
    };
    uploader.onAfterAddingAll = function(addedFileItems) {
        console.info('onAfterAddingAll', addedFileItems);
    };
    uploader.onBeforeUploadItem = function(item) {
        console.info('onBeforeUploadItem', item);
    };
    uploader.onProgressItem = function(fileItem, progress) {
        console.info('onProgressItem', fileItem, progress);
    };
    uploader.onProgressAll = function(progress) {
        console.info('onProgressAll', progress);
    };
    uploader.onSuccessItem = function(fileItem, response, status, headers) {
        console.info('onSuccessItem', fileItem, response, status, headers);
    };
    uploader.onErrorItem = function(fileItem, response, status, headers) {
        console.info('onErrorItem', fileItem, response, status, headers);
        $rootScope.systemAlert(-1, ' ', fileItem.file.name + ' ไม่สามารถ Upload ไฟล์ได้ กรุณาตรวจสอบรูปแบบไฟล์');
    };
    uploader.onCancelItem = function(fileItem, response, status, headers) {
        console.info('onCancelItem', fileItem, response, status, headers);
    };
    uploader.onCompleteItem = function(fileItem, response, status, headers) {
    	console.info('onCompleteItem', fileItem, response, status, headers);
        
    	if(response.statusCode != 9999) return;
        
    	$scope.datas = response.files;
    	$scope.totalItems = response.totalItems;    	
    };
    uploader.onCompleteAll = function() {
        console.info('onCompleteAll');
    };
    
    
    
    
    $scope.search();
    //--------------------------------------------------------------------------------------------
    angular.element(document).ready(function () {
    	$('#myTabs a').click(function (e) {
    		  e.preventDefault()
    		  $(this).tab('show')
    	})
    });
	
});