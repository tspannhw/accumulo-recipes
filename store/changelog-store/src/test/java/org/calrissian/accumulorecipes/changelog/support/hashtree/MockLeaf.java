package org.calrissian.accumulorecipes.changelog.support.hashtree;

import org.calrissian.accumlorecipes.changelog.support.hashtree.Leaf;

public class MockLeaf extends Leaf {

    public MockLeaf() {
        super();
    }

    public MockLeaf(String hash) {

        super(hash);
    }


    public int compareTo(Object o) {

        MockLeaf obj = (MockLeaf)o;
        return hash.compareTo(obj.hash);
    }


}
