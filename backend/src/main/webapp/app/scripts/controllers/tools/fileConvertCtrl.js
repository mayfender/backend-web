angular.module('sbAdminApp').controller('FileConvertCtrl', function($rootScope, $scope, $stateParams, $state, $base64, $http, $localStorage, $translate, $filter, FileUploader, urlPrefix) {
	$scope.$parent.isShowBack = true;
	$scope.$parent.titlePanel = $stateParams.desc;
	var uploader;
	
	if($stateParams.type == 1) {		
		$scope.isEncodingShow = true;
	}
	
	$scope.encodings = [{code: 'tis620', name: 'ANSI'}, {code: 'UTF-8', name: 'UTF8'}];
	$scope.encoding = $scope.encodings[0].code;
	$scope.splitters = [{code: 1, name: 'pipe'}, {code: 2, name: 'space'}, {code: 0, name: 'none'}];
	$scope.splitter = $scope.splitters[0].code;
	
	function download(fileName) {
		$http.get(urlPrefix + '/restAct/tools/download?fileName=' + fileName, {responseType: 'arraybuffer'}).then(function(data) {	
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
	
	$scope.convert = function(item) {
		item.formData[0].encoding = $scope.encoding;
		item.formData[0].splitter = $scope.splitter;
		item.upload();
	}
	
	//---------------------------------------------------------------------------------------------------------------------------------
	uploader = $scope.uploader = new FileUploader({
		url: urlPrefix + '/restAct/tools/upload', 
        headers:{'X-Auth-Token': $localStorage.token[$rootScope.username]},
        formData: [{type: $stateParams.type}]
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
        	var isValid = false;
        	
        	if($stateParams.type == 1) {
        		isValid = item.name.endsWith(".xls") || item.name.endsWith(".xlsx");
        	} else if($stateParams.type == 2) {
        		isValid = item.name.endsWith(".xls") || item.name.endsWith(".xlsx") || 
        				  item.name.endsWith(".doc") || item.name.endsWith(".docx") || item.name.endsWith(".pdf");
        	}
        	
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
        
    	download(response.fileName);
    };
    uploader.onCompleteAll = function() {
        console.info('onCompleteAll');
    };
	
});