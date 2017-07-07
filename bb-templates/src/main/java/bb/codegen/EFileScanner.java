package bb.codegen;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class EFileScanner {
    private static final String additionalDirectory = "";

    public static void main(String[] args) {
        String inputDir = args[0];
        String outputDir = args[1];
        generateFiles(inputDir, outputDir);
    }

    private static void generateFiles(String inputPath, String outputPath) {
        Map<File, String> files = new HashMap<>();
        File startDirectory = new File(inputPath);
        scanDirectory(startDirectory, "", files);
        ETemplateGen generator = new ETemplateGen();
        for (File f: files.keySet()) {
            String relPath = files.get(f);
            try {
                File toWrite = new File(parseOutputFile(f, outputPath, relPath));
                File directory = new File(outputPath + relPath);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                BufferedWriter bw = new BufferedWriter(new FileWriter(toWrite.getAbsoluteFile()));
                bw.write(generator.generateCode(getFullyQualifiedName(f, outputPath, relPath), parseFile(f)));
                bw.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static String getFullyQualifiedName(File f, String outputDir, String relPath) {
        String outputFile = parseOutputFile(f, outputDir, relPath);
        outputFile = outputFile.replaceAll("\\.java", "");
        if (outputFile.contains("java")) {
            outputFile = outputFile.substring(outputFile.indexOf("java") + 5);
        }
        return outputFile.replaceAll("/", ".");
    }

    private static String parseOutputFile(File f, String outputDir, String relativePath) {
        String path = outputDir + relativePath + "/" + getFileName(f);
        while (path.charAt(0) == '.' || path.charAt(0) == '/') {
            path = path.substring(1);
        }
        return path;
    }


    private static String getFileName(File f) {
        String fileName = f.getName();
        fileName = fileName.substring(0, fileName.indexOf("bb")) + "java";
        return fileName;
    }

    /** Scans the directory for files that have an ending of .bb.*.
     *  Upon seeing a valid filename, will add to validFiles, with the value being
     *  the relative path from the initial directory. If a given file is a directory, the
     * method will recursively scan the directory.
     * @param directory the directory to search
     * @param relativePath in a recursive call, the file path relative to the initial directory
     * @param validFiles the set of validFiles that will be added to
     */
    private static void scanDirectory(File directory, String relativePath, Map<File, String> validFiles) {
        File[] files = directory.listFiles();
         for (File file: files) {
            if (file.isDirectory()) {
                scanDirectory(file, relativePath + "/" + file.getName(), validFiles);
            }
            else if (file.getName().matches(".*\\.bb\\..*")) {
                validFiles.put(file, relativePath);
            }
        }
    }

    private static String parseFile(File f) {
        try {
            Scanner contentScanner = new Scanner(f).useDelimiter("\\Z");
            String content = null;
            if (contentScanner.hasNext()) {
                content = contentScanner.next();
            }
            return content;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
