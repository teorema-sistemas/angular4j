/**
 * Copyright (C) 2014 Red Hat, Inc, and individual contributors. Copyright (C) 2011-2012 VMware, Inc.
 */

package angular4J.sockjs;

public class ResponseReceiver extends GenericReceiver {

   protected SockJsRequest request;
   protected SockJsResponse response;
   protected SockJsServer.Options options;
   protected int currResponseSize;
   protected int maxResponseSize = -1;

   public ResponseReceiver(SockJsRequest request, SockJsResponse response, SockJsServer.Options options) {
      this.request = request;
      this.response = response;
      this.options = options;
      this.currResponseSize = 0;
      this.maxResponseSize = options.responseLimit;
   }

   @Override
   public boolean doSendFrame(String payload) {
      return doSendFrame(payload, true);
   }

   private boolean doSendFrame(String payload, boolean checkSize) {
      if (checkSize) {
         this.currResponseSize += payload.length();
      }
      boolean r = false;
      try {
         this.response.write(payload);
         r = true;
      }
      catch (SockJsException x) {
         didAbort();
         return r;
      }
      if (checkSize) {
         if (maxResponseSize >= 0 && currResponseSize >= maxResponseSize) {
            didClose();
         }
      }
      return r;
   }

   @Override
   public void checkAlive() {
      doSendFrame("h", false);
   }

   @Override
   protected void didClose() {
      super.didClose();
      try {
         response.end();
      }
      catch (Exception x) {}

      response = null;
   }
}
