import os
from deepgram import DeepgramClient, LiveTranscriptionEvents, LiveOptions
import asyncio

class DeepgramSTT:
    def __init__(self):
        self.client = DeepgramClient(os.environ.get("DEEPGRAM_API_KEY"))
        self.connection = None
        self.on_transcript = None

    async def start(self):
        self.connection = self.client.listen.asynclive.v("1")
        
        async def on_message(self_conn, result, **kwargs):
            transcript = result.channel.alternatives[0].transcript
            if transcript and self.on_transcript:
                await self.on_transcript(transcript, result.is_final)
        
        self.connection.on(LiveTranscriptionEvents.Transcript, on_message)
        
        options = LiveOptions(
            model="nova-2",
            language="hi",
            punctuate=True,
            interim_results=True,
            encoding="linear16",
            sample_rate=16000
        )
        
        await self.connection.start(options)

    async def send_audio(self, audio_bytes: bytes):
        if self.connection:
            await self.connection.send(audio_bytes)

    async def stop(self):
        if self.connection:
            await self.connection.finish()
