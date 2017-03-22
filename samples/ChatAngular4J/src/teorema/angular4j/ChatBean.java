package teorema.angular4j;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import angular4J.api.Angular4J;
import angular4J.realtime.RealTime;
import angular4J.realtime.RealTimeClient;
import angular4J.util.ModelQuery;

@Angular4J
@ApplicationScoped
public class ChatBean implements Serializable {

   @Inject
   RealTimeClient realTimeClient;

   @Inject
   ModelQuery model;

   @RealTime
   public void send(ChatMessage chatMessage) {

      realTimeClient.broadcast(model.pushTo("messages", chatMessage), false);

   }

}
