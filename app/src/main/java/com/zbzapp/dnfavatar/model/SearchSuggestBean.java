package com.zbzapp.dnfavatar.model;

import java.util.ArrayList;

/**
 * Created by kiefer on 2017/11/3.
 */

public class SearchSuggestBean {
        private ArrayList<SearchSuggestItemBean> KEYWORDS;

        public ArrayList<SearchSuggestItemBean> getKEYWORDS() {
                return KEYWORDS;
        }

        public void setKEYWORDS(ArrayList<SearchSuggestItemBean> KEYWORDS) {
                this.KEYWORDS = KEYWORDS;
        }

        public static class SearchSuggestItemBean{
                private String N;
                private String R;

                public String getN() {
                        return N;
                }

                public void setN(String n) {
                        N = n;
                }

                public String getR() {
                        return R;
                }

                public void setR(String r) {
                        R = r;
                }
        }
}
