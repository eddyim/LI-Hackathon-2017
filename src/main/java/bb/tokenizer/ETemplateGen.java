package bb.tokenizer;

public class ETemplateGen {
    public static void main(String[] args) {
        String inputDir = args[0];
        String outputDir = args[1];
        //TODO: scan input dir for all files with .bb.* ending and generate
        // a corresponding java file to the given output dir, preserving the package
        // relative to the input dir root, with a .render() static function that
        // renders the template
    }
}
