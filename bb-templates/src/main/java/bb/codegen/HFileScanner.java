package bb.codegen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiPredicate;


public class HFileScanner {
    private static class fileTypeChecker implements BiPredicate {
        public boolean test(Object path, Object attr){
            String regexStr1 = ".*\\.bb\\..*";
            String regexStr2 = ".*\\.bb";
            return (path.toString().matches(regexStr1) || path.toString().matches(regexStr2));
        }
    }

    private static String getPathName(String inputDir, String outputDir, Path bbFile) {
        String fileName;
        String withoutFileType;
        if (bbFile.toString().matches(".*\\.bb\\..*")) {
            fileName = bbFile.toFile().getName().split("\\.bb\\.")[0];
            withoutFileType = bbFile.toString().split(fileName + "\\.bb\\.")[0];
        } else if (bbFile.toString().matches(".*\\.bb")) {
            fileName = bbFile.toFile().getName();
            fileName = fileName.substring(0, fileName.length() - 3);
            withoutFileType = bbFile.toString().split(fileName + "\\.bb")[0];
        } else {
            throw new RuntimeException(bbFile.toString() + "is not an supported file type");
        }
        //@TODO: \bb\hgen is temporary
        String relativePath = withoutFileType.substring(inputDir.length() + 1, withoutFileType.length() - 1);
        String javaWholePath = outputDir + "\\" + relativePath + "\\" + fileName;
        return javaWholePath;
    }

    //to fully qualified name
    private static String convertPathNameToFCN(String pathName) {
        return pathName.substring(2).replaceAll("\\\\", "\\.");
    }

    public static void main(String[] args) {
        // scan files and invoke template generator
        String inputDir = args[0];
        String outputDir = args[1];

        Path root = Paths.get(inputDir);

        try {
            Object[] filesToConvert = Files.find(root, Integer.MAX_VALUE,  new fileTypeChecker()).toArray();
            for (Object p : filesToConvert) {
                String pathName = getPathName(inputDir, outputDir, (Path) p);
                String fullyQualifiedOutputName = convertPathNameToFCN(pathName);
                File writeTo = new File(pathName + ".java");
                if (!writeTo.getParentFile().exists()) {
                    writeTo.getParentFile().mkdirs();
                }
                if (writeTo.createNewFile()){
                    System.out.println("File is created!");
                }else{
                    System.out.println("File already exists.");
                }

                ITemplateCodeGenerator codeGenerator = new HTemplateGen();
                String content = codeGenerator.generateCode(fullyQualifiedOutputName, new String(Files.readAllBytes(Paths.get(p.toString()))));
                FileWriter fw = null;
                BufferedWriter bw = null;

                try {
                    fw = new FileWriter(writeTo);
                    bw = new BufferedWriter(fw);
                    bw.write(content);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (bw != null) {
                            bw.close();
                        }
                        if (fw != null) {
                            fw.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("The given parameter is not a valid directory.");
        }

    }
}
