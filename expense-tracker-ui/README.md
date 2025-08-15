# Expense Tracker UI

A modern React-based user interface for the AI-powered expense tracking application. This frontend provides an intuitive way to interact with the expense tracking system through natural language input and visual expense history.

## 🎯 Features

- **Natural Language Input**: Chat-like interface for entering expenses
- **Real-time AI Processing**: Instant parsing and categorization of expenses
- **Expense History**: Visual display of all tracked expenses
- **Year-based Filtering**: Filter expenses by year
- **Responsive Design**: Works seamlessly on desktop and mobile devices
- **Modern UI**: Clean, intuitive interface with smooth animations

## 🚀 Getting Started

### Prerequisites

- Node.js 16 or higher
- npm or yarn package manager
- Backend server running (see main README)

### Installation

1. **Navigate to the UI directory**
   ```bash
   cd expense-tracker-ui
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Start the development server**
   ```bash
   npm start
   ```

The application will open in your browser at `http://localhost:3000`.

### Building for Production

```bash
npm run build
```

This creates an optimized production build in the `build` folder.

## 🏗️ Project Structure

```
expense-tracker-ui/
├── public/
│   ├── index.html
│   └── favicon.ico
├── src/
│   ├── App.js              # Main application component
│   ├── App.css             # Application styles
│   ├── index.js            # Application entry point
│   └── index.css           # Global styles
├── package.json
└── README.md
```

## 🎨 UI Components

### Main Layout

The application uses a split-panel layout:

- **Left Panel**: Expense input and AI chat interface
- **Right Panel**: Expense history and filtering

### Key Features

#### 1. Natural Language Input
- Large text area for entering expense descriptions
- Real-time processing with loading indicators
- Error handling and user feedback

#### 2. Expense History
- Tabular display of all expenses
- Year-based filtering dropdown
- Automatic sorting by date (newest first)
- Color-coded expense/income indicators

#### 3. Responsive Design
- Mobile-friendly interface
- Adaptive layout for different screen sizes
- Touch-friendly controls

## 🔧 Configuration

### API Endpoints

The UI connects to the backend API at `http://localhost:8282`. Key endpoints:

- `POST /v1/expense/chat` - Process natural language input
- `GET /v1/expenses?year={year}` - Retrieve expenses for a year

### Environment Variables

Create a `.env` file in the UI directory to customize the API endpoint:

```env
REACT_APP_API_URL=http://localhost:8282
```

## 🎯 Usage

### Adding Expenses

1. **Natural Language Input**
   ```
   "Spent $25.50 on lunch today at Chipotle"
   "Paid $120 for groceries yesterday"
   "Coffee this morning was $4.75"
   ```

2. **Query Expenses**
   ```
   "How much did I spend on food this month?"
   "Show me all expenses from last week"
   "What's my total spending this year?"
   ```

### Viewing Expense History

- Use the year dropdown to filter expenses
- View expenses in a clean, organized table
- See total expenses for the selected period
- Sort by date, category, or amount

## 🧪 Testing

```bash
# Run tests in watch mode
npm test

# Run tests with coverage
npm test -- --coverage

# Run tests once
npm test -- --watchAll=false
```

## 🚀 Deployment

### Standalone Deployment

1. **Build the application**
   ```bash
   npm run build
   ```

2. **Deploy the `build` folder**
   - Upload to a web server
   - Deploy to platforms like Netlify, Vercel, or AWS S3

### Integrated Deployment

The UI is automatically built and served by the Spring Boot backend:

1. The Maven build process includes UI compilation
2. Built files are copied to `src/main/resources/static`
3. Spring Boot serves the React app at the root URL

## 🎨 Styling

The application uses:
- **CSS Grid and Flexbox** for layout
- **Custom CSS** for styling (no external UI libraries)
- **Responsive design** principles
- **Modern color scheme** with accessibility considerations

### Key CSS Classes

- `.split-container` - Main layout container
- `.left-half` / `.right-half` - Panel containers
- `.text-input` - Styled text input areas
- `.submit-button` - Primary action buttons
- `.response` - AI response display
- `.error` - Error message styling

## 🔍 Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## 🐛 Troubleshooting

### Common Issues

1. **API Connection Errors**
   - Ensure the backend server is running
   - Check the API URL configuration
   - Verify CORS settings on the backend

2. **Build Errors**
   - Clear node_modules and reinstall: `rm -rf node_modules && npm install`
   - Check Node.js version compatibility
   - Verify all dependencies are installed

3. **Runtime Errors**
   - Check browser console for detailed error messages
   - Verify API responses are in expected format
   - Check network connectivity

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure the build passes
6. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For UI-specific issues:
1. Check the browser console for errors
2. Review the React DevTools
3. Check the network tab for API issues
4. Refer to the main project README for backend-related questions
