import com.rsbuddy.event.events.MessageEvent;
import com.rsbuddy.event.listeners.MessageListener;
import com.rsbuddy.event.listeners.PaintListener;
import com.rsbuddy.script.ActiveScript;
import com.rsbuddy.script.Manifest;
import com.rsbuddy.script.methods.Mouse;
import com.rsbuddy.script.methods.Objects;
import com.rsbuddy.script.methods.Players;
import com.rsbuddy.script.methods.Widgets;
import com.rsbuddy.script.task.Task;
import com.rsbuddy.script.util.Random;
import com.rsbuddy.script.util.Timer;
import com.rsbuddy.script.wrappers.Component;
import com.rsbuddy.script.wrappers.GameObject;
import org.rsbuddy.tabs.Inventory;
import org.rsbuddy.widgets.Bank;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

@Manifest(authors = "programer", name = "Cooker",
        keywords = "Cooking", version = 0.1, description = "Cooks")
public class Cooker extends ActiveScript implements MessageListener, PaintListener, MouseListener {
    private Strategy currentStrategy;
    private List<Strategy> strategyList;
    private Painter painter;
    private static boolean isCooking;
    private final static int COOKED = 15272;
    private final static int UNCOOKED = 15270;
    private String cookingAlert = "You successfully cook a";


    @Override
    public boolean onStart() {
        this.getContainer().submit(new EventWatch());
        painter = new Painter();
        strategyList = new ArrayList<Strategy>();
        strategyList.add(new Banking());
        strategyList.add(new PreparingToCook());
        strategyList.add(new Cooking());
        strategyList.add(new FinishedCooking());
        return true;
    }

    @Override
    public void onFinish() {
        log.info("Runtime: " + Timer.format(painter.getRunTime()));
        log.info("XP gained: " + painter.getXPgained());
        log.info("XP/hr: " + painter.getXPHour());
    }

    public static void resetIsCooking() {
        isCooking = false;
    }

    public static boolean isCooking() {
        return isCooking;
    }

    @Override
    public int loop() {
        for (Strategy strategy : strategyList) {
            Task.sleep(600, 800);
            if (strategy.isValid()) {
                Mouse.setSpeed(Random.nextInt(7, 10));
                currentStrategy = strategy;
                painter.getStatus(currentStrategy.currentStatus());
                currentStrategy.execute();
                if (Widgets.canContinue()) {
                    Widgets.clickContinue();
                }
            }
        }
        return 0;
    }

    public void messageReceived(MessageEvent messageEvent) {
        currentStrategy.messageReceived(messageEvent);
    }

    public void onRepaint(Graphics graphics) {
        painter.onRepaint(graphics);
    }

    public void mouseClicked(MouseEvent e) {
        painter.mouseClicked(e);
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    private static abstract class Strategy implements MessageListener {

        protected abstract boolean isValid();

        protected abstract void execute();

        protected abstract String currentStatus();
    }

    private class Banking extends Strategy {

        public boolean isValid() {
            return Bank.isOpen();
        }

        public void execute() {
            int invCount = Inventory.getCount();
            if (Inventory.contains(COOKED)) {
                Bank.depositAll();
                waitForInventoryChange(invCount);
            }
            if (!Inventory.contains(UNCOOKED)) {
                if (Bank.getCount(UNCOOKED) == 0) {
                    log.warning("Run out of cooking item");
                    getContainer().stop();
                } else {
                    Bank.withdraw(UNCOOKED, 0);
                    waitForInventoryChange(invCount);
                    Bank.close();
                }
            }
        }

        private void waitForInventoryChange(int count) {
            int ms = Random.nextInt(500, 2000);
            for (int i = 0; i < ms; i += 20) {
                if (Inventory.getCount() != count) {
                    break;
                }
                Task.sleep(20);
            }
        }

        protected String currentStatus() {
            return "Banking";
        }

        public void messageReceived(MessageEvent messageEvent) {
        }
    }

    private class PreparingToCook extends Strategy {

        private static final int RANGE_ID = 24283;
        private GameObject rangeObj;
        private Component makeAll;

        public boolean isValid() {
            return (!isCooking && Inventory.contains(UNCOOKED) && !Bank.isOpen());
        }

        public void execute() {
            rangeObj = Objects.getNearest(RANGE_ID);
            makeAll = Widgets.getComponent(905, 14);
            if (!makeAll.isValid()) {
                if (rangeObj != null && !Players.getLocal().isMoving()) {
                    Inventory.useItem(Inventory.getItem(UNCOOKED), rangeObj);
                }
            } else {
                makeAll.interact("Cook All");
            }
        }

        @Override
        protected String currentStatus() {
            return "Preparing to cook";
        }

        public void messageReceived(MessageEvent messageEvent) {
            if (messageEvent.getId() == MessageEvent.MESSAGE_ACTION) {
                isCooking = messageEvent.getMessage().contains(cookingAlert);
            }
        }
    }

    private class Cooking extends Strategy {

        @Override
        protected boolean isValid() {
            return isCooking;
        }

        @Override
        protected void execute() {
            if (Mouse.isPresent()) {
                Mouse.moveOffScreen();
            }
            if (!Inventory.contains(UNCOOKED) || Widgets.getComponent(210, 1).isVisible() || getContainer().isPaused()) {
                isCooking = false;
            }
        }

        @Override
        protected String currentStatus() {
            return "Cooking";
        }

        public void messageReceived(MessageEvent messageEvent) {
            if (messageEvent.getId() == MessageEvent.MESSAGE_ACTION) {
                isCooking = messageEvent.getMessage().contains(cookingAlert);
            }
        }
    }

    private class FinishedCooking extends Strategy {

        @Override
        protected boolean isValid() {
            return !isCooking && !Inventory.contains(UNCOOKED) || Inventory.getCount() == 0;
        }

        @Override
        protected void execute() {
            if (!Bank.isOpen() && !Players.getLocal().isMoving()) {
                Bank.open();
            }
        }

        @Override
        protected String currentStatus() {
            return "Finished cooking, going to bank";
        }

        public void messageReceived(MessageEvent messageEvent) {
            if (messageEvent.getId() == MessageEvent.MESSAGE_ACTION) {
                isCooking = messageEvent.getMessage().contains(cookingAlert);
            }
        }
    }
}