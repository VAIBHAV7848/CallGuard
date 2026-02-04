from .stt_deepgram import DeepgramSTT
from .llm_groq import GroqLLM
from .tts_edge import EdgeTTS

class DialogueController:
    def __init__(self):
        self.stt = DeepgramSTT()
        self.llm = GroqLLM()
        self.tts = EdgeTTS()
        self.is_muted = False
        self.current_intent = "UNKNOWN"
        
        self.on_transcript = None
        self.on_intent = None
        self.on_audio = None
    
    async def start(self):
        async def handle_transcript(text: str, is_final: bool):
            if self.on_transcript:
                await self.on_transcript(text, is_final)
            
            if is_final and text.strip():
                reply, intent = await self.llm.respond(text)
                self.current_intent = intent
                
                if self.on_intent:
                    await self.on_intent(intent)
                
                if not self.is_muted:
                    audio = await self.tts.synthesize(reply)
                    if self.on_audio:
                        await self.on_audio(audio)
        
        self.stt.on_transcript = handle_transcript
        await self.stt.start()
    
    async def process_audio(self, audio_bytes: bytes):
        await self.stt.send_audio(audio_bytes)
    
    def mute_tts(self, mute: bool):
        self.is_muted = mute
    
    async def get_summary(self) -> dict:
        summary_data = await self.llm.summarize()
        return {
            "summary": summary_data["summary"],
            "intent": self.current_intent,
            "key_points": summary_data["key_points"]
        }
    
    async def stop(self):
        await self.stt.stop()
