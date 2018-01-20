package com.zbzapp.dnfavatar.model;

/**
 * Created by kiefer on 2017/10/26.
 */

public class RelatedVideoItem {
        private long id;
        private String u; //详情url
        private String i; //封面
        private String t; //title
        private String d; //time

        public long getId() {
                return id;
        }

        public void setId(long id) {
                this.id = id;
        }

        public String getU() {
                return u;
        }

        public void setU(String u) {
                this.u = u;
        }

        public String getI() {
                return i;
        }

        public void setI(String i) {
                this.i = i;
        }

        public String getT() {
                return t;
        }

        public void setT(String t) {
                this.t = t;
        }

        public String getD() {
                return d;
        }

        public void setD(String d) {
                this.d = d;
        }
}
