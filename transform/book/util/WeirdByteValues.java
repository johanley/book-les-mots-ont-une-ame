package book.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** 
 Scan text and gather info regarding unusual byte values in the context of the CP-1252 encoding.
 
 Flags both values that are completely unexpected, and values that are expected but also of interest for some reason.
 Here, I flag things that wouldn't be used in English, French, or Italian; that's just a personal preference.
 
 Files coded as UTF-8 will usually have a too-high number of weird characters.
 Note: most UTF-8 files do not include the byte-order mark (and such things are not recommended practice).
*/
public final class WeirdByteValues {
  
  public static void main(String[] args) throws IOException {
    WeirdByteValues examineBytes = new WeirdByteValues(TABS_ARE_WEIRD);
    //examineBytes.reportRecursive("C:\\johanley\\ProjectsPhoton\\book-les-mots-ont-une-ame\\input");
    //examineBytes.reportRecursive("C:\\johanley\\ProjectsPhoton\\book-les-mots-ont-une-ame\\transform\\book\\util");
    examineBytes.reportRecursive("C:\\johanley\\ProjectsPhoton\\book-les-mots-ont-une-ame\\output\\");
  }
  
  WeirdByteValues(boolean tabsAreWeird){
    this.tabsAreWeird = tabsAreWeird;
  }
  private boolean tabsAreWeird;
  private static final boolean TABS_ARE_WEIRD = true;
  private static final boolean TABS_ARE_EXPECTED = false;
  
  void report(String filePath) {
    try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath))) {
      byte[] buffer = new byte[4096]; // 4KB chunk size
      int bytesRead;
      // read(buffer) returns actual bytes read, or -1 at EOF
      while ((bytesRead = bis.read(buffer)) != -1) {
        for (int i = 0; i < bytesRead; i++) {
          Integer val = Byte.toUnsignedInt(buffer[i]);
          if (isUnmapped(val)) {
            COMPLETELY_UNMAPPED.add(val);  
          }
          else if (isWeirdControlCode(val)) {
            WEIRD_CONTROL_CODES.add(val);
          }
          else if (WEIRDO_DESCRIPTIONS.keySet().contains(val)) {
            if (WEIRDOS.keySet().contains(val)) {
              Integer currentCount = WEIRDOS.get(val);
              WEIRDOS.put(val, ++currentCount);
            }
            else {
              WEIRDOS.put(val, 1);
            }
          }
          else {
            EXPECTED_BYTE_VALUES.add(val);
          }
        }
      }
      log(filePath);
      log("  Expected byte values: " + EXPECTED_BYTE_VALUES.size() + " different characters. Max byte value: " + Collections.max(EXPECTED_BYTE_VALUES));
      if (!COMPLETELY_UNMAPPED.isEmpty()) log("  Unmapped bytes (decimal): " + COMPLETELY_UNMAPPED);
      if (!WEIRD_CONTROL_CODES.isEmpty()) log("  Weird control code bytes (decimal): " + WEIRD_CONTROL_CODES);
      if (isLikelyUTF8()) {
        log(" "); 
        log("*** PROBABLY UTF-8. *** ");
        log(" "); 
      }
      if (!WEIRDOS.isEmpty()) {
        log("  " +  WEIRDOS.keySet().size() + " Weird bytes (decimal): " + WEIRDOS.keySet());
        if (!WEIRDOS.keySet().isEmpty()) {
          for(Integer val : WEIRDOS.keySet()) {
            log("    " + val + " (" +hex(val) + ") : " + WEIRDO_DESCRIPTIONS.get(val) + " count=" + WEIRDOS.get(val));
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  void reportRecursive(String dirPath) throws IOException {
    File dir = new File(dirPath);
    if (!dir.isDirectory()) {
      log("Not a directory. Can't proceed: " + dirPath);
    }
    else {
      FileVisitor<Path> fileProcessor = new ProcessFile();
      Files.walkFileTree(Paths.get(dirPath), fileProcessor);      
    }
  }

  private final class ProcessFile extends SimpleFileVisitor<Path> {
    @Override public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
      WeirdByteValues examineFile = new WeirdByteValues(tabsAreWeird);
      examineFile.report(path.toAbsolutePath().toString());
      return FileVisitResult.CONTINUE;
    }
    @Override  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
      //System.out.println("Processing directory:" + dir);
      return FileVisitResult.CONTINUE;
    }
  }  

  /** Bytes that are not mapped at all in the CP-1252 encoding. */
  private boolean isUnmapped(Integer val) {
    return val == 129 || val == 141 || val == 143 || val == 144 || val == 157;
  }
  
  /** Byte values for control codes (the smallest values); excludes carriage return and line feed. */
  private boolean isWeirdControlCode(Integer val) {
    boolean result = val <= 31 && val != 10 && val != 13;
    if (!tabsAreWeird) {
      result = result && val != 9; 
    }
    return result;
  }
  
  private boolean isLikelyUTF8() {
    //the apostrophe (curly single quote) is euro+trademark in UTF-8
    return 
      WEIRDOS.containsKey(128) || // euro 
      WEIRDOS.containsKey(153) || //trademark
      WEIRDOS.size() > 9
    ;
  }

  private Set<Integer /*0..255*/> COMPLETELY_UNMAPPED = new LinkedHashSet<>();
  private Set<Integer /*0..255*/> WEIRD_CONTROL_CODES = new LinkedHashSet<>();
  private Map<Integer /*0..255*/, Integer /*count*/> WEIRDOS = new LinkedHashMap<>();
  private Set<Integer /*0..255*/> EXPECTED_BYTE_VALUES = new LinkedHashSet<>();
  
  private void log(Object thing) {
    System.out.println(thing.toString());
  }
  
  private String hex(Integer val) {
    return Integer.toHexString(val).toUpperCase();
  }

  /** Descriptions of unusual byte values that I wish to track. */
  private static Map<Integer /*0..255*/, String /*description*/> WEIRDO_DESCRIPTIONS = new LinkedHashMap<>();
  static {
    WEIRDO_DESCRIPTIONS.put(127, "delete");
    WEIRDO_DESCRIPTIONS.put(128, "euro");
    WEIRDO_DESCRIPTIONS.put(130, "bottom-single-quote");
    WEIRDO_DESCRIPTIONS.put(131, "florin");
    WEIRDO_DESCRIPTIONS.put(132, "bottom-double-quote");
    WEIRDO_DESCRIPTIONS.put(133, "ellipsis");
    WEIRDO_DESCRIPTIONS.put(134, "dagger");
    WEIRDO_DESCRIPTIONS.put(135, "double-dagger");
    WEIRDO_DESCRIPTIONS.put(136, "caret");
    WEIRDO_DESCRIPTIONS.put(137, "per-mille");
    WEIRDO_DESCRIPTIONS.put(138, "s-caron-capital");
    WEIRDO_DESCRIPTIONS.put(139, "guillemet-single-open");
    WEIRDO_DESCRIPTIONS.put(140, "oe-ligature-capital");
    WEIRDO_DESCRIPTIONS.put(142, "z-caron-capital");
    WEIRDO_DESCRIPTIONS.put(145, "single-quote-open");
    WEIRDO_DESCRIPTIONS.put(146, "single-quote-close (apostrophe)");
    WEIRDO_DESCRIPTIONS.put(147, "double-quote-open");
    WEIRDO_DESCRIPTIONS.put(148, "double-quote-close");
    WEIRDO_DESCRIPTIONS.put(149, "bullet");
    WEIRDO_DESCRIPTIONS.put(150,  "en-dash");
    WEIRDO_DESCRIPTIONS.put(151, "em-dash");
    WEIRDO_DESCRIPTIONS.put(152, "tilde");
    WEIRDO_DESCRIPTIONS.put(153, "trademark");
    WEIRDO_DESCRIPTIONS.put(154, "s-caron-small");
    WEIRDO_DESCRIPTIONS.put(155, "guillemet-single-close");
    WEIRDO_DESCRIPTIONS.put(156, "oe-ligature-small");
    WEIRDO_DESCRIPTIONS.put(158, "z-caron-small");
    WEIRDO_DESCRIPTIONS.put(159, "y-diaresis");
    WEIRDO_DESCRIPTIONS.put(160, "non-breaking-space");
    WEIRDO_DESCRIPTIONS.put(161, "spanish-exclamation");
    WEIRDO_DESCRIPTIONS.put(162, "cent");
    WEIRDO_DESCRIPTIONS.put(163, "pound");
    WEIRDO_DESCRIPTIONS.put(164, "generic-currency");
    WEIRDO_DESCRIPTIONS.put(165, "yen");
    WEIRDO_DESCRIPTIONS.put(166, "broken-pipe");
    WEIRDO_DESCRIPTIONS.put(167, "section");
    WEIRDO_DESCRIPTIONS.put(168, "diaresis");
    WEIRDO_DESCRIPTIONS.put(169, "copywrite");
    WEIRDO_DESCRIPTIONS.put(170, "a-ordinal");
    WEIRDO_DESCRIPTIONS.put(171, "guillemet-double-open");
    WEIRDO_DESCRIPTIONS.put(172, "logical-negation");
    WEIRDO_DESCRIPTIONS.put(173, "syllable-hyphen");
    WEIRDO_DESCRIPTIONS.put(174, "registered-trademark");
    WEIRDO_DESCRIPTIONS.put(175, "macron");
    WEIRDO_DESCRIPTIONS.put(176, "degree");
    WEIRDO_DESCRIPTIONS.put(177, "plus-minus");
    WEIRDO_DESCRIPTIONS.put(178, "super-2");
    WEIRDO_DESCRIPTIONS.put(179, "super-3");
    WEIRDO_DESCRIPTIONS.put(180, "acute-accent");
    WEIRDO_DESCRIPTIONS.put(181, "micro");
    WEIRDO_DESCRIPTIONS.put(182, "paragraph");
    WEIRDO_DESCRIPTIONS.put(183, "interpunct");
    WEIRDO_DESCRIPTIONS.put(184, "cedilla");
    WEIRDO_DESCRIPTIONS.put(185, "super-1");
    WEIRDO_DESCRIPTIONS.put(186, "o-ordinal");
    WEIRDO_DESCRIPTIONS.put(187, "guillemet-double-close");
    WEIRDO_DESCRIPTIONS.put(188, "one-quarter");
    WEIRDO_DESCRIPTIONS.put(189, "half");
    WEIRDO_DESCRIPTIONS.put(190, "three-quarters");
    WEIRDO_DESCRIPTIONS.put(191, "spanish-question");
    WEIRDO_DESCRIPTIONS.put(208, "eth");
    WEIRDO_DESCRIPTIONS.put(216, "o-with-slash");
    WEIRDO_DESCRIPTIONS.put(221, "y-acute");
    WEIRDO_DESCRIPTIONS.put(222, "thorn");
    WEIRDO_DESCRIPTIONS.put(248, "o-with-slash-small");
    WEIRDO_DESCRIPTIONS.put(253, "y-acute-small");
    WEIRDO_DESCRIPTIONS.put(254, "thorn-small");
  }
}
