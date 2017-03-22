/**
 * Copyright (C) 2014 Red Hat, Inc, and individual contributors.
 * Copyright (C) 2011-2012 VMware, Inc.
 */

package angular4J.sockjs.servlet;

import static angular4J.sockjs.ReadyState.CLOSED;
import static angular4J.sockjs.ReadyState.CLOSING;
import static angular4J.sockjs.ReadyState.OPEN;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.CloseReason;
import javax.websocket.MessageHandler;

import angular4J.sockjs.GenericReceiver;
import angular4J.sockjs.Session;
import angular4J.sockjs.SockJsRequest;
import angular4J.sockjs.SockJsServer;

/**
 * RawWebsocketSessionReceiver logic from sockjs-node's trans-websocket.coffee
 */
public class RawWebsocketSessionReceiver extends Session {
   
   private javax.websocket.Session ws;

   private static final Logger log = Logger.getLogger(RawWebsocketSessionReceiver.class.getName());

   public RawWebsocketSessionReceiver(SockJsRequest req, SockJsServer server, javax.websocket.Session ws) {
      super(null, server);
      this.ws = ws;
      this.ws.addMessageHandler(new MessageHandler.Whole<String>(){

         @Override
         public void onMessage(String message) {
            didMessage(message);
         }
      });

      readyState = OPEN;
      recv = new GenericReceiver(){

         {
            protocol = "websocket-raw";
         }

         @Override
         public boolean doSendFrame(String payload) {
            return false;
         }

         @Override
         public void checkAlive() {}
      };
      decorateConnection(req);
      server.emitConnection(connection);
   }

   @Override
   public void didMessage(String payload) {
      if (readyState.equals(OPEN)) {
         connection.emitData(payload);
      }
   }

   @Override
   public boolean send(String payload, boolean async) {

      if (!readyState.equals(OPEN)) {
         return false;
      }
      try {

         if (!async) {
            ws.getBasicRemote().sendText(payload);
         } else {
            ws.getAsyncRemote().sendText(payload);
         }
      }
      catch (IOException ex) {
         log.log(Level.WARNING, "Error sending raw websocket data", ex);
      }
      return true;
   }

   @Override
   public boolean close(int status, String reason) {
      if (!readyState.equals(OPEN)) {
         return false;
      }
      readyState = CLOSING;
      try {
         ws.close(new CloseReason(CloseReason.CloseCodes.getCloseCode(status), reason));
      }
      catch (IOException ex) {
         log.log(Level.FINE, "Error closing raw websocket", ex);
      }
      return true;
   }

   public void didClose() {
      if (ws == null) {
         return;
      }
      try {
         ws.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Normal closure"));
      }
      catch (IOException x) {
         log.log(Level.FINE, "Error closing receiver", x);
      }
      ws = null;

      readyState = CLOSED;
      connection.emitClose();
      connection = null;
   }
}
