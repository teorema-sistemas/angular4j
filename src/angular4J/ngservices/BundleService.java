package angular4J.ngservices;

/**
 * this component render the angularJS service code that contain the loadBundle() js method (useful
 * for i18n for example)
 */

@NGExtension
public class BundleService implements NGService {

   @Override
   public String render() {
      StringBuilder result = new StringBuilder();
      result.append("app.service(\"bundleService\",['$http','$rootScope','$timeout',function($http,$rootScope,$timeout){");
      result.append("this.loadBundle=function(bundleName,aleas){");
      result.append("$http.get($rootScope.baseUrl+'resources/'+bundleName).success(function(data){");
      result.append("$rootScope[aleas]=data;");
      result.append("});};;}]);");

      return result.toString();
   }
}
