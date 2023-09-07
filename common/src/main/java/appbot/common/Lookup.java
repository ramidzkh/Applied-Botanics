package appbot.common;

import org.jetbrains.annotations.Nullable;

public interface Lookup<A, C> {

    @Nullable
    A find(C context);
}
