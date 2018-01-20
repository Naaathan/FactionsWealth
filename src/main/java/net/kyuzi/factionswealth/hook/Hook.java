package net.kyuzi.factionswealth.hook;

import net.kyuzi.factionswealth.exception.HookFailureException;

public class Hook<T> {

    private final T instance;

    protected Hook(T instance) throws HookFailureException {
        if (instance == null) {
            throw new HookFailureException("Hook instance cannot be null!");
        }

        this.instance = instance;
    }

    public T getHook() {
        return instance;
    }

}
