package angular4J.util;

import java.io.Serializable;

public class Pair<K, V> implements Serializable {

   private K key = null;
   private V value = null;

   public Pair() {}

   public Pair(K key, V value) {
      this.key = key;
      this.value = value;
   }

   public K getKey() {
      return this.key;
   }

   public void setKey(K key) {
      this.key = key;
   }

   public V getValue() {
      return this.value;
   }

   public void setValue(V value) {
      this.value = value;
   }

   public boolean isEmpty() {
      return (this.key == null && this.value == null);
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      }

      if (!(o instanceof Pair)) {
         return false;
      }

      try {
         K key = (K) ((Pair) o).getKey();

         if (key == null && this.key != null) {
            return false;
         }
         if (!key.equals(this.key)) {
            return false;
         }

         V value = (V) ((Pair) o).getValue();
         if (value == null && this.value != null) {
            return false;
         }
         if (!value.equals(this.value)) {
            return false;
         }
      }
      catch (ClassCastException unused) {
         return false;
      }
      catch (NullPointerException unused) {
         return false;
      }

      return true;
   }

   public void clear() {
      this.key = null;
      this.value = null;
   }
}