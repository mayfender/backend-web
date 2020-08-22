angular.module('liffApp', ['cp.ngConfirm']).controller('LiffCtrl', function($scope, $timeout, $q, $ngConfirm) {
	$scope.lineData = {};
	$scope.keyword = '';
	$scope.setNumberDigit = null;
	$scope.isValidToNext = false;
	$scope.prediction = {};
	const prediction = new Prediction();
	
	$scope.kbs = [
		['1', '4', '7','0'],
		['2', '5', '8','00'],
		['3', '6', '9', '-'],
		['=', 'x', ',', 'ลบ']
	];
	$scope.formData = {};
	$scope.formData.functionId = '1';
	$scope.functions = [{id: '1', name: 'ทั่วไป'}, {id: '2', name: 'ชุด'}, {id: '3', name: 'ก๊อบปี้วาง'}];
	var startPredictIndex = 0;
	
	$scope.nextStep = function() {
		if($scope.formData.functionId == 1) {
			$scope.keyword += ',';
		} else if($scope.formData.functionId == 2) {
			//
		} else if($scope.formData.functionId == 3) {
			// 
		}
	}
	
	$scope.changeFunction = function() {
		$scope.keyword = '';
		$scope.setNumberDigit = null;
	}
	
	
	
	
	
	
	$scope.predictSet = function(value) {
		$scope.keyword += value + ',';
	}
	
	/*function predict1(val) {
		var index1 = $scope.keyword.indexOf("=", startPredictIndex);
		
		if(index1 != -1) {
			if(val == 'x' || val == ',') {
				$scope.predicted = '';
				startPredictIndex = $scope.keyword.length;
				return;
			}
			
			var equalVal = $scope.keyword.substring(index1);
			if(equalVal.length == 1 && !val) {
				$scope.predicted = '';
			} else if(equalVal) {
				$scope.predicted = 'x' + $scope.keyword.substring(index1 + 1) + val;	
			}
		}
	}*/
	
	$scope.kbPressed = function(val) {
		if(val == 'ลบ') {
			$scope.keyword = $scope.keyword.slice(0, -1);
			
			/*if($scope.formData.functionId == '1') {
				var indexX = $scope.keyword.lastIndexOf("x");
				var indexCom = $scope.keyword.lastIndexOf(",");
				
				if(indexX == -1 && indexCom == -1) {
					startPredictIndex = 0;
				} else if(indexX > indexCom) {
					startPredictIndex = indexX;
				} else {
					startPredictIndex = indexCom;				
				}
				predict1('');
			}*/
		} else {
			if($scope.formData.functionId == '1') {
//				predict1(val);
				$scope.keyword += val;
			} else if($scope.formData.functionId == '2') {
				if(!$scope.setNumberDigit) {					
					$scope.keyword += val;
					if($scope.keyword.length == 2) {
						askDigit();
					}
					return;
				}
				
				if($scope.keyword.length == $scope.setNumberDigit) {
					$scope.keyword += ',';
				} else if($scope.keyword.length > $scope.setNumberDigit){
					var index = $scope.keyword.lastIndexOf(",") + 1
					var dummy = $scope.keyword.substring(index);
					
					if(dummy.length == $scope.setNumberDigit) {
						$scope.keyword += ',';			
					}
				}
				$scope.keyword += val;				
			} else if($scope.formData.functionId == '3') {
				
			}
		}
		
		$('#valueBox').animate({scrollLeft: '+=1500'}, 500);		
	}
	
	$scope.clear = function() {
		$scope.keyword = '';
		$scope.setNumberDigit = null;
	}
	
	 $scope.$watch('keyword', function(newValue, oldValue, scope) {
		 if($scope.formData.functionId == 1) {
			 if($scope.keyword) {
				 $scope.isValidToNext = true;
			 } else {
				 $scope.isValidToNext = false;				 
			 }			 
			 $scope.prediction = prediction.getPredictPrice($scope.keyword);		 
			 
		 } else if($scope.formData.functionId == 2) {
			 if($scope.setNumberDigit) {
				 $scope.isValidToNext = true;
			 } else {
				 $scope.isValidToNext = false;				 
			 }
		 } else if($scope.formData.functionId == 3) {
			// 
		 }
	 });
	
	function askDigit() {
		$ngConfirm({
		    title: 'เลือกเลขชุด',
		    content: "",
		    type: 'blue',
		    typeAnimated: true,
		    scope: $scope,
		    columnClass: 'col-xs-10 col-xs-offset-1',
		    buttons: {
			    digit2: {
		        	text: 'เลข 2 ตัว',
		        	btnClass: 'btn-red',
		        	action: function(scope, button){
		        		$scope.setNumberDigit = 2;
		        	}
		        },
		        digit3: {
		        	text: 'เลข 3 ตัว',
		        	btnClass: 'btn-green',
		        	action: function(scope, button){
		        		$scope.setNumberDigit = 3;
		        	}
		        }
		    }
		});	
	}
	
	function runApp() {
		return $q(function(resolve, reject) {
			liff.getProfile().then(profile => {
				resolve(profile);
			}).catch(err => reject(err));
		});
    }
	
	/*$scope.$watch('$viewContentLoaded', 
		function() {
			$timeout(function() {
			    liff.init({ liffId: "1654799308-zj2ewgpV" }, () => {
			    	if (liff.isLoggedIn()) {
			    		runApp().then(function(profile) {
			    			$scope.lineData.mayfender = 'testing';
							$scope.lineData.pictureUrl = profile.pictureUrl;
							$scope.lineData.userId = profile.userId;
							$scope.lineData.displayName = profile.displayName;
							$scope.lineData.statusMessage = profile.statusMessage;
							$scope.lineData.getDecodedIDToken = liff.getDecodedIDToken().email;
			        	}, function(err) {
			        		console.error(err)
			        	});
			      	} else {
			        	liff.login();
			     	}
			    }, err => console.error(err.code, error.message));
		},0);
	});*/
});


//-- Classes
class Prediction {
	constructor() {}
	
	lastOrderNumber(val) {
		this.lastDigit = val.substring(val.length - 1, val.length);
		var commaIndex = val.lastIndexOf(",") + 1;
		if(this.lastDigit == '=') {
			this.orderNumber = val.substring(commaIndex, val.length - 1);
			this.lastEqualIndex = val.lastIndexOf("=");
			return this.orderNumber;
		} else {
			this.orderNumber = null;
		}
		return this.orderNumber;
	}
	
	getProbNum(orderNumber) {
		if(!orderNumber) return 0;
		
		var text = '', ch, count = 0;
		for( var x = 0; x < orderNumber.length; x++ ) {
			ch = orderNumber.charAt(x);
			if(text.includes(ch)) {
				count++;
			}
			text += orderNumber.charAt(x);
		}
		
		var probNum = 0;
		if(orderNumber.length == 2) {
			probNum = count == 0 ? 2 : 0;			
		} else if(orderNumber.length == 3) {
			probNum = count == 0 ? 6 : (count == 1 ? 3 : 0);
		} else if(orderNumber.length == 4) {
			probNum = count == 0 ? 24 : (count == 1 ? 12 : (count == 2 ? 4 : 0));
		} else if(orderNumber.length == 5) {
			probNum = count == 0 ? 60 : (count == 1 ? 33 : (count == 2 ? 13 : (count == 3 ? 4 : 0)));
		}
		return {probNum: probNum, orderNumberLen: orderNumber.length};
	}
	
	getPredictPrice(val) {
		var eChInd = val.lastIndexOf("=");
		var xChInd = val.lastIndexOf("x");
		var commChInd = val.lastIndexOf(",");
		if(eChInd == -1) return null;
		
		this.startPredictIndex = eChInd;
		if(xChInd > this.startPredictIndex) {
			this.startPredictIndex = xChInd;		
		}
		if(commChInd > this.startPredictIndex) {
			this.startPredictIndex = commChInd;			
		}
		
		var index1 = val.indexOf("=", this.startPredictIndex);
		if(index1 != -1) {
			var equalVal = val.substring(index1);
			if(equalVal.length == 1) {
				return null;
			} else if(equalVal) {
				var probNumObj = this.getProbNum(val.substring(commChInd + 1, index1));
				return {
					orderNumberLen: probNumObj.orderNumberLen, 
					probNum: probNumObj.probNum, price: equalVal.substring(1)
				}
			}				
		}
		return null;
	}
	
}