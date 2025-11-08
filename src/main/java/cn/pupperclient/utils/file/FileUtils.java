package cn.pupperclient.utils.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtils {

	public static void deleteDirectory(Path directory) throws IOException {
		Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				if (!dir.equals(directory)) {
					Files.delete(dir);
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public static void zip(File source, File zipFile) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(zipFile); ZipOutputStream zos = new ZipOutputStream(fos)) {
			addFileToZip(source, source, zos);
		}
	}

	private static void addFileToZip(File root, File source, ZipOutputStream zos) throws IOException {
		if (source.isDirectory()) {
			File[] files = source.listFiles();
			if (files != null) {
				for (File file : files) {
					addFileToZip(root, file, zos);
				}
			}
		} else {
			String relativePath = root.toURI().relativize(source.toURI()).getPath();
			try (FileInputStream fis = new FileInputStream(source)) {
				ZipEntry zipEntry = new ZipEntry(relativePath);
				zos.putNextEntry(zipEntry);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = fis.read(buffer)) > 0) {
					zos.write(buffer, 0, length);
				}
				zos.closeEntry();
			}
		}
	}

	public static void unzip(File zipFile, File destDir) throws IOException {
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
			ZipEntry zipEntry;
			while ((zipEntry = zis.getNextEntry()) != null) {
				File newFile = newFile(destDir, zipEntry);
				if (zipEntry.isDirectory()) {
					if (!newFile.isDirectory() && !newFile.mkdirs()) {
						throw new IOException("Failed to create directory " + newFile);
					}
				} else {
					File parent = newFile.getParentFile();
					if (!parent.exists() && !parent.mkdirs()) {
						throw new IOException("Failed to create directory " + parent);
					}
					try (FileOutputStream fos = new FileOutputStream(newFile)) {
						byte[] buffer = new byte[1024];
						int len;
						while ((len = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
					}
				}
				zis.closeEntry();
			}
		}
	}

	private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());
		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target directory: " + zipEntry.getName());
		}
		return destFile;
	}
	
	public static String getMd5Checksum(File file) throws IOException, NoSuchAlgorithmException {

		MessageDigest md = MessageDigest.getInstance("MD5");
		FileInputStream fis = new FileInputStream(file);
		DigestInputStream dis = new DigestInputStream(fis, md);

		byte[] buffer = new byte[8192];
		int bytesRead;

		while ((bytesRead = dis.read(buffer)) != -1) {
			md.update(buffer, 0, bytesRead);
		}

		byte[] digest = md.digest();

		StringBuilder result = new StringBuilder();
		for (byte b : digest) {
			result.append(String.format("%02x", b));
		}

		dis.close();
		fis.close();

		return result.toString();
	}

	public static void createFile(File file) {
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void createDir(File file) {
		file.mkdir();
	}
}
