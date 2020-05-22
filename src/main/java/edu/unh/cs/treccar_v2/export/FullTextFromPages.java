package edu.unh.cs.treccar_v2.export;

import co.nstant.in.cbor.CborException;
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: dietz
 * Date: 12/10/16
 * Time: 6:11 PM
 */
public class FullTextFromPages {
    private static List<String> recurseArticle(Data.PageSkeleton skel, String query){

        if(skel instanceof Data.Section){
            final Data.Section section = (Data.Section) skel;
            String query2 = section.getHeading();

            List<String> result = new ArrayList<>();
            for(Data.PageSkeleton child : section.getChildren()) {
                result.addAll(recurseArticle(child, query+" " + query2));
            }
            return result;

        } else if (skel instanceof Data.Para) {
            Data.Para para = (Data.Para) skel;
            Data.Paragraph paragraph = para.getParagraph();

            StringBuilder text = new StringBuilder();
            for(Data.ParaBody body: paragraph.getBodies()){
               if(body instanceof Data.ParaLink) text.append(((Data.ParaLink) body).getAnchorText());
               if(body instanceof Data.ParaText) text.append(((Data.ParaText) body).getText());
            }
//            if(text.length()>10) {
                return Collections.singletonList(query + " " + text);
//            } else return Collections.emptyList();
        } else if (skel instanceof Data.Image) {
            return Collections.singletonList("");
        } else if (skel instanceof Data.ListItem) {
            final Data.Paragraph paragraph = ((Data.ListItem) skel).getBodyParagraph();

            StringBuilder text = new StringBuilder();
            for(Data.ParaBody body: paragraph.getBodies()){
                if(body instanceof Data.ParaLink) text.append(((Data.ParaLink) body).getAnchorText());
                if(body instanceof Data.ParaText) text.append(((Data.ParaText) body).getText());
            }
            if(text.length()>10) {
                return Collections.singletonList(query + " " + text);
            } else return Collections.emptyList();
        } else if (skel instanceof Data.InfoBox) {
            Data.InfoBox box = (Data.InfoBox) skel;
            StringBuilder text = new StringBuilder();
            text.append("Infobox ("+box.getInfoboxType()+") ");
            for(Data.Entry<String, List<Data.PageSkeleton>> entry: box.getEntries()) {
                if(entry.getValue().size()>0) {
                    text.append(entry.getKey() + ": [");
                    for (Data.PageSkeleton val : entry.getValue()) {
                        List<String> result = recurseArticle(val, "");
                        for(String str: result) {
                            text.append(str+" ");
                        }
                    }
                    text.append("] ");
                }
            }
            return Collections.singletonList(query + " " +text.toString());
        }
        else throw new UnsupportedOperationException("not known skel "+skel);
    }


    public static void main(String[] args) throws FileNotFoundException {
        System.setProperty("file.encoding", "UTF-8");
        final FileInputStream fileInputStream = new FileInputStream(new File(args[0]));

        for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream)) {
            String query = page.getPageName();

            List<String> result = new ArrayList<>();
            for(Data.PageSkeleton skel: page.getSkeleton()){
                result.addAll(recurseArticle(skel, page.getPageName()));
            }

            for(String line: result){
                System.out.println(line);
            }
        }


    }

}
