package edu.unh.cs.treccar_v2.export;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Get text from all paragraphs in paragraphCorpus.cbor
 * User: dietz
 * Date: 5/23/19
 * Time: 4:06 PM
 */
public class TitlesFromPages {
  public static void main(String[] args) throws FileNotFoundException {
    System.setProperty("file.encoding", "UTF-8");
    final FileInputStream fileInputStream = new FileInputStream(new File(args[0]));

    for (Data.Page page : DeserializeData.iterableAnnotations(fileInputStream)) {
      System.out.println(page.getPageId() + "\t" + page.getPageName());
    }
  }
}
