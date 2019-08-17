angular.module('sbAdminApp').controller('EngTplCtrl', function($rootScope, $scope, $state, $base64, $http, $localStorage, $translate, FileUploader, urlPrefix, loadData) {
	
	console.log(loadData);
	
	$scope.datas = loadData.files;
	$scope.totalItems = loadData.totalItems;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	
	$scope.tplType = [
		{id: 1, name: 'TPL Account List'},
		{id: 2, name: 'TPL Forecast'},
		{id: 3, name: 'TPL Trace'},
		{id: 4, name: 'TPL Payment'},
		{id: 5, name: 'TPL Latter(XDoc)'},
		{id: 6, name: 'TPL Printing'}];
	$scope.currType = $scope.tplType[0];
	var uploader;
	
	function getTpl() {
		$http.get(urlPrefix + '/restAct/engTpl/getTpl?currentPage=' + $scope.formData.currentPage + 
    			'&itemsPerPage=' + $scope.formData.itemsPerPage + 
    			'&type=' + $scope.currType.id + 
    			'&prodId=' + $rootScope.workingOnProduct.id, {
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.datas = result.files;
			$scope.totalItems = result.totalItems;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.selectTplType = function(tpl) {
		$scope.currType = tpl;
		uploader.formData[0].type = $scope.currType.id;
		getTpl();
	}
	
	$scope.download = function(id) {
		$http.post(urlPrefix + '/restAct/engTpl/download', {
			id: id,
			productId: $rootScope.workingOnProduct.id
		}, {responseType: 'arraybuffer'}).then(function(data) {	
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
	
	$scope.updateEnabled = function(item) {
		$http.post(urlPrefix + '/restAct/engTpl/updateEnabled', {
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
	
	$scope.updateTemplateName = function(item) {
		$http.post(urlPrefix + '/restAct/engTpl/updateTemplateName', {
			id: item.id,
			tplName: item.tplName,
			productId: $rootScope.workingOnProduct.id
		}).then(function(data) {
			if(data.data.statusCode != 9999) {
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.deleteItem = function(id) {
		
		var isDelete = confirm('ยืนยันการลบข้อมูล');
	    if(!isDelete) return;
		
		$http.post(urlPrefix + '/restAct/engTpl/delete', {
			id: id,
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
			productId: $rootScope.workingOnProduct.id,
			type: $scope.currType.id
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}	    		
    		
    		$rootScope.systemAlert(result.statusCode, 'ลบข้อมูลสำเร็จ');
    		$scope.datas = result.files;
			$scope.totalItems = result.totalItems;
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	$scope.pageChanged = function() {
		getTpl();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		getTpl();
	}
	
	
	
	
	//---------------------------------------------------------------------------------------------------------------------------------
	uploader = $scope.uploader = new FileUploader({
        url: urlPrefix + '/restAct/engTpl/uploadTpl', 
        headers:{'X-Auth-Token': $localStorage.token[$rootScope.username]}, 
        formData: [{prodId: $rootScope.workingOnProduct.id, type: 1}]
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
    	
});