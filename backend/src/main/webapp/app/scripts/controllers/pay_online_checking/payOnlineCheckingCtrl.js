angular.module('sbAdminApp').controller('PayOnlineCheckingCtrl', function($rootScope, $scope, $stateParams, $state, $base64, $http, $localStorage, $translate, $filter, FileUploader, urlPrefix, loadData) {
	
	$scope.datas = loadData.files;
	$scope.totalItems = loadData.totalItems;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.maxSize = 5;
	
	$scope.dateConf = {
	    	format: 'dd/mm/yyyy',
		    autoclose: true,
		    todayBtn: true,
		    clearBtn: true,
		    todayHighlight: true,
		    language: 'th-en'
		}
	
	//----------------------------: Mock Data :---------------------------------
	$scope.headers = [{columnName: 'test_1'}, {columnName: 'test_2'}, {columnName: 'test_3'}, {columnName: 'test_4'}, {columnName: 'test_5'}];
	$scope.paymentDetails = [{test_1: 'A1111', test_2: 2222, test_3: 3333, test_4: 4444, test_5: 55555},
	                         {test_1: 'B1111', test_2: 2222, test_3: 3333, test_4: 4444, test_5: 55555},
	                         {test_1: 'C1111', test_2: 2222, test_3: 3333, test_4: 4444, test_5: 55555},
	                         {test_1: 'D1111', test_2: 2222, test_3: 3333, test_4: 4444, test_5: 55555},
	                         {test_1: 'E1111', test_2: 2222, test_3: 3333, test_4: 4444, test_5: 55555}];
	//----------------------------: Mock Data :---------------------------------
	
	
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.deleteItem = function(id) {
		
		var isDelete = confirm('ยืนยันการลบข้อมูล');
	    if(!isDelete) return;
		
		$http.post(urlPrefix + '/restAct/noticeXDoc/deleteBatchNoticeFile', {
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

	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/noticeXDoc/findBatchNotice', {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$scope.datas = data.data.files;
			$scope.totalItems = data.data.totalItems;
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
	
	
	//---------------------------------------------------------------------------------------------------------------------------------
	uploader = $scope.uploader = new FileUploader({
		url: urlPrefix + '/restAct/noticeXDoc/uploadBatchNotice', 
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
        $rootScope.systemAlert(-1, ' ', fileItem.file.name + ' ไม่สามารถแปลงไฟล์ได้ กรุณาตรวจสอบรูปแบบไฟล์');
    };
    uploader.onCancelItem = function(fileItem, response, status, headers) {
        console.info('onCancelItem', fileItem, response, status, headers);
    };
    uploader.onCompleteItem = function(fileItem, response, status, headers) {
    	console.info('onCompleteItem', fileItem, response, status, headers);
        
    	if(response.statusCode != 9999) return;
        
    	console.log(response);
    	
    	$scope.datas = response.files;
    	$scope.totalItems = response.totalItems;
    	
    	download(response.fileName);
    };
    uploader.onCompleteAll = function() {
        console.info('onCompleteAll');
    };
    
    
    
    angular.element(document).ready(function () {
    	$('#myTabs a').click(function (e) {
    		  e.preventDefault()
    		  $(this).tab('show')
    	})
    });
	
});