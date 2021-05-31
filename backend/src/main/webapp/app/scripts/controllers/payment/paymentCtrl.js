angular.module('sbAdminApp').controller('PaymentCtrl', function($rootScope, $state, $scope, $base64, $http, $timeout, $translate, $q, $localStorage, $ngConfirm, $filter, urlPrefix, loadData) {
	$scope.periods = loadData.periods;
	$scope.users = loadData.users;
	$scope.roles = [{id: 3, name: 'ผู้ดูแล'}, {id: 1, name: 'ลูกค้า'}];
	$scope.priceData;
	$scope.tabActived = 0;
	$scope.adminSum = 0;
	$scope.formData = {
			period: $scope.periods[0]._id,
			userRole: 3
	};
	$scope.checkBoxType = {
		bon3: true, bon2: true, lang2: true, 
		loy: true, pair4: true, pair5: true, runBon: true, runLang: true
	};
	$scope.tabStatusObj = {
		0: {sendRoundId: null},
		1: {sendRoundId: null}
	};
	
	var typeTitleList = [
		{'1':'รวม 3 บน', 'percent':'percentBon3'}, {'5':'รวมโต๊ด', 'percent':'percentTod'}, {'2':'รวม 2 บน', 'percent':'percentBon2'}, 
		{'3':'รวม 2 ล่าง', 'percent':'percentLang2'}, {'4':'รวมลอย', 'percent':'percentLoy'}, {'41':'รวมแพ 4', 'percent':'percentPare4'}, 
		{'42':'รวมแพ 5', 'percent':'percentPare5'}, {'43':'รวมวิ่งบน', 'percent':'percentRunBon'}, {'44':'รวมวิ่งล่าง', 'percent':'percentRunLang'},
		{'60,61,62':'รวมปักบน', 'percent':'percentPugBon'}, {'63,64':'รวมปักล่าง', 'percent':'percentPugLang'}
	];
	
	
	//---:
	$scope.changeRole = function() {
		$scope.paymentDataList = new Array();
		$scope.formData.userSearchId = null;
		$scope.formData.orderName = null;
		$scope.formData.priceList = null;
	}
	
	function getSumPaymentByOne(sendRoundId) {
		var deferred = $q.defer();
		
		$http.post(urlPrefix + '/restAct/order/getSumPaymentByOne', {
			orderName: $scope.formData.orderName && $scope.formData.orderName.name,
			userId: $scope.formData.userSearchId && $scope.formData.userSearchId.id,
			userRole: $scope.formData.userRole,
			sendRoundId: sendRoundId,
			periodId: $scope.formData.period,
			dealerId: $rootScope.workingOnDealer.id
		}, {
			ignoreLoadingBar: true
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			var map = {};
			map[sendRoundId] = result;
			deferred.resolve(map);
		}, function(response) {
			deferred.reject(response);
		});
		return deferred.promise;
	}
	
	$scope.changeOrderName = function(from) {
		var isBlocked = false;
		if($scope.formData.userRole == 3) {
			if(!$scope.formData.orderName) isBlocked = true;
		} else {
			if(!$scope.formData.userSearchId) isBlocked = true;
		}
		
		if(isBlocked) {
			$scope.paymentDataList = null;
			$scope.formData.priceList = null;
			
			var all;
			if((all = $scope.sendRound.map['all'])) {
				if(all.value && all.value.sum) {
					all.value.sum.str = null;
				}
				if(all.discount && all.discount.sumDiscount) {
					all.discount.sumDiscount.str = null;
				}
				if(all.discount && all.discount.sumAfterDiscount) {
					all.discount.sumAfterDiscount.str = null;					
				}
				
				$scope.netPriceTotal = null;
			}
			return;
		}
		
		//-----------------------------------------------------------------------------
		$scope.sendRound.map['all'].value = {};
		var promises = new Array();
		
		if($scope.formData.sendRoundId) {
			promises.push(getSumPaymentByOne($scope.formData.sendRoundId));
			
			$scope.sendRound.map[$scope.formData.sendRoundId].value = {
				sum: 0
			};
		} else {
			var sendR, sendRoundId;
			for(var x in $scope.sendRound.list) {
				sendR = $scope.sendRound.list[x];
				sendRoundId = sendR.id;
				promises.push(getSumPaymentByOne(sendRoundId));
				
				$scope.sendRound.map[sendRoundId].value = {
					sum: 0
				};
				$scope.sendRound.map['all'].value['sum_' + sendRoundId] = 0;
			}
		}
		
		$q.all(promises).then(function(datas){
			$scope.resultObj = {};
			var result, data, keyOut;
			$scope.paymentDataList = new Array();
			var typeObj, value, key, keyArr;
			
			for(var i in typeTitleList) {
				typeObj = typeTitleList[i];
				key = Object.keys(typeObj)[0];
				
				for(var x in datas) {
					value = 0;
					data = datas[x];
					keyOut = Object.keys(data)[0];
					result = data[keyOut];
					
					if(result.statusCode != 9999) {
						$rootScope.systemAlert(result.statusCode);
						return;
					}
					
					//---:
					if(key.indexOf(',') != -1) {
						keyArr = key.split(",");
						
						for(var i in keyArr) {
							value += result.totalPriceSumAllMap[keyArr[i]];
						}
					} else {
						value = result.totalPriceSumAllMap[key];					
					}
					
					//---:
					$scope.sendRound.map[keyOut].value[typeObj[key]] = {
						num: value,
						str: $filter('number')(value, 2),
						sum: value
					};
					$scope.sendRound.map[keyOut].value.sum += value;
					
					if($scope.sendRound.map['all'].value[typeObj[key]]) {
						$scope.sendRound.map['all'].value[typeObj[key]] = {
								num: value,
								str: $scope.sendRound.map['all'].value[typeObj[key]].str + ' | ' + $filter('number')(value, 2),
								sum: $scope.sendRound.map['all'].value[typeObj[key]].sum + value
						};
					} else {						
						$scope.sendRound.map['all'].value[typeObj[key]] = {
							num: value,
							str: $filter('number')(value, 2),
							sum: value
						}
					}
					$scope.sendRound.map['all'].value['sum_' + keyOut] += value;
				}
				$scope.paymentDataList.push({
					title: typeObj[key],
					percent: typeObj.percent
				});
			}
			
			//----:
			if($scope.formData.sendRoundId) {
				$scope.sendRound.map[$scope.formData.sendRoundId].value.sum = {
						num: $scope.sendRound.map[$scope.formData.sendRoundId].value.sum,
						str: $filter('number')($scope.sendRound.map[$scope.formData.sendRoundId].value.sum, 2)
				};
			} else {
				var sendR;
				for(var x in $scope.sendRound.list) {
					sendR = $scope.sendRound.list[x];
					if($scope.sendRound.map['all'].value.sum) {
						$scope.sendRound.map['all'].value.sum.str = $scope.sendRound.map['all'].value.sum.str + ' | ' + $filter('number')($scope.sendRound.map['all'].value['sum_' + sendR.id], 2);
					} else {
						$scope.sendRound.map['all'].value.sum = {
							str: $filter('number')($scope.sendRound.map['all'].value['sum_' + sendR.id], 2)
						};
					}
				}	
			}
			
			//---:
			if($scope.formData.orderName) {
				$scope.formData.priceList = $scope.formData.orderName.price;				
			}
			if($scope.formData.userSearchId) {
				$scope.formData.priceList = $scope.formData.userSearchId.price;				
			}
			$scope.changePriceList(from);
		});
	}
	
	$scope.changePeriod = function() {
		$scope.formData.orderName = null;
		$scope.formData.userSearchId = null;
		$scope.formData.sendRoundId = null;
		
		getSumPaymentAll();
	}
	
	$scope.changeSendRound = function() {
		$scope.tabStatusObj[$scope.tabActived].sendRoundId = $scope.formData.sendRoundId;
		
		if($scope.tabActived == 0) {
			getSumPaymentAll();			
		} else {
			$scope.changeOrderName();
		}
	}
	
	$scope.changeTab = function(tab) {
		$scope.tabActived = tab;
		
		if($scope.tabActived == 1) {
			$scope.formData.sendRoundId = null;
			
			//set to default
			$scope.formData.userRole = 3; 
			$scope.formData.userSearchId = null;
			$scope.formData.orderName = null;
			$scope.changeOrderName();
		} else {
			$scope.formData.sendRoundId = $scope.tabStatusObj[$scope.tabActived].sendRoundId;			
		}
	}
	
	$scope.changePrice = function(obj) {
		$http.post(urlPrefix + '/restAct/order/changePrice', {
			userId: obj.id,
			name: obj.name,
			priceId: obj.price,
			isCustomer: obj.isCustomer,
			dealerId: $rootScope.workingOnDealer.id
		}).then(function(data) {
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			if(obj.isCustomer) {
				$scope.formData.userRole = 1;
				$scope.formData.userSearchId = $filter('filter')($scope.paymentCustomerData, {id: obj.id}, true)[0];
				$scope.formData.orderName = null;
			} else {
				$scope.formData.userRole = 3;
				$scope.formData.orderName = $filter('filter')($scope.paymentAdminData, {name: obj.name}, true)[0];
				$scope.formData.userSearchId = null;
			}
			
			$scope.changeOrderName(0);
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	$scope.changePriceList = function(from) {	
		var firstPrice = $filter('filter')($scope.priceList, {id: $scope.formData.priceList}, true)[0];
		$scope.priceData = {};
		
		if($scope.formData.sendRoundId) {
			if(!$scope.formData.priceList) {
				$scope.priceData[$scope.formData.sendRoundId] = angular.copy(firstPrice.priceData[$scope.formData.sendRoundId]);
			} else {
				$scope.priceData[$scope.formData.sendRoundId] = firstPrice.priceData[$scope.formData.sendRoundId];
			}			
		} else {
			var sendR;
			for(var x in $scope.sendRound.list) {
				sendR = $scope.sendRound.list[x];
				$scope.priceData[sendR.id] = firstPrice.priceData[sendR.id];
			}			
		}
		
		//---:
		$scope.sendRound.map['all'].discount = {};
		$scope.netPriceTotal = 0;
		var obj, discountDummy, value, sum, percent;
		
		for(var i in $scope.paymentDataList) {
			obj = $scope.paymentDataList[i];
			
			if($scope.formData.sendRoundId) {
				if(!$scope.formData.priceList) {
					percent = 0;
				} else {
					if($scope.priceData[$scope.formData.sendRoundId] && $scope.priceData[$scope.formData.sendRoundId][obj.percent]) {
						percent = $scope.priceData[$scope.formData.sendRoundId][obj.percent];
					} else {
						percent = 0;							
					}
				}
				
				value = $scope.sendRound.map[$scope.formData.sendRoundId].value[obj.title].num;
				sum = $scope.sendRound.map[$scope.formData.sendRoundId].value[obj.title].sum;
				discountDummy = (percent / 100) * value;
				
				if(!$scope.sendRound.map[$scope.formData.sendRoundId].discount) {					
					$scope.sendRound.map[$scope.formData.sendRoundId].discount = {};
				}
				$scope.sendRound.map[$scope.formData.sendRoundId].discount[obj.title] = {
					num: discountDummy,
					str: $filter('number')(discountDummy, 2) + ' (' + percent + '%)',
					percent: percent,
					afterDiscountStr: $filter('number')(value - discountDummy, 2),
					totalDiscount: discountDummy,
					netPrice: sum - discountDummy
				}
				
				if(!$scope.sendRound.map['all'].discount['sumDiscount_' + $scope.formData.sendRoundId]) {
					$scope.sendRound.map['all'].discount['sumDiscount_' + $scope.formData.sendRoundId] = 0;
				}
				$scope.sendRound.map['all'].discount['sumDiscount_' + $scope.formData.sendRoundId] += discountDummy;
				
				if(!$scope.sendRound.map['all'].discount['sumAfterDiscount_' + $scope.formData.sendRoundId]) {
					$scope.sendRound.map['all'].discount['sumAfterDiscount_' + $scope.formData.sendRoundId] = 0;
				}
				$scope.sendRound.map['all'].discount['sumAfterDiscount_' + $scope.formData.sendRoundId] += (value - discountDummy);
				
				$scope.netPriceTotal += $scope.sendRound.map[$scope.formData.sendRoundId].discount[obj.title].netPrice;
			} else {
				var sendR;
				for(var x in $scope.sendRound.list) {
					sendR = $scope.sendRound.list[x];
					
					if(!$scope.formData.priceList) {
						percent = 0;
					} else {
						if($scope.priceData[sendR.id] && $scope.priceData[sendR.id][obj.percent]) {
							percent = $scope.priceData[sendR.id][obj.percent];
						} else {
							percent = 0;							
						}
					}
					
					value = $scope.sendRound.map[sendR.id].value[obj.title].num;
					sum = $scope.sendRound.map[sendR.id].value[obj.title].sum;
					discountDummy = (percent / 100) * value;
					
					if($scope.sendRound.map['all'].discount[obj.title]) {
						$scope.sendRound.map['all'].discount[obj.title].str = $scope.sendRound.map['all'].discount[obj.title].str + ' | ' + $filter('number')(discountDummy, 2)  + ' (' + percent + '%)';
						$scope.sendRound.map['all'].discount[obj.title].percent = percent;
						$scope.sendRound.map['all'].discount[obj.title].afterDiscountStr = $scope.sendRound.map['all'].discount[obj.title].afterDiscountStr + ' | ' + $filter('number')(value - discountDummy, 2);
						$scope.sendRound.map['all'].discount[obj.title].totalDiscount = $scope.sendRound.map['all'].discount[obj.title].totalDiscount + discountDummy;
						$scope.sendRound.map['all'].discount[obj.title].netPrice = $scope.sendRound.map['all'].discount[obj.title].netPrice + sum - discountDummy;
					} else {
						$scope.sendRound.map['all'].discount[obj.title] = {
							str: $filter('number')(discountDummy, 2) + ' (' + percent + '%)',
							percent: percent,
							afterDiscountStr: $filter('number')(value - discountDummy, 2),
							totalDiscount: discountDummy,
							netPrice: sum - discountDummy
						};
					}
					if(!$scope.sendRound.map['all'].discount['sumDiscount_' + sendR.id]) {
						$scope.sendRound.map['all'].discount['sumDiscount_' + sendR.id] = 0;
					}
					$scope.sendRound.map['all'].discount['sumDiscount_' + sendR.id] += discountDummy;
					
					if(!$scope.sendRound.map['all'].discount['sumAfterDiscount_' + sendR.id]) {
						$scope.sendRound.map['all'].discount['sumAfterDiscount_' + sendR.id] = 0;
					}
					$scope.sendRound.map['all'].discount['sumAfterDiscount_' + sendR.id] += (value - discountDummy);
				}
				
				$scope.netPriceTotal += $scope.sendRound.map['all'].discount[obj.title].netPrice;
			}
		}
		
		//---:
		if($scope.formData.sendRoundId) {
			$scope.sendRound.map[$scope.formData.sendRoundId].discount.sumDiscount = {
				str: $filter('number')($scope.sendRound.map['all'].discount['sumDiscount_' + $scope.formData.sendRoundId], 2),
				num: $scope.sendRound.map['all'].discount['sumDiscount_' + $scope.formData.sendRoundId]
			};
			
			$scope.sendRound.map[$scope.formData.sendRoundId].discount.sumAfterDiscount = {
				str: $filter('number')($scope.sendRound.map['all'].discount['sumAfterDiscount_' + $scope.formData.sendRoundId], 2)
			};
		} else {
			var sendR;
			for(var x in $scope.sendRound.list) {
				sendR = $scope.sendRound.list[x];
				
				//---:
				if($scope.sendRound.map['all'].discount.sumDiscount) {
					$scope.sendRound.map['all'].discount.sumDiscount.str = $scope.sendRound.map['all'].discount.sumDiscount.str + ' | ' + $filter('number')($scope.sendRound.map['all'].discount['sumDiscount_' + sendR.id], 2);
					$scope.sendRound.map['all'].discount.sumDiscount.num = $scope.sendRound.map['all'].discount.sumDiscount.num + $scope.sendRound.map['all'].discount['sumDiscount_' + sendR.id];
				} else {					
					$scope.sendRound.map['all'].discount.sumDiscount = {
						str: $filter('number')($scope.sendRound.map['all'].discount['sumDiscount_' + sendR.id], 2),
						num: $scope.sendRound.map['all'].discount['sumDiscount_' + sendR.id]
					};
				}
				
				//---:
				if($scope.sendRound.map['all'].discount.sumAfterDiscount) {
					$scope.sendRound.map['all'].discount.sumAfterDiscount.str = $scope.sendRound.map['all'].discount.sumAfterDiscount.str + ' | ' + $filter('number')($scope.sendRound.map['all'].discount['sumAfterDiscount_' + sendR.id], 2);
				} else {					
					$scope.sendRound.map['all'].discount.sumAfterDiscount = {
						str: $filter('number')($scope.sendRound.map['all'].discount['sumAfterDiscount_' + sendR.id], 2)
					};
				}
			}	
		}
		
		//---:
		if(from == 0) {
			if($scope.formData.userSearchId) {				
				$scope.sumDiscount1 -= $scope.formData.userSearchId.sumDiscount;
				$scope.sumDiscount1 += $scope.sendRound.map['all'].discount.sumDiscount.num;
				$scope.formData.userSearchId.sumDiscount = $scope.sendRound.map['all'].discount.sumDiscount.num;
			} else {
				$scope.sumDiscount1 -= $scope.formData.orderName.sumDiscount;
				$scope.sumDiscount1 += $scope.sendRound.map['all'].discount.sumDiscount.num;
				$scope.formData.orderName.sumDiscount = $scope.sendRound.map['all'].discount.sumDiscount.num;
			}
		}
	}
	
	function getPriceList() {
		$http.get(urlPrefix + '/restAct/receiver/getPriceList?dealerId=' + $rootScope.workingOnDealer.id + '&enabled=true').then(function(data){
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.priceList = result.priceList;
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	function getSumPaymentAll() {
		$scope.isLoadProgress = true;
		$http.post(urlPrefix + '/restAct/order/getSumPaymentAll', {
			sendRoundId: $scope.formData.sendRoundId,
			periodId: $scope.formData.period,
			dealerId: $rootScope.workingOnDealer.id
		},{
			ignoreLoadingBar: true
		}).then(function(data) {
			$scope.isLoadProgress = false;
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.paymentAdminData = result.paymentData['admin'];
			$scope.paymentCustomerData = result.paymentData['customer'];
			
			$scope.paymentAllData = $scope.paymentAdminData.concat($scope.paymentCustomerData);
			
			//---:
			calSum1();
		}, function(response) {
			$rootScope.systemAlert(response.status);
			$scope.isLoadProgress = false;
		});
	}
	
	function calSum1() {
		$scope.sumDiscount1 = 0;
		$scope.sum1 = 0;
		var orderObj;
		for(var i in $scope.paymentAdminData) {
			orderObj = $scope.paymentAdminData[i];
			orderObj.desc = parseInt(i)+1 + '. ' + orderObj.name;
			
			$scope.sum1 += orderObj.sum;
			$scope.sumDiscount1 += orderObj.sumDiscount;
		}
		for(var i in $scope.paymentCustomerData) {
			orderObj = $scope.paymentCustomerData[i];
			orderObj.desc = parseInt(i)+1 + '. ' + orderObj.name;
			
			$scope.sum1 += orderObj.sum;
			$scope.sumDiscount1 += orderObj.sumDiscount;
		}
	}
	
	function getSendRound() {
		$http.get(urlPrefix + '/restAct/sendRound/getDataList?dealerId=' + $rootScope.workingOnDealer.id + '&enabled=true').then(function(data){
			var result = data.data;
			if(result.statusCode != 9999) {
				$rootScope.systemAlert(result.statusCode);
				return;
			}
			
			$scope.sendRound = {};
			$scope.sendRound.map = {};
			$scope.sendRound.list = result.dataList;
			var sendR, allTitle = '';
			for(var x in $scope.sendRound.list) {
				sendR = $scope.sendRound.list[x];
				sendR.desc = sendR.name + ' ไม่เกิน ' + $filter('date')(sendR.limitedTime, 'HH:mm');
				$scope.sendRound.map[sendR.id] = {name: sendR.name};
				
				if(x == $scope.sendRound.list.length-1) {
					allTitle += sendR.name;	
				} else {
					allTitle += sendR.name + ' | ';						
				}
			}
			$scope.sendRound.map['all'] = {name: allTitle};
		}, function(response) {
			$rootScope.systemAlert(response.status);
		});
	}
	
	//---:
	getPriceList();
	getSumPaymentAll();
	getSendRound();
	
});