import com.rsbuddy.event.listeners.PaintListener;
import com.rsbuddy.script.methods.Mouse;
import com.rsbuddy.script.methods.Skills;
import com.rsbuddy.script.methods.Widgets;
import com.rsbuddy.script.util.Timer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

class Painter implements PaintListener, MouseListener {

    private final Font FONT = new Font("Arial", Font.BOLD, 12);
    private int startXP = 0;
    private int startLvl = 0;


    public int getXPHour() {
        return XPHour;
    }

    public int getXPgained() {
        return XPgained;
    }

    public long getRunTime() {
        return runTime;
    }

    private int XPgained = 0;
    private int XPHour = 0;
    private long startTime = 0;
    private long runTime;
    private Rectangle hideFrame;
    private boolean hidePaint;
    private String status;
    private Image mouseImage;

    Painter() {
        try {
            mouseImage = ImageIO.read(Cooker.class.getResourceAsStream("cooking.png"));
        } catch (IOException ignored) {
        }
        startLvl = Skills.getCurrentLevel(Skills.COOKING);
        startTime = System.currentTimeMillis();
        startXP = Skills.getCurrentExp(Skills.COOKING);
    }

    public void getStatus(String status) {
        this.status = status;
    }

    public void onRepaint(Graphics g) {
        Color gold = new Color(230, 180, 18, 245);
        Color darkAdamant = new Color(24, 37, 24, 255);
        int x = Widgets.getComponent(137, 0).getAbsLocation().x;
        int y = Widgets.getComponent(137, 0).getAbsLocation().y;
        int width = Widgets.getComponent(137, 0).getWidth();
        int height = Widgets.getComponent(137, 0).getHeight();
        hideFrame = new Rectangle(x, y, width, height);
        XPgained = Skills.getCurrentExp(Skills.COOKING) - startXP;
        int currLevel = Skills.getCurrentLevel(Skills.COOKING);
        int percentToLevel = Skills.getPercentToNextLevel(Skills.COOKING);
        runTime = System.currentTimeMillis() - startTime;
        int XPTNL = Skills.getExpToNextLevel(Skills.COOKING);
        XPHour = (int) ((XPgained) * 3600000.0 / runTime);
        long TTL = (long) ((double) XPTNL / (double) XPHour * 3600000);
        if (!hidePaint) {
            g.setColor(darkAdamant);
            g.drawRoundRect(x, y, width, height, 10, 10);
            g.fillRoundRect(x, y, width, height, 10, 10);
            g.setFont(FONT);
            g.setColor(gold);
            g.drawString("Runtime : " + Timer.format(runTime) + "|| Status: " + status, x + 10, y + 20);
            g.drawString("Current Cooking Level : " + currLevel + " (+" + (currLevel - startLvl) + ")", x + 10, y + 60);
            if (XPgained > 0) {
                g.drawString("TTL : " + Timer.format(TTL), x + 10, y + 40);
            } else {
                g.drawString("TTL : Waiting to cook..", x + 10, y + 40);
            }
            if (XPHour > 1000) {
                if(XPgained > 1000)
                    g.drawString("XP Gained : " + (XPgained / 1000) + "." + ((XPgained % 1000) / 100) + "k || " + (XPHour / 1000) + "." + ((XPHour % 1000) / 100) + "k/Hr", x + 10, y + 80);
                else
                    g.drawString("XP Gained : " + XPgained + " || " + (XPHour / 1000) + "." + ((XPHour % 1000) / 100) + "k/Hr", x + 10, y + 80);
            } else {
                g.drawString("XP Gained : " + XPgained + " || " + XPHour + "/Hr", x + 10, y + 80);
            }
            g.setColor(Color.BLACK);
            g.fillRoundRect(x + 10, y + 110, 100, FONT.getSize(), 2, 2);
            g.setColor(new Color(99, 116, 99));
            g.fillRoundRect(x + 10, y + 110, percentToLevel, FONT.getSize(), 2, 2);
            g.setColor(gold);
            g.drawRoundRect(x + 10, y + 110, 100, FONT.getSize(), 2, 2);
            g.drawString("XP To level : %" + percentToLevel + " (" + Skills.getExpToNextLevel(Skills.COOKING) + ")", x + 10, y + 100);
        }
        if (Mouse.isPresent()) {
            g.drawImage(mouseImage, Mouse.getLocation().x, Mouse.getLocation().y, null);
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (hideFrame.contains(e.getPoint())) {
            hidePaint = !hidePaint;
        }
    }

    public void mousePressed(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }
}
