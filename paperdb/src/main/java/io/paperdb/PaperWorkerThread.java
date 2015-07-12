package io.paperdb;

import java.util.concurrent.ThreadPoolExecutor;

public abstract class PaperWorkerThread<T> implements Runnable {

    private IPaperCallback mCallback;

    Book mBook;

    public PaperWorkerThread(Book book, IPaperCallback callback) {
        this.mCallback = callback;
        this.mBook = book;
    }

    @Override
    public void run() {
        try {
            T result = paperCall();
            mCallback.onSuccess(result);
        } catch (Exception e) {
            mCallback.onFailure(e);
        }
    }

    abstract T paperCall();
}
