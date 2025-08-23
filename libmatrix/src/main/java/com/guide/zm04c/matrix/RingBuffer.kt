package com.guide.zm04c.matrix

class RingBuffer {

    private lateinit var byteArray: ByteArray

    private var mReadPositon = 0

    private var mUnReadLength = 0

    constructor(size: Int) {
        byteArray = ByteArray(size)
    }

    constructor(buffer: ByteArray) {
        byteArray = buffer
    }

    constructor(buffer: ByteArray, tail: Int, length: Int) {
        byteArray = buffer
        mReadPositon = tail
        mUnReadLength = length
    }

    fun write(buffer: ByteArray?, offset: Int, length: Int): Int {
        var head: Int
        var toEnd: Int
        var toWrite: Int
        synchronized(this) {
            head = (mReadPositon + mUnReadLength) % byteArray.size
            toEnd = byteArray.size - head
            toWrite = Math.min(length, byteArray.size - mUnReadLength)
        }
        if (toWrite > 0) {
            if (toWrite > toEnd) {
                System.arraycopy(buffer!!, offset, byteArray, head, toEnd)
                System.arraycopy(buffer!!, offset + toEnd, byteArray, 0, toWrite - toEnd)
            } else {
                System.arraycopy(buffer!!, offset, byteArray, head, toWrite)
            }

            synchronized(this) { mUnReadLength += toWrite }
        }
        return toWrite
    }

    fun read(buffer: ByteArray?, offset: Int, length: Int): Int {
        var toEnd: Int
        var toRead: Int
        synchronized(this) {
            toEnd = byteArray.size - mReadPositon
            toRead = Math.min(length, mUnReadLength)
        }
        if (toRead > toEnd) {
            System.arraycopy(byteArray, mReadPositon, buffer, offset, toEnd)
            System.arraycopy(byteArray, 0, buffer, offset + toEnd, toRead - toEnd)
        } else {
            System.arraycopy(byteArray, mReadPositon, buffer, offset, toRead)
        }

        synchronized(this) {
            mReadPositon = (mReadPositon + toRead) % byteArray.size
            mUnReadLength -= toRead
        }
        return toRead
    }

    fun moveForward(length: Int): Int {
        synchronized(this) {
            mReadPositon = (mReadPositon + length) % byteArray.size
            mUnReadLength -= length
        }
        return length
    }

    fun moveBack(length: Int): Int {
        synchronized(this) {
            if (mReadPositon > length) {
                mReadPositon -= length
            } else {
                mReadPositon = mReadPositon - length + byteArray.size
            }
            mUnReadLength += length
        }
        return length
    }

    fun getUnReadLength(): Int {
        return mUnReadLength
    }

    fun getMaxLength(): Int {
        return byteArray.size
    }

    fun getFreeSpace(): Int {
        return byteArray.size - mUnReadLength
    }

    fun getByteArray(): ByteArray? {
        return byteArray
    }

    fun getReadPositon(): Int {
        return mReadPositon
    }

    override fun toString(): String {
        return "RingBuffer(byteArray=${byteArray.contentToString()}, mReadPositon=$mReadPositon, mUnReadLength=$mUnReadLength)"
    }

}