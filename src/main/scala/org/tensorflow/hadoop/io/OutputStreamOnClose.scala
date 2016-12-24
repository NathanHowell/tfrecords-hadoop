/* Copyright 2016 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package org.tensorflow.hadoop.io

import java.io.OutputStream
import java.util.concurrent.atomic.AtomicReference

private final class OutputStreamOnClose(
    wrapped: OutputStream,
    onClose: () => Unit)
  extends OutputStream {
  private val onlyOnce = new AtomicReference(onClose)

  override def write(b: Int): Unit = {
    wrapped.write(b)
  }

  override def write(b: Array[Byte]): Unit = {
    wrapped.write(b)
  }

  override def write(b: Array[Byte], off: Int, len: Int): Unit = {
    wrapped.write(b, off, len)
  }

  override def flush(): Unit = {
    wrapped.flush()
  }

  override def close(): Unit = {
    try {
      onlyOnce.getAndSet(() => ())()
    } finally {
      wrapped.close()
    }
  }
}
