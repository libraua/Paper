package io.paperdb.multithread;

import android.test.AndroidTestCase;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.paperdb.IPaperCallback;
import io.paperdb.Paper;
import io.paperdb.PaperDbException;
import io.paperdb.testdata.Person;
import io.paperdb.testdata.TestDataGenerator;

/**
 * Tests read/write into Paper data from multiple threads
 */
public class MultiThreadTest extends AndroidTestCase {

    private class CallbackForTest<T> implements IPaperCallback<T> {

        final List<Person> mTestData;

        public CallbackForTest(List<Person> testData) {
            this.mTestData = testData;
        }

        public CallbackForTest() {
            this.mTestData = null;
        }

        @Override
        public void onSuccess(T result) {
            if (result != null && result instanceof List) {
                for (Person t : (List<Person>)result) {
                    if (!mTestData.contains(t)) throw new PaperDbException("Failed to pass");
                }
            }
        }

        @Override
        public void onFailure(Throwable t) {
           throw new RuntimeException(t);
        }
    }

    public void testAsyncCalls() throws Exception {
        CallbackForTest cbft = null;
        for (int i = 2; i <= 1000; i++) {
            if (i % 2 == 0) {
                int size = new Random().nextInt(200) + 1;
                final List<Person> inserted100 = TestDataGenerator.genPersonList(size);
                cbft = new CallbackForTest(inserted100);
                Paper.book().writeAsync(String.valueOf(i / 2),
                        inserted100, new CallbackForTest(inserted100));
            } else {
                Paper.book().readAsync(String.valueOf(i / 2), cbft);
            }
        }
    }

    public void testMultiThreadAccess() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Callable<Object>> todo = new LinkedList<>();

        for (int i = 0; i <= 1000; i++) {
            Runnable task;
            if (i % 2 == 0) {
                task = getInsertRunnable();
            } else {
                task = getSelectRunnable();
            }
            todo.add(Executors.callable(task));
        }
        List<Future<Object>> futures = executor.invokeAll(todo);
        for (Future<Object> future : futures) {
            future.get();
        }
    }

    private Runnable getInsertRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                int size = new Random().nextInt(200);
                final List<Person> inserted100 = TestDataGenerator.genPersonList(size);
                Paper.book().write("persons", inserted100);
            }
        };
    }

    private Runnable getSelectRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                Paper.book().read("persons");
            }
        };
    }
}
