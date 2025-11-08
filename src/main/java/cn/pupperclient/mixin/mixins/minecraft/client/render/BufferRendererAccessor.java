package cn.pupperclient.mixin.mixins.minecraft.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferRenderer;

@Mixin(BufferRenderer.class)
public interface BufferRendererAccessor {
	@Accessor("currentVertexBuffer")
	static void setCurrentVertexBuffer(VertexBuffer vertexBuffer) {
	}
}
