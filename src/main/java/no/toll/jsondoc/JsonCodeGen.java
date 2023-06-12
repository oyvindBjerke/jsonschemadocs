package no.toll.jsondoc;

import net.pwall.json.schema.codegen.CodeGenerator;
import net.pwall.json.schema.codegen.TargetLanguage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

class JsonCodeGen {

    private static final int MAX_DEPTH = 32; // Randomly selected maxepth for tree traversal
    private final TargetLanguage lang;
    private final Path tmpdir;
    private final String basePackage;
    private final String genComm;

    private static TargetLanguage string2lang(final String langString) {
        return TargetLanguage.valueOf(langString.toUpperCase());
    }

    JsonCodeGen(final Context context, final Path tmpdir) {
        this.tmpdir = tmpdir;
        lang = string2lang(context.value(Context.CODE).orElse(Context.JAVA));
        basePackage = context.value(Context.PACKAGE).orElse("");
        genComm =  context.value(Context.GEN_COMM).orElse("Generated by " + JsonDoc.class.getCanonicalName());
    }


    String generate(final String inputFile) {
        final CodeGenerator codeGen = createGenerator();
        codeGen.generate(new File(inputFile));
        final Set<String> files = listAllFiles(this.tmpdir.toFile().getAbsolutePath());
        final var results = new StringBuilder();
        for (final var file: files) {
            try { results.append(Files.readString(Path.of(file))); }
            catch (final IOException e) { throw new RuntimeException(e);}
        }
        return results.toString();
    }

    private CodeGenerator createGenerator() {
        final var codeGen = new CodeGenerator();
        codeGen.setBaseDirectoryName(tmpdir.toFile().getAbsolutePath());
        codeGen.setTargetLanguage(this.lang);
        codeGen.setBasePackageName(this.basePackage);
        codeGen.setGeneratorComment(this.genComm);
        return codeGen;
    }

    private static Set<String> listAllFiles(final String startIn) {
        try (final var tree = Files.walk(Paths.get(startIn), MAX_DEPTH)) {
            return tree.filter(f -> !Files.isDirectory(f))
                       .map(f-> f.toFile().getAbsolutePath())
                       .collect(Collectors.toSet());
        }
        catch (final IOException e) { throw new RuntimeException(e); }
    }
}