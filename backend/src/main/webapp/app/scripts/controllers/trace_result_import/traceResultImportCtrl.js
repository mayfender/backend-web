angular.module('sbAdminApp').controller('TraceResultImportCtrl', function($rootScope, $scope, $state, $base64, $http, $localStorage, $translate, $ngConfirm, FileUploader, urlPrefix, loadData) {
	
	$scope.datas = loadData.files;
	$scope.totalItems = loadData.totalItems;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	var uploader;
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/traceResultImport/find', {
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
	
	$scope.deleteItem = function(id) {
		
		var isDelete = confirm('ยืนยันการลบข้อมูล');
	    if(!isDelete) return;
		
		$http.post(urlPrefix + '/restAct/traceResultImport/deleteFile', {
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
	
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	
	$scope.changeProduct = function(prod) {
		if(prod == $rootScope.workingOnProduct) return;
		
		$rootScope.workingOnProduct = prod;
		
		uploader.clearQueue();
		uploader.formData[0].currentProduct = $rootScope.workingOnProduct.id;
		$scope.search();
	}
	
	$scope.processUpload = function(item) {
		/*if(loadData.onApi == 1) {
			confirmObj = $ngConfirm({
				title: 'กรุณาเลือกรูปแบบการ Upload',
				closeIcon: true,
				type: 'blue',
				scope: $scope,
				content: '<strong>- DMS</strong><br />&nbsp;&nbsp;&nbsp;Upload ข้อมูลลงระบบ DMS เท่านั้น<br /><br /><strong>- DMS และ API</strong><br />&nbsp;&nbsp;&nbsp;Upload ข้อมูลลงทั้งระบบ DMS และ API',
				buttons: {
					DMS_API: {
						text: 'DMS และ API',
						btnClass: 'btn-orange',
						action: function() {
							
							$ngConfirm({
								title: 'API Upload',
								closeIcon: true,
								type: 'red',
								content: 'การ upload ข้อมูลไปที่ API <strong><i><u>จะไม่สามารถลบหรือยกเลิกได้</u></i></strong><br />ต้องการดำเนินการต่อหรือไม่ ?',
								buttons: {
									Yes: {
										text: 'ดำเนินการ',
										btnClass: 'btn-red',
										action: function() {
											item.formData[0].isAPIUpload = true;
											item.upload();
										}
									},
									No: {
										text: 'ยกเลิก'										
									}
								}
							});
						}
					},
					DMS: {
						text: 'DMS',
						btnClass: 'btn-green',
						action: function() {
							item.formData[0].isAPIUpload = false;
							item.upload();
						}
					}
				}
			});			
		} else {
			item.upload();			
		}*/
		
		item.upload();
	}
	
	
	//---------------------------------------------------------------------------------------------------------------------------------
	uploader = $scope.uploader = new FileUploader({
        url: urlPrefix + '/restAct/traceResultImport/upload', 
        headers:{'X-Auth-Token': $localStorage.token[$rootScope.username]}, 
        formData: [{currentProduct: $rootScope.workingOnProduct.id}]
    });
	
	 // FILTERS
    uploader.filters.push({
        name: 'customFilter',
        fn: function(item /*{File|FileLikeObject}*/, options) {
            return this.queue.length < 10;
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
        $rootScope.systemAlert(-1, ' ', fileItem.file.name + ' ไม่สามารถนำเข้าได้ กรุณาตรวจสอบรูปแบบไฟล์');
    };
    uploader.onCancelItem = function(fileItem, response, status, headers) {
        console.info('onCancelItem', fileItem, response, status, headers);
    };
    uploader.onCompleteItem = function(fileItem, response, status, headers) {
        console.info('onCompleteItem', fileItem, response, status, headers);
        
        if(response.statusCode == 9999) {
        	$scope.datas = response.files;
        	$scope.totalItems = response.totalItems;
        	
        	$scope.formData.currentPage = 1;
        	$scope.formData.itemsPerPage = 10;
        }
    };
    uploader.onCompleteAll = function() {
        console.info('onCompleteAll');
    };

//    console.info('uploader', uploader);
    	
});