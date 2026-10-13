// ============================================================================
// JAVA MULTITHREADING & CONCURRENCY — Complete Guide with Examples
// ============================================================================
// Covers: Thread creation, Runnable, Callable, ExecutorService, synchronized,
// volatile, ReentrantLock, ReadWriteLock, CountDownLatch, CyclicBarrier,
// Semaphore, CompletableFuture, deadlock, AtomicInteger, thread states
// ============================================================================

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;
import java.util.*;

public class MultithreadingDemo {

    // ========================================================================
    // 1. THREAD CREATION — 3 Ways
    // ========================================================================

    // Way 1: Extend Thread class (not recommended — wastes single inheritance)
    static class MyThread extends Thread {
        @Override
        public void run() {
            System.out.println("  [Thread] " + getName() + " running");
        }
    }

    // Way 2: Implement Runnable (preferred — allows extending another class)
    static class MyRunnable implements Runnable {
        @Override
        public void run() {
            System.out.println("  [Runnable] " + Thread.currentThread().getName() + " running");
        }
    }

    // Way 3: Implement Callable<V> (can return a value + throw checked exceptions)
    static class MyCallable implements Callable<String> {
        @Override
        public String call() throws Exception {
            Thread.sleep(100);
            return "Result from " + Thread.currentThread().getName();
        }
    }

    static void threadCreation() throws Exception {
        System.out.println("=== THREAD CREATION ===");

        // Way 1: Thread
        Thread t1 = new MyThread();
        t1.setName("Thread-1");
        t1.start();  // start() creates a new thread. NEVER call run() directly!

        // Way 2: Runnable
        Thread t2 = new Thread(new MyRunnable(), "Thread-2");
        t2.start();

        // Way 2b: Lambda (since Runnable is a @FunctionalInterface)
        Thread t3 = new Thread(() -> {
            System.out.println("  [Lambda] " + Thread.currentThread().getName() + " running");
        }, "Thread-3");
        t3.start();

        // Way 3: Callable + Future
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new MyCallable());
        System.out.println("  [Callable] " + future.get());  // Blocks until result ready
        executor.shutdown();

        // Wait for threads to finish
        t1.join();
        t2.join();
        t3.join();

        // INTERVIEW Q: start() vs run()?
        // start() → Creates a NEW OS thread, which then calls run()
        // run()   → Executes in the CURRENT thread (no new thread created!)
        // Always use start(). Calling run() directly defeats the purpose.

        // INTERVIEW Q: Runnable vs Callable?
        // Runnable: void run(), no return value, can't throw checked exceptions
        // Callable: V call(), returns a value, can throw checked exceptions
    }

    // ========================================================================
    // 2. THREAD LIFECYCLE / STATES
    // ========================================================================
    //
    //  NEW → RUNNABLE → RUNNING → TERMINATED
    //                ↕
    //    BLOCKED / WAITING / TIMED_WAITING
    //
    //  NEW:            Thread created, not yet started
    //  RUNNABLE:       start() called, waiting for CPU
    //  RUNNING:        Executing run() (not an official Java state — part of RUNNABLE)
    //  BLOCKED:        Waiting for a monitor lock (synchronized)
    //  WAITING:        wait(), join(), park() — waits indefinitely
    //  TIMED_WAITING:  sleep(ms), wait(ms), join(ms) — waits with timeout
    //  TERMINATED:     run() completed or exception thrown

    // ========================================================================
    // 3. SYNCHRONIZED — Mutual Exclusion
    // ========================================================================

    static class Counter {
        private int count = 0;

        // synchronized method — only one thread can execute at a time
        // Lock is on the object instance (this)
        public synchronized void increment() {
            count++;  // Not atomic! Read → Modify → Write
        }

        // synchronized block — more granular control
        public void incrementBlock() {
            // Only this block is synchronized, not the whole method
            synchronized (this) {
                count++;
            }
        }

        // Static synchronized — lock is on the Class object
        public static synchronized void staticMethod() {
            // Lock on Counter.class, not on any instance
        }

        public int getCount() { return count; }
    }

    static void synchronizedDemo() throws InterruptedException {
        System.out.println("\n=== SYNCHRONIZED ===");

        Counter counter = new Counter();
        int numThreads = 1000;

        // Without synchronization, this would give wrong results due to race condition
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            Thread t = new Thread(counter::increment);
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) t.join();

        System.out.println("Counter (synchronized): " + counter.getCount());  // Always 1000

        // INTERVIEW Q: What is a race condition?
        // A: When multiple threads access shared data concurrently and the result
        //    depends on the thread scheduling order. count++ is actually 3 operations:
        //    read count → increment → write count. Two threads could read the same
        //    value and both write the same incremented value → lost update.
    }

    // ========================================================================
    // 4. VOLATILE — Visibility guarantee
    // ========================================================================

    static volatile boolean running = true;  // volatile ensures visibility across threads

    static void volatileDemo() throws InterruptedException {
        System.out.println("\n=== VOLATILE ===");

        Thread worker = new Thread(() -> {
            int count = 0;
            while (running) {  // Without volatile, thread might cache 'running' and never stop!
                count++;
            }
            System.out.println("  Worker stopped after " + count + " iterations");
        });
        worker.start();

        Thread.sleep(10);
        running = false;  // Main thread changes the flag — worker sees it due to volatile
        worker.join();

        // INTERVIEW Q: volatile vs synchronized?
        // volatile: Guarantees VISIBILITY (all threads see latest value).
        //           Does NOT guarantee ATOMICITY.
        //           No locking, no blocking, lightweight.
        //           Use for flags, status variables.
        //
        // synchronized: Guarantees both VISIBILITY and ATOMICITY.
        //               Uses locking, can cause blocking/deadlock.
        //               Use for compound actions (check-then-act, read-modify-write).

        // INTERVIEW Q: Does volatile make count++ thread-safe?
        // A: NO! volatile only ensures the latest value is read.
        //    count++ is still 3 operations (read-modify-write), which is not atomic.
        //    Use AtomicInteger or synchronized for thread-safe increment.
    }

    // ========================================================================
    // 5. REENTRANT LOCK — More flexible than synchronized
    // ========================================================================

    static class ReentrantCounter {
        private int count = 0;
        private final ReentrantLock lock = new ReentrantLock();

        public void increment() {
            lock.lock();  // Acquire lock
            try {
                count++;
            } finally {
                lock.unlock();  // ALWAYS unlock in finally!
            }
        }

        // tryLock — Non-blocking attempt to acquire lock
        public boolean tryIncrement() {
            if (lock.tryLock()) {
                try {
                    count++;
                    return true;
                } finally {
                    lock.unlock();
                }
            }
            return false;  // Lock not available — skip instead of blocking
        }

        public int getCount() { return count; }
    }

    // ReadWriteLock — Multiple readers OR one writer
    static class CachedData {
        private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        private String data = "initial";

        public String read() {
            rwLock.readLock().lock();  // Multiple threads can read simultaneously
            try {
                return data;
            } finally {
                rwLock.readLock().unlock();
            }
        }

        public void write(String newData) {
            rwLock.writeLock().lock();  // Exclusive — blocks all readers and writers
            try {
                data = newData;
            } finally {
                rwLock.writeLock().unlock();
            }
        }
    }

    // INTERVIEW Q: ReentrantLock vs synchronized?
    // ┌────────────────────────┬─────────────────────┬────────────────────┐
    // │ Feature                │ synchronized        │ ReentrantLock      │
    // ├────────────────────────┼─────────────────────┼────────────────────┤
    // │ Lock/unlock            │ Implicit (auto)     │ Explicit (manual)  │
    // │ tryLock()              │ ❌ Not possible     │ ✅ Yes             │
    // │ Fairness               │ Not guaranteed      │ Configurable       │
    // │ Interruptible waiting  │ ❌ No               │ ✅ lockInterrupt.. │
    // │ Multiple conditions    │ One (wait/notify)   │ Multiple newCond.. │
    // │ read/write separation  │ ❌ No               │ ✅ ReadWriteLock   │
    // │ Risk of forgetting     │ ❌ Can't forget     │ ⚠️ Must unlock!   │
    // └────────────────────────┴─────────────────────┴────────────────────┘

    // ========================================================================
    // 6. ATOMIC CLASSES — Lock-free thread safety
    // ========================================================================

    static void atomicDemo() throws InterruptedException {
        System.out.println("\n=== ATOMIC CLASSES ===");

        AtomicInteger atomicCount = new AtomicInteger(0);

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Thread t = new Thread(() -> atomicCount.incrementAndGet());
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) t.join();

        System.out.println("AtomicInteger count: " + atomicCount.get());  // Always 1000

        // Key AtomicInteger methods:
        // get(), set(val), incrementAndGet(), decrementAndGet()
        // getAndIncrement(), getAndDecrement()
        // compareAndSet(expect, update) — CAS operation
        // addAndGet(delta), getAndAdd(delta)

        // INTERVIEW Q: How do Atomic classes work?
        // A: They use CAS (Compare-And-Swap) CPU instruction. No locks needed.
        //    compareAndSet(expected, new): atomically checks if value == expected,
        //    if yes → set to new. If no → retry. This is a hardware-level atomic op.
    }

    // ========================================================================
    // 7. EXECUTOR SERVICE & THREAD POOLS
    // ========================================================================

    static void executorServiceDemo() throws Exception {
        System.out.println("\n=== EXECUTOR SERVICE ===");

        // Fixed thread pool — exactly N threads
        ExecutorService fixedPool = Executors.newFixedThreadPool(3);

        // Submit Runnables
        for (int i = 0; i < 5; i++) {
            final int taskNum = i;
            fixedPool.submit(() -> {
                System.out.println("  Task " + taskNum + " on " + Thread.currentThread().getName());
            });
        }

        // Submit Callable — returns Future
        Future<Integer> result = fixedPool.submit(() -> {
            Thread.sleep(100);
            return 42;
        });
        System.out.println("  Future result: " + result.get());  // Blocks until done

        fixedPool.shutdown();  // No new tasks; finishes existing tasks
        fixedPool.awaitTermination(5, TimeUnit.SECONDS);

        // Other pool types:
        // Executors.newSingleThreadExecutor()  — single thread, sequential execution
        // Executors.newCachedThreadPool()       — creates threads on demand, reuses idle
        // Executors.newScheduledThreadPool(n)   — for delayed/periodic tasks

        // INTERVIEW Q: Why use thread pools?
        // A: Creating threads is expensive (OS-level resource). Thread pools:
        //    1. Reuse threads — avoid creation overhead
        //    2. Control concurrency — limit max threads
        //    3. Manage lifecycle — orderly shutdown

        // INTERVIEW Q: shutdown() vs shutdownNow()?
        // shutdown()    → No new tasks accepted. Finishes queued tasks. Graceful.
        // shutdownNow() → Attempts to stop all tasks. Returns unexecuted tasks.
    }

    // ========================================================================
    // 8. COMPLETABLE FUTURE — Async programming (Java 8+)
    // ========================================================================

    static void completableFutureDemo() throws Exception {
        System.out.println("\n=== COMPLETABLE FUTURE ===");

        // Basic async task
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            // Runs on ForkJoinPool.commonPool() by default
            return "Hello";
        });

        // Chaining transformations (like .then() in JavaScript!)
        CompletableFuture<String> future2 = future1
            .thenApply(s -> s + " World")           // Transform result
            .thenApply(String::toUpperCase);          // Chain another transform

        System.out.println("  Result: " + future2.get());  // "HELLO WORLD"

        // thenCompose — flatMap equivalent (when transformation returns another CF)
        CompletableFuture<String> composed = CompletableFuture
            .supplyAsync(() -> "user-123")
            .thenCompose(userId -> CompletableFuture.supplyAsync(() -> 
                "Profile of " + userId  // Simulating DB lookup
            ));
        System.out.println("  Composed: " + composed.get());

        // thenCombine — combine two independent futures
        CompletableFuture<String> nameF = CompletableFuture.supplyAsync(() -> "Rubbal");
        CompletableFuture<Integer> ageF = CompletableFuture.supplyAsync(() -> 25);
        CompletableFuture<String> combined = nameF.thenCombine(ageF, 
            (name, age) -> name + " is " + age);
        System.out.println("  Combined: " + combined.get());

        // allOf — wait for all futures
        CompletableFuture<Void> all = CompletableFuture.allOf(nameF, ageF);
        all.get();  // Blocks until both complete
        System.out.println("  All completed!");

        // Exception handling
        CompletableFuture<String> withError = CompletableFuture
            .supplyAsync(() -> {
                if (true) throw new RuntimeException("Oops!");
                return "OK";
            })
            .exceptionally(ex -> "Recovered: " + ex.getMessage());
        System.out.println("  Error handled: " + withError.get());

        // handle() — processes both result AND exception
        CompletableFuture<String> handled = CompletableFuture
            .supplyAsync(() -> "Success")
            .handle((result, ex) -> {
                if (ex != null) return "Error: " + ex.getMessage();
                return "Got: " + result;
            });
        System.out.println("  Handled: " + handled.get());

        // INTERVIEW Q: thenApply vs thenCompose?
        // thenApply:   T → U          (map — simple transformation)
        // thenCompose: T → CF<U>      (flatMap — when transform returns another future)
        // Same as map vs flatMap in Streams!
    }

    // ========================================================================
    // 9. SYNCHRONIZATION AIDS
    // ========================================================================

    static void synchronizationAids() throws Exception {
        System.out.println("\n=== SYNCHRONIZATION AIDS ===");

        // CountDownLatch — Wait for N events
        // One-shot: once count reaches zero, can't be reset
        CountDownLatch latch = new CountDownLatch(3);

        for (int i = 0; i < 3; i++) {
            final int id = i;
            new Thread(() -> {
                System.out.println("  Worker " + id + " done");
                latch.countDown();  // Decrement count
            }).start();
        }
        latch.await();  // Main thread waits until count = 0
        System.out.println("  All workers completed (latch)!");

        // CyclicBarrier — All threads wait for each other at a barrier point
        // Reusable: can be used multiple times
        CyclicBarrier barrier = new CyclicBarrier(3, () -> {
            System.out.println("  === Barrier reached! All 3 threads synchronized ===");
        });

        for (int i = 0; i < 3; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    System.out.println("  Thread " + id + " waiting at barrier");
                    barrier.await();  // Wait until all 3 threads arrive
                    System.out.println("  Thread " + id + " passed barrier");
                } catch (Exception e) { e.printStackTrace(); }
            }).start();
        }
        Thread.sleep(500);

        // Semaphore — Control access to N resources
        Semaphore semaphore = new Semaphore(2);  // Max 2 concurrent accesses

        for (int i = 0; i < 4; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    semaphore.acquire();  // Blocks if 2 permits already taken
                    System.out.println("  Semaphore: Thread " + id + " accessing resource");
                    Thread.sleep(100);
                    semaphore.release();  // Release permit
                } catch (InterruptedException e) { e.printStackTrace(); }
            }).start();
        }
        Thread.sleep(500);
    }

    // ========================================================================
    // 10. DEADLOCK — Classic interview topic
    // ========================================================================

    static final Object lockA = new Object();
    static final Object lockB = new Object();

    static void deadlockDemo() {
        System.out.println("\n=== DEADLOCK (prevented) ===");

        // DEADLOCK scenario (DO NOT DO THIS):
        // Thread 1: locks A, then tries to lock B
        // Thread 2: locks B, then tries to lock A
        // Both wait forever!

        // PREVENTION — Always acquire locks in the SAME ORDER
        Thread t1 = new Thread(() -> {
            synchronized (lockA) {      // Always lock A first
                synchronized (lockB) {  // Then lock B
                    System.out.println("  Thread 1: Got both locks");
                }
            }
        });

        Thread t2 = new Thread(() -> {
            synchronized (lockA) {      // Same order! A first
                synchronized (lockB) {  // Then B
                    System.out.println("  Thread 2: Got both locks");
                }
            }
        });

        t1.start();
        t2.start();

        // INTERVIEW Q: How to prevent deadlock?
        // 1. Lock ordering — always acquire locks in the same order
        // 2. Lock timeout — use tryLock() with timeout
        // 3. Avoid nested locks — minimize lock scope
        // 4. Use higher-level concurrency utilities (java.util.concurrent)
        
        // INTERVIEW Q: Deadlock vs Livelock vs Starvation?
        // Deadlock:    Two threads waiting for each other's locks — both stuck forever
        // Livelock:    Threads keep changing state in response to each other but make no progress
        // Starvation:  A thread can never get CPU time / lock because others keep getting priority
    }

    // ========================================================================
    // 11. WAIT / NOTIFY — Inter-thread communication
    // ========================================================================

    static class ProducerConsumer {
        private final Queue<Integer> queue = new LinkedList<>();
        private final int capacity = 5;

        public synchronized void produce(int item) throws InterruptedException {
            while (queue.size() == capacity) {
                wait();  // Release lock and wait until consumer takes something
            }
            queue.add(item);
            System.out.println("  Produced: " + item + " | Queue: " + queue);
            notifyAll();  // Wake up waiting consumers
        }

        public synchronized int consume() throws InterruptedException {
            while (queue.isEmpty()) {
                wait();  // Release lock and wait until producer adds something
            }
            int item = queue.poll();
            System.out.println("  Consumed: " + item + " | Queue: " + queue);
            notifyAll();  // Wake up waiting producers
            return item;
        }
    }

    // INTERVIEW Q: wait() vs sleep()?
    // wait():  Releases the lock. Must be called inside synchronized. Woken by notify().
    // sleep(): Does NOT release the lock. Can be called anywhere. Wakes after timeout.
    //
    // INTERVIEW Q: Why use while instead of if before wait()?
    // A: Spurious wakeups! The thread can wake up without being notified.
    //    Using while rechecks the condition. This is a critical best practice.
    //
    // INTERVIEW Q: notify() vs notifyAll()?
    // notify():    Wakes ONE random waiting thread. Risk of lost signals.
    // notifyAll(): Wakes ALL waiting threads. Safer — recommended.

    // ========================================================================
    // MAIN
    // ========================================================================
    public static void main(String[] args) throws Exception {
        threadCreation();
        synchronizedDemo();
        
        running = true;  // Reset for volatile demo
        volatileDemo();
        
        atomicDemo();
        executorServiceDemo();
        completableFutureDemo();
        synchronizationAids();
        deadlockDemo();

        Thread.sleep(500);  // Wait for async demos to finish
        System.out.println("\n✅ All Multithreading concepts demonstrated!");
    }
}
