# Expense Tracker UI

A modern React-based user interface for the Expense Tracker AI application.

## Features

- Clean and modern UI design
- Text input for expense details and questions
- Real-time API communication with the backend
- Loading states and error handling
- Responsive design for mobile and desktop

## Prerequisites

- Node.js (version 14 or higher)
- npm or yarn
- The backend API running on `localhost:8282`

## Installation

1. Navigate to the project directory:
   ```bash
   cd expense-tracker-ui
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

## Running the Application

1. Start the development server:
   ```bash
   npm start
   ```

2. Open your browser and navigate to `http://localhost:3000`

3. Make sure your backend API is running on `localhost:8282`

## Usage

1. Enter your expense details or ask a question in the text area
2. Click the "Submit" button
3. The response from the AI will be displayed below the form

## API Endpoint

The UI communicates with the backend API at:
- **URL**: `http://localhost:8282/v1/expense/chat/`
- **Method**: POST
- **Content-Type**: application/json
- **Body**: `{ "message": "your text here" }`

## Available Scripts

- `npm start` - Runs the app in development mode
- `npm test` - Launches the test runner
- `npm run build` - Builds the app for production
- `npm run eject` - Ejects from Create React App (one-way operation)

## Project Structure

```
expense-tracker-ui/
├── public/
├── src/
│   ├── App.js          # Main application component
│   ├── App.css         # Application styles
│   ├── index.js        # Application entry point
│   └── ...
├── package.json
└── README.md
```

## Technologies Used

- React 18
- Create React App
- Modern CSS with gradients and animations
- Fetch API for HTTP requests
