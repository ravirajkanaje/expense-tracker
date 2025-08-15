# Expense Tracker

An AI-powered expense tracking application that combines natural language processing with traditional expense management. The application uses Ollama AI to parse expense information from natural language input and stores data in Google Sheets.

## 🏗️ Architecture

This project consists of two main components:

- **Backend Server** - Spring Boot application with AI integration
- **Frontend UI** - React-based web interface

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Node.js 16 or higher
- Maven 3.6+
- Ollama (for AI processing)
- Google Sheets API credentials

### Running the Application

1. **Start Ollama AI Service**
   ```bash
   # Install Ollama (if not already installed)
   curl -fsSL https://ollama.ai/install.sh | sh
   
   # Pull the required model
   ollama pull qwen3:latest
   
   # Start Ollama service
   ollama serve
   ```

2. **Configure Google Sheets API**
   - Create a Google Cloud Project
   - Enable Google Sheets API
   - Create service account credentials
   - Download the JSON credentials file
   - Update the path in `application.properties`

3. **Build and Run the Server**
   ```bash
   # Build the project
   mvn clean install
   
   # Run the application
   mvn spring-boot:run
   ```

4. **Start the UI (Optional - for development)**
   ```bash
   cd expense-tracker-ui
   npm install
   npm start
   ```

The application will be available at:
- Backend API: http://localhost:8282
- Swagger UI: http://localhost:8282/swagger-ui.html
- Frontend: http://localhost:3000 (if running separately)

## 📁 Project Structure

```
expense-tracker/
├── src/main/java/org/rkanaje/expense/
│   ├── ai/                    # AI-related components
│   │   ├── config/           # AI configuration
│   │   ├── controller/       # REST controllers
│   │   ├── model/           # Data models
│   │   ├── service/         # Business logic
│   │   ├── tool/            # AI tools
│   │   └── utils/           # Utility classes
│   ├── controller/          # Traditional REST controllers
│   └── service/             # Core services
├── src/main/resources/
│   ├── application.properties
│   └── prompts/             # AI prompt templates
├── expense-tracker-ui/      # React frontend
└── pom.xml
```

## 🔧 Configuration

### Application Properties

Key configuration options in `application.properties`:

```properties
# Server configuration
server.port=8282
spring.application.name=expense-tracker

# Ollama AI configuration
ollama.host=http://localhost:11434
ollama.chat.model=qwen3:latest
ollama.chat.options.num-ctx=4096
ollama.chat.options.temperature=0.5

# Google Sheets configuration
google.sheets.credentials.json=/path/to/credentials.json
google.sheets.spreadsheet.id=your-spreadsheet-id
```

## 🛠️ API Endpoints

### Chat Endpoints

- `POST /v1/expense/chat` - Process natural language expense input
- `POST /v1/expense/parse` - Parse and extract expense data from text

### Traditional REST Endpoints

- `GET /v1/expenses?year={year}` - Retrieve expenses for a specific year
- Additional endpoints for CRUD operations

## 🤖 AI Features

The application leverages Ollama AI for:

- **Natural Language Processing**: Parse expense information from conversational input
- **Data Extraction**: Automatically extract amount, category, date, and description
- **Smart Categorization**: AI-powered expense categorization
- **Contextual Understanding**: Understand complex expense scenarios

### Example Usage

```
Input: "I spent $25.50 on lunch today at Chipotle"
Output: {
  "amount": 25.50,
  "category": "Food & Dining",
  "date": "2024-01-15",
  "description": "Lunch at Chipotle"
}
```

## 🗄️ Data Storage

Expenses are stored in Google Sheets for:
- Easy access and sharing
- Built-in backup and version control
- Integration with Google Workspace
- No database setup required

## 🧪 Testing

```bash
# Run backend tests
mvn test

# Run frontend tests
cd expense-tracker-ui
npm test
```

## 🚀 Deployment

### Backend Deployment

The application can be deployed as a standard Spring Boot JAR:

```bash
mvn clean package
java -jar target/expense-tracker-0.0.1-SNAPSHOT.jar
```

### Frontend Deployment

The React app is automatically built and served by the Spring Boot application, but can also be deployed separately:

```bash
cd expense-tracker-ui
npm run build
# Deploy the build folder to your web server
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For issues and questions:
1. Check the [Issues](../../issues) page
2. Review the Swagger documentation at `/swagger-ui.html`
3. Check the application logs for detailed error information
