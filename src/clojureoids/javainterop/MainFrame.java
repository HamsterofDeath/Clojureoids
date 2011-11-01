package clojureoids.javainterop;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Developed with pleasure :)<br>
 * User: HoD<br>
 * Date: 31.10.11<br>
 * Time: 14:14<br>
 */
public class MainFrame {

  public static UIAccess createFrame(final int width, final int height) {
    final BufferedImage[] bufferedImage = {null};
    final JFrame frame = new JFrame("- Clojureoids renderer -");
    frame.setPreferredSize(new Dimension(width, height));
    final JPanel imageContainer = new JPanel() {
      @Override
      public void paint(final Graphics g) {
        g.drawImage(bufferedImage[0], 0, 0, null);
      }
    };
    imageContainer.setBackground(Color.DARK_GRAY);
    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(imageContainer, BorderLayout.CENTER);
    frame.pack();
    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent e) {
        System.exit(12345);
      }
    });
    frame.setVisible(true);
    return new UIAccess() {

      private Timer runningTimer;

      private BufferedImage provideCleanRenderTarget() {
        //if we were living in 1995, we might try to save memory by re-using images created earlier
        //but in 2011, this won't even show up in a profiler...
        final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = (Graphics2D) img.getGraphics();
        graphics.setColor(Color.black);
        graphics.fillRect(0, 0, width, height);
        return img;
      }

      public void initAdvanceCallback(final AdvanceCallback callback) {
        if (runningTimer != null) {throw new AssertionError();}
        runningTimer = new Timer(true);
        runningTimer.scheduleAtFixedRate(new TimerTask() {
          @Override
          public void run() {
            final BufferedImage image = provideCleanRenderTarget();
            callback.onTick(image);
            bufferedImage[0] = image;
            frame.repaint();
          }
        }, 0, 1000 / 60);
      }
    };
  }
}
