package cn.pupperclient.shader;

import static org.lwjgl.opengl.GL11C.GL_FALSE;
import static org.lwjgl.opengl.GL11C.GL_TRUE;
import static org.lwjgl.opengl.GL20C.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20C.glDeleteProgram;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public class Shader {

	public static Shader BOUND;
	private final int id;
	private final Map<String, Integer> locations = new HashMap<>();

	private static final FloatBuffer MAT = BufferUtils.createFloatBuffer(16);

	public Shader(String vertPath, String fragPath) {
		int vertex = GlStateManager.glCreateShader(GL_VERTEX_SHADER);
		int fragment = GlStateManager.glCreateShader(GL_FRAGMENT_SHADER);

		GlStateManager.glShaderSource(vertex, read(vertPath));
		GlStateManager.glShaderSource(fragment, read(fragPath));

		String vertexLog = ShaderHelper.compileShader(vertex);
		String fragmentLog = ShaderHelper.compileShader(fragment);

		if (vertexLog != null || fragmentLog != null) {
			System.err.println("Vertex Log (" + vertPath + "): " + vertexLog);
			System.err.println("Fragment Log (" + fragPath + "): " + fragmentLog);
		}

		this.id = GlStateManager.glCreateProgram();
		String programLog = ShaderHelper.linkProgram(id, vertex, fragment);

		if (programLog != null) {
			System.err.println("Program Log (" + vertPath + ", " + fragPath + "): " + programLog);
		}

		GlStateManager.glDeleteShader(vertex);
		GlStateManager.glDeleteShader(fragment);
	}

	public void set(String name, double v) {
		set(name, (float) v);
	}

	public void set(String name, double v1, double v2) {
		set(name, (float) v1, (float) v2);
	}

	private String read(String path) {
		try {
			return IOUtils.toString(
					MinecraftClient.getInstance().getResourceManager()
							.getResource(Identifier.of("pupper", "shaders/" + path)).get().getInputStream(),
					StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException("Could not read shader '" + path + "'", e);
		}
	}

	public void bind() {
		ShaderHelper.useProgram(id);
		BOUND = this;
	}

	public int getLocation(String name) {
		if (locations.containsKey(name)) {
			return locations.get(name);
		}

		int location = ShaderHelper.getUniformLocation(id, name);
		locations.put(name, location);

		return location;
	}

	public void set(String name, int v) {
		ShaderHelper.uniformInt(getLocation(name), v);
	}

	public void set(String name, float v) {
		ShaderHelper.uniformFloat(getLocation(name), v);
	}

	public void set(String name, float v1, float v2) {
		ShaderHelper.uniformFloat2(getLocation(name), v1, v2);
	}

	public void set(String name, float v1, float v2, float v3) {
		ShaderHelper.uniformFloat3(getLocation(name), v1, v2, v3);
	}

	public void set(String name, float v1, float v2, float v3, float v4) {
		ShaderHelper.uniformFloat4(getLocation(name), v1, v2, v3, v4);
	}

	public void set(String name, float[] v) {
		ShaderHelper.uniformFloat3Array(getLocation(name), v);
	}

	public void set(String name, Matrix4f mat) {
		mat.get(MAT);
		ShaderHelper.uniformMatrix4(getLocation(name), false, MAT);
	}

	public void setDefaults() {
		set("u_Proj", RenderSystem.getProjectionMatrix());
		// Modern approach: use RenderSystem matrices directly or pass from context
	}

    public void unbind() {
        ShaderHelper.useProgram(0);
        BOUND = null;
    }

    public void delete() {
        glDeleteProgram(id);
    }
}
