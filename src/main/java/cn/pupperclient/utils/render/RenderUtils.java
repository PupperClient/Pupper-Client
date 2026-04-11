/**
 * @Author: oneachina
 * @link: github.com/oneachina
 */
package cn.pupperclient.utils.render;

import cn.pupperclient.utils.minecraft.interfaces.IMinecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class RenderUtils implements IMinecraft {
    public static Vec3 center;
    public static final Matrix4f projection = new Matrix4f();

    public static void updateScreenCenter(Matrix4f projection, Matrix4f view) {
        RenderUtils.projection.set(projection);

        Matrix4f invProjection = new Matrix4f(projection).invert();
        Matrix4f invView = new Matrix4f(view).invert();

        Vector4f center4 = new Vector4f(0, 0, 0, 1).mul(invProjection).mul(invView);
        center4.div(center4.w);

        Vec3 camera = client.gameRenderer.getMainCamera().position();
        center = new Vec3(camera.x + center4.x, camera.y + center4.y, camera.z + center4.z);
    }
}
