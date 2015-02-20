import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

public class App {
	final static Logger logger = Logger.getLogger(App.class);

	public static void main(String[] args) throws IOException, Exception {
		
		Properties prop = new Properties();
		InputStream input = null;

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();

		logger.info("start: " + dateFormat.format(date));
		
		final File devlopmentLoc = new File("C:\\code\\gtnexus\\development");
		final File tcardLoc = new File("C:\\code\\gtnexus\\development\\modules\\main\\tcard");

		FileInputStream in = new FileInputStream("C:\\code\\gtnexus\\development\\modules\\main\\tcard\\sonar-project.properties");
		Properties props = new Properties();
		props.load(in);
		in.close();

		File folder = new File("C:\\Sonar\\conf"); /* load properties files */

		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
			if (file.isFile()) {
				String fileName = file.getName();
				fileName = fileName.replaceAll(".properties", "");
				logger.info("File Name: " + fileName);
				input = new FileInputStream(file);
				prop.load(input);

				if (prop.getProperty("flag") == "true") {

					logger.info("Remote Branch FullName: "
							+ prop.getProperty("remoteBranchFullName"));
					logger.info("Remote Branch Name: "
							+ prop.getProperty("remoteBranchName"));
					logger.info("LocalBranch Name: "
							+ prop.getProperty("localBranchName"));

					FileOutputStream out = new FileOutputStream("C:\\code\\gtnexus\\development\\modules\\main\\tcard\\sonar-project.properties");
					
					props.setProperty(
							"sonar.projectKey",
							"TCx.SLDev.Core." + prop.getProperty("team")
									+ prop.getProperty("remoteBranchName"));
					props.setProperty("sonar.projectName", "TCx-" + fileName);
					props.store(out, null);
					out.close();

					List<String> gitCmd = new ArrayList<String>();

					gitCmd.add("cmd.exe");
					gitCmd.add("/C");
					gitCmd.add("start");

					gitCmd.add("git");
					gitCmd.add("checkout");
					gitCmd.add("-b");
					gitCmd.add(prop.getProperty("localBranchName"));
					gitCmd.add(prop.getProperty("remoteBranchFullName"));

					ProcessBuilder processBuilder = new ProcessBuilder(gitCmd);

					processBuilder.directory(devlopmentLoc);

					processBuilder.redirectError();
					Process gitprocess = processBuilder.start();

					logger.info(fileName + " Process terminated with "
							+ gitprocess.waitFor());

					try {
						Thread.sleep(60000); // 1000 milliseconds is one second.
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}

					List<String> buildCmd = new ArrayList<String>();

					buildCmd.add("cmd.exe");
					buildCmd.add("/C");
					buildCmd.add("start");
					buildCmd.add("build");
					buildCmd.add("runApt");

					processBuilder.command(buildCmd);
					processBuilder.directory(tcardLoc);
					Process buildProcess = processBuilder.start();

					try {
						Thread.sleep(90000); // 1000 milliseconds is one second.
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
					date = new Date();
					logger.info("Sonar start: " + dateFormat.format(date));

					List<String> sonarCmd = new ArrayList<String>();
					sonarCmd.add("cmd.exe");
					sonarCmd.add("/C");
					sonarCmd.add("start");
					sonarCmd.add("sonar-runner");

					processBuilder.command(sonarCmd);
					processBuilder.directory(tcardLoc);

					Process sonarProcess = processBuilder.start();
					InputStreamConsumer isc = new InputStreamConsumer(
							sonarProcess.getInputStream());
					isc.start();
					int exitCode = sonarProcess.waitFor();

					isc.join();
					logger.info("Process terminated with " + exitCode);

					date = new Date();
					logger.info("Sonar End: " + dateFormat.format(date));

					logger.info("\n\n\n");
					
					FileOutputStream confOut= new FileOutputStream(file);

				} else {
					logger.info(prop.getProperty("remoteBranchFullName")
							+ ": branch  not modified");

				}
			}
		}
		date = new Date();
		logger.info("End: " + dateFormat.format(date));
		try {

		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static class InputStreamConsumer extends Thread {

		private InputStream is;

		public InputStreamConsumer(InputStream is) {
			this.is = is;
		}

		@Override
		public void run() {

			try {
				int value = -1;
				while ((value = is.read()) != -1) {
					System.out.print((char) value);

				}
			} catch (IOException exp) {
				exp.printStackTrace();
			}

		}

	}

}
