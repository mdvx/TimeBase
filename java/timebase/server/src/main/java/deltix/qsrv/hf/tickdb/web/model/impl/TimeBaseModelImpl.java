package deltix.qsrv.hf.tickdb.web.model.impl;

import deltix.qsrv.hf.tickdb.pub.mon.TBMonitor;
import deltix.qsrv.hf.tickdb.web.model.pub.MenuModel;
import deltix.qsrv.hf.tickdb.web.model.pub.TimeBaseModel;
import deltix.util.Version;
import deltix.util.time.GMT;
import deltix.util.time.TimeKeeper;

import java.util.Date;

/**
 *
 */
public class TimeBaseModelImpl implements TimeBaseModel {

    private MenuModel menuModel;

    public String getVersion() {
        return Version.VERSION_STRING;
    }

    public String getTitle() {
        return "TimeBase Monitor";
    }

    @Override
    public Date getCurrentDate() {
        return new Date(TimeKeeper.currentTime);
    }

    @Override
    public String getFormat(Date forDate) {
        if (forDate != null && forDate.getTime() != Long.MIN_VALUE)
            return (GMT.clearTime(new Date()).getTime() != GMT.clearTime(forDate).getTime() ? "yyyy-MM-dd " : "") + "HH:mm:ss.SSS";
        return "";
    }

    @Override
    public void setMenuModel(MenuModel menuModel) {
        this.menuModel = menuModel;
    }

    @Override
    public MenuModel getMenuModel() {
        return menuModel;
    }

    @Override
    public TBMonitor getMonitor() {
        return (TBMonitor) deltix.qsrv.hf.tickdb.http.AbstractHandler.TDB;
    }

}
