package cn.pupperclient.utils.minecraft.player;

import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import cn.pupperclient.mixin.interfaces.IMixinMinecraftClient;

public class SkinUtils {

	public static File getSkin(ResourceLocation identifier) {
		String fileName = identifier.getPath().replace("skins/", "");
		String folder = fileName.substring(0, 2);
		File file = new File(((IMixinMinecraftClient) Minecraft.getInstance()).getAssetDir(),
				"skins/" + folder + "/" + fileName);
		return file;
	}
}
