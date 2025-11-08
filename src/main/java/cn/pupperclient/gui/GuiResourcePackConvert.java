package cn.pupperclient.gui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cn.pupperclient.Soar;
import cn.pupperclient.libraries.resourcepack.ResourcePackConverter;
import cn.pupperclient.utils.JsonUtils;
import cn.pupperclient.utils.Multithreading;
import cn.pupperclient.utils.file.FileLocation;

import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

public class GuiResourcePackConvert extends Screen {

	private String progress;
	private Screen prevScreen;
	
	public GuiResourcePackConvert(Screen prevScreen) {
		super(Text.of("PackConvert"));
		this.prevScreen = prevScreen;
	}

	@Override
	public void init() {
		Multithreading.runAsync(() -> {
			ResourcePackConverter converter = createConverter();
            try {
                converter.run();
            } catch (Exception e) {
                Soar.LOGGER.error("converter error: {}", e.getMessage());
            }
            if (client != null) {
                client.setScreen(prevScreen);
            }
        });
		super.init();
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, Text.of(progress), this.width / 2, this.height / 2 - 50, Colors.WHITE);
	}
	
	private ResourcePackConverter createConverter() {
		
		List<ObjectObjectImmutablePair<File, File>> packs = new ArrayList<>();
		File cacheDir = new File(FileLocation.CACHE_DIR, "resourcepack");
		
		try {
			Files.createDirectories(cacheDir.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(File f : detectPacks()) {
			
			try {
				
				File targetFile = new File(cacheDir, f.getName());
				File packDir = new File(client.runDirectory, "resourcepacks");
				File outputFile = new File(packDir, f.getName());
				
				Files.move(f.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
				
				packs.add(ObjectObjectImmutablePair.of(targetFile, outputFile));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return new ResourcePackConverter(packs, cacheDir, progress -> {
			this.progress = progress.toString();
		});
	}
	
	private List<File> detectPacks() {
		
		List<File> packs = getOldResourcePacks();
		List<File> convertPacks = new ArrayList<>();

		for (File f : packs) {

			try (FileInputStream fis = new FileInputStream(f); ZipInputStream zipIn = new ZipInputStream(fis)) {

				ZipEntry entry;
				while ((entry = zipIn.getNextEntry()) != null) {
					if (!entry.isDirectory()) {
						if (entry.getName().equals("pack.mcmeta")) {

							JsonObject jsonObject = readJsonFromZip(zipIn);
							JsonObject packJsonObject = JsonUtils.getObjectProperty(jsonObject, "pack");

							if (packJsonObject != null) {

								int version = JsonUtils.getIntProperty(packJsonObject, "pack_format", -1);
								boolean convert = JsonUtils.getBooleanProperty(packJsonObject, "convert",
										false);

								if (version == 1 || (version != ResourcePackConverter.MC_VERSION && convert)) {
									convertPacks.add(f);
								}
							}
						}
					}
					zipIn.closeEntry();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return convertPacks;
	}

	private List<File> getOldResourcePacks() {

		List<File> files = new ArrayList<>();
		File packDir = new File(client.runDirectory, "resourcepacks");

		for (File f : packDir.listFiles()) {
			if (f.getName().endsWith(".zip")) {
				files.add(f);
			}
		}

		return files;
	}

	private static JsonObject readJsonFromZip(ZipInputStream zipIn) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;
		while ((len = zipIn.read(buffer)) > 0) {
			baos.write(buffer, 0, len);
		}
		String jsonString = baos.toString("UTF-8");
		return JsonParser.parseString(jsonString).getAsJsonObject();
	}
}
