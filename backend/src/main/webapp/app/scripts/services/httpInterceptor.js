angular.module('sbAdminApp').factory("httpInterceptor", function ($rootScope, $q, $window, $location, $localStorage, $log, urlPrefix) {
    return {
    	'request': function (config) {
    		if(!config.ignoreUpdateLastTimeAccess) {
    			$rootScope.lastTimeAccess = new Date();
    		}
            config.headers = config.headers || {};
            
            if($rootScope.username) {
            	if ($localStorage.token && $localStorage.token[$rootScope.username]) {
            		config.headers['X-Auth-Token'] = $localStorage.token[$rootScope.username];
            	}
            } else {
            	if($localStorage.token && Object.keys($localStorage.token)[0]) {
            		config.headers['X-Auth-Token'] = $localStorage.token[Object.keys($localStorage.token)[0]];            		
            	}
            }
            
            return config;
        },
        "response": function (response) {
           var responseHeaders;
           responseHeaders = response.headers();
           
           if (responseHeaders["content-type"] 
           		  && responseHeaders["content-type"].indexOf("text/html") !== -1
                  && response.data 
                  && response.data.indexOf('<meta name="unauthorized" content="true">') !== -1) {
        	   
        	 
        	 $window.location.href = urlPrefix + '/logout';
             return $q.reject(response);
           }
           return response;
         },
        "responseError": function (rejection) {
        	if (rejection.status === 401) {
        		
//        		$window.location.href = urlPrefix + '/logout';
//        		$location.path(urlPrefix + '/logout');
            }
        	return $q.reject(rejection);
        }
       }
});