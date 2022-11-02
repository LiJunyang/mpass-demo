package cn.hsbcsd.mpaastest.Rx.scheduler

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class NewThreadMainScheduler<T> private constructor() :
    BaseScheduler<T>(Schedulers.newThread(), AndroidSchedulers.mainThread())
