
package angular4J.remote;

import java.io.Serializable;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gson.JsonObject;

import angular4J.context.NGSessionScopeContext;
import angular4J.context.NGLocator;

/**
 * Realtime remote calls handler
 */

@SuppressWarnings("serial")
@Dependent
public class RealTimeInvoker implements Serializable {

   @Inject
   InvocationHandler remoteInvoker;

   public void process(@Observes @DataReceivedEvent RealTimeDataReceivedEvent event) {

      JsonObject jObj = event.getData();
      String UID = event.getSessionId();
      String beanName = jObj.get("service").getAsString();
      String method = jObj.get("method").getAsString();
      long reqId = jObj.get("reqId").getAsLong();
      JsonObject paramsObj = jObj.get("params").getAsJsonObject();

      NGSessionScopeContext.getInstance().setCurrentContext(UID);

      if (reqId == 0) {
         return;
      }

      Object model = NGLocator.getInstance().lookup(beanName, UID);

      remoteInvoker.realTimeInvoke(model, method, paramsObj, event, reqId, UID);
   }
}
