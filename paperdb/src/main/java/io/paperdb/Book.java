package io.paperdb;

import android.content.Context;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Book {

    private static final int NUMBER_OF_THREAD = 10;

    private final Storage mStorage;

    private final Executor mExecutor;

    protected Book(Context context, String dbName) {
        mStorage = new DbStoragePlainFile(context.getApplicationContext(), dbName);
        mExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREAD);
    }

    /**
     * Destroys all data saved in Book.
     */
    public void destroy() {
        mStorage.destroy();
    }

    public void destroy(IPaperCallback<Void> callback) {
        mExecutor.execute(new PaperWorkerThread<Void>(this, callback) {
            @Override
            Void paperCall() {
                this.mBook.destroy();
                return null;
            }
        });
    }

    /**
     * Saves any types of POJOs or collections in Book storage.
     *
     * @param key   object key is used as part of object's file name
     * @param value object to save, must have no-arg constructor
     * @param <T>   object type
     * @return this Book instance
     */
    public <T> Book write(String key, T value) {
        if (value == null) {
            mStorage.deleteIfExists(key);
        } else {
            mStorage.insert(key, value);
        }
        return this;
    }

    public <T> void writeAsync(final String key, final T value, IPaperCallback<Void> callback) {
        mExecutor.execute(new PaperWorkerThread<Book>(this, callback) {
            @Override
            Book paperCall() {
                return this.mBook.write(key, value);
            }
        });
    }

    /**
     * Instantiates saved object using original object class (e.g. LinkedList). Support limited
     * backward and forward compatibility: removed fields are ignored, new fields have their
     * default values.
     * <p/>
     * All instantiated objects must have no-arg constructors.
     *
     * @param key object key to read
     * @return the saved object instance or null
     */
    public <T> T read(String key) {
        return read(key, null);
    }

    public <T> void readAsync(final String key, IPaperCallback<T> callback) {
        mExecutor.execute(new PaperWorkerThread<T>(this, callback) {
            @Override
            T paperCall() {
                return this.mBook.read(key);
            }
        });
    }

    /**
     * Instantiates saved object using original object class (e.g. LinkedList). Support limited
     * backward and forward compatibility: removed fields are ignored, new fields have their
     * default values.
     * <p/>
     * All instantiated objects must have no-arg constructors.
     *
     * @param key          object key to read
     * @param defaultValue will be returned if key doesn't exist
     * @return the saved object instance or null
     */
    public <T> T read(String key, T defaultValue) {
        T value = mStorage.select(key);
        return value == null ? defaultValue : value;
    }

    public <T> void readAsync(final String key, final T defaultValue, IPaperCallback<T> callback) {
        mExecutor.execute(new PaperWorkerThread<T>(this, callback) {
            @Override
            T paperCall() {
                return this.mBook.read(key, defaultValue);
            }
        });
    }

    /**
     * Check if an object with the given key is saved in Book storage.
     *
     * @param key object key
     * @return true if object with given key exists in Book storage, false otherwise
     */
    public boolean exist(String key) {
        return mStorage.exist(key);
    }

    public <T> void existAsync(final String key, IPaperCallback<T> callback) {
        mExecutor.execute(new PaperWorkerThread<Boolean>(this, callback) {
            @Override
            Boolean paperCall() {
                return this.mBook.exist(key);
            }
        });
    }

    /**
     * Delete saved object for given key if it is exist.
     *
     * @param key object key
     */
    public void delete(String key) {
        mStorage.deleteIfExists(key);
    }

    public <T> void deleteAsync(final String key, IPaperCallback<T> callback) {
        mExecutor.execute(new PaperWorkerThread<Void>(this, callback) {
            @Override
            Void paperCall() {
                this.mBook.delete(key);
                return null;
            }
        });
    }
}
