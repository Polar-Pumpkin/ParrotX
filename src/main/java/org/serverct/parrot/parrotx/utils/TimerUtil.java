package org.serverct.parrot.parrotx.utils;

import lombok.NonNull;
import org.serverct.parrot.parrotx.data.PID;

import java.util.*;

public class TimerUtil {
    // 时间间隔
    private static final long PERIOD_DAY = 24 * 60 * 60 * 1000;

    private static Timer TIMER;
    private static Map<PID, TimerTask> REGISTEREDTASK;

    static {
        TIMER = new Timer();
        REGISTEREDTASK = new HashMap<>();
    }

    public static TimerTask get(@NonNull PID pid) {
        return REGISTEREDTASK.getOrDefault(pid, null);
    }

    public static void cancel(@NonNull PID id) {
        TimerTask task = get(id);
        if (task != null) task.cancel();
    }

    public static boolean restart(@NonNull PID id, String refreshTime) {
        TimerTask task = get(id);
        cancel(id);
        if (task != null) return startTask(id, task, refreshTime);
        else return false;
    }

    public static boolean startTask(@NonNull PID id, @NonNull TimerTask task, String refreshTime) {
        try {
            Calendar calendar = Calendar.getInstance();

            if (refreshTime == null || refreshTime.equalsIgnoreCase("")) refreshTime = "8:00:00";
            String[] dataSet = refreshTime.split("[:]");
            int hour = Integer.parseInt(dataSet[0]);
            int minute = dataSet.length >= 2 ? Integer.parseInt(dataSet[1]) : 0;
            int second = dataSet.length >= 3 ? Integer.parseInt(dataSet[2]) : 0;

            // 定制何时执行方法

            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, second);

            Date date = calendar.getTime(); // 第一次执行定时任务的时间

            // 如果第一次执行定时任务的时间 小于 当前的时间 (Date 08:00, Current 11:00)
            // 此时要在 第一次执行定时任务的时间 加一天，以便此任务在下个时间点执行。如果不加一天，任务会立即执行。
            if (date.before(new Date())) {
                date = addDay(date, 1);
            }

            // 安排指定的任务在指定的时间开始进行重复的固定延迟执行。
            TIMER.schedule(task, date, PERIOD_DAY);
            REGISTEREDTASK.put(id, task);
            id.getPlugin().lang.logAction(I18n.REGISTER, "定时刷新任务/每天 " + refreshTime);

            return true;
        } catch (Throwable e) {
            id.getPlugin().lang.logError(I18n.REGISTER, "定时刷新任务", e, null);
            return false;
        }
    }

    // 增加或减少天数
    public static Date addDay(Date date, int amount) {
        Calendar startDT = Calendar.getInstance();
        startDT.setTime(date);
        startDT.add(Calendar.DAY_OF_MONTH, amount);
        return startDT.getTime();
    }
}
