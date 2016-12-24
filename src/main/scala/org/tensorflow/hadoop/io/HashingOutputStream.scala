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

import com.google.common.hash.{HashCode, Hasher}

private final class HashingOutputStream(
    outputStream: OutputStream,
    hasher: Hasher)
  extends OutputStream {

  override def write(b: Int): Unit = {
    hasher.putByte(b.toByte)
    outputStream.write(b)
  }

  override def write(b: Array[Byte]): Unit = {
    hasher.putBytes(b)
    outputStream.write(b)
  }

  override def write(b: Array[Byte], off: Int, len: Int): Unit = {
    hasher.putBytes(b, off, len)
    outputStream.write(b, off, len)
  }

  override def flush(): Unit = {
    outputStream.flush()
  }

  override def close(): Unit = {
    try {
      flush()
    } finally {
      outputStream.close()
    }
  }

  def hash(): HashCode = {
    hasher.hash()
  }
}
