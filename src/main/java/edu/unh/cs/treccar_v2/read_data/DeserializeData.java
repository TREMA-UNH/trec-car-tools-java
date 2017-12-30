package edu.unh.cs.treccar_v2.read_data;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.Header;
import org.jetbrains.annotations.NotNull;

public class DeserializeData {
    private static final List<String> SUPPORTED_RELEASES = Arrays.asList("trec-car v1.6", "trec-car v2.0");
    private static final String SUPPORTED_RELEASES_STR =  SUPPORTED_RELEASES.get(0)+" or "+SUPPORTED_RELEASES.get(1);


    private static void checkSupportedRelease(Header.TrecCarHeader header) {
        if(header == null){
            throw new CborFileTypeException("This method only supports releases " + SUPPORTED_RELEASES_STR+", but this input has no release information. Please use an appropriate reader.");
        }

        final String dataReleaseName = header.getProvenance().getDataReleaseName();
        if(!SUPPORTED_RELEASES.contains(dataReleaseName)) {
            throw new CborFileTypeException("This method only supports releases  "+SUPPORTED_RELEASES_STR+", but input is of data release "+dataReleaseName+". Please use an appropriate reader.");
        }
    }

    private static void checkIsPagesOrOutlines(Header.TrecCarHeader header) {
        final Header.FileType fileType = header.getFileType();
        if(! Header.FileType.OutlinesFile.equals(fileType) && !Header.FileType.PagesFile.equals(fileType)){
            throw new CborFileTypeException("This method only supports "+Header.FileType.PagesFile+" or "+Header.FileType.OutlinesFile+", but input is of file type "+fileType+". Please use an appropriate reader.");
        }
    }


    private static void checkIsParagraphFile(Header.TrecCarHeader header) {
        final Header.FileType fileType = header.getFileType();
        if(! Header.FileType.ParagraphsFile.equals(fileType)){
            throw new CborFileTypeException("This method only supports "+Header.FileType.ParagraphsFile+", but input is of file type "+fileType+". Please use an appropriate reader.");
        }
    }

    // =========== Pages ===================

    /**
     * Iterator to read pages from the CBOR file.
     * @param inputStream  file input stream of pages CBOR file
     * @return Iterator over pages
     * @throws CborRuntimeException
     */
    @NotNull
    public static Iterator<Data.Page> iterAnnotations(InputStream inputStream) throws CborRuntimeException, CborFileTypeException {
        class PageIterator extends CborListWithHeaderIterator<Data.Page> {
            private PageIterator(CborDecoder decoder) throws CborRuntimeException {
                super(decoder);
            }
            protected Data.Page parseItem(DataItem dataItem) {
                return pageFromCbor(dataItem);
            }
        };

        final CborDecoder decode = new CborDecoder(inputStream);
        final PageIterator pageIterator = new PageIterator(decode);

        final Header.TrecCarHeader header = pageIterator.getHeader();
        checkSupportedRelease(header);
        checkIsPagesOrOutlines(header);

        return pageIterator;
    }


    public static Header.TrecCarHeader getTrecCarHeader(InputStream inputStream) throws Header.InvalidHeaderException {
        class PageIterator extends CborListWithHeaderIterator<Data.Page> {
            private PageIterator(CborDecoder decoder) throws CborRuntimeException {
                super(decoder);
            }
            protected Data.Page parseItem(DataItem dataItem) {
                return pageFromCbor(dataItem);
            }
        };

        final CborDecoder decode = new CborDecoder(inputStream);
        final Header.TrecCarHeader header = (new PageIterator(decode)).getHeader();
        checkSupportedRelease(header);
        return header;
    }


    /**
     * Iteratable reading pages from the CBOR file.
     * @param inputStream  file input stream of pages CBOR file
     * @return Iterable over pages
     * @throws CborRuntimeException
     */
    public static Iterable<Data.Page> iterableAnnotations(final InputStream inputStream) throws CborRuntimeException, CborFileTypeException {
        return new Iterable<Data.Page>() {
            @NotNull
            public Iterator<Data.Page> iterator() {
                return iterAnnotations(inputStream);
            }
        };
    }


    /**
     * Reads a page at a given byte offset in file input stream from a CBOR file.   Does not check file type.
     *
     * Use at your own risk!
     *
     * @param inputStream  file input stream of pages CBOR file
     * @return Iterator over pages
     * @param offset byteoffset into the stream. Note that if the offset is wrong, this will and return `null`.
     * @throws CborRuntimeException
     *
     * @return null if no valid object can be located at the byte offsset
     */
    public static Data.Page annotationAtOffset(final InputStream inputStream, long offset) throws CborRuntimeException, IOException {
        inputStream.skip(offset);
        return iterAnnotations(inputStream).next();
    }



    // =========== Paragraphs ===================

    /**
     * Iterator to read paragraphs from the CBOR file.
     * @param inputStream  file input stream of pages CBOR file
     * @return Iterator over paragraphs
     * @throws CborRuntimeException
     */
    @NotNull
    public static Iterator<Data.Paragraph> iterParagraphs(InputStream inputStream) throws CborRuntimeException, CborFileTypeException {
        class ParagraphIterator extends CborListWithHeaderIterator<Data.Paragraph> {
            private ParagraphIterator(CborDecoder decoder) throws CborRuntimeException {
                super(decoder);
            }
            protected Data.Paragraph parseItem(DataItem dataItem) {
                return paragraphFromCbor(dataItem);
            }
        };

        final CborDecoder decode = new CborDecoder(inputStream);
        final ParagraphIterator paragraphIterator = new ParagraphIterator(decode);

        checkSupportedRelease(paragraphIterator.getHeader());
        checkIsParagraphFile(paragraphIterator.getHeader());

        return paragraphIterator;
    }



    /**
     * Iterable to read paragraphs from the CBOR file.
     * @param inputStream  file input stream of pages CBOR file
     * @return Iterator over paragraphs
     * @throws CborRuntimeException
     */
    public static Iterable<Data.Paragraph> iterableParagraphs(final InputStream inputStream) throws CborRuntimeException, CborFileTypeException {
        return new Iterable<Data.Paragraph>() {
            @Override
            @NotNull
            public Iterator<Data.Paragraph> iterator() {
                return iterParagraphs(inputStream);
            }
        };
    }


    // ============ Data accessors ==================

    private static ArrayList<String> getUnicodeArray(List<DataItem> resultArray) {
        ArrayList<String> result = new ArrayList<String>(resultArray.size());
        for (DataItem item: resultArray) {
            if (Special.BREAK.equals(item)) {
                break;
            }
            String s = ((UnicodeString) item).getString();
            result.add(s);
        }
        return result;
    }


    private static ArrayList<String> getByteArray(List<DataItem> resultArray) {
        ArrayList<String> result = new ArrayList<String>(resultArray.size());
        for (DataItem item: resultArray) {
            if (Special.BREAK.equals(item)) {
                break;
            }
            String s = new String(((ByteString) item).getBytes());
            result.add(s);
        }
        return result;
    }

    private static Data.PageType pageTypeFromCbor(DataItem dataItem) {
//        Data.PageType pageType = Data.PageType.fromInt(((UnsignedInteger) (((Array) dataItem).get(1)).getDataItems().get(0)).getValue().intValue());
        final DataItem tag = ((Array) dataItem).getDataItems().get(0);
        final int tagValue = ((UnsignedInteger) tag).getValue().intValue();
        return Data.PageType.fromInt(tagValue);
//        return  pageType;
    }

    // page type 0: article, 1: category, 2: Disambiguation, 3: redirect (with link)
    private static Data.PageMetadata pageMetadataFromCbor(DataItem dataItem) {
        List<DataItem> outerArray = ((Array) dataItem).getDataItems();

        final Data.PageMetadata pageMetadata = new Data.PageMetadata();

        for (int i = 0; i < outerArray.size(); i+=2) {
            DataItem tagArray = outerArray.get(i);
            if (Special.BREAK.equals(tagArray)) {
                break;
            }


            final DataItem tag = ((Array) tagArray).getDataItems().get(0);
            final long tagValue = ((UnsignedInteger) tag).getValue().longValue();

            DataItem item = outerArray.get(i+1);
            if (Special.BREAK.equals(item)) {
                throw new RuntimeException("Illegal protocol when decoding page metadata. Tag is "+tag+" but item is BREAK.");
            }


//            long tagValue = tag.getTag().getValue();
            if (tagValue == 0L) {
                final ArrayList<String> array = getUnicodeArray(((Array) item).getDataItems());
                pageMetadata.getRedirectNames().addAll(array);
            } else if (tagValue == 1L) {
                final ArrayList<String> array = getUnicodeArray(((Array) item).getDataItems());
                pageMetadata.getDisambiguationNames().addAll(array);
            } else if (tagValue == 2L) {
                final ArrayList<String> array = getByteArray(((Array) item).getDataItems());
                pageMetadata.getDisambiguationIds().addAll(array);
            } else if (tagValue == 3L) {
                final ArrayList<String> array = getUnicodeArray(((Array) item).getDataItems());
                pageMetadata.getCategoryNames().addAll(array);
            } else if (tagValue == 4L) {
                final ArrayList<String> array = getByteArray(((Array) item).getDataItems());
                pageMetadata.getCategoryIds().addAll(array);
            } else if (tagValue == 5L) {
                final ArrayList<String> array = getByteArray(((Array) item).getDataItems());
                pageMetadata.getInlinkIds().addAll(array);
            } else if (tagValue == 6L) {
                final ArrayList<String> array = getUnicodeArray(((Array) item).getDataItems());
                pageMetadata.getInlinkAnchors().addAll(array);
            }
        }

        return pageMetadata;
    }

    private static Data.Page pageFromCbor(DataItem dataItem) {
        List<DataItem> array = ((Array) dataItem).getDataItems();

        assert(array.get(0).getTag().getValue() == 0L);

        UnicodeString pageName = (UnicodeString) array.get(1);
        ByteString pageId = (ByteString) array.get(2);
        DataItem skeletons = array.get(3);
        Data.PageType pageType = null;
        Data.PageMetadata pageMetadata = null;
        if (array.size() > 4) {
            pageType = pageTypeFromCbor(array.get(4));
            pageMetadata = pageMetadataFromCbor(array.get(5));// [ tag1, payload1, tag2, payload2, ...]
        }

        return new Data.Page(pageName.getString(), new String(pageId.getBytes()), pageSkeletonsFromCbor(skeletons), pageType, pageMetadata);
    }

    private static Data.Image imageFromCbor(DataItem imageUrlDataItem, DataItem skeletonDataItem) {
        UnicodeString imageUrl = (UnicodeString) imageUrlDataItem;

        return new Data.Image(imageUrl.getString(), pageSkeletonsFromCbor(skeletonDataItem));
    }

    private static Data.ListItem listFromCbor(DataItem nestingLevelItem, DataItem paragraphItem) {
        UnsignedInteger nestingLevel = (UnsignedInteger) nestingLevelItem;
        return new Data.ListItem(nestingLevel.getValue().intValue(), paragraphFromCbor(paragraphItem));
    }

    private static Data.Para paraFromCbor(DataItem dataItem){
        return new Data.Para(paragraphFromCbor(dataItem));
    }

    private static Data.Paragraph paragraphFromCbor(DataItem dataItem) {
        List<DataItem> array = ((Array) dataItem).getDataItems();
        assert(array.get(0).getTag().getValue() == 0L);

//        List<DataItem> array2 = ((Array) array.get(1)).getDataItems();
//        assert(((UnsignedInteger) array2.get(0)).getValue().intValue() == 0);
        ByteString paraid = (ByteString) array.get(1);

//            List<DataItem> bodiesItem = ((Array) array.get(2)).getDataItems();
        DataItem bodiesItem = (Array) array.get(2);

        return new Data.Paragraph( new String(paraid.getBytes()), paraBodiesFromCbor(bodiesItem));
    }


    private static Data.PageSkeleton pageSkeletonFromCbor(DataItem dataItem){
        List<DataItem> array = ((Array) dataItem).getDataItems();

        switch( ((UnsignedInteger) array.get(0)).getValue().intValue()) {
            case 0: {
                UnicodeString heading = (UnicodeString) array.get(1);
                ByteString headingId = (ByteString) array.get(2);
                return new Data.Section(heading.getString(), new String(headingId.getBytes()), pageSkeletonsFromCbor(array.get(3)));
            }
            case 1: return paraFromCbor((array.get(1)));
            case 2: return imageFromCbor(array.get(1), array.get(2));
            case 3: return listFromCbor(array.get(1), array.get(2));
            default: throw new RuntimeException("pageSkeletonFromCbor found an unhandled case: "+array.toString());
        }
    }
    private static List<Data.PageSkeleton> pageSkeletonsFromCbor(DataItem dataItem){

        Array skeletons = (Array) dataItem;
        List<Data.PageSkeleton> result = new ArrayList<Data.PageSkeleton>();
        for(DataItem item:skeletons.getDataItems()){
            if (Special.BREAK.equals(item))  break;
            result.add(pageSkeletonFromCbor(item));
        }
        return result;
    }



    private static List<Data.ParaBody> paraBodiesFromCbor(DataItem dataItem) {
        Array bodies = (Array) dataItem;
        List<Data.ParaBody> result = new ArrayList<Data.ParaBody>();
        for (DataItem item : bodies.getDataItems()) {
            if (Special.BREAK.equals(item)) break;
            result.add(paraBodyFromCbor(item));
        }
        return result;
    }


    private static Data.ParaBody paraBodyFromCbor(DataItem dataItem) {

        List<DataItem> array = ((Array) dataItem).getDataItems();

        switch( ((UnsignedInteger) array.get(0)).getValue().intValue()) {
            case 0: {
                UnicodeString text = (UnicodeString) array.get(1);
                return new Data.ParaText(text.getString());
            }
            case 1: {
                List<DataItem> array_ = ((Array) array.get(1)).getDataItems();

                UnicodeString page = (UnicodeString) array_.get(1);
                ByteString pageId = (ByteString) array_.get(3);
                UnicodeString anchorText = (UnicodeString) array_.get(4);
                // this is either a list of one or zero elements
                List<DataItem> linkSectionMaybe = ((Array) array_.get(2)).getDataItems();
                if(linkSectionMaybe.size()>0) {
                    UnicodeString linkSection = ((UnicodeString) linkSectionMaybe.get(0));
                    return new Data.ParaLink(page.getString(), new String(pageId.getBytes()), linkSection.getString(), anchorText.getString());
                }else {
                    return new Data.ParaLink(page.getString(),  new String(pageId.getBytes()), anchorText.getString());
                }
            }
            default: throw new RuntimeException("paraBodyFromCbor found an unhandled case: "+array.toString());
        }
    }


    // =========== Header ===================



    private static Header.FileType fileTypeFromCbor(DataItem dataItem) {
        List<DataItem> array = ((Array) dataItem).getDataItems();
        return Header.FileType.fromInt(((UnsignedInteger) array.get(0)).getValue().intValue());
    }

    public static Header.TrecCarHeader headerFromCbor(DataItem dataItem) throws Header.InvalidHeaderException {
        List<DataItem> array = ((Array) dataItem).getDataItems();
        if (array.size() != 3) {
            throw new Header.InvalidHeaderException();
        }

        try {
            String magicWord = ((UnicodeString) array.get(0)).getString();
            if (!Objects.equals(magicWord, "CAR")) {
                throw new Header.InvalidHeaderException();
            }
        } catch (ClassCastException e) {
            throw new Header.InvalidHeaderException();
        }

        final Header.FileType fileType = fileTypeFromCbor(array.get(1));
        Header.Provenance provenance = provenanceFromCbor(array.get(2));
        return new Header.TrecCarHeader(fileType, provenance);
    }



    private static Header.Provenance provenanceFromCbor (DataItem dataItem) {
        List<DataItem> array = ((Array) dataItem).getDataItems();

        List<DataItem> siteProvenance = ((Array) array.get(1)).getDataItems();
        String dataReleaseName = ((UnicodeString) array.get(2)).getString();
        final Header.Provenance provenance = new Header.Provenance(dataReleaseName);
        List<DataItem> comments = ((Array) array.get(3)).getDataItems();
        List<DataItem> transforms = ((Array) array.get(4)).getDataItems();

        for (DataItem item: siteProvenance){
            if (Special.BREAK.equals(item))  break;
            provenance.getSiteProvenance().add( siteProvenanceFromCbor(item));
        }
        for (DataItem item: comments){
            if (Special.BREAK.equals(item))  break;
            provenance.getComments().add( ((UnicodeString) item).getString());
        }
        for (DataItem item: transforms){
            if (Special.BREAK.equals(item))  break;
            provenance.getTransforms().add( transformFromCbor(item));
        }

        return provenance;
    }

    private static Header.SiteProvenance siteProvenanceFromCbor(DataItem dataItem) {
        List<DataItem> array = ((Array) dataItem).getDataItems();

        String provSiteId = ((UnicodeString) array.get(1)).getString();
        String language = ((UnicodeString) array.get(2)).getString();
        String sourceName = ((UnicodeString) array.get(3)).getString();

        List<DataItem> comments = ((Array) array.get(4)).getDataItems();

        final Header.SiteProvenance siteProvenance = new Header.SiteProvenance(provSiteId, language, sourceName);

        for (DataItem item: comments){
            if (Special.BREAK.equals(item))  break;
            siteProvenance.getSiteComments().add( ((UnicodeString) item).getString());
        }
        return siteProvenance;
    }

    private static Header.Transform transformFromCbor(DataItem dataItem) {
        List<DataItem> array = ((Array) dataItem).getDataItems();

        String toolName = ((UnicodeString) array.get(1)).getString();
        String toolCommit = ((UnicodeString) array.get(2)).getString();
        return new Header.Transform(toolName, toolCommit);
    }
}
