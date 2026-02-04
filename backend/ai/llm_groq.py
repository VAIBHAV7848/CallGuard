import os
from groq import AsyncGroq

class GroqLLM:
    def __init__(self):
        self.client = AsyncGroq(api_key=os.environ.get("GROQ_API_KEY"))
        self.history = []
        self.system = """You are a polite call assistant for an Indian user.
Your role:
1. Answer unknown calls professionally
2. Determine intent: SPAM, DELIVERY, PERSONAL, FRAUD, or UNKNOWN
3. Keep responses SHORT (1-2 sentences, max 30 words)
4. Speak caller's language (Hindi/English/Hinglish)
5. If spam/fraud, politely end call

Start with: "Namaste, main assistant bol raha hoon. Kaise madad kar sakta hoon?"
"""
    
    async def respond(self, caller_text: str) -> tuple[str, str]:
        self.history.append({"role": "user", "content": f"Caller: {caller_text}"})
        
        response = await self.client.chat.completions.create(
            model="llama-3.1-70b-versatile",
            messages=[{"role": "system", "content": self.system}] + self.history,
            max_tokens=100,
            temperature=0.7
        )
        
        reply = response.choices[0].message.content
        self.history.append({"role": "assistant", "content": reply})
        
        intent = await self._classify()
        return reply, intent
    
    async def _classify(self) -> str:
        r = await self.client.chat.completions.create(
            model="llama-3.1-8b-instant",
            messages=[
                {"role": "system", "content": "Classify as: SPAM, DELIVERY, PERSONAL, FRAUD, or UNKNOWN. Reply with ONLY the word."},
                {"role": "user", "content": str(self.history[-4:] if len(self.history) > 4 else self.history)}
            ],
            max_tokens=10
        )
        intent = r.choices[0].message.content.strip().upper()
        return intent if intent in ["SPAM", "DELIVERY", "PERSONAL", "FRAUD", "UNKNOWN"] else "UNKNOWN"
    
    async def summarize(self) -> dict:
        r = await self.client.chat.completions.create(
            model="llama-3.1-8b-instant",
            messages=[
                {"role": "system", "content": "Summarize this call in 2-3 sentences. Also list 3 key points. Format: SUMMARY: ...\nKEY POINTS:\n- point1\n- point2\n- point3"},
                {"role": "user", "content": str(self.history)}
            ],
            max_tokens=200
        )
        
        text = r.choices[0].message.content
        lines = text.split("\n")
        summary = ""
        points = []
        
        for line in lines:
            if line.startswith("SUMMARY:"):
                summary = line.replace("SUMMARY:", "").strip()
            elif line.startswith("- "):
                points.append(line[2:].strip())
        
        return {"summary": summary, "key_points": points}
