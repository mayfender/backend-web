angular.module('sbAdminApp').controller('PaymentUploadCtrl', function($rootScope, $scope, $state, $base64, $http, $localStorage, $translate, FileUploader, urlPrefix, loadData) {
	
	$scope.$parent.isDetailPage = false;
	$scope.$parent.isShowPage = false;
	$scope.datas = loadData.files;
	$scope.totalItems = loadData.totalItems;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	var uploader;
	var itemFile;
	
	console.log($scope.$parent.isDetailPage);
	console.log($scope.$parent.isShowPage);
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/payment/find', {
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
	
	$scope.download = function(id) {
		$http.post(urlPrefix + '/restAct/payment/download', {
			id: id,
			productId: $rootScope.workingOnProduct.id
		}, {responseType: 'arraybuffer'}).then(function(data) {	
			var a = document.createElement("a");
			document.body.appendChild(a);
			a.style = "display: none";
			
			var fileName = decodeURIComponent(data.headers('fileName'));
				
			var type = fileName.endsWith('.doc') ? 'application/msword' : 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
			var file = new Blob([data.data], {type: type});
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
	
	$scope.updateEnabled = function(item) {
		$http.post(urlPrefix + '/restAct/payment/updateEnabled', {
			id: item.id,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			if(item.enabled) {
				item.enabled = false;
			} else {
				item.enabled = true;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	
	$scope.deleteItem = function(id) {
		
		var isDelete = confirm('ยืนยันการลบข้อมูล');
	    if(!isDelete) return;
		
		$http.post(urlPrefix + '/restAct/payment/deleteFile', {
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
	
	$scope.viewDetail = function(id) {
		$state.go('dashboard.payment.detail', {fileId: id, productId: $rootScope.workingOnProduct.id});
	}
	
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	
	$scope.$parent.changeProduct = function(prod) {
		if(prod == $rootScope.workingOnProduct) return;
		
		$rootScope.workingOnProduct = prod;
		
		uploader.clearQueue();
		uploader.formData[0].currentProduct = $rootScope.workingOnProduct.id;
		$scope.search();
	}
	
	
	
	
	//---------------------------------------------------------------------------------------------------------------------------------
	uploader = $scope.uploader = new FileUploader({
        url: urlPrefix + '/restAct/payment/upload', 
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
        	
        	if(response.colDateTypes || response.colNotFounds) {
	        	if(response.colDateTypes.length > 0 || response.colNotFounds.length > 0) {        		
	        		$scope.colDateTypes = response.colDateTypes;
	        		$scope.colNotFounds = response.colNotFounds;
	        		$scope.importChk($scope.colDateTypes);
	        	}
        	}
        }
    };
    uploader.onCompleteAll = function() {
        console.info('onCompleteAll');
    };

//    console.info('uploader', uploader);
    	
    
  //-----------------------------------------------------------------
	var importChkModal;
	var isDismissImportChkModal;
	$scope.importChk = function() {		
		if(!importChkModal) {
			importChkModal = $('#importChkModal').modal();			
			importChkModal.on('hide.bs.modal', function (e) {
				if(!isDismissImportChkModal) {
					return e.preventDefault();
				}
				isDismissImportChkModal = false;
			});
			importChkModal.on('hidden.bs.modal', function (e) {
				//--
			});
		} else {			
			importChkModal.modal('show');
		}		
	}
	
	$scope.dismissImportChkModal = function(isRemove) {
		console.log(importChkModal);
		isDismissImportChkModal = true;
		importChkModal.modal('hide');
		
		if(isRemove) itemFile.remove();
	}
	
	$scope.proceedImport = function() {
		itemFile.formData[0].isConfirmImport = true;
		var yearTypes = new Array();
		var obj;
		
		for(var i in  $scope.colDateTypes) {
			obj = $scope.colDateTypes[i];
			yearTypes.push({columnName: obj.columnName, yearType: obj.yearType});
		}
		
		itemFile.formData[0].yearTypes = angular.toJson(yearTypes);
		
		itemFile.upload();
		$scope.dismissImportChkModal();
	}
	
	$scope.uploadItem = function(item) {
		console.log('uploadItem');
		itemFile = item;
		itemFile.upload();
	}
	
});