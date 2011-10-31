package clojureoids.javainterop;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Image;

/**
 * Developed with pleasure :)<br>
 * User: HoD<br>
 * Date: 31.10.11<br>
 * Time: 14:14<br>
 */
public class MainFrame {
  public static UIAccess createFrame(final int width, final int height) {
    final JFrame frame = new JFrame("- Clojureoids renderer -");
    frame.setSize(width, height);
    final JPanel imageContainer = new JPanel();
    frame.getContentPane().add(imageContainer);
    frame.setVisible(true);
    return new UIAccess() {
      public void update(final Image newImage) {
        imageContainer.getGraphics().drawImage(newImage, 0, 0, null);
        frame.repaint();
      }
    };
  }
}
