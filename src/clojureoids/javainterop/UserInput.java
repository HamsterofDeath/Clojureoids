package clojureoids.javainterop;

/**
 * Developed with pleasure :)<br>
 * User: HoD<br>
 * Date: 02.11.11<br>
 * Time: 08:38<br>
 */
public class UserInput implements Cloneable {
  private boolean left;
  private boolean right;
  private boolean fire;
  private boolean accelerate;
  private boolean reverse;
  private boolean teleport;

  public boolean isLeft() {
    return left;
  }

  public void setLeft(final boolean left) {
    this.left = left;
  }

  public void setRight(final boolean right) {
    this.right = right;
  }

  public void setFire(final boolean fire) {
    this.fire = fire;
  }

  public void setAccelerate(final boolean accelerate) {
    this.accelerate = accelerate;
  }

  public void setReverse(final boolean reverse) {
    this.reverse = reverse;
  }

  public void setTeleport(final boolean teleport) {
    this.teleport = teleport;
  }

  public boolean isRight() {
    return right;
  }

  public boolean isFire() {
    return fire;
  }

  public boolean isAccelerate() {
    return accelerate;
  }

  public boolean isReverse() {
    return reverse;
  }

  public boolean isTeleport() {
    return teleport;
  }

  public boolean isLeftOrRight() {
    return left || right;
  }

  public boolean isMoving() {
    return accelerate || reverse;
  }

  @Override
  protected final UserInput clone() {
    try {
      return (UserInput) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}
