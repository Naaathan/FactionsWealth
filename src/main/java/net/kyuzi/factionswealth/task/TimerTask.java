package net.kyuzi.factionswealth.task;

import net.kyuzi.factionswealth.FactionsWealth;
import net.kyuzi.factionswealth.task.Task;

public abstract class TimerTask extends Task {

    private long delay;
    private long period;

    protected TimerTask(boolean async, long delay, long period) {
        super(async);
        this.delay = delay;
        this.period = period;
    }

    @Override
    public final void done() {
    }

    @Override
    public synchronized void start() {
        if (running) {
            return;
        }

        running = true;

        if (async) {
            runTaskTimerAsynchronously(FactionsWealth.getInstance(), delay, period);
        } else {
            runTaskTimer(FactionsWealth.getInstance(), delay, period);
        }
    }

}
