package net.jakartaee.tools.netdoc;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Util {
	final static int BUFFER = 2048;
	
	public static final String[] ignoreExt = new String[] { "gif", "png", "jpg", "jpeg", "svg", "css", "scss"};
	public static final Set<String> IGNORE_EXT = new HashSet<>(Arrays.asList(ignoreExt));

//	public static void unzip(String zipFilePath, File tempDir) throws IOException {
//		try {
//			BufferedOutputStream dest = null;
//			FileInputStream fis = new FileInputStream(zipFilePath);
//			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
//			ZipEntry entry;
//			while ((entry = zis.getNextEntry()) != null) {
//				int dotIndex =  entry.getName().lastIndexOf('.') + 1;
//				
//				if ( dotIndex <= 0 ) continue;
//				if (IGNORE_EXT.contains(entry.getName().substring(dotIndex) ) ) continue; 
//				
//				System.out.println("Extracting: " + entry + " with extension " + entry.getName().substring(dotIndex));
//				int count;
//				byte data[] = new byte[BUFFER];
//				// write the files to the disk
//				String outfilePath = tempDir.getAbsolutePath() + "/" + entry.getName().replaceAll("/",".");
//				System.out.println("Writing file: " + outfilePath);
//				FileOutputStream fos = new FileOutputStream(outfilePath);
//				dest = new BufferedOutputStream(fos, BUFFER);
//				while ((count = zis.read(data, 0, BUFFER)) != -1) {
//					dest.write(data, 0, count);
//				}
//				dest.flush();
//				dest.close();
//			}
//			zis.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
    private static final int BUFFER_SIZE = 4096;
    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    
    
    //public void unzip(String zipFilePath, String destDirectory) throws IOException {
    public static void unzip(String zipFilePath, File tempDir) throws IOException {
    	String destDirectory = tempDir.getAbsolutePath();
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        System.out.println("Unzipping file: " + zipFilePath + " to temp directory: " + tempDir);
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
    	try (
    			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));)
    	{
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }   		
    	}catch ( Exception e) {
    		System.out.println("EEEEEEEEEEEEError extracting file: " + filePath);
    	}

    }
	
	public static File createTempDir(String dirStr) throws IOException {
		String tempDirStr = File.createTempFile("temp-file", "tmp").getParent()+"/" + dirStr;
		File tempDir  = new File(tempDirStr);
		if (tempDir.exists()) {
			emptyDir(tempDir);
		} else {
			boolean createdTempFolder = tempDir.mkdirs();
			if (!createdTempFolder) throw new IOException();
			System.out.println("Created Temp Directory: "+tempDir.getAbsolutePath());			
		}
		return tempDir;
	}

	public static void deleteDir(File dir) {
		emptyDir(dir);
		dir.delete();
		System.out.println("Deleted Temp Directory: "+dir.getAbsolutePath());
	}
	
	public static void emptyDir(File dir) {
		String[]entries = dir.list();
		for(String s: entries){
		    File currentFile = new File(dir.getPath(),s);
		    currentFile.delete();
			System.out.println("Deleted file: "+currentFile.getAbsolutePath());
		}		
	}
	
	public static String runCommand(boolean isLinux, String cmd) {
		StringBuffer output = new StringBuffer();
		System.out.println("Running command: " + cmd);
        ProcessBuilder processBuilder = new ProcessBuilder();
        
        System.out.println("Running LINUX command syntax: "+ isLinux);
//        if ( isLinux )
//        	processBuilder.command("bash", "-c", cmd);
//        else
        	processBuilder.command("cmd.exe", "/c", cmd);			// Windows

        try {

            Process process = processBuilder.start();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                output.append(line);
            }

            int exitCode = process.waitFor();
            System.out.println("\nExited with error code : " + exitCode);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        return output.toString();

    }
	
	public static String convertJsonToJs(String json) {
		String bracketStr = json.replace("\"connections\":", " const connections = ")
								.replace("\"servlets\":", " const servlets = ")
							    .replace("\"services\":", " const services = ")
							    .replace("\"sockets\":", " const sockets = ")
							    .replace("\"info\":", " const info = ")
							    .replace(", const", "; const");								// Replace commas with semi-colon between the objects.  Semicolon necessary if no line breaks
		return bracketStr.substring(1, bracketStr.length() - 1);							// Need to remove the first and last brackets {}
		
	}
	
	public static String convertJsToHtml(String js) {
		final String before = "<!DOCTYPE html><html><title>Net Doc</title> <script type=\"text/javascript\" src=\"js/templates/servlet.js\"></script> <script type=\"text/javascript\" src=\"js/templates/service.js\"></script> <script type=\"text/javascript\" src=\"js/templates/connection.js\"></script> <script type=\"text/javascript\" src=\"js/templates/socket.js\"></script> <link rel=\"stylesheet\" href=\"netdoc.css\">";
		final String after = "<body><h1><center>Net Doc Report</center></h1><div id=\"app\"></div> <script>document.getElementById(\"app\").innerHTML=`<h1>Servlets</h1><ul>${servlets.map(servletTemplate).join(\"\")}</ul><h1>Web Services</h1><ul>${services.map(serviceTemplate).join(\"\")}</ul><h1>Net Connections</h1><ul>${connections.map(connectionTemplate).join(\"\")}</ul><h1>Web Sockets</h1><ul>${sockets.map(socketTemplate).join(\"\")}</ul>`;</script></body>";
		return before + "<script>" + js + "</script>" + after;
	}
}
