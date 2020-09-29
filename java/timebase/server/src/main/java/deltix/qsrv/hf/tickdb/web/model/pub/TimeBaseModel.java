package deltix.qsrv.hf.tickdb.web.model.pub;

import deltix.qsrv.hf.tickdb.pub.mon.TBMonitor;

import java.util.Date;

/**
 *
 */
public interface TimeBaseModel {

    String getVersion();

    String getTitle();

    Date getCurrentDate();

    String getFormat(Date forDate);

    void            setMenuModel(MenuModel menuModel);

    MenuModel getMenuModel();

    TBMonitor       getMonitor();
}
