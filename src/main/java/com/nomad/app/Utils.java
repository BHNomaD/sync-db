package com.nomad.app;

import com.nomad.app.exception.ApplicationException;
import com.nomad.app.model.EnumerationList;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.apache.commons.lang.SystemUtils.FILE_SEPARATOR;

/**
 * @author Md Shariful Islam
 */
public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static String toCSV(Collection<String> collection) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : collection) {
            stringBuilder.append(s).append(",");
        }

        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }

        return stringBuilder.toString();
    }

    public static List<String> asList(String csv) {
        if (csv == null) {
            return new ArrayList<String>();
        }

        return Arrays.asList(csv
                .replaceAll("^[,\\s]+", "")
                .replaceAll("[,\\s]+$", "")
                .replaceAll("[,\\s]+", ";").split(";"));
    }

    public static String generateUuid(long dongleId) {
        return new StringBuilder(Long.toHexString(System.nanoTime()))
                .append('-')
                .append(Long.toHexString(dongleId))
                .append('-')
                .append(UUID.randomUUID().toString()).toString();
    }

    public static Date parseDateFromString(String dateInString) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return formatter.parse(dateInString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void logMemoryStatus() {
        logMemoryStatus("");
    }

    public static void logMemoryStatus(String prefix) {
        DecimalFormat formatter = new DecimalFormat("#0000.0000");
        long total = Runtime.getRuntime().totalMemory();
        double totald = ((total * 1.0) / 1024) / 1024;
        long free = Runtime.getRuntime().freeMemory();
        double freed = ((free * 1.0) / 1024) / 1024;
        logger.info("{}, memory stats: total {} MB, used {} MB", prefix, formatter.format(totald), formatter.format(totald - freed));
    }

    public static String cm2FeetInch(Float cm) {
        if (cm < 1) return "NA";
        int inch = (int) Math.ceil(cm / 2.54);
        String ret = (inch / 12) + " feet " + ((inch % 12 == 0) ? "" : ((inch % 12) + " inch"));
        return ret;
    }

    public static void createTarArchive(List<String> files, String compressedFileName) {
        try {
            File output = new File(compressedFileName);
            // Create the output stream for the output file
            FileOutputStream fos = new FileOutputStream(output);
            // Wrap the output file stream in streams that will tar and gzip everything
            TarArchiveOutputStream taos = new TarArchiveOutputStream(new GZIPOutputStream(new BufferedOutputStream(fos)));
            // TAR originally didn't support long file names, so enable the support for it
            taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            // Get to putting all the files in the compressed output file
            for (String str : files) {
                File f = new File(str);
                addFilesToTarArchive(taos, f, ".");
            }
            // Close everything up
            taos.close();
            fos.close();
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    private static void addFilesToTarArchive(TarArchiveOutputStream taos, File file, String dir) throws IOException {
        // Create an entry for the file
        taos.putArchiveEntry(new TarArchiveEntry(file, dir + FILE_SEPARATOR + file.getName()));
        if (file.isFile()) {
            // Add the file to the archive
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            IOUtils.copy(bis, taos);
            taos.closeArchiveEntry();
            bis.close();
        } else if (file.isDirectory()) {
            // close the archive entry
            taos.closeArchiveEntry();
            // go through all the files in the directory and using recursion, add them to the archive
            for (File childFile : file.listFiles()) {
                addFilesToTarArchive(taos, childFile, file.getName());
            }
        }
    }

    public static void cleanFiles(String reportDir, String sourcePDFBaseName) throws ApplicationException {
        final File folder = new File(reportDir);
        final File[] files = folder.listFiles((dir, name) -> name.matches(sourcePDFBaseName + ".*pdf"));
        EnumerationList.State reportGenerationState = EnumerationList.State.CLEANING_TEMPORARY_FILE;

        boolean isAllFilesDeleted = true;
        for (final File file : files) {
            if (!file.delete()) {
                isAllFilesDeleted = false;
                logger.warn("Can't remove file " + file.getAbsolutePath());
            }
        }
    }

    public static void createZipArchive(String fileBaseDir, List<String> files, String zipFileName) throws ApplicationException {
        EnumerationList.State reportGenerationState = EnumerationList.State.CREATING_ZIP;
        try {
            FileOutputStream fos = new FileOutputStream(zipFileName);
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (String name : files) {
                addToZipArchive(".", fileBaseDir + File.separator + name, zos);
            }

            zos.close();
            fos.close();
        } catch (IOException ioex) {
            ioex.printStackTrace();
            throw new ApplicationException(EnumerationList.ErrorHeader.CREATE_ARCHIEVE_ERROR, ioex);
        }
    }

    public static void addToZipArchive(String dir, String fileName, ZipOutputStream zos) throws IOException {
        File file = new File(fileName);
        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(dir + FILE_SEPARATOR + file.getName());
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }

        zos.closeEntry();
        fis.close();
    }
}
