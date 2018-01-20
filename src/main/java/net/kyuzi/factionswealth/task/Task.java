package net.kyuzi.factionswealth.task;

import net.kyuzi.factionswealth.FactionsWealth;

import org.bukkit.scheduler.BukkitRunnable;

public abstract class Task extends BukkitRunnable {

    protected boolean async;
    protected boolean complete;
    protected boolean running;

    protected Task(boolean async) {
        this.async = async;
        this.complete = false;
        this.running = false;
    }

    public boolean isAsync() {
        return async;
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean isRunning() {
        return running;
    }

    public abstract void done();

    public synchronized void start() {
        if (running) {
            return;
        }

        running = true;

        if (async) {
            runTaskAsynchronously(FactionsWealth.getInstance());
        } else {
            runTask(FactionsWealth.getInstance());
        }
    }

    public synchronized void stop() {
        if (!running) {
            return;
        }

        cancel();

        running = false;
    }

}
