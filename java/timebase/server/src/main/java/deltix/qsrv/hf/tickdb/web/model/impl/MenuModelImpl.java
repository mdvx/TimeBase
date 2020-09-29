package deltix.qsrv.hf.tickdb.web.model.impl;

import deltix.qsrv.hf.tickdb.web.model.pub.MenuModel;
import deltix.qsrv.hf.tickdb.web.model.pub.MenuSection;

/**
 *
 */
public class MenuModelImpl implements MenuModel {

    final static MenuSection[] MENU_SECTIONS = MenuSection.values();

    private MenuSection currentMenuSection;

    public MenuModelImpl(MenuSection currentMenuSection) {
        this.currentMenuSection = currentMenuSection;
    }

    public MenuSection[] getMenuSections() {
        return MENU_SECTIONS;
    }

    public MenuSection getCurrentMenuSection() {
        return currentMenuSection;
    }

}
