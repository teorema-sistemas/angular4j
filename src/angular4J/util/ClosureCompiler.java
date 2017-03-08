package angular4J.util;

import java.util.logging.Logger;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.VariableRenamingPolicy;

/**
 * A closure compiler utility class used buy Angular4J to "minify" the generated ng4j.js
 * script
 */
public final class ClosureCompiler {

   private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());
   private final CompilerOptions options;

   private static final Object lock = new Object();

   public ClosureCompiler() {
      synchronized (lock) {
         this.options = new CompilerOptions();
         CompilationLevel.WHITESPACE_ONLY.setOptionsForCompilationLevel(options);
         options.setVariableRenaming(VariableRenamingPolicy.OFF);
         options.setAngularPass(true);
         options.setTightenTypes(false);
         options.prettyPrint = false;
      }
   }

   public ClosureCompiler(CompilerOptions options) {
      this.options = options;
   }

   public String getCompressedJavaScript(String jsContent) {
      String compiled = jsContent;
      try {
         compiled = compile(jsContent);
      }
      catch (Exception e) {
         logger.warning("could not compress JS, compression disabled, check for error or your guava library version. Cause:" + e.getMessage());
      }

      return compiled.replace("_delete", "delete");
   }

   public String compile(String code) {
      Compiler compiler = new Compiler();
      compiler.disableThreads();

      SourceFile extern = SourceFile.fromCode("externs.js", "function alert(x){}");
      SourceFile input = SourceFile.fromCode("input.js", code);

      compiler.compile(extern, input, options);
      return compiler.toSource();
   }
}
