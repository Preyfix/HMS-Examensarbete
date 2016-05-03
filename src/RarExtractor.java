import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.impl.FileVolumeManager;
import com.github.junrar.rarfile.FileHeader;

public class RarExtractor {

	public static void extractArchive(File archive, File destination) {
		Archive arch = null;
		try {
			arch = new Archive(archive);
		} catch (RarException e) {
			// Handle exception?
		} catch (IOException e1) {
			// Handle exception?
		}
		
		if (arch != null) {
			if (arch.isEncrypted()) {
				System.out.println("Archive encrypted. Exiting.");
				return;
			}
			FileHeader fh = null;
			while (true) {
				fh = arch.nextFileHeader();
				if (fh == null) {
					break;
				}
				
				// --- Check if file is encrypted --- \\
				if (fh.isEncrypted()) {
					System.out.println("Encrypted file, unable to extract: " + fh.getFileNameString());
					continue;
				}
				System.out.println("Extracting: " + fh.getFileNameString());
				try {
					if (fh.isDirectory()) {
						createDirectory(fh, destination);
					} else {
						File f = createFile(fh, destination);
						OutputStream stream = new FileOutputStream(f);
						arch.extractFile(fh, stream);
						stream.close();
					}
				} catch (IOException e) {
					// Handle exception?
				} catch (RarException e) {
					// Handle exception?
				}
			}
		}
	}

	private static File createFile(FileHeader fh, File destination) {
		File f = null;
		String name = null;
		if (fh.isFileHeader() && fh.isUnicode()) {
			name = fh.getFileNameW();
		} else {
			name = fh.getFileNameString();
		}
		f = new File(destination, name);
		if (!f.exists()) {
			try {
				f = makeFile(destination, name);
			} catch (IOException e) {
				//
			}
		}
		return f;
	}

	private static File makeFile(File destination, String name)
			throws IOException {
		String[] dirs = name.split("\\\\");
		if (dirs == null) {
			return null;
		}
		String path = "";
		int size = dirs.length;
		if (size == 1) {
			return new File(destination, name);
		} else if (size > 1) {
			for (int i = 0; i < dirs.length - 1; i++) {
				path = path + File.separator + dirs[i];
				new File(destination, path).mkdir();
			}
			path = path + File.separator + dirs[dirs.length - 1];
			File f = new File(destination, path);
			f.createNewFile();
			return f;
		} else {
			return null;
		}
	}

	private static void createDirectory(FileHeader fh, File destination) {
		File f = null;
		if (fh.isDirectory() && fh.isUnicode()) {
			f = new File(destination, fh.getFileNameW());
			if (!f.exists()) {
				makeDirectory(destination, fh.getFileNameW());
			}
		} else if (fh.isDirectory() && !fh.isUnicode()) {
			f = new File(destination, fh.getFileNameString());
			if (!f.exists()) {
				makeDirectory(destination, fh.getFileNameString());
			}
		}
	}

	private static void makeDirectory(File destination, String fileName) {
		String[] dirs = fileName.split("\\\\");
		if (dirs == null) {
			return;
		}
		String path = "";
		for (String dir : dirs) {
			path = path + File.separator + dir;
			new File(destination, path).mkdir();
		}
	}

	public static void showFileListRar(String sourcePath, PrintStream print)
			throws Exception {

		File f = new File(sourcePath);
		try (Archive archive = new Archive(new FileVolumeManager(f))) {
			if (archive != null) {
				archive.getMainHeader().print();
				FileHeader fh = archive.nextFileHeader();
				while (fh != null) {
					print.println(fh.getFileNameString());
					fh = archive.nextFileHeader();
				}
			}
		} catch (Exception e) {
			throw e;
		}
	}

	public static void showFileListRar(String sourcePath) throws Exception {
		showFileListRar(sourcePath, System.out);
	}
}