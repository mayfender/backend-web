angular.module('sbAdminApp').factory("httpInterceptor", function ($q, $window, $localStorage, $log, urlPrefix) {
    return {
    	'request': function (config) {
            config.headers = config.headers || {};
            
            if ($localStorage.token) {
                config.headers['X-Auth-Token'] = $localStorage.token;
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
         }
       }
});