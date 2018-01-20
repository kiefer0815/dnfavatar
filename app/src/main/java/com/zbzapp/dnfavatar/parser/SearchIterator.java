package com.zbzapp.dnfavatar.parser;

import com.zbzapp.dnfavatar.model.Comic;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public interface SearchIterator {

    boolean empty();

    boolean hasNext();

    Comic next();

}
