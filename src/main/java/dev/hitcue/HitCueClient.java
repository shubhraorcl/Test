package dev.hitcue;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

/**
 * Purely cosmetic crosshair overlay. It reads the same two values the vanilla
 * attack indicator uses (attack cooldown and the entity under the crosshair)
 * and never sends packets or acts on the player's behalf.
 */
public class HitCueClient implements ClientModInitializer {
    private static final int GAP = 5;      // distance from crosshair centre to brackets
    private static final int ARM = 3;      // bracket arm length
    private static final int BAR_WIDTH = 16;

    private static final int READY_ON_TARGET = 0xFF55FF55;
    private static final int READY = 0x80FFFFFF;
    private static final int CHARGING = 0xA0A0A0A0;
    private static final int BAR_BG = 0x80000000;

    @Override
    public void onInitializeClient() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CROSSHAIR,
                Identifier.of("hitcue", "crosshair_cue"),
                HitCueClient::render);
    }

    private static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || player.isSpectator() || client.options.hudHidden
                || !client.options.getPerspective().isFirstPerson()) {
            return;
        }

        float charge = player.getAttackCooldownProgress(0.0F);
        boolean ready = charge >= 1.0F;
        boolean onTarget = client.targetedEntity instanceof LivingEntity target && target.isAlive();

        int color = ready ? (onTarget ? READY_ON_TARGET : READY) : CHARGING;
        int cx = ctx.getScaledWindowWidth() / 2;
        int cy = ctx.getScaledWindowHeight() / 2;
        drawBrackets(ctx, cx, cy, color);

        if (!ready) {
            int x = cx - BAR_WIDTH / 2;
            int y = cy + GAP + 4;
            ctx.fill(x, y, x + BAR_WIDTH, y + 2, BAR_BG);
            ctx.fill(x, y, x + Math.round(BAR_WIDTH * charge), y + 2, CHARGING);
        }
    }

    private static void drawBrackets(DrawContext ctx, int cx, int cy, int color) {
        int l = cx - GAP - 1, r = cx + GAP + 1;
        int t = cy - GAP - 1, b = cy + GAP + 1;
        // top-left
        ctx.fill(l, t, l + ARM, t + 1, color);
        ctx.fill(l, t, l + 1, t + ARM, color);
        // top-right
        ctx.fill(r - ARM, t, r, t + 1, color);
        ctx.fill(r - 1, t, r, t + ARM, color);
        // bottom-left
        ctx.fill(l, b - 1, l + ARM, b, color);
        ctx.fill(l, b - ARM, l + 1, b, color);
        // bottom-right
        ctx.fill(r - ARM, b - 1, r, b, color);
        ctx.fill(r - 1, b - ARM, r, b, color);
    }
}
