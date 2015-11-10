angular.module('sbAdminApp').factory("httpInterceptor", function ($q, $window, $log, urlPrefix) {
    return {
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