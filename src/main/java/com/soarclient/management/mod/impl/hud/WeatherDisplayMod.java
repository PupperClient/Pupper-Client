package com.soarclient.management.mod.impl.hud;

import com.soarclient.event.EventBus;
import com.soarclient.event.client.RenderSkiaEvent;
import com.soarclient.management.mod.api.hud.SimpleHUDMod;
import com.soarclient.skia.font.Icon;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class WeatherDisplayMod extends SimpleHUDMod {

	public WeatherDisplayMod() {
		super("mod.weatherdisplay.name", "mod.weatherdisplay.description", Icon.SUNNY);
	}

	public final EventBus.EventListener<RenderSkiaEvent> onRenderSkia = event -> {
		this.draw();
	};

	@Override
	public String getText() {

		String prefix = "Weather: ";
		ClientWorld world = mc.world;
		ClientPlayerEntity player = mc.player;
		BlockPos playerPos = player.getBlockPos();
		RegistryEntry<Biome> biomeEntry = world.getBiome(playerPos);

		if (world.isThundering()) {
			return prefix + "Thundering";
		}
		
		if (world.isRaining()) {
			if (biomeEntry.value().getPrecipitation(playerPos, world.getSeaLevel()).equals(Biome.Precipitation.SNOW)) {
				return prefix + "Snowing";
			} else {
				return prefix + "Raining";
			}
		}

		return prefix + "Cleaning";
	}

	@Override
	public String getIcon() {

		ClientWorld world = mc.world;
		ClientPlayerEntity player = mc.player;
		BlockPos playerPos = player.getBlockPos();
		RegistryEntry<Biome> biomeEntry = world.getBiome(playerPos);

		String iconFont = Icon.SUNNY;

		if (world.isThundering()) {
			iconFont = Icon.THUNDERSTORM;
		}
		
		if (world.isRaining()) {
			if (biomeEntry.value().getPrecipitation(playerPos, world.getSeaLevel()).equals(Biome.Precipitation.SNOW)) {
				iconFont = Icon.WEATHER_SNOWY;
			} else {
				iconFont = Icon.RAINY;
			}
		}

		return iconFont;
	}
}