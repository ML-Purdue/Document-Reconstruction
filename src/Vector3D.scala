import java.util.Random;
import java.awt.Color

class Vector3D(var x: Double, var y: Double, var z: Double) {
  val random = new Random();

  def this() = this(0, 0, 0)

  def this(other: Vector3D) = this(other.x, other.y, other.z);

  def +(that: Vector3D) = new Vector3D(this.x + that.x, this.y + that.y, this.z + that.z)

  def -(that: Vector3D) = new Vector3D(this.x - that.x, this.y - that.y, this.z - that.z)

  def *(that: Double) = new Vector3D(this.x * that, this.y * that, this.z * that)

  def /(that: Double) = new Vector3D(this.x / that, this.y / that, this.z / that)

  def *(that: Vector3D) = this.x * that.x + this.y * that.y + this.z * that.z

  def unary_-() = new Vector3D(-x, -y, -z)

  def unitize() = this / magnitude

  def magnitude() = Math.sqrt(x * x + y * y)

  def toColor(): Color = new Color((x * 255).toInt, (y * 255).toInt, (z * 255).toInt)

  override def toString() = "(%f, %f, %f)" format (x, y, z)
}