angular.module('sbAdminApp').controller('SettingCtrl', function($rootScope, $scope, $base64, $http, $translate, $localStorage, $state, FileUploader, urlPrefix, loadData) {
	var setting = loadData.setting;
	
	if(setting) {
		$scope.companyName = setting.companyName;		
		$scope.mongdumpPath = setting.mongdumpPath;
		$scope.backupPath = setting.backupPath;
		
		for(var x in setting.backupPathSpares) {
			$scope.backupPath += ',' +setting.backupPathSpares[x]
		}
		
		$scope.backupUsername = setting.backupUsername;
		$scope.backupPassword = setting.backupPassword;
		$scope.phoneWsServer = setting.phoneWsServer;
		$scope.phoneRealm = setting.phoneRealm;
		$scope.phoneDefaultPass = setting.phoneDefaultPass;
		$scope.isDisable = setting.isDisable;
		$scope.pythonPath = setting.pythonPath;
		$scope.tesseractPath = setting.tesseractPath;
		$scope.wkhtmltopdfPath = setting.wkhtmltopdfPath;
		
		//------------------------: Website Extract :---------------------------------
		$scope.webExtractIsEnabled = setting.webExtractIsEnabled;
		$scope.siteSpshUsername = setting.siteSpshUsername;
		$scope.siteSpshPassword = setting.siteSpshPassword;
		$scope.siteComptrollerUsername = setting.siteComptrollerUsername;
		$scope.siteComptrollerPassword = setting.siteComptrollerPassword;
		$scope.siteTrueTVUsername = setting.siteTrueTVUsername;
		$scope.siteTrueTVPassword = setting.siteTrueTVPassword;
		$scope.warning = setting.warning;
	}
	
	$scope.update = function() {
		$http.post(urlPrefix + '/restAct/setting/update', {
			companyName: $scope.companyName,
			mongdumpPath: $scope.mongdumpPath,
			backupPath: $scope.backupPath,
			backupUsername: $scope.backupUsername,
			backupPassword: $scope.backupPassword,
			phoneWsServer: $scope.phoneWsServer,
			phoneRealm: $scope.phoneRealm,
			phoneDefaultPass: $scope.phoneDefaultPass,
			productKey: $rootScope.$$childTail.productKey,
			pythonPath: $scope.pythonPath,
			tesseractPath: $scope.tesseractPath,
			wkhtmltopdfPath: $scope.wkhtmltopdfPath,
			webExtractIsEnabled: $scope.webExtractIsEnabled,
			siteSpshUsername: $scope.siteSpshUsername,
			siteSpshPassword: $scope.siteSpshPassword,
			siteComptrollerUsername: $scope.siteComptrollerUsername,
			siteComptrollerPassword: $scope.siteComptrollerPassword,
			siteTrueTVUsername: $scope.siteTrueTVUsername,
			siteTrueTVPassword: $scope.siteTrueTVPassword,
			warning: $scope.warning
		}).then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.forceBackup = function() {
		$http.get(urlPrefix + '/restAct/setting/forceBackup').then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			$rootScope.systemAlert(data.data.statusCode, 'Backup Success');
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.openAndClose = function() {
		$http.post(urlPrefix + '/restAct/setting/openAndClose', {
			isDisable: $scope.isDisable
		}).then(function(data) {
			if(data.data.statusCode != 9999) {			
				$rootScope.systemAlert(data.data.statusCode);
				return;
			}
			$rootScope.systemAlert(data.data.statusCode, 'Change Success');
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	
	
	var uploaderThailandDb = $scope.uploaderThailandDb = new FileUploader({
        url: urlPrefix + '/restAct/thaiRegion/upload', 
        headers:{'X-Auth-Token': $localStorage.token[$rootScope.username]}, 
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
        	$rootScope.systemAlert(response.statusCode, 'Upload is completed.');
        } else {
        	$rootScope.systemAlert(response.statusCode);
        }
    };
    uploaderThailandDb.onCompleteAll = function() {
        console.info('onCompleteAll');
    };
	
});