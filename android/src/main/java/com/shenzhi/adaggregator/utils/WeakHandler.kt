package com.shenzhigromore.utils

import android.os.Handler
import android.os.Looper
import android.os.Message
import java.lang.ref.WeakReference

class WeakHandler : Handler {
  interface IHandler {
    fun handleMsg(msg: Message?)
  }

  private val mRef: WeakReference<IHandler>

  constructor(handler: IHandler) : super(Looper.getMainLooper()) {
    mRef = WeakReference(handler)
  }

  constructor(looper: Looper, handler: IHandler) : super(looper) {
    mRef = WeakReference(handler)
  }

  @Suppress("unused")
  override fun handleMessage(msg: Message) {
    mRef.get()?.handleMsg(msg)
  }
}
