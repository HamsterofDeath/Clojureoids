package clojureoids.javainterop;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Developed with pleasure :)<br>
 * User: HoD<br>
 * Date: 06.11.11<br>
 * Time: 09:38<br>
 */
public class TransformUtils {
  public static Point2D.Double transform(final AffineTransform transform, final double x, final double y) {
    final Point2D.Double ret = new Point2D.Double();
    return (Point2D.Double) transform.transform(new Point2D.Double(x, y), ret);
  }

  public static Rectangle2D newRect(final double x, final double y, final double w, final double h) {
    return new Rectangle2D.Double(x, y, w, h);
  }
}
