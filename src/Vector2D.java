import java.awt.geom.Point2D;
import java.util.Random;

public class Vector2D extends Point2D.Double {
    public static final Vector2D i = new Vector2D(1.0, 0.0);
    public static final Vector2D j = new Vector2D(0.0, 1.0);
    private static Random random = new Random();

    public Vector2D() {
        super();
    }

    public Vector2D(double x, double y) {
        super(x, y);
    }

    public Vector2D(Vector2D other) {
        super(other.x, other.y);
    }

    public Vector2D add(Vector2D other) {
        return new Vector2D(this.x + other.x, this.y + other.y);
    }

    public Vector2D subtract(Vector2D other) {
        return new Vector2D(this.x - other.x, this.y - other.y);
    }

    public Vector2D multiply(double scalar) {
        return new Vector2D(x * scalar, y * scalar);
    }

    public Vector2D divide(double scalar) {
        if (scalar == 0.0) {
            return this;
        }
        return new Vector2D(x / scalar, y / scalar);
    }

    public Vector2D negate() {
        return new Vector2D(-x, -y);
    }

    public Vector2D unitize() {
        return divide(magnitude());
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public double dot(Vector2D other) {
        return x * other.x + y * other.y;
    }

    public double angle() {
        return Math.atan2(y, x);
    }

    public Vector2D rotate(double angle) {
        return new Vector2D(x * Math.cos(angle) - y * Math.sin(angle), x * Math.sin(angle) + y * Math.cos(angle));
    }

    public Vector2D project(Vector2D other) {
        return this.multiply(this.dot(other) / this.dot(this));
    }

    public double component(Vector2D other) {
        return this.dot(other) / this.magnitude();
    }

    public Vector2D interpolate(Vector2D other, double t) {
        return this.add(other.subtract(this).multiply(t));
    }

    public static Vector2D randomInSquare(double lowerLimit, double upperLimit) {
        return new Vector2D(lowerLimit + random.nextDouble() * (upperLimit - lowerLimit), lowerLimit + random.nextDouble() * (upperLimit - lowerLimit));
    }

    public static Vector2D randomInCircle(double radius) {
        double angle = 2.0 * Math.PI * random.nextDouble();
        double distance = radius * Math.sqrt(random.nextDouble());
        return new Vector2D(distance * Math.cos(angle), distance * Math.sin(angle));
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}