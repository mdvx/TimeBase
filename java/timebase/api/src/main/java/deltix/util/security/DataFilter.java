package deltix.util.security;

import deltix.util.lang.Disposable;

public interface DataFilter<T> extends Disposable {

    boolean accept(T obj);
}