package edu.unh.cs.treccar_v2;

import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/*
 * User: dietz
 * Date: 12/10/16
 * Time: 11:55 AM
 */


/**
 * Representation of TREC CAR's Wikipedia dump.
 */
public class Data {
    /**
     * Get a section path ID
     * @param sections list of sections, each represented by their heading #getHeadingId  -- not displayed text.
     */
    public static String sectionPathId(String pageId, List<Section> sections){
        List<String> result = new ArrayList<>();
        result.add(pageId);
        for(Section section:sections){
            result.add(section.headingId);
        }
        return StringUtils.join(result, "/");
    }

    /**
     * Get a section path ID
     * @param sections list of sections, each represented by their heading #getHeading  -- not id.
     */
    public static List<String> sectionPathHeadings(List<Section> sections){
        List<String> result = new ArrayList<>();
        for(Section section:sections){
            result.add(section.heading);
        }
        return result;
    }


    /**
     * Type of a page, namely Article, Category, Disambiguation, or Redirect
     */
    public static enum PageType {
        Article(0), Category(1), Disambiguation(2), Redirect(3);

        private int value;
        private PageType(int value) {
            this.value = value;
        }

        private static PageType[] values = null;
        public static PageType fromInt(int i) {
            if (PageType.values == null) {
                PageType.values = PageType.values();
            }
            return PageType.values[i];
        }
    }

    /**
     * Page metadata containing information about categories, disambiguations, redirect names, and inlinks.
     */
    public final static class PageMetadata {
        private final ArrayList<String> redirectNames;
        private final ArrayList<String> disambiguationNames;
        private final ArrayList<String> disambiguationIds;
        private final ArrayList<String> categoryNames;
        private final ArrayList<String> categoryIds;
        private final ArrayList<String> inlinkIds;
        private final ArrayList<String> inlinkAnchors;

        public PageMetadata(ArrayList<String> redirectNames, ArrayList<String> disambiguationNames, ArrayList<String> disambiguationIds, ArrayList<String> categoryNames, ArrayList<String> categoryIds, ArrayList<String> inlinkIds, ArrayList<String> inlinkAnchors) {
            this.redirectNames = redirectNames;
            this.disambiguationNames = disambiguationNames;
            this.disambiguationIds = disambiguationIds;
            this.categoryNames = categoryNames;
            this.categoryIds = categoryIds;
            this.inlinkIds = inlinkIds;
            this.inlinkAnchors = inlinkAnchors;
        }

        public PageMetadata() {
            this.redirectNames = new ArrayList<>();
            this.disambiguationNames = new ArrayList<>();
            this.disambiguationIds = new ArrayList<>();
            this.categoryNames = new ArrayList<>();
            this.categoryIds = new ArrayList<>();
            this.inlinkIds = new ArrayList<>();
            this.inlinkAnchors = new ArrayList<>();
        }

        /**
         * Names of redirect pages that point here.
         *
         * These can be interpreted as alternative references to the entity represented by this page.
         * @return Array of names
         *
         */
        public ArrayList<String> getRedirectNames() {
            return redirectNames;
        }

        /**
         * Names of disambiguation pages that point here (see clarification below).
         *
         * These can be interpreted as ambiguous alternative references to the entity
         * represented by this page. Ambiguous means that several other entities also go by this name.
         *
         * Wikipedia is inconsistent in their use of links on disambiguation pages. To avoid many false
         * positives, only the first link in a list item is considered here.
         *
         * @return Array of names
         *
         */
        public ArrayList<String> getDisambiguationNames() {
            return disambiguationNames;
        }

        /**
         * As {@link #getDisambiguationNames()}, but returns list as TREC CAR page ids.
         * @return list of page ids
         */
        public ArrayList<String> getDisambiguationIds() {
            return disambiguationIds;
        }

        /**
         * Names of Categories this page is tagged with
         *
         * These can be interpreted as information on the entity type represented by this page.
         * @return Array of names
         *
         */
        public ArrayList<String> getCategoryNames() {
            return categoryNames;
        }

        /**
         * Same as {@link #getCategoryNames()}, but returns list as TREC CAR page ids.
         * @return list of page ids
         */
        public ArrayList<String> getCategoryIds() {
            return categoryIds;
        }

        /**
         * Page Ids of article, disambiguation, and category pages that link here.
         * @return list of page ids
         */
        public ArrayList<String> getInlinkIds() {
            return inlinkIds;
        }

        /**
         * Anchor text of links on other pages that point here.
         * @return list of String
         */
        public ArrayList<String> getInlinkAnchors() {
            return inlinkAnchors;
        }

        @Override
        public String toString() {
            return "PageMetadata{" +
                    "redirectNames=" + redirectNames +
                    "\n, disambiguationNames=" + disambiguationNames +
                    "\n, disambiguationIds=" + disambiguationIds +
                    "\n, categoryNames=" + categoryNames +
                    "\n, categoryIds=" + categoryIds +
                    "\n, inlinkIds=" + inlinkIds +
                    "\n, inlinkAnchors=" + inlinkAnchors +
                    '}';
        }
    }



    /**
     * Shared interface of page elements
     */
    public static interface PageSkeleton {
    }


    /**
     * Representation of a Page in the TREC CAR data set.
     */
    public final static class Page {
        private final String pageName;
        private final String pageId;
        private final List<PageSkeleton> skeleton;
        private final PageType pageType;
        private final ArrayList<Section> childSections;
        private final PageMetadata pageMetadata;

        public Page(String pageName, String pageId, List<PageSkeleton> skeleton, PageType pageType, PageMetadata pageMetadata) {
            this.pageName = pageName;
            this.pageId = pageId;
            this.skeleton = skeleton;
            this.pageType = pageType;
            this.childSections = new ArrayList<Section>();
            for(PageSkeleton skel : skeleton) {
                if (skel instanceof Section) childSections.add((Section) skel);
            }
            this.pageMetadata = pageMetadata;
        }

        /**
         * Page title (aka Page name) as human-readable string --- no underscores, but may
         * contain diacritics and other UTF-8 characters.
         * @return
         */
        public String getPageName() {
            return pageName;
        }

        /**
         * The TREC CAR page id of this page, similar to {@link #getPageName()} but guaranteed to be an
         * ASCII string without spaces or slashes.
         *
         * Don't try to parse the page name out of this string! Use {@link #getPageName()} instead.
         */
        public String getPageId() {
            return pageId;
        }

        /**
         * The type of the page, e.g. Article, Category, Disambiguation, or Redirect.
         * @return
         */
        public PageType getPageType() {  return pageType; }

        /**
         * Get metadata of this page, e.g., inlinks, category tags, etc.
         * @return
         */
        public PageMetadata getPageMetadata() {
            return pageMetadata;
        }

        /**
         * Access the elements this page is composed off (sections, paragraphs, ...)
         * @return
         */
        public List<PageSkeleton> getSkeleton() {
            return skeleton;
        }

        /**
         * Get sections contained in this page. Similar to {@link #getSkeleton()}, but only contains sections.
         * @return
         */
        public ArrayList<Section> getChildSections() {
            return childSections;
        }

        private static List<List<Section>> flatSectionPaths_(List<Section> prefix, List<Section> headings) {
            List<List<Section>> result = new ArrayList<>();

            for(Section heading : headings){
                final List<Section> newPrefix = new ArrayList<>();
                newPrefix.addAll(prefix);
                newPrefix.add(heading);

                if(heading.getChildSections().isEmpty()) {
                    result.add(newPrefix);
                } else {
                    result.addAll(flatSectionPaths_(newPrefix,heading.getChildSections()));
                }

            }
            return result;

        }

        public List<List<Section>> flatSectionPaths() {
            return flatSectionPaths_(Collections.<Section>emptyList(), childSections);
        }


        static public class SectionPathParagraphs{
            private final List<Section> sectionPath;
            private final Paragraph paragraph;

            public SectionPathParagraphs(List<Section> sectionPath, Paragraph paragraph) {
                this.sectionPath = sectionPath;
                this.paragraph = paragraph;
            }

            public List<Section> getSectionPath() {
                return sectionPath;
            }

            public Paragraph getParagraph() {
                return paragraph;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                SectionPathParagraphs that = (SectionPathParagraphs) o;

                if (sectionPath != null ? !sectionPath.equals(that.sectionPath) : that.sectionPath != null)
                    return false;
                return paragraph != null ? paragraph.equals(that.paragraph) : that.paragraph == null;
            }

            @Override
            public int hashCode() {
                int result = sectionPath != null ? sectionPath.hashCode() : 0;
                result = 31 * result + (paragraph != null ? paragraph.hashCode() : 0);
                return result;
            }

            @Override
            public String toString() {
                return "SectionPathParagraphs{" +
                        "sectionPath=" + sectionPath +
                        ", paragraph=" + paragraph +
                        '}';
            }
        }
        private static List<SectionPathParagraphs> flatSectionPathsParagraphs_(List<Section> prefix, List<PageSkeleton> skeletonList) {
            List<SectionPathParagraphs> result = new ArrayList<>();

            for(PageSkeleton skel : skeletonList){
                if(skel instanceof Section) {
                    Section section = (Section) skel;

                    final List<Section> newPrefix = new ArrayList<>();
                    newPrefix.addAll(prefix);
                    newPrefix.add(section);

                    if (section.getChildren().isEmpty()) {

                    } else {
                        result.addAll(flatSectionPathsParagraphs_(newPrefix, section.getChildren()));
                    }

                }

                if(skel instanceof Para) {
                    Paragraph paragraph = ((Para) skel).getParagraph();
                    result.add(new SectionPathParagraphs(prefix, paragraph));
                }
            }
            return result;
        }


        public List<SectionPathParagraphs> flatSectionPathsParagraphs() {
            return flatSectionPathsParagraphs_(Collections.<Section>emptyList(), getSkeleton());
        }





        @Override
        public String toString() {
            return "Page{" +
                    "pageName='" + pageName + '\'' +
                    "\n, pageMetadata=" + pageMetadata +
                    "\n, skeleton=" + skeleton +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Page)) return false;

            Page page = (Page) o;

            if (getPageId() != null ? !getPageId().equals(page.getPageId()) : page.getPageId() != null) return false;
            return getSkeleton() != null ? getSkeleton().equals(page.getSkeleton()) : page.getSkeleton() == null;
        }

        @Override
        public int hashCode() {
            int result = getPageId() != null ? getPageId().hashCode() : 0;
            result = 31 * result + (getSkeleton() != null ? getSkeleton().hashCode() : 0);
            return result;
        }
    }

    public final static class Section implements PageSkeleton {
        private final String heading;
        private final String headingId;
        private final List<PageSkeleton> children;
        private final List<Section> childSections;

        public Section(String heading, String headingId,  List<PageSkeleton> children) {
            this.heading = heading;
            this.headingId = headingId;
            this.children = children;
            this.childSections = new ArrayList<Section>();
            for(PageSkeleton skel : children) {
                if (skel instanceof Section) childSections.add((Section) skel);
            }

        }

        /**
         * The text heading of this section.
         */
        public String getHeading() {
            return heading;
        }

        /**
         * Same as {@link #getHeading()} but provides an identifier that is guaranteed to be ASCII, without spaces or slashes.
         * @return
         */
        public String getHeadingId() {
            return headingId;
        }

        /**
         * Traverse child elements of this section, e.g. paragraphs, nested sections, etc.
         */
        public List<PageSkeleton> getChildren() {
            return children;
        }

        /**
         * List nested sections.
         * @return
         */
        public List<Section> getChildSections() { return childSections; }


        @Override
        public String toString() {
            return "Section{" +
                    "heading='" + heading + '\'' +
                    ", headingId='" + headingId + '\'' +
                    ", children=" + children +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Section)) return false;

            Section section = (Section) o;

            if (getHeadingId() != null ? !getHeadingId().equals(section.getHeadingId()) : section.getHeadingId() != null)
                return false;
            return getChildren() != null ? getChildren().equals(section.getChildren()) : section.getChildren() == null;
        }

        @Override
        public int hashCode() {
            int result = getHeadingId() != null ? getHeadingId().hashCode() : 0;
            result = 31 * result + (getChildren() != null ? getChildren().hashCode() : 0);
            return result;
        }
    }

    /**
     * Shared interface of elements inside a paragraph.
     */
    public static interface ParaBody {
    }

    /**
     * Page element containing a single paragraph.
     */
    public final static class Para implements PageSkeleton {
        private final Paragraph paragraph;

        public Para(Paragraph paragraph) {
            this.paragraph = paragraph;
        }

        public Paragraph getParagraph() {
            return paragraph;
        }

        @Override
        public String toString() {
            return "Para{" +
                    "paragraph=" + paragraph +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Para)) return false;

            Para para = (Para) o;

            return getParagraph() != null ? getParagraph().equals(para.getParagraph()) : para.getParagraph() == null;
        }

        @Override
        public int hashCode() {
            return getParagraph() != null ? getParagraph().hashCode() : 0;
        }
    }


    /**
     * An image on the page.
     */
    public final static class Image implements PageSkeleton {
        private final String imageUrl;
        private final List<PageSkeleton> captionSkel;

        public Image(String paraId, List<PageSkeleton> caption) {
            this.imageUrl= paraId;
            this.captionSkel = caption;
        }

        /**
         * URL to the image on the internet
         * @return
         */
        public String getImageUrl() {
            return imageUrl;
        }

        /**
         * Access to caption.
         * @return
         */
        public List<PageSkeleton> getCaptionSkel() {
            return captionSkel;
        }

        @Override
        public String toString() {
            return "Image{" +
                    "imageUrl='" + imageUrl + '\'' +
                    ", caption=" + captionSkel +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Image)) return false;

            Image image = (Image) o;

            if (getImageUrl() != null ? !getImageUrl().equals(image.getImageUrl()) : image.getImageUrl() != null)
                return false;
            return getCaptionSkel() != null ? getCaptionSkel().equals(image.getCaptionSkel()) : image.getCaptionSkel() == null;
        }

        @Override
        public int hashCode() {
            int result = getImageUrl() != null ? getImageUrl().hashCode() : 0;
            result = 31 * result + (getCaptionSkel() != null ? getCaptionSkel().hashCode() : 0);
            return result;
        }
    }

    /**
     * List entry on a page. There is no representation of a single list as a whole.
     *
     * Instead, there is one page element per list entry with a nesting level. This jigsaw puzzle is brought to you by Wikipedia.
     */
    public final static class ListItem implements PageSkeleton {
        private final int nestingLevel;
        private final Paragraph bodyParagraph;

        public ListItem(int nestingLevel, Paragraph bodyParagraph) {
            this.nestingLevel = nestingLevel;
            this.bodyParagraph = bodyParagraph;
        }

        /**
         * Nesting level at which this list item was found.
         * @return
         */
        public int getNestingLevel() {
            return nestingLevel;
        }

        /**
         * Content of the list entry.
         * @return
         */
        public Paragraph getBodyParagraph() {
            return bodyParagraph;
        }

        @Override
        public String toString() {
            return "* " + bodyParagraph.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ListItem)) return false;

            ListItem listItem = (ListItem) o;

            if (getNestingLevel() != listItem.getNestingLevel()) return false;
            return getBodyParagraph() != null ? getBodyParagraph().equals(listItem.getBodyParagraph()) : listItem.getBodyParagraph() == null;
        }

        @Override
        public int hashCode() {
            int result = getNestingLevel();
            result = 31 * result + (getBodyParagraph() != null ? getBodyParagraph().hashCode() : 0);
            return result;
        }
    }


    /**
     * A paragraph element.
     */
    public final static class Paragraph  {
        private final String paraId;
        private final List<ParaBody> bodies;

        public Paragraph(String paraId, List<ParaBody> bodies) {
            this.paraId = paraId;
            this.bodies = bodies;
        }

        /**
         * Unique id of the paragraph.
         *
         * Note that any paragraph with the same text, will get the same identifier.
         * Therefore, it is likely that several pages will contain a paragraph with this identifier.
         * This is because the unique identifier is based on the text content.
         * @return
         */
        public String getParaId() {
            return paraId;
        }

        /**
         * Content of the paragraph, as a list of text and links. (No further nesting)
         * @return
         */
        public List<ParaBody> getBodies() {
            return bodies;
        }

        @Override
        public String toString() {
            return "Paragraph{" +
                    "paraId='" + paraId + '\'' +
                    ", captionSkel=" + bodies +
                    '}';
        }

        /**
         * Convenience accessor to just get the text of this paragraph (replacing links with their anchor text).
         *
         * For more control use {@link #getBodies()}.
         */
        public String getTextOnly() {
            StringBuilder result = new StringBuilder();
            for(ParaBody body: bodies){
                if(body instanceof ParaLink){
                    result.append(((ParaLink) body).getAnchorText());
                }
                else if (body instanceof ParaText){
                    result.append(((ParaText) body).getText());
                }
            }
            return result.toString();
        }

        /**
         * Convenience accessor to all  entities (as page names -- not IDs!) in this paragraph.
         *
         * For more control use {@link #getBodies()}.
         */
        public List<String> getEntitiesOnly() {
            List<String> result = new ArrayList<>();
            for(ParaBody body: bodies){
                if(body instanceof ParaLink){
                    result.add(((ParaLink) body).getPage());
                }
            }
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Paragraph)) return false;

            Paragraph paragraph = (Paragraph) o;

            return getParaId() != null ? getParaId().equals(paragraph.getParaId()) : paragraph.getParaId() == null;
        }

        @Override
        public int hashCode() {
            return getParaId() != null ? getParaId().hashCode() : 0;
        }
    }


    /**
     * A text fragment in a paragraph.
     */
    public final static class ParaText implements ParaBody {
        private final String text;

        public ParaText(String text) {
            this.text = text;
        }

        /**
         * Get the text of this segment.
         * @return
         */
        public String getText() {
            return text;
        }

        @Override
        public String toString() {
            return "ParaText{" +
                    "text='" + text + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ParaText)) return false;

            ParaText paraText = (ParaText) o;

            return getText() != null ? getText().equals(paraText.getText()) : paraText.getText() == null;
        }

        @Override
        public int hashCode() {
            return getText() != null ? getText().hashCode() : 0;
        }
    }

    /**
     * A hyper link inside a paragraph.
     *
     * The link is represented by {@link #getAnchorText()},
     * the target page as name ({@link #getPage()}) and id ({@link #getPageId()}.
     *
     * In some cases links refer to sections inside pages. In this case the name of the section is provided in {@link #getLinkSection()} (which is otherwise `null`. Check with {@link #hasLinkSection()})
     */
    public final static class ParaLink implements ParaBody {
        private final String linkSection;
        private final String pageId;
        private final String anchorText;
        private final String page;

        public ParaLink(String page, String pageId, String anchorText) {
            this.pageId = pageId;
            this.anchorText = anchorText;
            this.page = page;
            this.linkSection = "";
        }

        public ParaLink(String page, String pageId, String linkSection, String anchorText) {
            this.linkSection = linkSection;
            this.pageId = pageId;
            this.anchorText = anchorText;
            this.page = page;
        }

        /**
         * True if this link points to a section, if false points to a page.
         * @return
         */
        public boolean hasLinkSection(){
            return (this.linkSection != null) && (!Objects.equals(this.linkSection, ""));
        }

        /**
         * Section heading if the link points to a section.
         * Will return null or "" if not defined. Check with {@link #hasLinkSection()}
         */
        public String getLinkSection() {
            return linkSection;
        }

        /**
         * The page id of the link target.
         * @return
         */
        public String getPageId() {
            return pageId;
        }

        /**
         * Anchor text of the link
         * @return
         */
        public String getAnchorText() {
            return anchorText;
        }

        /**
         * Page name of the link target
         * @return
         */
        public String getPage() {
            return page;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ParaLink)) return false;

            ParaLink paraLink = (ParaLink) o;

            if (getLinkSection() != null ? !getLinkSection().equals(paraLink.getLinkSection()) : paraLink.getLinkSection() != null)
                return false;
            if (getPageId() != null ? !getPageId().equals(paraLink.getPageId()) : paraLink.getPageId() != null)
                return false;
            if (getAnchorText() != null ? !getAnchorText().equals(paraLink.getAnchorText()) : paraLink.getAnchorText() != null)
                return false;
            return getPage() != null ? getPage().equals(paraLink.getPage()) : paraLink.getPage() == null;
        }

        @Override
        public int hashCode() {
            int result = getLinkSection() != null ? getLinkSection().hashCode() : 0;
            result = 31 * result + (getPageId() != null ? getPageId().hashCode() : 0);
            result = 31 * result + (getAnchorText() != null ? getAnchorText().hashCode() : 0);
            result = 31 * result + (getPage() != null ? getPage().hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ParaLink{" +
                    " page='" + page + '\'' +
                    ", linkSection='" + linkSection + '\'' +
                    ", pageId='" + pageId + '\'' +
                    ", anchorText='" + anchorText + '\'' +
                    '}';
        }
    }

}
