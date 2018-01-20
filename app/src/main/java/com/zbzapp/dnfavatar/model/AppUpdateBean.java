package com.zbzapp.dnfavatar.model;

/**
 * Created by kiefer on 16/9/21.
 */
public class AppUpdateBean {
        private int appVersion;
        private String apkUrl;
        private String updateDesc;
        private boolean force;
        private boolean updateBackground;
        private String channel;
        private boolean cleanCache;
        private String config;




        public String getConfig() {
                return config;
        }

        public void setConfig(String config) {
                this.config = config;
        }

        public int getAppVersion() {
                return appVersion;
        }

        public void setAppVersion(int appVersion) {
                this.appVersion = appVersion;
        }

        public String getApkUrl() {
                return apkUrl;
        }

        public void setApkUrl(String apkUrl) {
                this.apkUrl = apkUrl;
        }

        public String getUpdateDesc() {
                return updateDesc;
        }

        public void setUpdateDesc(String updateDesc) {
                this.updateDesc = updateDesc;
        }

        public boolean isForce() {
                return force;
        }

        public void setForce(boolean force) {
                this.force = force;
        }

        public boolean isUpdateBackground() {
                return updateBackground;
        }

        public void setUpdateBackground(boolean updateBackground) {
                this.updateBackground = updateBackground;
        }

        public String getChannel() {
                return channel;
        }

        public void setChannel(String channel) {
                this.channel = channel;
        }

        public boolean isCleanCache() {
                return cleanCache;
        }

        public void setCleanCache(boolean cleanCache) {
                this.cleanCache = cleanCache;
        }


}
