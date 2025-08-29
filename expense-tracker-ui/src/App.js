import React, { useState, useEffect } from 'react';
import './App.css';

// Icons
const Icons = {
  Send: () => (
    <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
    </svg>
  ),
  Loading: () => (
    <svg className="animate-spin -ml-1 mr-2 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
    </svg>
  ),
  Calendar: () => (
    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
    </svg>
  ),
  ClipboardList: () => (
    <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
    </svg>
  ),
  Refresh: ({ className = '' }) => (
    <svg xmlns="http://www.w3.org/2000/svg" className={`h-5 w-5 ${className}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
    </svg>
  )
};

// Format currency
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount || 0);
};

function App() {
  const [inputText, setInputText] = useState('');
  const [response, setResponse] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  // Right panel state
  const [selectedPeriod, setSelectedPeriod] = useState(new Date().getFullYear().toString());
  const [expenses, setExpenses] = useState([]);
  const [expensesLoading, setExpensesLoading] = useState(true);
  const [expensesError, setExpensesError] = useState('');
  const [totalExpenses, setTotalExpenses] = useState(0);

  // Generate years for dropdown (last 6 years)
  const currentYear = new Date().getFullYear();
  const years = Array.from({ length: 6 }, (_, i) => currentYear - i);
  
  // Create year options
  const timeOptions = years.map(year => ({ 
    label: year === currentYear ? 'This Year' : year.toString(), 
    value: year.toString() 
  }));
  
  // Calculate total expenses
  useEffect(() => {
    if (expenses && expenses.length > 0) {
      const total = expenses.reduce((sum, expense) => {
        return sum + (parseFloat(expense.amount) || 0);
      }, 0);
      setTotalExpenses(total);
    } else {
      setTotalExpenses(0);
    }
  }, [expenses]);

  // Fetch expenses for selected year
  const fetchExpenses = async (year) => {
    setExpensesLoading(true);
    setExpensesError('');
    
    try {
      const response = await fetch(`http://localhost:8282/v1/expenses?year=${year}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error(`Error: ${response.status} - Unable to fetch expenses`);
      }

      const data = await response.json();
      const expensesData = data.expenses || data || [];
      
      // Sort expenses by date (newest first)
      const sortedExpenses = [...expensesData].sort((a, b) => {
        return new Date(b.date || 0) - new Date(a.date || 0);
      });
      
      setExpenses(sortedExpenses);
    } catch (err) {
      console.error('Error fetching expenses:', err);
      setExpensesError(err.message || 'Failed to load expenses. Please try again.');
      setExpenses([]);
    } finally {
      setExpensesLoading(false);
    }
  };

  // Fetch expenses when component mounts and when period changes
  useEffect(() => {
    fetchExpenses(selectedPeriod);
  }, [selectedPeriod]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!inputText.trim()) {
      setError('Please enter some text');
      return;
    }

    setLoading(true);
    setError('');
    setResponse('');

    try {
      const response = await fetch('http://localhost:8282/v1/expense/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          message: inputText
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      setResponse(data.message || 'No message found in response');
    } catch (err) {
      setError(`Error: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="App">
      <div className="split-container">
                {/* Left Panel - Expense Input */}
        <div className="left-half">
          <div className="left-half-content">
            <div className="bg-white p-6 border-b border-gray-200">
              <div className="mb-8">
                <h1>Track Your Expenses</h1>
              </div>
            </div>
            
            <div className="flex-1 overflow-auto p-6">
              <form onSubmit={handleSubmit} className="form">
                <div className="input-group">
                  <div className="relative">
                    <textarea
                      id="expense-input"
                      value={inputText}
                      onChange={(e) => setInputText(e.target.value)}
                      placeholder="e.g., Spent $25.50 on lunch today, or 'How much did I spend on food this month?'"
                      className="text-input min-h-[120px]"
                      rows="4"
                      disabled={loading}
                    />
                    
                  </div>
                </div>
                
                <div className="flex justify-end mt-4">
                  <button 
                    type="submit" 
                    className="submit-button flex items-center justify-center"
                    disabled={loading || !inputText.trim()}
                  >
                    {loading ? (
                      <>
                        <Icons.Loading />
                        Processing...
                      </>
                    ) : (
                      "Submit"
                    )}
                  </button>
                </div>
              </form>

              {error && (
                <div className="error">
                  <svg className="w-5 h-5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                  </svg>
                  <span>{error}</span>
                </div>
              )}

              {response && (
                <div className="p-4 rounded-lg mb-4 bg-blue-100 border border-blue-400 text-blue-700">
                  <div className="response-content">
                    {response}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
        
        <div className="right-half">
          <div className="right-half-content">
            <div className="bg-white p-6 border-b border-gray-200">
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                                 <div>
                   <h1 className="text-2xl font-semibold text-gray-900">Expense History</h1>
                 </div>
                                 <div className="flex items-center gap-2">
                  <div className="relative w-40">
                    <select
                      id="period-select"
                      value={selectedPeriod}
                      onChange={(e) => setSelectedPeriod(e.target.value)}
                      className="w-full px-3 py-2 pr-8 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white text-gray-900 dark:bg-gray-800 dark:text-gray-100 dark:border-gray-600"
                      disabled={expensesLoading}
                    >
                      {timeOptions.map(option => (
                        <option key={option.value} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                    </select>
                    <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-700">
                      <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                      </svg>
                    </div>
                  </div>
                  <button
                    onClick={() => fetchExpenses(selectedPeriod)}
                    disabled={expensesLoading}
                    className="p-2 text-indigo-600 hover:text-indigo-800 hover:bg-indigo-50 rounded-full transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed"
                    title="Refresh expenses"
                  >
                    <Icons.Refresh className={`w-5 h-5 ${expensesLoading ? 'animate-spin' : ''}`} />
                  </button>
                </div>
              </div>
            </div>

            {expensesError && (
              <div className="error">
                <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
                <span>{expensesError}</span>
              </div>
            )}

            <div className="flex-1 overflow-auto">
              {expensesLoading ? (
              <div className="flex-1 flex items-center justify-center py-12">
                <div className="text-center">
                  <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-indigo-600 border-r-transparent"></div>
                  <p className="mt-3 text-sm font-medium text-gray-600">Loading your expenses...</p>
                </div>
              </div>
            ) : expenses.length > 0 ? (
              <div className="h-full">
                <div className="h-full overflow-auto">
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                      <tr>
                        <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          Date
                        </th>
                        <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          Category
                        </th>
                        <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                          Amount
                        </th>
                      </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                      {expenses.map((expense, index) => {
                        const amount = parseFloat(expense.amount || expense.value || 0);
                        const isExpense = amount < 0;
                        // Parse date string in YYYY-MM-DD format and create a UTC date
                        const parseDate = (dateStr) => {
                          if (!dateStr) return new Date(NaN);
                          const [year, month, day] = dateStr.split('-').map(Number);
                          return new Date(Date.UTC(year, month - 1, day));
                        };
                        
                        const date = parseDate(expense.date || expense.timestamp);
                        const formattedDate = isNaN(date.getTime()) 
                          ? 'N/A' 
                          : date.toLocaleDateString('en-US', { 
                              timeZone: 'UTC',
                              month: 'short', 
                              day: 'numeric',
                              year: 'numeric'
                            });
                        
                        return (
                          <tr 
                            key={index} 
                            className="hover:bg-gray-50 transition-colors"
                          >
                            <td className="px-6 py-4 whitespace-nowrap">
                              <div className="text-sm font-medium text-gray-900">
                                {formattedDate}
                              </div>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap">
                              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-indigo-100 text-indigo-800">
                                {expense.topic || expense.category || 'General'}
                              </span>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                              <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-sm font-medium ${
                                isExpense 
                                  ? 'bg-red-100 text-red-800' 
                                  : 'bg-green-100 text-green-800'
                              }`}>
                                {isExpense ? '-' : ''}{formatCurrency(Math.abs(amount))}
                              </span>
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
                
                {/* Summary row */}
                {expenses.length > 0 && (
                  <div className="bg-white border-t border-gray-200 px-6 py-3">
                    <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2">
                      <div className="text-sm text-gray-500">
                        Showing <span className="font-medium">{expenses.length}</span> {expenses.length === 1 ? 'expense' : 'expenses'}
                      </div>
                      
                    </div>
                  </div>
                )}
                </div>
              ) : (
                <div className="empty-state">
                  <Icons.ClipboardList className="text-gray-300" />
                  <h3>No expenses</h3>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
