import com.rsbuddy.script.task.LoopTask;

class EventWatch extends LoopTask {
    @Override
    public int loop() {
        if (this.getContainer().isPaused() && Cooker.isCooking())
            Cooker.resetIsCooking();
        return 0;
    }
}