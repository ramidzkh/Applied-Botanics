package appbot.client;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import appbot.ae2.ManaKey;
import appbot.ae2.ManaKeyType;

import appeng.api.client.AEKeyRenderHandler;
import appeng.api.client.AEKeyRendering;
import appeng.client.gui.style.Blitter;

public class ManaRenderer implements AEKeyRenderHandler<ManaKey> {

    public static void initialize(IEventBus bus) {
        bus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            AEKeyRendering.register(ManaKeyType.TYPE, ManaKey.class, new ManaRenderer());
        }));
    }

    private TextureAtlasSprite waterSprite;

    // The constructor is called before modelManager is initialized
    private void lazyInitSprite() {
        if (this.waterSprite == null)
            this.waterSprite = Objects.requireNonNull(
                    Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                            .apply(botaniaRL("block/mana_water")));
    }

    @Override
    public void drawInGui(Minecraft minecraft, GuiGraphics guiGraphics, int x, int y, ManaKey stack) {
        lazyInitSprite();
        Blitter.sprite(waterSprite)
                // Most fluid texture have transparency, but we want an opaque slot
                .blending(false)
                .dest(x, y, 16, 16)
                .blit(guiGraphics);
    }

    @Override
    public void drawOnBlockFace(PoseStack poseStack, MultiBufferSource buffers, ManaKey what, float scale,
            int combinedLight, Level level) {
        lazyInitSprite();
        poseStack.pushPose();
        // Push it out of the block face a bit to avoid z-fighting
        poseStack.translate(0, 0, 0.01f);

        var buffer = buffers.getBuffer(RenderType.solid());

        // In comparison to items, make it _slightly_ smaller because item icons
        // usually don't extend to the full size.
        scale -= 0.05f;

        // y is flipped here
        var x0 = -scale / 2;
        var y0 = scale / 2;
        var x1 = scale / 2;
        var y1 = -scale / 2;

        var transform = poseStack.last().pose();
        buffer.addVertex(transform, x0, y1, 0)
                .setColor(-1)
                .setUv(waterSprite.getU0(), waterSprite.getV1())
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(combinedLight)
                .setNormal(0, 0, 1);
        buffer.addVertex(transform, x1, y1, 0)
                .setColor(-1)
                .setUv(waterSprite.getU1(), waterSprite.getV1())
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(combinedLight)
                .setNormal(0, 0, 1);
        buffer.addVertex(transform, x1, y0, 0)
                .setColor(-1)
                .setUv(waterSprite.getU1(), waterSprite.getV0())
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(combinedLight)
                .setNormal(0, 0, 1);
        buffer.addVertex(transform, x0, y0, 0)
                .setColor(-1)
                .setUv(waterSprite.getU0(), waterSprite.getV0())
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(combinedLight)
                .setNormal(0, 0, 1);
        poseStack.popPose();
    }

    @Override
    public Component getDisplayName(ManaKey stack) {
        return ManaKeyType.MANA;
    }

    @Override
    public List<Component> getTooltip(ManaKey stack) {
        return List.of(getDisplayName(stack));
    }
}
