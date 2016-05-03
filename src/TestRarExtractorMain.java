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
 * Extract an archive to the given location
 */
public class TestRarExtractorMain {
	public static void main(String[] args) {
		String archive1 = "C:/users/martin/desktop/hms global/workspace local/testing junrar/Vivado2015.4.rar";
		String archive2 = "C:/users/martin/desktop/hms global/workspace local/testing junrar/packed1.rar";
		String target = "C:/users/martin/desktop/hms global/workspace local/testing junrar/";

		File source = new File(archive1);
		File destination = new File(target);

		StopWatch watch = new StopWatch();
		watch.start();

		try {
			// InputStream is = new FileInputStream(archive);
			// OutputStream os = new FileOutputStream(archive);
			// IOUtils.copy(is, os);

			RarExtractor.extractArchive(source, destination);
			// ArchiveUtilWithStreams.extractArchive(source, destination);

			System.out.println("Extracted.");
			// is.close();
			// os.close();
		} catch (Exception e) {
			System.out.println("Exception thrown in main");
		}
		watch.stop();
		long time = watch.getTime();
		long min = time / (1000 * 60);
		long sec = (time / 1000) % 60;
		System.out.println("Unpacker finished. No exceptions.");
		System.out.println("Elapsed time: " + min + " minutes " + sec
				+ " seconds.");

	}
}