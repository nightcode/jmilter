package org.nightcode.milter.util;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

/**
 * Executor's utility methods.
 */
public enum ExecutorUtils {
  ;

  private static final class NamedThreadFactory implements ThreadFactory {
    private final ThreadGroup group;
    private final String namePrefix;
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    private NamedThreadFactory(String prefix) {
      SecurityManager securityManager = System.getSecurityManager();
      group = (securityManager != null) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
      namePrefix = prefix + "-thread-";
    }

    @Override public Thread newThread(Runnable r) {
      Thread thread = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
      if (thread.isDaemon()) {
        thread.setDaemon(false);
      }
      if (thread.getPriority() != Thread.NORM_PRIORITY) {
        thread.setPriority(Thread.NORM_PRIORITY);
      }
      return thread;
    }
  }

  /**
   * Creates named ThreadFactory.
   *
   * @param prefix ThreadFactory's name prefix
   * @return named ThreadFactory
   */
  public static ThreadFactory namedThreadFactory(String prefix) {
    return new NamedThreadFactory(prefix);
  }

  /**
   * Trying gracefully shutdown Executor service.
   *
   * @param executorService ExecutorService
   */
  public static boolean shutdown(ExecutorService executorService) {
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
        List<Runnable> neverCommencedExecution = executorService.shutdownNow();
        for (Runnable r : neverCommencedExecution) {
          Log.warn().log(ExecutorUtils.class, () -> format("[%s]: shutdown now %s", executorService, r));
        }
        executorService.awaitTermination(30, TimeUnit.SECONDS);
      }
    } catch (InterruptedException ex) {
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
    return executorService.isTerminated();
  }
}
