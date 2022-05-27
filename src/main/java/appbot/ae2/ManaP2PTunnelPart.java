package appbot.ae2;

import java.util.List;

import appbot.AppliedBotanics;
import appbot.storage.Apis;
import appbot.storage.ManaVariant;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.parts.p2p.P2PModels;
import appeng.parts.p2p.StorageP2PTunnelPart;

@SuppressWarnings("UnstableApiUsage")
public class ManaP2PTunnelPart extends StorageP2PTunnelPart<ManaP2PTunnelPart, ManaVariant> {

    private static final P2PModels MODELS = new P2PModels(AppliedBotanics.id("part/mana_p2p_tunnel"));

    public ManaP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem, Apis.BLOCK, ManaKeyType.TYPE);
    }

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }
}
