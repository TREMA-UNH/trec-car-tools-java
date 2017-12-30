package edu.unh.cs.treccar_v2.read_data;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.*;
import edu.unh.cs.treccar_v2.Header;

import java.util.Iterator;

public abstract class CborListWithHeaderIterator<T> implements Iterator<T> {
    private final CborDataItemIterator listIter;
    private DataItem firstElem;
    private  Header.TrecCarHeader header = null;

    public CborListWithHeaderIterator(CborDecoder decoder) throws CborRuntimeException {
        // try reading the header
        try {
            DataItem dataItem = decoder.decodeNext();
            try {
                this.header = DeserializeData.headerFromCbor(dataItem);

                // decode contents, this should begin with an indefinite array
                try {
                    decoder.setAutoDecodeInfinitiveArrays(false);
                    Array arr = (Array) decoder.decodeNext();
                    decoder.setAutoDecodeInfinitiveArrays(true);
                } catch (CborException e) {
                    throw new CborRuntimeException(e);
                }
                this.firstElem = null;
            } catch (Header.InvalidHeaderException e) {
                // there is no header
                this.header = null;
                this.firstElem = dataItem;
            }

            this.listIter = new CborDataItemIterator(decoder);
        } catch (CborException e) {
            throw new CborRuntimeException(e);
        }
    }

    public boolean hasNext() {
        return this.firstElem != null || this.listIter.hasNext();
    }

    public T next() {
        if (this.firstElem != null) {
            DataItem first = this.firstElem;
            this.firstElem = null;
            return parseItem(first);
        } else {
            return parseItem(this.listIter.next());
        }
    }

    protected abstract T parseItem(DataItem dataItem);

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Read-only iterator.");
    }

    public Header.TrecCarHeader getHeader() {
        return header;
    }
}
