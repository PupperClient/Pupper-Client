package cn.pupperclient.utils;

import java.io.File;

import cn.pupperclient.mixin.interfaces.IMixinMinecraftClient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public class SkinUtils {

	public static File getSkin(Identifier identifier) {
		String fileName = identifier.getPath().replace("skins/", "");
		String folder = fileName.substring(0, 2);
		File file = new File(((IMixinMinecraftClient) MinecraftClient.getInstance()).getAssetDir(),
				"skins/" + folder + "/" + fileName);
		return file;
	}
}
