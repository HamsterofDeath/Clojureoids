package clojureoids.javainterop;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * Developed with pleasure :)<br>
 * User: HoD<br>
 * Date: 31.10.11<br>
 * Time: 14:14<br>
 */
public class MainFrame {
  public static UIAccess createFrame(final int width, final int height) {
    final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    final JFrame frame = new JFrame("- Clojureoids renderer -");
    frame.setPreferredSize(new Dimension(width, height));
    final JPanel imageContainer = new JPanel() {
      @Override
      public void paint(final Graphics g) {
        g.drawImage(bufferedImage, 0, 0, null);
      }
    };
    imageContainer.setBackground(Color.DARK_GRAY);
    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(imageContainer, BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
    return new UIAccess() {


      public void afterRenderingFinished() {
        frame.repaint();
      }

      public BufferedImage provideCleanRenderTarget() {
        final Graphics graphics = bufferedImage.getGraphics();
        graphics.setColor(Color.black);
        graphics.fillRect(0, 0, width, height);
        return bufferedImage;
      }
    };
  }
}
