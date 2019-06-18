/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.store;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.InternalLogger;
import org.apache.rocketmq.logging.InternalLoggerFactory;
import org.apache.rocketmq.store.config.MessageStoreConfig;
import org.apache.rocketmq.store.util.LibC;
import sun.nio.ch.DirectBuffer;

public class TransientStorePool {
    private static final InternalLogger log = InternalLoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);
//池大小 默认5
    private final int poolSize;
    //文件大小 默认1G
    private final int fileSize;
    //可用的buffre 双端队列
    private final Deque<ByteBuffer> availableBuffers;
    //配置
    private final MessageStoreConfig storeConfig;

    public TransientStorePool(final MessageStoreConfig storeConfig) {
        this.storeConfig = storeConfig;
        this.poolSize = storeConfig.getTransientStorePoolSize();
        this.fileSize = storeConfig.getMapedFileSizeCommitLog();
        this.availableBuffers = new ConcurrentLinkedDeque<>();
    }

    /**
     * It's a heavy init method.
     */
    //这里需要分配5G内存 并且不可交换 所以只能初始化一次
    //因为分配的空间太大 这也可能是没有默认开启池化技术的原因
    public void init() {
        for (int i = 0; i < poolSize; i++) {
            //堆外内存1G
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(fileSize);
            //把当前地址锁住 避免其被交换到SWAP分区 导致写或者读的时候发生缺页异常 影响效率
            final long address = ((DirectBuffer) byteBuffer).address();
            Pointer pointer = new Pointer(address);
            LibC.INSTANCE.mlock(pointer, new NativeLong(fileSize));
           //分配的内存加入到队列中
            availableBuffers.offer(byteBuffer);
        }
    }

    //把分配的内存解锁 可以分配到Swap分区
    public void destroy() {
        for (ByteBuffer byteBuffer : availableBuffers) {
            final long address = ((DirectBuffer) byteBuffer).address();
            Pointer pointer = new Pointer(address);
            LibC.INSTANCE.munlock(pointer, new NativeLong(fileSize));
        }
    }

    //归还使用的buffer
    public void returnBuffer(ByteBuffer byteBuffer) {
        //当前buffer position和limit重置为初始值
        byteBuffer.position(0);
        byteBuffer.limit(fileSize);
        //加入头部
        this.availableBuffers.offerFirst(byteBuffer);
    }

    //获取一个可用的buffer
    public ByteBuffer borrowBuffer() {
        //获取第一个 也就是最后归还的一个buffer使用
        ByteBuffer buffer = availableBuffers.pollFirst();
        //如果可用的内存块数木小于80% warn
        if (availableBuffers.size() < poolSize * 0.4) {
            log.warn("TransientStorePool only remain {} sheets.", availableBuffers.size());
        }
        return buffer;
    }

    //剩余的可用内存块数目
    public int remainBufferNumbs() {
        if (storeConfig.isTransientStorePoolEnable()) {
            return availableBuffers.size();
        }
        return Integer.MAX_VALUE;
    }
}
