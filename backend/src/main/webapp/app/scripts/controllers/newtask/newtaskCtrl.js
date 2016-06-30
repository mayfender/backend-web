angular.module('sbAdminApp').controller('NewtaskCtrl', function($rootScope, $scope, $state, $base64, $http, $localStorage, $translate, FileUploader, urlPrefix, loadData) {
	
	$scope.datas = loadData.files;
	$scope.totalItems = loadData.totalItems;
	$scope.productsSelect = loadData.products;
	var selectedProductObj = $scope.productsSelect && $scope.productsSelect[0];
	$scope.selectedProduct = selectedProductObj && selectedProductObj.id;
	$scope.productName = selectedProductObj && selectedProductObj.productName;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.format = "dd-MM-yyyy HH:mm:ss";
	var uploader;
	
	$scope.search = function() {
		$http.post(urlPrefix + '/restAct/newTask/findAll', {
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			currentProduct: $scope.selectedProduct || ($localStorage.setting && $localStorage.setting.currentProduct)
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
		$state.go('dashboard.taskdetail', {taskFileId: id, productId: $scope.selectedProduct || ($localStorage.setting && $localStorage.setting.currentProduct)});
	}
	
	$scope.deleteItem = function(id) {
		
		console.log(id);
		
		var isDelete = confirm('ยืนยันการลบข้อมูล');
	    if(!isDelete) return;
		
		$http.post(urlPrefix + '/restAct/newTask/deleteFileTask', {
			id: id,
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			currentProduct: $scope.selectedProduct || ($localStorage.setting && $localStorage.setting.currentProduct)
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
	
	$scope.changeProduct = function(id, productName) {
		
		if(id == $scope.selectedProduct) return;
		
		$scope.productName = productName;
		$scope.selectedProduct = id;
		uploader.clearQueue();
		uploader.formData[0].currentProduct = $scope.selectedProduct;
		$scope.search();
	}
	
	
	
	
	//---------------------------------------------------------------------------------------------------------------------------------
	uploader = $scope.uploader = new FileUploader({
        url: urlPrefix + '/restAct/newTask/upload', 
        headers:{'X-Auth-Token': $localStorage.token}, 
        formData: [{currentProduct: $scope.selectedProduct || ($localStorage.setting && $localStorage.setting.currentProduct)}]
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
    
    //------------------------------------------------------------------------
    var isNextPage = false;
    var menuInfo;
    $scope.gotoImportOthers = function(item) {
    	menuInfo = item;
    	isNextPage = true;
    	$scope.dismissModal();
    }
    
    function nextPage() {
    	$state.go('dashboard.importOthers', {
    		'itemsPerPage': $scope.itemsPerPage, 
    		'currentPage': 1,
    		'menuInfo': menuInfo,
    		'productInfo': {productId: $scope.selectedProduct || ($localStorage.setting && $localStorage.setting.currentProduct), productName: $scope.productName}
    	});    	
    }
    
    //------------------------------: Editable :----------------------------------------
    $scope.addMenu = function() {
        $scope.inserted = {menuName: ''};
        $scope.menus.push($scope.inserted);
    };
    
    $scope.cancelNewMenu = function(item) {
    	for(i in $scope.menus) {
    		if($scope.menus[i] == item) {
    			$scope.menus.splice(i, 1);
    		}
    	}
    }

	$scope.removeMenu = function(index, id) {
	    $http.post(urlPrefix + '/restAct/importMenu/delete', {
			id: id,
			productId: $scope.selectedProduct || ($localStorage.setting && $localStorage.setting.currentProduct)
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.menus.splice(index, 1);
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	};
	
	$scope.saveMenu = function(data, item, index) {
		$http.post(urlPrefix + '/restAct/importMenu/save', {
			id: item.id,
			menuName: data.menuName,
			productId: $scope.selectedProduct || ($localStorage.setting && $localStorage.setting.currentProduct)
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$scope.removeMenu(index);
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			if(!item.id) {
				item.id = result.menuId;
			}
		}, function(response) {
			$scope.removeMenu(index);
			$rootScope.systemAlert(response.status);
		});
	}
    
    
    //------------------------------: Modal dialog :------------------------------------
    var myModal;
	var isDismissModal;
	$scope.showOthersUploadMenu = function() {
		$http.post(urlPrefix + '/restAct/importMenu/find', {
			enabled: true,
			productId: $scope.selectedProduct || ($localStorage.setting && $localStorage.setting.currentProduct)
		}).then(function(data) {
			var result = data.data;
			
			if(result.statusCode != 9999) {
				$scope.removeMenu(index);
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.menus = result.menus;
			
			if(!myModal) {
				myModal = $('#myModal').modal();			
				myModal.on('hide.bs.modal', function (e) {
					if(!isDismissModal) {
						return e.preventDefault();
					}
					isDismissModal = false;
				});
				myModal.on('hidden.bs.modal', function (e) {
					if(isNextPage) {
						nextPage();
						isNextPage = false;
					}
  				});
			} else {			
				myModal.modal('show');
			}
		}, function(response) {
			$scope.removeMenu(index);
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.dismissModal = function() {
		isDismissModal = true;
		myModal.modal('hide');
	}
	
});