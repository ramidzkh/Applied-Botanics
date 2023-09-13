package appbot.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;

import appbot.ae2.ManaKey;
import appbot.ae2.ManaKeyType;
import vazkii.botania.client.core.handler.MiscellaneousModels;

import appeng.api.client.IAEStackRenderHandler;
import appeng.client.gui.style.Blitter;

public class ManaRenderer implements IAEStackRenderHandler<ManaKey> {

    @Override
    public void drawInGui(Minecraft minecraft, PoseStack poseStack, int x, int y, int zIndex, ManaKey stack) {
        Blitter.sprite(MiscellaneousModels.INSTANCE.manaWater.sprite())
                // Most fluid texture have transparency, but we want an opaque slot
                .blending(false)
                .dest(x, y, 16, 16)
                .blit(poseStack, 100 + zIndex);
    }

    @Override
    public void drawOnBlockFace(PoseStack poseStack, MultiBufferSource buffers, ManaKey what, float scale,
            int combinedLight) {
        var sprite = MiscellaneousModels.INSTANCE.manaWater.sprite();

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
        buffer.vertex(transform, x0, y1, 0)
                .color(-1)
                .uv(sprite.getU0(), sprite.getV1())
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(combinedLight)
                .normal(0, 0, 1)
                .endVertex();
        buffer.vertex(transform, x1, y1, 0)
                .color(-1)
                .uv(sprite.getU1(), sprite.getV1())
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(combinedLight)
                .normal(0, 0, 1)
                .endVertex();
        buffer.vertex(transform, x1, y0, 0)
                .color(-1)
                .uv(sprite.getU1(), sprite.getV0())
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(combinedLight)
                .normal(0, 0, 1)
                .endVertex();
        buffer.vertex(transform, x0, y0, 0)
                .color(-1)
                .uv(sprite.getU0(), sprite.getV0())
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(combinedLight)
                .normal(0, 0, 1)
                .endVertex();
        poseStack.popPose();
    }

    @Override
    public Component getDisplayName(ManaKey stack) {
        return ManaKeyType.MANA;
    }
}
