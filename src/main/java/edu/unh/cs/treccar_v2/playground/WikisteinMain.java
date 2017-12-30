package edu.unh.cs.treccar_v2.playground;

import co.nstant.in.cbor.CborException;

import java.io.IOException;

/**
 * User: dietz
 * Date: 12/20/16
 * Time: 3:08 PM
 */
public class WikisteinMain {
    public static void main(String[] args) throws IOException, CborException {
        System.setProperty("file.encoding", "UTF-8");
//        for(String topic: new String[]{"chocolate","biodiversity", "fuel"}) {

        String topic = "hi";
//            String articles = "/home/dietz/trec-car/code/lstm-car/data/all-"+topic+"-train-filter.cbor";
            String articles = "/home/dietz/trec-car/topic-release/hi.cbor";
            String train = "wikistein-train-"+topic+".tsv";
            String test = "wikistein-test-"+topic+".tsv";
            String cluster =  "wikistein-cluster-"+topic+".tsv";
            String qrels =  "wikistein-qrels-"+topic+".qrels";

            SimpleCarTrainData_old.main(new String[]{articles, train, test, cluster, qrels});

//        }
    }

}
