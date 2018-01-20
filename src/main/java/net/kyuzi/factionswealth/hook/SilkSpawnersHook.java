package net.kyuzi.factionswealth.hook;

import de.dustplanet.util.SilkUtil;

import net.kyuzi.factionswealth.exception.HookFailureException;

public class SilkSpawnersHook extends Hook<SilkUtil> {

    public SilkSpawnersHook() throws HookFailureException {
        super(SilkUtil.hookIntoSilkSpanwers());
    }

}
