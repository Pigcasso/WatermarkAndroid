package io.zhuliang.watermark;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <pre>
 *     author : ZhuLiang
 *     time   : 2019/11/18
 *     desc   :
 *     version: 1.0
 * </pre>
 */

public class WatermarkThreadTool {

    private static int poolCount = Runtime.getRuntime().availableProcessors();

    private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(poolCount);

    public static void execute(Runnable runnable) {
        fixedThreadPool.execute(runnable);
    }
}
