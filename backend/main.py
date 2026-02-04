from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from contextlib import asynccontextmanager
import json
from dotenv import load_dotenv
load_dotenv()
from ai.dialogue_controller import DialogueController

@asynccontextmanager
async def lifespan(app: FastAPI):
    yield

app = FastAPI(lifespan=lifespan)

@app.get("/health")
async def health():
    return {"status": "ok"}

@app.websocket("/ws/call")
async def websocket_call_handler(websocket: WebSocket):
    await websocket.accept()
    controller = DialogueController()
    
    try:
        await controller.start()
        
        async def send_transcript(text: str, is_final: bool):
            await websocket.send_json({"type": "transcript", "text": text, "is_final": is_final})
        
        async def send_intent(intent: str):
            await websocket.send_json({"type": "intent", "intent": intent})
        
        async def send_audio(audio_bytes: bytes):
            await websocket.send_bytes(audio_bytes)
        
        controller.on_transcript = send_transcript
        controller.on_intent = send_intent
        controller.on_audio = send_audio
        
        while True:
            message = await websocket.receive()
            
            if "bytes" in message:
                await controller.process_audio(message["bytes"])
            elif "text" in message:
                data = json.loads(message["text"])
                cmd = data.get("command")
                
                if cmd == "STOP":
                    break
                elif cmd == "MUTE_TTS":
                    controller.mute_tts(True)
                elif cmd == "UNMUTE_TTS":
                    controller.mute_tts(False)
                elif cmd == "GET_SUMMARY":
                    summary = await controller.get_summary()
                    await websocket.send_json({"type": "summary", **summary})
                    
    except WebSocketDisconnect:
        pass
    finally:
        await controller.stop()
