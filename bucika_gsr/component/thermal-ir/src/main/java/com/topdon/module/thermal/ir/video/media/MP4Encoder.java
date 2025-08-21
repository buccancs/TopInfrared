package com.topdon.module.thermal.ir.video.media;

import static android.media.MediaCodec.CONFIGURE_FLAG_ENCODE;
import static android.media.MediaCodec.INFO_OUTPUT_FORMAT_CHANGED;
import static android.media.MediaCodec.INFO_TRY_AGAIN_LATER;
import static android.media.MediaCodecInfo.CodecProfileLevel.MPEG2ProfileHigh;
import static android.media.MediaFormat.KEY_AAC_PROFILE;
import static android.media.MediaFormat.KEY_BIT_RATE;
import static android.media.MediaFormat.KEY_COLOR_FORMAT;
import static android.media.MediaFormat.KEY_FRAME_RATE;
import static android.media.MediaFormat.KEY_I_FRAME_INTERVAL;
import static android.media.MediaFormat.MIMETYPE_AUDIO_AAC;
import static android.media.MediaFormat.MIMETYPE_VIDEO_AVC;
import static android.media.MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4;

import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MP4Encoder extends Encoder {

    private static final int BIT_RATE = 600000;//码率2000000
    private static final int FRAME_RATE = 20;//帧数
    private static final int I_FRAME_INTERVAL = 5;
    private static final long ONE_SEC = 1000000;
    private static final String TAG = MP4Encoder.class.getSimpleName();
    private static final int TIMEOUT_US = 10000;
    private int addedFrameCount;
    //TODO 设置4096在高版本会出现崩溃 java.nio.BufferOverflowException
    //音频文件不需要处理
//    private byte[] audioArray = new byte[2048];
    private MediaCodec audioCodec;
    private int audioTrackIndex;
    private BufferInfo bufferInfo;
    private int encodedFrameCount;
    private boolean isMuxerStarted = false;
    private boolean isStarted = false;
    private MediaMuxer mediaMuxer;
    private int trackCount = 0;
    private MediaCodec videoCodec;
    private int videoTrackIndex;

    @Override
    protected void onInit() {
    }

    @Override
    protected void onStart() {
        isStarted = true;
        addedFrameCount = 0;
        encodedFrameCount = 0;
        int width = getWidth();
        int height = getHeight();
        try {
            bufferInfo = new BufferInfo();
            videoCodec = MediaCodec.createEncoderByType(MIMETYPE_VIDEO_AVC);
            MediaFormat videoFormat = MediaFormat.createVideoFormat(MIMETYPE_VIDEO_AVC, width, height);
            videoFormat.setInteger(KEY_BIT_RATE, BIT_RATE);
            videoFormat.setInteger(KEY_FRAME_RATE, FRAME_RATE);
            videoFormat.setInteger(KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL);
            videoFormat.setInteger(KEY_COLOR_FORMAT, getColorFormat());
            videoCodec.configure(videoFormat, null, null, CONFIGURE_FLAG_ENCODE);
            videoCodec.start();
            audioCodec = MediaCodec.createEncoderByType(MIMETYPE_AUDIO_AAC);
            MediaFormat audioFormat = MediaFormat.createAudioFormat(MIMETYPE_AUDIO_AAC, 44100, 1);
            int profile;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                profile = MPEG2ProfileHigh;
            } else {
                profile = 5;
            }
            audioFormat.setInteger(KEY_AAC_PROFILE, profile);
            audioFormat.setInteger(KEY_BIT_RATE, 65536);
            audioCodec.configure(audioFormat, null, null, CONFIGURE_FLAG_ENCODE);
            audioCodec.start();
            mediaMuxer = new MediaMuxer(outputFilePath, MUXER_OUTPUT_MPEG_4);
        } catch (IOException ioe) {
            throw new RuntimeException("MediaMuxer creation failed", ioe);
        }
    }

    @Override
    protected void onStop() {
        if (isStarted) {
            encode();
            if (this.addedFrameCount > 0) {
                Log.i(TAG, String.format("Total frame count = %s", this.addedFrameCount));
                if (videoCodec != null) {
                    videoCodec.stop();
                    videoCodec.release();
                    videoCodec = null;
                    Log.i(TAG, "RELEASE VIDEO CODEC");
                }
                if (audioCodec != null) {
                    audioCodec.stop();
                    audioCodec.release();
                    audioCodec = null;
                    Log.i(TAG, "RELEASE AUDIO CODEC");
                }
                if (mediaMuxer != null) {
                    mediaMuxer.stop();
                    mediaMuxer.release();
                    mediaMuxer = null;
                    Log.i(TAG, "RELEASE MUXER");
                }
            } else {
                Log.e(TAG, "not added any frame");
            }
            isStarted = false;
        }
    }

    @Override
    protected void onAddFrame(Bitmap bitmap) {
        if (!isStarted) {
            Log.d(TAG, "already finished. can't add Frame ");
        } else if (bitmap == null) {
            Log.e(TAG, "Bitmap is null");
        } else {
            int inputBufIndex = videoCodec.dequeueInputBuffer(TIMEOUT_US);
            if (inputBufIndex >= 0) {
//                byte[] input = getNV12(bitmap.getWidth(), bitmap.getHeight(), bitmap);
                byte[] input = EncodeYuvTools.INSTANCE.getNV12(bitmap.getWidth(), bitmap.getHeight(), bitmap, getColorFormat());
                ByteBuffer inputBuffer = videoCodec.getInputBuffer(inputBufIndex);
                inputBuffer.clear();
                inputBuffer.put(input);
                videoCodec.queueInputBuffer(inputBufIndex, 0, input.length,
                        getPresentationTimeUsec(addedFrameCount), 0);
            }
            int audioInputBufferIndex = audioCodec.dequeueInputBuffer(TIMEOUT_US);
            if (audioInputBufferIndex >= -1) {
//                ByteBuffer encoderInputBuffer = audioCodec.getInputBuffer(audioInputBufferIndex);
//                encoderInputBuffer.clear();
//                encoderInputBuffer.put(audioArray);
//                audioCodec.queueInputBuffer(audioInputBufferIndex, 0, audioArray.length,
//                        getPresentationTimeUsec(addedFrameCount), 0);
            }
            addedFrameCount++;
            while (addedFrameCount > encodedFrameCount) {
                encode();
            }
        }
    }

    private void encode() {
        encodeVideo();
        encodeAudio();

    }

    private void encodeAudio() {
        int audioStatus = audioCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);
        Log.i(TAG, "Audio encoderStatus = " + audioStatus + ", presentationTimeUs = "
                + bufferInfo.presentationTimeUs);
        if (audioStatus == INFO_OUTPUT_FORMAT_CHANGED) {
            MediaFormat audioFormat = audioCodec.getOutputFormat();
            Log.i(TAG, String.format("output format changed. audio format: %s", audioFormat.toString()));
            audioTrackIndex = mediaMuxer.addTrack(audioFormat);
            trackCount++;
            if (trackCount == 2) {
                Log.i(TAG, "started media muxer.");
                mediaMuxer.start();
                isMuxerStarted = true;
            }
        } else if (audioStatus == INFO_TRY_AGAIN_LATER) {
            Log.d(TAG, "no output from audio encoder available");
        } else {
            ByteBuffer audioData = audioCodec.getOutputBuffer(audioStatus);
            if (audioData != null) {
                audioData.position(bufferInfo.offset);
                audioData.limit(bufferInfo.offset + bufferInfo.size);
                if (isMuxerStarted) {
                    mediaMuxer.writeSampleData(audioTrackIndex, audioData, bufferInfo);
                }
                audioCodec.releaseOutputBuffer(audioStatus, false);
            }
        }
    }

    private void encodeVideo() {
        int encoderStatus = videoCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);
        Log.i(TAG, "Video encoderStatus = " + encoderStatus + ", presentationTimeUs = "
                + bufferInfo.presentationTimeUs);
        if (encoderStatus == INFO_OUTPUT_FORMAT_CHANGED) {
            MediaFormat videoFormat = videoCodec.getOutputFormat();
            Log.i(TAG, String.format("output format changed. video format: %s", videoFormat.toString()));
            videoTrackIndex = mediaMuxer.addTrack(videoFormat);
            trackCount++;
            if (trackCount == 2) {
                Log.i(TAG, "started media muxer.");
                mediaMuxer.start();
                isMuxerStarted = true;
            }
        } else if (encoderStatus == INFO_TRY_AGAIN_LATER) {
            Log.d(TAG, "no output from video encoder available");
        } else {
            ByteBuffer encodedData = videoCodec.getOutputBuffer(encoderStatus);
            if (encodedData != null) {
                encodedData.position(bufferInfo.offset);
                encodedData.limit(bufferInfo.offset + bufferInfo.size);
                if (isMuxerStarted) {
                    mediaMuxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo);
                }
                videoCodec.releaseOutputBuffer(encoderStatus, false);
                encodedFrameCount++;
            }
            Log.i(TAG, "encoderOutputBuffer " + encoderStatus + " was null");
        }
    }

    private static long getPresentationTimeUsec(int frameIndex) {
        return (((long) frameIndex) * ONE_SEC) / 20;
    }

    /**
     * COLOR_FormatYUV420SemiPlanar 默认
     * COLOR_FormatYUV420Planar     个例使用
     */
    private int getColorFormat() {
        if ("GOOGLE".equalsIgnoreCase(Build.BRAND) && "PIXEL 4".equalsIgnoreCase(Build.MODEL)) {
            return MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar;
        } else {
            return MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
        }
    }

}