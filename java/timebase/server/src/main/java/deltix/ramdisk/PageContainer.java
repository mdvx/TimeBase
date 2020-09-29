package deltix.ramdisk;

import deltix.util.collections.QuickList;

final class PageContainer extends QuickList.Entry<PageContainer> {
    Page              checkedOutPage;
}