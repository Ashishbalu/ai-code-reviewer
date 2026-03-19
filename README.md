# 🚀 AI Code Reviewer

AI-powered code review engine built using Spring Boot and local LLM (Ollama + DeepSeek).

## 🔥 Features
- Analyze Java code using LLM
- GitHub repository scanning
- Automatic bug & improvement detection
- Time & space complexity estimation
- Structured JSON output

## ⚙️ Architecture
Client → Controller → Service → LLM (Ollama)

## 📌 API Endpoints

### 1. Code Review
POST /api/review

### 2. GitHub Review
POST /api/github-review

## 🛠 Tech Stack
- Java
- Spring Boot
- REST APIs
- Ollama (DeepSeek)

## ▶️ Run Locally
1. Install Ollama
2. Pull model:
   ollama pull deepseek-coder:1.3b
3. Run Spring Boot
4. Test via Postman