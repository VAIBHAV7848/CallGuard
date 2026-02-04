package com.callguard.ai

import android.media.*
import com.callguard.core.Constants
import kotlinx.coroutines.*

class AudioStreamer(private val onAudioData: (ByteArray) -> Unit) {
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var isRecording = false
    private val scope = CoroutineScope(Dispatchers.IO)

    fun start() {
        isRecording = true
        val bufferSize = AudioRecord.getMinBufferSize(Constants.AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        audioRecord = AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, Constants.AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
        audioRecord?.startRecording()
        
        scope.launch {
            val buffer = ByteArray(Constants.AUDIO_BUFFER_SIZE)
            while (isRecording) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) onAudioData(buffer.copyOf(read))
            }
        }
        
        // Setup playback
        val playBufferSize = AudioTrack.getMinBufferSize(Constants.AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).build())
            .setAudioFormat(AudioFormat.Builder().setSampleRate(Constants.AUDIO_SAMPLE_RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).setEncoding(AudioFormat.ENCODING_PCM_16BIT).build())
            .setBufferSizeInBytes(playBufferSize)
            .build()
        audioTrack?.play()
    }

    fun playAudio(audioBytes: ByteArray) { audioTrack?.write(audioBytes, 0, audioBytes.size) }
    fun stop() { isRecording = false; audioRecord?.stop(); audioRecord?.release(); audioTrack?.stop(); audioTrack?.release(); scope.cancel() }
}
