package own.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Immutable2DList<T> implements Iterable<ImmutableList<T>> {

    private final List<ImmutableList<T>> myList;

    private Immutable2DList(List<ImmutableList<T>> myList) {
        this.myList = Collections.unmodifiableList(myList);
    }

    public static <T> Immutable2DList<T> create(T[][] arr) {
        List<ImmutableList<T>> immutableLists = new ArrayList<>();
        for (T[] ts : arr) {
            immutableLists.add(ImmutableList.create(ts));
        }
        return new Immutable2DList<>(immutableLists);
    }

    public ImmutableList<T> get(int i) {
        return myList.get(i);
    }

    public T get(int i, int j) {
        return myList.get(i).get(j);
    }

    @Override
    public Iterator<ImmutableList<T>> iterator() {
        return myList.iterator();
    }
}
