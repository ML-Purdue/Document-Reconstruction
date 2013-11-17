import java.util.ArrayList;

public class CircularArrayList<E> extends ArrayList<E> {
    public E get(int i) {
        return super.get(i % size());
    }
}
