package own.main;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ImmutableMap<K, V> {
    public final int size;

    private final Map<K, V> myMap;

    private ImmutableMap(Map<K, V> myMap) {
        this.myMap = Collections.unmodifiableMap(myMap);
        size = myMap.size();
    }

    public static <K, V> ImmutableMap<K, V> create() {
        return new ImmutableMap<>(new HashMap<>());
    }

    public static <K, V> ImmutableMap<K, V> create(Map<K, V> myMap) {
        return new ImmutableMap<>(new HashMap<>(myMap));
    }

    public V get(K key) {
        return myMap.get(key);
    }
}
