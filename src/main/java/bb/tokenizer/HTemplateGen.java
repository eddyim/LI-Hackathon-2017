package bb.tokenizer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.function.BiPredicate;
import java.util.stream.Stream;


public class HTemplateGen {
    private static class fileTypeChecker implements BiPredicate {
        public boolean test(Object path, Object attr){
            String regexStr = ".*\\.bb\\..*";
            return path.toString().matches(regexStr);
        }
    }


    public static void main(String[] args) {
        String inputDir = args[0];
        String outputDir = args[1];

        Path root = Paths.get(inputDir);

        try {//@TODO: there is a max depth, which is problematic, actual sol can't be hacky like this...
            Object[] filesToConvert = Files.find(root, 100,  new fileTypeChecker()).toArray();
             for (Object p : filesToConvert){
                 System.out.println(p.toString());

                 String extra = p.toString().substring(inputDir.length());
                 File writeTo = new File(outputDir + "\\bb\\hgen" + extra + ".java");
                 System.out.println(outputDir + "\\bb\\hgen" + extra + ".java");
                 if (!writeTo.getParentFile().exists()) {
                     writeTo.getParentFile().mkdirs();
                 }
                 if (writeTo.createNewFile()){
                     System.out.println("File is created!");
                 }else{
                     System.out.println("File already exists.");
                 }

                 FileWriter fw = null;
                 BufferedWriter bw = null;

                 try {
                     String content = "This is the content to write into file\n";

                     fw = new FileWriter(writeTo);
                     bw = new BufferedWriter(fw);
                     bw.write(content);

                 } catch (IOException e) {
                     e.printStackTrace();
                 }
//               finally {
//                     try {
//                         if (fw != null) {
//                             fw.close();
//                         }
//                         if (bw != null) {
//                             bw.close();
//                         }
//                     } catch (IOException e) {
//                         e.printStackTrace();
//                     }
//                 }

//                 if (!writeTo.getParentFile().exists()) {
//                    writeTo.getParentFile().mkdirs();
//                }
//
//                try {
//                    PrintWriter writer = new PrintWriter(writeTo);
//                    writer.print("writing anything...");
//                    writer.close();
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("The given parameter is not a valid directory.");
        }





//        File root = new File(inputDir);
//
//        File[] filesToConvert = root.listFiles();
//        System.out.println(inputDir);
//
//        for (File f : filesToConvert) {
//            if (f.isDirectory()) {
//                HTemplateGen.main(new String[]{f.toString(), outputDir, Integer.toString(inputDir.length())});
//            } else if (f.toString().matches(".*\\.bb\\..*")) {
//                //@TODO: get rid of the .bb.* ending
//                String extra;
//                if (args.length == 3) {
//                    extra = f.toString().substring(Integer.parseInt(args[2]));
//                } else {
//                    extra = f.toString().substring(inputDir.length());
//                }
//                File writeTo = new File(outputDir + "/bb/hgen" + extra + ".java");
//                if (!writeTo.getParentFile().exists()) {
//                    writeTo.getParentFile().mkdirs();
//                }
//                try {
//                    PrintWriter writer = new PrintWriter(writeTo);
//                    writer.print("writing anything...");
//                    writer.close();
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//                System.out.println(f.toString());
//
//            }
//        }




        //TODO: scan input dir for all files with .bb.* ending and generate
        // a corresponding java file to the given output dir, preserving the package
        // relative to the input dir root, with a .render() static function that
        // renders the template
    }
}
