package codr7.jalang;

import java.util.Arrays;

public final class FixedStack<T> {
    public final T[] items;
    private final int maxSize;
    private int size = 0;

    public FixedStack(final T[] in, final int size) {
        maxSize = size;
        items = Arrays.copyOf(in, maxSize);
    }

    public T peek() {
        return items[size - 1];
    }

    public T pop() {
        size--;
        return items[size];
    }

    public void push(final T it) {
        items[size] = it;
        size++;
    }

    public int size() {
        return size;
    }
}
