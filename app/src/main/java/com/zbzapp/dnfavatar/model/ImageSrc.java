package com.zbzapp.dnfavatar.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by kiefer on 2017/9/15.
 */
@Entity
public class ImageSrc {

        @Id(autoincrement = true) private Long id;
        @NotNull
        private Long comicid;
        @NotNull
        private int imageurlid;
        @NotNull private String src;

        public ImageSrc(Long comicid, String src) {
                this.comicid = comicid;
                this.src = src;
        }
        @Generated(hash = 1763658697)
        public ImageSrc(Long id, @NotNull Long comicid, int imageurlid,
                @NotNull String src) {
            this.id = id;
            this.comicid = comicid;
            this.imageurlid = imageurlid;
            this.src = src;
        }
        @Generated(hash = 1770339904)
        public ImageSrc() {
        }
        @Override
        public boolean equals(Object o) {
                return o instanceof ImageSrc && ((ImageSrc) o).id.equals(id);
        }

       

        public Long getComicid() {
                return comicid;
        }

        public void setComicid(Long comicid) {
                this.comicid = comicid;
        }

        public String getSrc() {
                return src;
        }

        public void setSrc(String src) {
                this.src = src;
        }

        public Long getId() {
            return this.id;
        }

        public void setId(Long id) {
            this.id = id;
        }
        public int getImageurlid() {
            return this.imageurlid;
        }
        public void setImageurlid(int imageurlid) {
            this.imageurlid = imageurlid;
        }
}
