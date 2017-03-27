package angular4J.ngservices;

/**
 * responseHandler angularJs Service to handle any server response (Half-duplex or realtime
 * protocol)
 */

@NGExtension
public class ResponseHandlerService implements NGService {

   @Override
   public String render() {

      StringBuilder result = new StringBuilder();
      result.append("app.service('responseHandler',['logger','$rootScope','$filter',function(logger,$rootScope,$filter){");
      result.append("this.handleResponse=function(msg,caller,isRPC){");
      result.append("var mainReturn={};");
      result.append("for (var key in msg) {");
      result.append("if((key==='zadd')||(key==='rm')||(key==='rm-k')){");
      result.append("var equalsKey='--';");
      result.append("for(var modelkey in msg[key]){");
      result.append("if (!(angular.isDefined(caller[modelkey]))){");
      result.append("caller[modelkey]=[]; }");
      result.append("var tab=msg[key][modelkey];");
      result.append("for (var value in tab){");
      result.append("if (typeof tab[value] == 'string' || tab[value] instanceof String){");
      result.append("if(tab[value].indexOf('equalsKey:') > -1){equalsKey=tab[value].replace('equalsKey:',''); ;}}}");
      result.append("for (var value in tab){");
      result.append("if((key==='rm')||(key==='rm-k')){");
      result.append("if(equalsKey=='NAN'){");
      result.append("if(tab[value]==='equalsKey:NAN'){continue;}");
      result.append("var index=ng4J.isIn(caller[modelkey],tab[value]);");
      result.append("if(index>-1){caller[modelkey].splice(index, 1);continue;}");
      result.append("}else{");
      result.append("var criteria={};");
      result.append("criteria[equalsKey]='!'+tab[value];");
      result.append("caller[modelkey] = $filter('filter')(caller[modelkey], criteria);");
      result.append("}};");
      result.append("if(key==='zadd'){ ");
      result.append("var found=false; ");
      result.append("if(ng4J.isIn(caller[modelkey],tab[value])>-1){ found=true;}");
      result.append("if(!(found)){");
      result.append("caller[modelkey].push(tab[value]);");
      result.append("}};}}}");
      // --------------------------------------------------------------------
      result.append("if(!(key in ['rootScope','zadd','mainReturn','rm','rm-k'])){");
      result.append("caller[key]=msg[key];");
      result.append("}");
      result.append("if ((key==='mainReturn')&&(msg[key])){");
      result.append("mainReturn=msg[key];}");
      result.append("}");
      result.append("if(!isRPC || !$rootScope.$$phase) {$rootScope. $digest ;$rootScope.$apply();}");
      result.append("if(msg.log){logger.log(msg.log);}");
      result.append("return mainReturn;");
      result.append("};}]);");

      return result.toString();
   }

}
