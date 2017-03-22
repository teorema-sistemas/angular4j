/**
 * Copyright (C) 2014 Red Hat, Inc, and individual contributors.
 * Copyright (C) 2011-2012 VMware, Inc.
 */

package angular4J.sockjs;

import java.util.ArrayList;
import java.util.List;

import angular4J.util.CommonUtils;
import angular4J.util.NGParser;

public abstract class GenericReceiver {

   protected void didAbort() {
      Session session = this.session;
      didClose();
      if (session != null) {
         session.didTimeout();
      }
   }

   protected void didClose() {
      if (session != null) {
         session.unregister();
      }
   }

   public void doSendBulk(List<String> messages) {
      List<String> qMsgs = new ArrayList<>(messages.size());
      for (String m: messages) {
         qMsgs.add(NGParser.getInstance().serialize(m));
      }
      doSendFrame("a[" + CommonUtils.parseStrArray(qMsgs, ",") + "]");
   }

   public abstract boolean doSendFrame(String payload);

   public abstract void checkAlive();

   public Session session;
   protected String protocol = null;
}
