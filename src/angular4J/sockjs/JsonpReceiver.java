/**
 * Copyright (C) 2014 Red Hat, Inc, and individual contributors.
 * Copyright (C) 2011-2012 VMware, Inc.
 */

package angular4J.sockjs;

public class JsonpReceiver extends ResponseReceiver {

   public JsonpReceiver(SockJsRequest req, SockJsResponse res, SockJsServer.Options options, String callback) {
      super(req, res, options);
      this.callback = callback;
      protocol = "jsonp-polling";
      maxResponseSize = 1;
   }

   @Override
   public boolean doSendFrame(String payload) {
      return super.doSendFrame(callback + "(" + Utils.jsonStringify(payload) + ");\r\n");
   }

   private String callback;
}
