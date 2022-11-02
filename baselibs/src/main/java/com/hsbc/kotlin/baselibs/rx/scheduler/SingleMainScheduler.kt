package cn.hsbcsd.mpaastest.Rx.scheduler

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class SingleMainScheduler<T> private constructor() :
    BaseScheduler<T>(Schedulers.single(), AndroidSchedulers.mainThread())
