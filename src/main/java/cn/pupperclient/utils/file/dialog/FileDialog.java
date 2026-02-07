package cn.pupperclient.utils.file.dialog;

import java.io.File;
import java.nio.ByteBuffer;

import cn.pupperclient.PupperLogger;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.nfd.NFDFilterItem;
import org.lwjgl.util.nfd.NativeFileDialog;

import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

public class FileDialog {
    static {
        if (NativeFileDialog.NFD_Init() != NativeFileDialog.NFD_OKAY) {
            PupperLogger.error("FileDialog" , "Failed to initialize Native File Dialog!");
        }
    }

	public static ObjectObjectImmutablePair<Boolean, File> chooseFile(String name, String... extensions) {

		try (MemoryStack stack = MemoryStack.stackPush()) {

			NFDFilterItem.Buffer filters = NFDFilterItem.malloc(1);

			filters.get(0).name(stack.UTF8(name)).spec(stack.UTF8(String.join(",", extensions)));

			PointerBuffer path = stack.mallocPointer(1);

            int result = NativeFileDialog.NFD_OpenDialog(path, filters, (ByteBuffer) null);
            if (result == NativeFileDialog.NFD_OKAY) {
                String p = path.getStringUTF8(0);
                return ObjectObjectImmutablePair.of(true, new File(p));
            }
            return ObjectObjectImmutablePair.of(false, null);
		}
	}

	public static ObjectObjectImmutablePair<Boolean, File> chooseFolder() {

		PointerBuffer path = MemoryUtil.memAllocPointer(1);

		try {
			return ObjectObjectImmutablePair.of(isSuccess(NativeFileDialog.NFD_PickFolder(path, (ByteBuffer) null)),
					new File(path.getStringUTF8(0)));
		} finally {
			MemoryUtil.memFree(path);
		}
	}

	private static boolean isSuccess(int result) {
		return result == NativeFileDialog.NFD_OKAY;
	}

    public static void shutdown() {
        NativeFileDialog.NFD_Quit();
    }
}
