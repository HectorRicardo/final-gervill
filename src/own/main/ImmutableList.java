package own.main;

import java.util.*;

public class ImmutableList<T> implements Iterable<T> {

    public final int length;

    public interface ElementGenerator<T> {
        T generate(int index);
    }

    private final List<T> myList;

    private ImmutableList(List<T> myList) {
        this.myList = Collections.unmodifiableList(myList);
        length = myList.size();
    }

    public static <T> ImmutableList<T> create() {
        return new ImmutableList<>(new ArrayList<>());
    }

    public static <T> ImmutableList<T> create(List<T> myList) {
        return new ImmutableList<>(new ArrayList<>(myList));
    }

    public static <T> ImmutableList<T> create(List<T> myList, Comparator<T> comparator) {
        List<T> copyList = new ArrayList<>(myList);
        copyList.sort(comparator);
        return new ImmutableList<>(copyList);
    }

    public static <T> ImmutableList<T> create(T elem1) {
        return new ImmutableList<>(Collections.singletonList(elem1));
    }

    public static <T> ImmutableList<T> create(T elem1, T elem2) {
        return new ImmutableList<>(Arrays.asList(elem1, elem2));
    }

    public static <T> ImmutableList<T> create(T[] arr) {
        return ImmutableList.create(Arrays.asList(arr));
    }

    public static <T> ImmutableList<T> create(int count, ElementGenerator<T> generator) {
        List<T> myList = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            myList.add(generator.generate(i));
        }
        return new ImmutableList<>(myList);
    }

    public static <T> ImmutableList<T> create(ImmutableList<T> base, int extra, ElementGenerator<T> generator) {
        assert extra >= 0;
        List<T> myList = base.toList();
        for (int i = 0; i < extra; i++) {
            myList.add(generator.generate(i + base.length));
        }
        return new ImmutableList<>(myList);
    }

    public static ImmutableList<Byte> create(byte[] arr) {
        List<Byte> myList = new ArrayList<>(arr.length);
        for (byte theByte : arr) {
            myList.add(theByte);
        }
        return new ImmutableList<>(myList);
    }

    public ImmutableList<T> append(T elem) {
        List<T> copy = new ArrayList<>(myList);
        copy.add(elem);
        return new ImmutableList<>(copy);
    }

    public ImmutableList<T> set(int index, T elem) {
        List<T> copy = new ArrayList<>(myList);
        copy.set(index, elem);
        return new ImmutableList<>(copy);
    }

    public T get(int index) {
        return myList.get(index);
    }

    public int size() {
        return length;
    }

    public List<T> toList() {
        return new ArrayList<>(myList);
    }

    public static byte[] toArray(ImmutableList<Byte> bytes) {
        byte[] arr = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            arr[i] = bytes.get(i);
        }
        return arr;
    }

    public static void copy(ImmutableList<Byte> src, int srcPos, byte[] dest, int destPos, int length) {
        for (int i = 0; i < length; i++) {
            dest[destPos + i] = src.get(srcPos + i);
        }
    }

    public T[] toArray(T[] arr) {
        return myList.toArray(arr);
    }

    @Override
    public Iterator<T> iterator() {
        return myList.iterator();
    }
}
