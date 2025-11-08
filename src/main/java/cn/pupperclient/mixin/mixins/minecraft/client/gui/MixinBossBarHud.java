package cn.pupperclient.mixin.mixins.minecraft.client.gui;

import java.util.Map;
import java.util.UUID;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Maps;
import cn.pupperclient.management.mod.api.Position;
import cn.pupperclient.management.mod.impl.hud.BossBarMod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;

@Mixin(BossBarHud.class)
public abstract class MixinBossBarHud {

	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	@Final
	final Map<UUID, ClientBossBar> bossBars = Maps.<UUID, ClientBossBar>newLinkedHashMap();

	@Shadow
	public abstract void renderBossBar(DrawContext context, int x, int y, BossBar bossBar);

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void render(DrawContext context, CallbackInfo ci) {

		BossBarMod mod = BossBarMod.getInstance();

		if (mod.isEnabled() && !mod.isVanillaPosition()) {
			Position position = mod.getPosition();
			onCustomRender(context, (int) position.getX(), (int) position.getY());
			position.setScale(1.0F);
			position.setSize(182, 14);
			ci.cancel();
		} else if (!mod.isEnabled()) {
			ci.cancel();
		}
	}

	public void onCustomRender(DrawContext context, int x, int y) {

		if (!this.bossBars.isEmpty()) {

			Profiler profiler = Profilers.get();
			profiler.push("bossHealth");
			int j = y;

			for (ClientBossBar clientBossBar : this.bossBars.values()) {

				Text text = clientBossBar.getName();
				int m = this.client.textRenderer.getWidth(text);
				int n = x - m / 2;
				context.drawTextWithShadow(this.client.textRenderer, text, n, j, 16777215);

				int k = x - 91;
				this.renderBossBar(context, k, j + 9, clientBossBar);

				j += 10 + 9;
				if (j >= context.getScaledWindowHeight() / 3) {
					break;
				}
			}

			profiler.pop();
		}
	}
}
