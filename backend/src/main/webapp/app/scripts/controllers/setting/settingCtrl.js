angular.module('sbAdminApp').controller('SettingCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, $state, FileUploader, urlPrefix, loadData) {
	console.log(loadData);
	
	var setting = loadData.setting;
	$scope.companyName = setting && setting.companyName;
	
	$scope.update = function() {
		$http.post(urlPrefix + '/restAct/setting/update', {
			companyName: $scope.companyName
		}).then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			
			$rootScope.companyName = $scope.companyName;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	
	
	
	
	
	
	
	
	var uploaderThailandDb = $scope.uploaderThailandDb = new FileUploader({
        url: urlPrefix + '/restAct/thaiRegion/upload', 
        headers:{'X-Auth-Token': $localStorage.token}, 
        formData: [{}]
    });
	
	// FILTERS
    uploaderThailandDb.filters.push({
        name: 'customFilter',
        fn: function(item /*{File|FileLikeObject}*/, options) {
            return this.queue.length < 10;
        }
    });

    // CALLBACKS
    uploaderThailandDb.onWhenAddingFileFailed = function(item /*{File|FileLikeObject}*/, filter, options) {
        console.info('onWhenAddingFileFailed', item, filter, options);
    };
    uploaderThailandDb.onAfterAddingFile = function(fileItem) {
        console.info('onAfterAddingFile', fileItem);
        fileItem.upload();
    };
    uploaderThailandDb.onAfterAddingAll = function(addedFileItems) {
        console.info('onAfterAddingAll', addedFileItems);
    };
    uploaderThailandDb.onBeforeUploadItem = function(item) {
        console.info('onBeforeUploadItem', item);
    };
    uploaderThailandDb.onProgressItem = function(fileItem, progress) {
        console.info('onProgressItem', fileItem, progress);
    };
    uploaderThailandDb.onProgressAll = function(progress) {
        console.info('onProgressAll', progress);
    };
    uploaderThailandDb.onSuccessItem = function(fileItem, response, status, headers) {
        console.info('onSuccessItem', fileItem, response, status, headers);
    };
    uploaderThailandDb.onErrorItem = function(fileItem, response, status, headers) {
        console.info('onErrorItem', fileItem, response, status, headers);
        $rootScope.systemAlert(-1, ' ', fileItem.file.name + ' ไม่สามารถนำเข้าได้ กรุณาตรวจสอบรูปแบบไฟล์');
    };
    uploaderThailandDb.onCancelItem = function(fileItem, response, status, headers) {
        console.info('onCancelItem', fileItem, response, status, headers);
    };
    uploaderThailandDb.onCompleteItem = function(fileItem, response, status, headers) {
        console.info('onCompleteItem', fileItem, response, status, headers);
        
        if(response.statusCode == 9999) {
        	$scope.formData.currentPage = 1;
        	$scope.formData.itemsPerPage = 10;
        	
        	$rootScope.systemAlert(response.statusCode, 'Work Assinging is completed.');
        	
        	$scope.search();        	
        } else {
        	$rootScope.systemAlert(response.statusCode);
        }
    };
    uploaderThailandDb.onCompleteAll = function() {
        console.info('onCompleteAll');
    };
	
});