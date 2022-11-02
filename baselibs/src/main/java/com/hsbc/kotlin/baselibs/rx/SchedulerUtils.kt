package cn.hsbcsd.mpaastest.Rx

import cn.hsbcsd.mpaastest.Rx.scheduler.IoMainScheduler

object SchedulerUtils {

    fun <T> ioToMain(): IoMainScheduler<T> = IoMainScheduler()

}