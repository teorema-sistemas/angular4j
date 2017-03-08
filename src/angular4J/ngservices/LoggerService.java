
package angular4J.ngservices;

/**
 * Angular4J logger service
 */

@NGExtension
public class LoggerService implements NGService {

   @Override
   public String render() {
      StringBuilder result = new StringBuilder();
      result.append("app.service('logger',['$log',function($log){ var self=this;");
      result.append("self.log=function(logMessages){");
      result.append("for (var i in logMessages){");
      result.append("var message=logMessages[i].message;");
      result.append("var level=logMessages[i].level;");
      result.append("if(level===\"info\"){$log.info(message);};");
      result.append("if(level===\"error\"){$log.error(message);};");
      result.append("if(level===\"warn\"){$log.warn(message);};");
      result.append("if(level===\"debug\"){$log.debug(message);};");
      result.append("}}}]);");

      return result.toString();
   }
}
