angular.module('sbAdminApp').controller('NotificationCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, $timeout, FileUploader, urlPrefix) {
	
	$scope.totalItems = 10;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.notificationList = [{id: '', name: 'New Comment', dateTimeDesc: '4 minutes ago', isTakeAction: true}, 
	                           {id: '', name: 'Message Sent', dateTimeDesc: '12 minutes ago', isTakeAction: false}, 
	                           {id: '', name: 'New Task', dateTimeDesc: '27 minutes ago', isTakeAction: true}, 
	                           {id: '', name: 'New Followers', dateTimeDesc: '43 minutes ago', isTakeAction: true}, 
	                           {id: '', name: 'Server Rebooted', dateTimeDesc: '11:32 AM', isTakeAction: false}, 
	                           {id: '', name: 'Server Crashed!', dateTimeDesc: '11:13 AM', isTakeAction: false}, 
	                           {id: '', name: 'Server Not Responding', dateTimeDesc: '10:57 AM', isTakeAction: true}, 
	                           {id: '', name: 'New Order Placed', dateTimeDesc: '9:49 AM', isTakeAction: true}, 
	                           {id: '', name: 'นัด Call', dateTimeDesc: 'Yesterday', isTakeAction: false}, 
	                           {id: '', name: 'นัดชำระ', dateTimeDesc: 'Yesterday', isTakeAction: true}];
	
	
	
});