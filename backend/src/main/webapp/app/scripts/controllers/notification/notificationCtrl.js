angular.module('sbAdminApp').controller('NotificationCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, $timeout, FileUploader, urlPrefix) {
	
	$scope.totalItems = 10;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.notificationList = [{id: '', name: 'New Comment', dateTimeDesc: '4 minutes ago', status: 1}, 
	                           {id: '', name: 'Message Sent', dateTimeDesc: '12 minutes ago', status: 2}, 
	                           {id: '', name: 'New Task', dateTimeDesc: '27 minutes ago', status: 2}, 
	                           {id: '', name: 'New Followers', dateTimeDesc: '43 minutes ago', status: 1}, 
	                           {id: '', name: 'Server Rebooted', dateTimeDesc: '11:32 AM', status: 2}, 
	                           {id: '', name: 'Server Crashed!', dateTimeDesc: '11:13 AM', status: 2}, 
	                           {id: '', name: 'Server Not Responding', dateTimeDesc: '10:57 AM', status: 1}, 
	                           {id: '', name: 'New Order Placed', dateTimeDesc: '9:49 AM', status: 1}, 
	                           {id: '', name: 'นัด Call', dateTimeDesc: 'Yesterday', status: 1}, 
	                           {id: '', name: 'นัดชำระ', dateTimeDesc: 'Yesterday', status: 1}];
	
	
	
});