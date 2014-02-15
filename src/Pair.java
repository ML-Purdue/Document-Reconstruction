public class Pair<L, R> {
    L l;
    R r;

    public Pair(L l, R r) {
        this.l = l;
        this.r = r;
    }

    public String toString() {
        return "(" + l + ", " + r + ")";
    }
}
