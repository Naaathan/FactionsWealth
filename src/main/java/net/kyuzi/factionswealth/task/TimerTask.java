package net.kyuzi.factionswealth.task;

import net.kyuzi.factionswealth.FactionsWealth;

public abstract class TimerTask extends Task {

    protected long delay;
    protected long period;

    protected TimerTask(boolean async, long delay, long period) {
        super(async);
        this.delay = delay;
        this.period = period;
    }

    @Override
    public void done() {
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
