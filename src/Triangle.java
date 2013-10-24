import quickhull3d.Point3d;
import quickhull3d.Vector3d;

// TODO move vector operations to a vector class

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

    /*
     * Translation of code from:
     * Author: Christer Ericson
     * Book: Real-Time Collision Detection
     * ISBN: 1-55860-732-3
     * Pages: 141-142
     */
    public Point3d nearestPoint(Point3d p) {
        // Check if P in vertex region outside A
        Vector3d ab = minus(b, a);
        Vector3d ac = minus(c, a);
        Vector3d ap = minus(p, a);
        double d1 = dot(ab, ap);
        double d2 = dot(ac, ap);
        if (d1 <= 0.0f && d2 <= 0.0f) {
            return new Point3d(a); // barycentric coordinates (1,0,0)
        }
        // Check if P in vertex region outside B
        Vector3d bp = minus(p, b);
        double d3 = dot(ab, bp);
        double d4 = dot(ac, bp);
        if (d3 >= 0.0f && d4 <= d3) {
            return new Point3d(b); // barycentric coordinates (0,1,0)
        }
        // Check if P in edge region of AB, if so return projection of P onto AB
        double vc = d1 * d4 - d3 * d2;
        if (vc <= 0.0f && d1 >= 0.0f && d3 <= 0.0f) {
            double v = d1 / (d1 - d3);
            return new Point3d(plus(a, times(ab, v))); // barycentric coordinates (1-v,v,0)
        }
        // Check if P in vertex region outside C
        Vector3d cp = minus(p, c);
        double d5 = dot(ab, cp);
        double d6 = dot(ac, cp);
        if (d6 >= 0.0f && d5 <= d6) {
            return new Point3d(c); // barycentric coordinates (0,0,1)142
        }
        // Check if P in edge region of AC, if so return projection of P onto AC
        double vb = d5 * d2 - d1 * d6;
        if (vb <= 0.0f && d2 >= 0.0f && d6 <= 0.0f) {
            double w = d2 / (d2 - d6);
            return new Point3d(plus(a, times(ac, w))); // barycentric coordinates (1-w,0,w)
        }
        // Check if P in edge region of BC, if so return projection of P onto BC
        double va = d3 * d6 - d5 * d4;
        if (va <= 0.0f && (d4 - d3) >= 0.0f && (d5 - d6) >= 0.0f) {
            double w = (d4 - d3) / ((d4 - d3) + (d5 - d6));
            return new Point3d(plus(b, times(minus(c, b), w))); // barycentric coordinates (0,1-w,w)
        }
        // P inside face region. Compute Q through its barycentric coordinates (u,v,w)
        double denom = 1.0f / (va + vb + vc);
        double v = vb * denom;
        double w = vc * denom;
        return new Point3d(plus(a, plus(times(ab, v), times(ac, w)))); // = u*a + v*b + w*c, u = va * denom = 1.0f - v - w
    }

    private Vector3d times(Vector3d a, double d) {
        return new Vector3d(a.x * d, a.y * d, a.z * d);
    }

    private double dot(Vector3d a, Vector3d b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    private Vector3d minus(Vector3d a, Vector3d b) {
        return new Point3d(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    private Vector3d plus(Vector3d a, Vector3d b) {
        return new Point3d(a.x + b.x, a.y + b.y, a.z + b.z);
    }
}
