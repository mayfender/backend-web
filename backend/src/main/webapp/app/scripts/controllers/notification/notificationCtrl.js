angular.module('sbAdminApp').controller('NotificationCtrl', function($rootScope, $stateParams, $localStorage, $scope, $state, $filter, $http, $timeout, FileUploader, urlPrefix) {
	
	$scope.totalItems = 10;
	$scope.maxSize = 5;
	$scope.formData = {currentPage : 1, itemsPerPage: 10};
	$scope.dateConf = {
	    	format: 'dd/mm/yyyy',
		    autoclose: true,
		    todayBtn: true,
		    clearBtn: false,
		    todayHighlight: true,
		    language: 'th-en'
		}
	$scope.timesCfg = {
		format: 'HH:mm',
		step: '30m'
	};
	$scope.startTimesCfg = {
		minTime: '07:00',
		maxTime: '21:00'
	};
	
	$scope.notificationGroups = [{id: 1, name: 'นัดชำระ', isActive: true, alertNum: 1}, 
	                             {id: 2, name: 'นัด Call', isActive: false}, 
	                             {id: 3, name: 'ทั่วไป', isActive: false, alertNum: 5}];
	
	$scope.notificationList = [{id: '', subject: 'New Comment', detail: '0910217894ผู้ค้ำนิภาพรหญิงแจ้งว่าเป็นลุกสาวแต่อยากทราบว่าเจ้าหน้าที่ติดต่อเรื่องอะไร 0910217894ผู้ค้ำนิภาพรหญิงแจ้งว่าเป็นลุกสาวแต่อยากทราบว่าเจ้าหน้าที่ติดต่อเรื่องอะไร', dateTimeDesc: '4 minutes ago', isTakeAction: true}, 
	                           {id: '', subject: 'Message Sent', detail: 'ค้นหาเบอร์เพิ่มเติม 2ไม่พบข้อมูล', dateTimeDesc: '12 minutes ago', isTakeAction: false}, 
	                           {id: '', subject: 'New Task', detail: '0887313792 รับสายแจ้งว่าคนซื้อจะไปจ่ายให้ แอต่ไม่รู้วันไหน', dateTimeDesc: '27 minutes ago', isTakeAction: true}, 
	                           {id: '', subject: 'New Followers', detail: '0925939632ผู้ซื้อฝากหมายเลขโทรกลับ/ไม่มีผู้ค้ำ', dateTimeDesc: '43 minutes ago', isTakeAction: true}, 
	                           {id: '', subject: 'Server Rebooted', detail: '0895487336ผู้ซื้อฝากหมายเลขโทรกลับ/ไม่มีเบอร์ติดต่อผู้ค้ำจากหน้าระบบ', dateTimeDesc: '11:32 AM', isTakeAction: false}, 
	                           {id: '', subject: 'Server Crashed!', detail: '0917645682 ผู้ซื้อคเชนทร์ พุกจีน ไม่สามารถติดต่อได้/0876887132 ผู้ค้ำ เรืองวิทย์ พงษ์แสวง ยังไม่เปิดใช้บริการ', dateTimeDesc: '11:13 AM', isTakeAction: false}, 
	                           {id: '', subject: 'Server Not Responding', detail: '0937791924 ผู้ค้ำ ผู้หญิงรับสายแจ้งไม่มีไม่รู้จัก', dateTimeDesc: '10:57 AM', isTakeAction: true}, 
	                           {id: '', subject: 'New Order Placed', detail: '062-3609484ติดต่อไม่ได้ 098-0233774 ไม่รับสาย', dateTimeDesc: '9:49 AM', isTakeAction: true}, 
	                           {id: '', subject: 'นัด Call', detail: '034296451 ผู้เช้าซื้อ ติดต่อไม่ได้ 0861757391 ผู้ค้ำ ติดต่อไม่ได้', dateTimeDesc: 'Yesterday', isTakeAction: false}, 
	                           {id: '', subject: 'นัดชำระ', detail: '0836945303ผู้ซื้อปิดเครื่อง/0834276267ผู้ค้ำศราวุธชายรับสายแจ้งผู้ค้ำไปทำงานเดี๋ยวให้ติดต่อกลับ', dateTimeDesc: 'Yesterday', isTakeAction: true}];
	
	
	
});