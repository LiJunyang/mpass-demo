package cert.hsbcsd.mpaastest.Rx.scheduler

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class ComputationMainScheduler<T> private constructor() :
    BaseScheduler<T>(Schedulers.computation(), AndroidSchedulers.mainThread())
