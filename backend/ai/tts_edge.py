import edge_tts
import io

class EdgeTTS:
    def __init__(self):
        self.voice = "hi-IN-SwaraNeural"
    
    async def synthesize(self, text: str) -> bytes:
        communicate = edge_tts.Communicate(text, self.voice)
        buffer = io.BytesIO()
        
        async for chunk in communicate.stream():
            if chunk["type"] == "audio":
                buffer.write(chunk["data"])
        
        return buffer.getvalue()
