# 🤖 AI Chat Assistant (Gemini API Integration)

This is an **AI Chat Application** built using **Java** in **Android Studio**.  
It integrates the **Google Gemini API** to generate smart and dynamic chat responses.

---

## 🚀 Features

* 💬 Real-time AI chat powered by **Google Gemini API**  
* 💾 Save and load chat history using **SQLite** database  
* 🎤 Voice input support using **Speech Recognizer**  
* 🪶 Clean and modern UI with **Material Design**  

---

## 🏗️ Tech Stack

| Component | Technology |
| :--- | :--- |
| **Language** | Java |
| **IDE** | Android Studio |
| **AI Model** | Google Gemini API (gemini-2.5-flash) |
| **Local Database** | SQLite (ChatDatabase.java) |
| **UI** | XML (Material Components) |

---

## ⚙️ How It Works

1. The user types or speaks a message.  
2. The message is sent to the **Gemini API** via the `GeminiApi.java` class.  
3. Gemini processes the request and returns a smart response.  
4. The response is displayed in the chat interface and stored in **SQLite** as chat history.

