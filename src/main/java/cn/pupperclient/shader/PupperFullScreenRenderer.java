package cn.pupperclient.shader;

import com.mojang.blaze3d.vertex.VertexFormat;

public class PupperFullScreenRenderer {
    // 预构建的顶点数据
    public static PupperMeshBuilder mesh;

    private PupperFullScreenRenderer() {}

    /**
     * 在客户端启动或着色器系统初始化时调用
     */
    public static void init() {
        // 使用 POS2 格式，因为全屏处理只需要二维坐标
        mesh = new PupperMeshBuilder(PupperVertexFormats.POS2, VertexFormat.DrawMode.TRIANGLES);

        // 开始构建全屏四边形
        mesh.begin();

        // 顶点坐标范围是 -1 到 1，覆盖整个 OpenGL 标准设备坐标系 (NDC)
        // Order: (x, y)
        int v1 = mesh.vec2(-1, -1).next(); // 左下
        int v2 = mesh.vec2(-1, 1).next();  // 左上
        int v3 = mesh.vec2(1, 1).next();   // 右上
        int v4 = mesh.vec2(1, -1).next();  // 右下

        // 组成两个三角形构成的四边形
        mesh.quad(v1, v2, v3, v4);

        mesh.end();
    }
}
// based on meteor
