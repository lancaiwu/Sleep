package com.lancaiwu.sleep.bean;

import java.util.List;

/**
 * Created by lancaiwu on 2019/1/21.
 */

public class SettingBean {
    /**
     * 是否开启
     */
    private boolean isEnable = false;

    private TimeBean startTime;
    private TimeBean endTime;

    public TimeBean getStartTime() {
        return startTime;
    }

    public void setStartTime(TimeBean startTime) {
        this.startTime = startTime;
    }

    public TimeBean getEndTime() {
        return endTime;
    }

    public void setEndTime(TimeBean endTime) {
        this.endTime = endTime;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    private List<AppBean> appBeans;

    private AppBean appBean;

    public AppBean getAppBean() {
        return appBean;
    }

    public void setAppBean(AppBean appBean) {
        this.appBean = appBean;
    }

    public List<AppBean> getAppBeans() {
        return appBeans;
    }

    public void setAppBeans(List<AppBean> appBeans) {
        this.appBeans = appBeans;
    }
}
