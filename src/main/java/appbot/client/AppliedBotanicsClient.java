package appbot.client;

import appbot.ae2.ManaKey;
import appbot.ae2.ManaKeyType;

import appeng.api.client.AEStackRendering;

public interface AppliedBotanicsClient {

    static void initialize() {
        AEStackRendering.register(ManaKeyType.TYPE, ManaKey.class, new ManaRenderer());
    }
}
