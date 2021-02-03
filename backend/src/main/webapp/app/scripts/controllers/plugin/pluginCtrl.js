angular.module('sbAdminApp').controller('PluginCtrl', function($rootScope, $scope, $state, $base64, $http, $localStorage, $translate, FileUploader, urlPrefix, loadData) {
	
	console.log('test plugin');
	
	$scope.datas = loadData.files;
	$scope.totalItems = loadData.totalItems;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.modules = [{code: 'JWS', name: 'jWebSocket'}];
	var uploader;
	
	$scope.pageChanged = function() {
		$scope.search();
	}
	
	$scope.changeItemPerPage = function() {
		$scope.formData.currentPage = 1;
		$scope.search();
	}
	
	$scope.deploy = function(id) {
		var isSure = confirm('Are you sure you want to deploy this version?');
	    if(!isSure) return;
	    
		$http.get(urlPrefix + '/restAct/plugin/deploy?id=' + id).then(function(data) {	
			if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}
			
			$rootScope.systemAlert(data.data.statusCode, 'Success');
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.start = function(id) {
		var isSure = confirm('Are you sure you want to deploy this version?');
	    if(!isSure) return;
	    
		$http.get(urlPrefix + '/restAct/plugin/start?id=' + id).then(function(data) {	
			if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}
			
			$rootScope.systemAlert(data.data.statusCode, 'Success');
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.stop = function(id) {
		var isSure = confirm('Are you sure you want to deploy this version?');
	    if(!isSure) return;
	    
		$http.get(urlPrefix + '/restAct/plugin/stop?id=' + id).then(function(data) {	
			if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}
			
			$rootScope.systemAlert(data.data.statusCode, 'Success');
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.download = function(id) {
		$http.post(urlPrefix + '/restAct/plugin/download', {
			id: id
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
	
	$scope.deleteItem = function(id) {
		
		var isDelete = confirm('ยืนยันการลบข้อมูล');
	    if(!isDelete) return;
		
		$http.post(urlPrefix + '/restAct/plugin/delete', {
			id: id,
			currentPage: $scope.formData.currentPage, 
			itemsPerPage: $scope.formData.itemsPerPage,
		}).then(function(data) {
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}	    		
    		
    		$rootScope.systemAlert(data.data.statusCode, 'Success');
    		$scope.datas = data.data.files;
			$scope.totalItems = data.data.totalItems;
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	$scope.updateCommand = function(data) {
		$http.post(urlPrefix + '/restAct/plugin/updateCommand', {
			id: data.id,
			option: data.option,
			command: data.command
		}).then(function(data) {
    		if(data.data.statusCode != 9999) {
    			$rootScope.systemAlert(data.data.statusCode);
    			return;
    		}	    		
    		
    		$rootScope.systemAlert(data.data.statusCode, 'Success');
	    }, function(response) {
	    	$rootScope.systemAlert(response.status);
	    });
	}
	
	$scope.updateEnabled = function(item) {
		$http.post(urlPrefix + '/restAct/plugin/updateEnabled', {
			id: item.id
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
	
	
	$scope.upload = function(item) {
		item.formData[0].module = $scope.module;		
		item.upload();
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	uploader = $scope.uploader = new FileUploader({
        url: urlPrefix + '/restAct/plugin/upload', 
        headers:{'X-Auth-Token': $localStorage.token[$rootScope.username]},
		formData: [{module: $scope.module}]
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
        name: 'fileTypeFilter',
        fn: function(item /*{File|FileLikeObject}*/, options) {
        	var isValid = false;
        	
        	if(!$scope.module) {
        		$rootScope.systemAlert(-1, ' ', 'Please select module');
        		return;
        	}
        	
        	if($scope.module == 'JWS') {
        		isValid = item.name.endsWith(".zip");
        	}
        	
        	if(!isValid) {
        		$rootScope.systemAlert(-1, ' ', 'File type is wrong.');
        	}
        	
            return isValid;
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