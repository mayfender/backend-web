angular.module('sbAdminApp').controller('MoneyCtrl', function($rootScope, $scope, $state, $http, $stateParams, $translate, $log, $stomp, $sce, toaster, urlPrefix) {
	console.log('Money report');
	
	$scope.moneys = [{date: '20-04-2016', value: '2,011.00'},
	                 {date: '21-04-2016', value: '6,565.00'},
	                 {date: '22-04-2016', value: '7,587.00'},
	                 {date: '22-04-2016', value: '7,587.00'},
	                 {date: '22-04-2016', value: '7,587.00'},
	                 {date: '22-04-2016', value: '7,587.00'},
	                 {date: '22-04-2016', value: '7,587.00'},
	                 {date: '22-04-2016', value: '7,587.00'},
	                 {date: '22-04-2016', value: '7,587.00'}];
	
});