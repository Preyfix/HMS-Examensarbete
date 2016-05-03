import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

/**
 * extract an archive to the given location
 * 
 * @author edmund wagner
 * 
 */
public class ExtractArchive {

	// Lägg till en counter som ökar vid varje exception.
	// Lägg även till en stopwatch.
	// Genomför mätningar.
	
	private static int RuntimeCounter = 0;
	private static int IOCounter = 0;
	private static int RarCounter = 0;
	
	private static Log logger = LogFactory.getLog(ExtractArchive.class
			.getName());

	public static void extractArchive(String archive, String destination) {
		if (archive == null || destination == null) {
			RuntimeCounter = RuntimeCounter + 1;
			throw new RuntimeException("archive and destination must be set");
		}
		File arch = new File(archive);
		if (!arch.exists()) {
			RuntimeCounter = RuntimeCounter + 1;
			throw new RuntimeException("the archive does not exit: " + archive);
		}
		File dest = new File(destination);
		if (!dest.exists() || !dest.isDirectory()) {
			RuntimeCounter = RuntimeCounter + 1;
			throw new RuntimeException(
					"the destination must exist and point to a directory: "
							+ destination);
		}
		extractArchive(arch, dest);
	}

	
	public static void main(String[] args) {
		String archive = "C:/users/martin/desktop/hms global/workspace local/testing junrar/Vivado2015.4.rar";
//		String archive = "C:/users/martin/desktop/hms global/workspace local/testing junrar/packed1.rar";
		String target = "C:/users/martin/desktop/hms global/workspace local/testing junrar/";
		
		File source = new File(archive);
		File destination = new File(target);
		
		StopWatch watch = new StopWatch();
		watch.start();
		
		try{
//			InputStream is = new FileInputStream(archive);
//			OutputStream os = new FileOutputStream(archive);
//			IOUtils.copy(is, os);
			
			ArchiveUtil.extractArchive(source, destination);
//			ArchiveUtilWithStreams.extractArchive(source, destination);
			
			System.out.println("Extracted.");
//			is.close();
//			os.close();
		}catch(Exception e){
			System.out.println("Exception thrown in main");
		}
		watch.stop();
		long time = watch.getTime();
		long min = time/(1000*60);
		long sec = (time/1000)%60;
		System.out.println("Unpacker finished. No exceptions.");
		System.out.println("Elapsed time: " + min + " minutes " + sec + " seconds.");
		
	}
	
	
	
	
	
//	public static void main(String[] args) {
//		//String archive = "C:/users/martin/desktop/hms global/workspace local/testing junrar/Vivado2015.4.rar";
//		String archive = "C:/users/martin/desktop/hms global/workspace local/testing junrar/dir.rar";
//		String dest = "C:/users/martin/desktop/hms global/workspace local/testing junrar/";
//		
//		StopWatch watch = new StopWatch();
//		watch.start();
//		extractArchive(archive, dest);
//		System.out.println("Number of Runtime exceptions:  " + RuntimeCounter);
//		System.out.println("Number of Rar exceptions:      " + RarCounter);
//		System.out.println("Number of IO exceptions:       " + IOCounter);
//
//		
//		
//		watch.stop();
//		long time = watch.getTime();
//		long min = time/(1000*60);
//		long sec = (time/1000)%60;
//		System.out.println("Rar extractor finished.");
//		System.out.println("Elapsed time: " + min + " minutes " + sec + " seconds.");
////		if (args.length == 2) {
////			extractArchive(args[0], args[1]);
////		} else {
////			System.out
////					.println("usage: java -jar extractArchive.jar <thearchive> <the destination directory>");
////		}
//	}

	public static void extractArchive(File archive, File destination) {
		Archive arch = null;
		try {
			arch = new Archive(archive);
		} catch (RarException e) {
			RarCounter = RarCounter + 1;
			logger.error(e);
		} catch (IOException e1) {
			IOCounter = IOCounter + 1;
			logger.error(e1);
		}
		if (arch != null) {
			if (arch.isEncrypted()) {
				logger.warn("archive is encrypted cannot extreact");
				return;
			}
			FileHeader fh = null;
			while (true) {
				fh = arch.nextFileHeader();
				if (fh == null) {
					break;
				}
				if (fh.isEncrypted()) {
					logger.warn("file is encrypted cannot extract: "
							+ fh.getFileNameString());
					continue;
				}
				//logger.info("extracting: " + fh.getFileNameString());
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
					IOCounter = IOCounter + 1;
					logger.error("error extracting the file", e);
				} catch (RarException e) {
					RarCounter = RarCounter + 1;
					logger.error("error extraction the file", e);
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
				IOCounter = IOCounter + 1;
				logger.error("error creating the new file: " + f.getName(), e);
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
}