package angular4J.sockjs;

/**
 * Enum class of connecting states.
 */
public enum ReadyState {

   CONNECTING(0),
   OPEN(1),
   CLOSING(2),
   CLOSED(3);

   private final int code;

   ReadyState(int code) {
      this.code = code;
   }

   public int code() {
      return code;
   }
}
