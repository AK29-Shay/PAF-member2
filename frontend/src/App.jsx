import { useState } from 'react'
import './index.css'
import BookingDashboard from './components/BookingDashboard'

function App() {
  // Simple state to toggle between USER and ADMIN for demonstration
  // Real implementation uses OAuth + Roles
  const [role, setRole] = useState('USER')
  
  // Hardcoded for testing. DataSeeder created these user IDs: 1 = User, 2 = Admin
  const currentUser = {
    id: role === 'ADMIN' ? 2 : 1,
    name: role === 'ADMIN' ? 'Admin User' : 'Standard User',
    role: role
  };

  return (
    <div className="app-container">
      <header className="app-header">
        <div className="logo">Smart Campus Hub</div>
        <div className="role-switcher">
          <span>Current Role: <strong>{role}</strong></span>
          <button onClick={() => setRole(role === 'USER' ? 'ADMIN' : 'USER')}>
            Switch to {role === 'USER' ? 'ADMIN' : 'USER'}
          </button>
        </div>
      </header>
      
      <main className="app-main">
        <BookingDashboard currentUser={currentUser} />
      </main>
    </div>
  )
}

export default App
