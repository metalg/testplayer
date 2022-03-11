package com.example.concatenatingplayer;

import static com.google.android.exoplayer2.decoder.DecoderReuseEvaluation.REUSE_RESULT_NO;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.decoder.DecoderReuseEvaluation;
import com.google.android.exoplayer2.mediacodec.MediaCodecAdapter;
import com.google.android.exoplayer2.mediacodec.MediaCodecInfo;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

class PlayListMediaCodecVideoRenderer extends MediaCodecVideoRenderer {

    public PlayListMediaCodecVideoRenderer(Context context, MediaCodecAdapter.Factory codecAdapterFactory, MediaCodecSelector mediaCodecSelector, long allowedJoiningTimeMs, boolean enableDecoderFallback, @Nullable Handler eventHandler, @Nullable VideoRendererEventListener eventListener, int maxDroppedFramesToNotify) {
        super(context, codecAdapterFactory, mediaCodecSelector, allowedJoiningTimeMs, enableDecoderFallback, eventHandler, eventListener, maxDroppedFramesToNotify);
    }

    @Override
    protected DecoderReuseEvaluation canReuseCodec(MediaCodecInfo codecInfo, Format oldFormat, Format newFormat) {
        if (needsReconfigureWorkaround(codecInfo, oldFormat, newFormat)) {
            return new DecoderReuseEvaluation(
                    codecInfo.name,
                    oldFormat,
                    newFormat,
                    REUSE_RESULT_NO,
                    DecoderReuseEvaluation.DISCARD_REASON_APP_OVERRIDE);
        }

        return super.canReuseCodec(codecInfo, oldFormat, newFormat);
    }

    private static boolean needsReconfigureWorkaround(MediaCodecInfo codecInfo, Format oldFormat, Format newFormat) {
        if (codecInfo != null &&
                !"OMX.qcom.video.decoder.hevc".equals(codecInfo.name)) {
            return false;
        }

        return isFormatMatches(oldFormat, "hvc1.2") && isFormatMatches(newFormat, "hvc1.1");
    }

    private static boolean isFormatMatches(Format format, String filter) {
        if (filter == null
                || format == null
                || format.sampleMimeType == null
                || format.codecs == null) {
            return false;
        }

        if (!format.sampleMimeType.equals("video/hevc")) {
            return false;
        }

        return format.codecs.startsWith(filter);
    }
}
