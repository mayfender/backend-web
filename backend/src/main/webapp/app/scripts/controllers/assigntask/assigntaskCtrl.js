angular.module('sbAdminApp').controller('AssignTaskCtrl', function($rootScope, $scope, $stateParams, $state, $base64, $http, $localStorage, $translate, $filter, FileUploader, urlPrefix, loadData) {
	
	$scope.datas = loadData.files;
	$scope.totalItems = loadData.totalItems;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	var uploader;
	
	if($stateParams.productId) {
		$scope.product = $filter('filter')($rootScope.products, {id: $stateParams.productId})[0];
	} else {
		$scope.product = $rootScope.products[0];
	}
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/newTask/findAll', {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			productId: $scope.product.id || ($rootScope.setting && $rootScope.setting.currentProduct)
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
	
	$scope.viewDetail = function(id) {
		$state.go('dashboard.taskdetail', {taskFileId: id, productId: $scope.product.id || ($rootScope.setting && $rootScope.setting.currentProduct), fromPage: 'assign'});
	}
	
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	
	$scope.changeProduct = function(prod) {
		if(prod == $scope.product) return;
		
		$scope.product = prod;
		
		uploader.clearQueue();
		uploader.formData[0].currentProduct = $scope.product.id;
		$scope.search();
	}
	
	$scope.updateEnabled = function(item) {
		$http.post(urlPrefix + '/restAct/newTask/updateEnabled', {
			id: item.id,
			productId: $scope.product.id || ($rootScope.setting && $rootScope.setting.currentProduct)
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
		
		$http.post(urlPrefix + '/restAct/newTask/deleteFileTask', {
			id: id,
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			productId: $scope.product.id || ($rootScope.setting && $rootScope.setting.currentProduct)
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
        url: urlPrefix + '/restAct/newTask/upload', 
        headers:{'X-Auth-Token': $localStorage.token}, 
        formData: [{currentProduct: $scope.product.id || ($rootScope.setting && $rootScope.setting.currentProduct)}]
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