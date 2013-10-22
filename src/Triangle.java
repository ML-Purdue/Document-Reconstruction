import quickhull3d.Point3d;
import quickhull3d.Vector3d;

public class Triangle {
    public Vector3d a, b, c;

    public static enum Side {
        FRONT, BACK;
    }

    public Triangle(Vector3d a, Vector3d b, Vector3d c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public Side side(Point3d point) {
        Vector3d edge1 = new Vector3d();
        Vector3d edge2 = new Vector3d();
        Vector3d normal = new Vector3d();
        Vector3d r = new Vector3d();
        edge1.sub(b, a);
        edge2.sub(c, b);
        normal.cross(edge1, edge2);
        r.sub(point, a);
        if (r.dot(normal) > 0) {
            return Side.FRONT;
        } else {
            return Side.BACK;
        }
    }
}
