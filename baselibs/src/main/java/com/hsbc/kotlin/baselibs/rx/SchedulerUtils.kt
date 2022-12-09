package cert.hsbcsd.mpaastest.Rx

import cert.hsbcsd.mpaastest.Rx.scheduler.IoMainScheduler

object SchedulerUtils {

    fun <T> ioToMain(): IoMainScheduler<T> = IoMainScheduler()

}