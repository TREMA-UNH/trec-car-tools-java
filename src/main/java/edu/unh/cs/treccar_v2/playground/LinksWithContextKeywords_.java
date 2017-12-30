package edu.unh.cs.treccar_v2.playground;

import co.nstant.in.cbor.CborException;
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: dietz
 * Date: 12/23/16
 * Time: 4:36 PM
 */
public class LinksWithContextKeywords_ {


    public static class LinkInstance {
        protected String fromPage;
        protected String sectionpath;
        protected String toPage;
        protected String paragraphContent;


        public LinkInstance(String fromPage, String sectionpath, String toPage, String paragraphContent) {
            this.fromPage = fromPage;
            this.sectionpath = sectionpath;
            this.toPage = toPage;
            this.paragraphContent = paragraphContent.replaceAll("[\n\t\r]"," ");
        }


        public List<String> toTsvSeqments() {
            List<String> result = new ArrayList<>();
            result.add(fromPage);
            result.add(toPage);
            result.add(sectionpath);
            result.add(paragraphContent);
            return result;
        }


        public String toTsvLine() {
            return StringUtils.join(toTsvSeqments(), "\t");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LinkInstance)) return false;

            LinkInstance instance = (LinkInstance) o;

            if (fromPage != null ? !fromPage.equals(instance.fromPage) : instance.fromPage != null) return false;
            if (sectionpath != null ? !sectionpath.equals(instance.sectionpath) : instance.sectionpath != null)
                return false;
            return toPage != null ? toPage.equals(instance.toPage) : instance.toPage == null;
        }

        @Override
        public int hashCode() {
            int result = fromPage != null ? fromPage.hashCode() : 0;
            result = 31 * result + (sectionpath != null ? sectionpath.hashCode() : 0);
            result = 31 * result + (toPage != null ? toPage.hashCode() : 0);
            return result;
        }
    }

    public LinksWithContextKeywords_() {
    }


//    private List<LinkInstance> extractLinkData(FileInputStream fileInputStream, List<String> keywords, boolean addParagraph, boolean filterByKeyword) throws IOException, CborException {
//        List<LinkInstance> megaresult = new ArrayList<LinkInstance>();
//
//        for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream)) {
//
//            List<LinkInstance> result = getInstances(page, keywords, addParagraph, filterByKeyword);
//            megaresult.addAll(result);
//        }
//
//        fileInputStream.close();
//
//        return megaresult;
//
//    }

    private static boolean paragraphTextContainsKeyword(Data.Paragraph para, List<String> keywords){
        final String normtext = para.getTextOnly().toLowerCase();
        for(String keyword: keywords) {
            if (normtext.contains(keyword)) return true;
        }
        return false;
    }

    private List<LinkInstance> getInstances(Data.Page page, List<String> keywords, boolean addParagraph, boolean filterByKeyword) {
        List<LinkInstance> result = new ArrayList<>();
        for(Data.Page.SectionPathParagraphs sectparas : page.flatSectionPathsParagraphs()){
            if(!filterByKeyword || paragraphTextContainsKeyword(sectparas.getParagraph(), keywords)){
                for(String toPage :sectparas.getParagraph().getEntitiesOnly()){
                    String text = "";
                    if(addParagraph) text = sectparas.getParagraph().getTextOnly();
                    final String sectPath = StringUtils.join(Data.sectionPathHeadings(sectparas.getSectionPath()), " ");
                    result.add(new LinkInstance(page.getPageName(), sectPath, toPage, text));
                }
            }
        }
        return result;
    }

    public static void main(String[] args) throws IOException, CborException {
        System.setProperty("file.encoding", "UTF-8");
        final String cborArticleInputFile = args[0];
        final String linkOutputFile = args[1];
        List<String> keywords = new ArrayList<>();
        for (int i = 2, argsLength = args.length; i < argsLength; i++) {
            String arg = args[i];
            keywords.add(arg.trim().toLowerCase());
        }

//        final String testOutputFile = args[2];
//        final String clusterOutputFile = args[3];

        boolean addParagraph = false;
        boolean filterByKeyword= true;



        if(filterByKeyword) {
            System.out.println("extract links with keyword "+keywords+" from file "+cborArticleInputFile);
        } else {
            System.out.println("extract all links from "+cborArticleInputFile);
        }

        {
            LinksWithContextKeywords_ extract = new LinksWithContextKeywords_();
            final FileInputStream fileInputStream = new FileInputStream(new File(cborArticleInputFile));

//            List<LinkInstance> trainData = extract.extractLinkData(fileInputStream, keywords, addParagraph, filterByKeyword);


            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(linkOutputFile)));
            for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream)) {

                List<LinkInstance> result = extract.getInstances(page, keywords, addParagraph, filterByKeyword);
                for(LinkInstance line: result){
//                System.out.println(line.toTsvSeqments());
                    writer.write(line.toTsvLine());
                    writer.newLine();

                }
            }

            fileInputStream.close();
            writer.close();

        }
    }

}
