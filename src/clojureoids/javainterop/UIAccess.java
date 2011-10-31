package clojureoids.javainterop;

import java.awt.image.BufferedImage;

/**
 * Developed with pleasure :)<br>
 * User: HoD<br>
 * Date: 31.10.11<br>
 * Time: 14:20<br>
 */
public interface UIAccess {
  void afterRenderingFinished();

  BufferedImage provideCleanRenderTarget();
}
