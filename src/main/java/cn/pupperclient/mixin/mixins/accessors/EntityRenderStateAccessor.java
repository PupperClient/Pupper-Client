/**
 * @Author: oneachina
 * @link: github.com/oneachina
 */
package cn.pupperclient.mixin.mixins.accessors;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityRenderState.class)
public interface EntityRenderStateAccessor {
    @Accessor("id")
    int getEntityId();
}
