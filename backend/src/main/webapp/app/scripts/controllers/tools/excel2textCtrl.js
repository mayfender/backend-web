angular.module('sbAdminApp').controller('Excel2TextCtrl', function($rootScope, $scope, $stateParams, $state, $base64, $http, $localStorage, $translate, $filter, FileUploader, urlPrefix) {
	$scope.$parent.isShowBack = true;
	
	//---------------------------------------------------------------------------------------------------------------------------------
	uploader = $scope.uploader = new FileUploader({
		url: urlPrefix + '/restAct/tools/excel2txt', 
        headers:{'X-Auth-Token': $localStorage.token, responseType: 'arraybuffer'}
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
        $rootScope.systemAlert(-1, ' ', fileItem.file.name + ' ไม่สามารถแปลงไฟล์ได้ กรุณาตรวจสอบรูปแบบไฟล์');
    };
    uploader.onCancelItem = function(fileItem, response, status, headers) {
        console.info('onCancelItem', fileItem, response, status, headers);
    };
    uploader.onCompleteItem = function(fileItem, response, status, headers) {
    	console.info('onCompleteItem', fileItem, response, status, headers);
        
        data = response;
        
        var a = document.createElement("a");
		document.body.appendChild(a);
		a.style = "display: none";
		
		console.log(headers.filename);
		var fileName = decodeURIComponent(headers.filename);
		
		var file = new Blob([data]);
        var url = URL.createObjectURL(file);
        
        a.href = url;
        a.download = fileName;
        a.click();
        a.remove();
        
        window.URL.revokeObjectURL(url); //-- Clear blob on client
    };
    uploader.onCompleteAll = function() {
        console.info('onCompleteAll');
    };
	
});