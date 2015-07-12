package io.paperdb;

/**
 * Created by Libra on 2015-07-12.
 */
public interface IPaperCallback<V> {
    void onSuccess(V result);
    void onFailure(Throwable t);
}
