package angular4J.ngservices;

/**
 * A sockJs RPC service wrapper for Angular4J
 */

@NGExtension
public class SockJsRpcService implements NGService {

   @Override
   public String render() {

      StringBuilder result = new StringBuilder();

      result.append("app.provider('realtimeManager',function RTSrvc(){");
      result.append("var self=this;");
      result.append("self.options={debug : false};");
      result.append("self.setOptions=function(options){self.options=options;};");
      result.append("self.webSocketEnabled=function(enabled){if(!enabled){WebSocket = undefined;}};");
      result.append("self.$get=function($log,logger,$rootScope,$http,responseHandler,$q,$injector){");
      result.append("var wsuri =sript_origin.replace(/http(s{0,1}):/,'ws$1:') +'rt-service/websocket';");
      result.append("var sjsuri = sript_origin +'rt-service/';");
      result.append("var ws={};");
      result.append("if (('WebSocket' in window) && (WebSocket!=undefined)){ws = new WebSocket(wsuri);}else{");
      result.append("if (!((typeof SockJS !=='undefined')&&(angular.isDefined(SockJS.constructor)))){");
      result.append("console.warn('websocket not supported, use sockJs client to polyfill...');}");
      result.append("else{ws = new SockJS(sjsuri, undefined, self.options);}};");
      result.append("var rt={ready:false,onReadyState:function(fn){");
      result.append("setTimeout(listen,500);");
      result.append("function listen(){");
      result.append("if(ws.readyState==1){");
      result.append("rt.ready=true;");
      result.append("fn();}else{");
      result.append("setTimeout(listen,500);}}}};");
      result.append("rt.rootScope=$rootScope;");
      result.append("var reqId=0;");
      result.append("var callbacks={};");
      result.append("var caller='';");
      result.append("ws.onopen = function (evt)");
      result.append("{ $log.log('%c>> CONNECTING...','color:blue;font-weight: bold');");
      result.append("var message = {");
      result.append("'reqId':0,");
      result.append("'session': rt.rootScope.sessionUID,");
      result.append("'service': 'ping',");
      result.append("'method': 'ping',");
      result.append("'params': {}");
      result.append("};");
      result.append("rt.send(message);};");
      result.append("ws.onmessage = function (evt)");
      result.append("{");
      result.append("var msg=angular.fromJson(evt.data);");
      result.append("var REQ_ID=parseInt(msg.reqId);");
      result.append("if (angular.isDefined(callbacks[REQ_ID])) {");
      result.append("var callback = callbacks[REQ_ID];");
      result.append("delete callbacks[REQ_ID];");
      result.append("callback.resolve(msg);");
      result.append("}");
      result.append("if (angular.isDefined(msg.ngEvent)) {");
      result.append("if(msg.ngEvent.name=='modelQuery'){");
      result.append("var caller={};");
      result.append("$injector.invoke([msg.ngEvent.data, function(icaller){caller=icaller;}]);");
      result.append("responseHandler.handleResponse(msg,caller,false);");
      result.append("}else{");
      result.append("$rootScope.$broadcast(msg.ngEvent.name,msg.ngEvent.data);");
      result.append("}");
      result.append("if(!$rootScope.$$phase){$rootScope.$digest;$rootScope.$apply();}");
      result.append("}};");
      result.append("rt.sendAsync = function(message){");
      result.append("setTimeout(listen,500);");
      result.append("function listen(){");
      result.append("if(ws.readyState==1){");
      result.append("ws.send(angular.toJson(message));");
      result.append("}else{");
      result.append("setTimeout(listen,500);}}");
      result.append("var deferred = $q.defer();");
      result.append("callbacks[message.reqId] = deferred;");
      result.append("return deferred.promise;");
      result.append("};");
      result.append("rt.send = function(message){");
      result.append("ws.send(angular.toJson(message));");
      result.append("};");
      result.append("rt.call=function(caller,invockation,params){");
      result.append("reqId++;");
      result.append("var message={");
      result.append("'reqId':reqId,");
      result.append("'service': invockation.split(\".\")[0],");
      result.append("'method': invockation.split(\".\")[1],");
      result.append("'params': params");
      result.append("};");
      result.append("return rt.sendAsync(message);");
      result.append("}; rt.onReadyState(function(){$log.log('%c>> REALTIME SESSION READY...','color:green;font-weight: bold');}) ;return rt; };});");

      return result.toString();
   }
}