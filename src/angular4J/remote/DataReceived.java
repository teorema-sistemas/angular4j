package angular4J.remote;

import com.google.gson.JsonObject;

/**
 * a superType for all DataReceived from the client events when used with @Observes that will
 * include HalfDuplexDataReceivedEvent and RealTimeDataReceivedEvent
 */

public interface DataReceived {

   JsonObject getData();
}
