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

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.compress.{CodecPool, DefaultCodec}
import org.apache.hadoop.mapreduce._
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.apache.hadoop.util.ReflectionUtils
import org.tensorflow.example.Example

object TFRecordsOutputFormat {
  val extension = ".tfr"
}

class TFRecordsOutputFormat extends FileOutputFormat[Void, Example] {
  override def getRecordWriter(context: TaskAttemptContext): RecordWriter[Void, Example] = {
    val conf = context.getConfiguration
    val (extension, codec) = getCodec(conf, context)
    val filePath = getDefaultWorkFile(context, extension)
    val fs = filePath.getFileSystem(conf)
    val file = fs.create(filePath)
    new TFRecordsWriter(codec(file))
  }

  private def getCodec(
      conf: Configuration,
      context: TaskAttemptContext): (String, OutputStream => OutputStream) = {
    import TFRecordsOutputFormat.extension

    if (FileOutputFormat.getCompressOutput(context)) {
      val codecClass = FileOutputFormat.getOutputCompressorClass(context, classOf[DefaultCodec])
      val codec = ReflectionUtils.newInstance(codecClass, conf)

      def wrap(stream: OutputStream): OutputStream = {
        val compressor = CodecPool.getCompressor(codec)
        val wrapped = codec.createOutputStream(stream, compressor)
        new OutputStreamOnClose(wrapped, {
          case () =>
            try {
              // flush the deflate stream before closing the file stream
              wrapped.close()
              require(compressor.finished())
              stream.close()
            } finally {
              CodecPool.returnCompressor(compressor)
            }
        })
      }

      (extension + codec.getDefaultExtension, wrap)
    } else {
      (extension, identity[OutputStream])
    }
  }
}
