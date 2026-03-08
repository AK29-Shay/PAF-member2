import React, { useState, useEffect } from 'react';
import axios from 'axios';

const BookingDashboard = ({ currentUser }) => {
  const [bookings, setBookings] = useState([]);
  const [resources, setResources] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // New Booking Form State
  const [requestForm, setRequestForm] = useState({
    resourceId: '', startTime: '', endTime: '', purpose: '', expectedAttendees: ''
  });

  const isRoleAdmin = currentUser.role === 'ADMIN';

  // Axios instance pointing to Spring Boot backend
  const api = axios.create({ baseURL: 'http://localhost:8080/api' });

  useEffect(() => {
    fetchBookings();
    fetchResources();
  }, [currentUser]); // Refetch when role changes

  const fetchResources = async () => {
    try {
      const response = await api.get('/resources');
      setResources(response.data.filter(r => r.status === 'ACTIVE'));
    } catch (err) {
      console.error('Failed to load resources');
    }
  };

  const fetchBookings = async () => {
    setLoading(true);
    try {
      const endpoint = isRoleAdmin 
        ? `/bookings/all` 
        : `/bookings/user/${currentUser.id}`;
      const response = await api.get(endpoint);
      setBookings(response.data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch bookings. Is the backend running?');
    } finally {
      setLoading(false);
    }
  };

  const handleRequestSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post('/bookings', { ...requestForm, userId: currentUser.id });
      alert('Booking requested successfully!');
      fetchBookings(); // Refresh list
      setRequestForm({
        resourceId: '', startTime: '', endTime: '', purpose: '', expectedAttendees: ''
      });
    } catch (err) {
      if (err.response?.status === 409) {
        alert('Conflict: Resource is already booked for this time.');
      } else if (err.response?.data?.error) {
        alert(`Error: ${err.response.data.error}`);
      } else {
        alert('Failed to request booking.');
      }
    }
  };

  const handleCancel = async (id) => {
    if(!window.confirm("Cancel this booking?")) return;
    try {
      await api.delete(`/bookings/${id}`);
      fetchBookings();
    } catch (err) {
      alert('Failed to cancel the booking.');
    }
  };

  const handleStatusUpdate = async (id, status) => {
    let reason = '';
    if (status === 'REJECTED') {
      reason = prompt("Please provide a mandatory reason for rejection:");
      if (!reason || reason.trim() === '') {
        alert("Rejection reason is mandatory.");
        return;
      }
    }
    
    try {
      await api.patch(`/bookings/${id}/status`, { status, reason });
      fetchBookings();
    } catch (err) {
      alert('Failed to update status.');
    }
  }

  return (
    <div className="booking-dashboard-container">
      <h1>{isRoleAdmin ? "Resource Management Hub" : "My Bookings"}</h1>
      
      {/* Search / Filter area could go here */}
      
      {loading && <div className="loader">Loading...</div>}
      {error && <div className="error-alert">{error}</div>}

      {/* Booking Form - Only shown for regular users */}
      {!isRoleAdmin && (
        <div className="card form-card">
          <h2>Request Resource</h2>
          <form onSubmit={handleRequestSubmit} className="booking-form">
            <div className="form-group">
                <label>Select Resource</label>
                <select required value={requestForm.resourceId}
                    onChange={e => setRequestForm({...requestForm, resourceId: e.target.value})}
                    className="form-select">
                  <option value="">-- Choose a Resource --</option>
                  {resources.map(r => (
                    <option key={r.id} value={r.id}>
                      {r.name} ({r.type} - {r.location}, Capacity: {r.capacity})
                    </option>
                  ))}
                </select>
            </div>
            <div className="form-row">
                <div className="form-group">
                    <label>Start Time</label>
                    <input type="datetime-local" required value={requestForm.startTime}
                        onChange={e => setRequestForm({...requestForm, startTime: e.target.value})} />
                </div>
                <div className="form-group">
                    <label>End Time</label>
                    <input type="datetime-local" required value={requestForm.endTime}
                        onChange={e => setRequestForm({...requestForm, endTime: e.target.value})} />
                </div>
            </div>
            <div className="form-group">
                <label>Purpose</label>
                <input type="text" placeholder="e.g. IT3030 Group Meeting" required value={requestForm.purpose}
                    onChange={e => setRequestForm({...requestForm, purpose: e.target.value})} />
            </div>
            <div className="form-group">
                <label>Expected Attendees</label>
                <input type="number" min="1" required value={requestForm.expectedAttendees}
                    onChange={e => setRequestForm({...requestForm, expectedAttendees: e.target.value})} />
            </div>
            <button type="submit" className="btn-primary">Submit Request</button>
          </form>
        </div>
      )}

      {/* Bookings List */}
      <div className="bookings-grid">
        {!loading && bookings.length === 0 && <p>No bookings found.</p>}
        {bookings.map(booking => (
          <div key={booking.id} className="booking-card">
            <div className="card-header">
                <h3>{booking.resourceName}</h3>
                <span className={`badge status-${booking.status.toLowerCase()}`}>{booking.status}</span>
            </div>
            <div className="card-body">
                <p><strong>Purpose:</strong> {booking.purpose}</p>
                <p><strong>Time:</strong> {new Date(booking.startTime).toLocaleString()} to {new Date(booking.endTime).toLocaleString()}</p>
                {isRoleAdmin && <p><strong>Requested By:</strong> {booking.userName}</p>}
                
                {booking.status === 'REJECTED' && (
                <p className="rejection-reason"><strong>Reason:</strong> {booking.rejectionReason}</p>
                )}
            </div>
            
            <div className="card-actions">
                {/* Action Buttons based on Role & Status */}
                {!isRoleAdmin && ['PENDING', 'APPROVED'].includes(booking.status) && (
                <button onClick={() => handleCancel(booking.id)} className="btn-danger">Cancel Booking</button>
                )}

                {isRoleAdmin && booking.status === 'PENDING' && (
                <>
                    <button onClick={() => handleStatusUpdate(booking.id, 'APPROVED')} className="btn-success">Approve</button>
                    <button onClick={() => handleStatusUpdate(booking.id, 'REJECTED')} className="btn-danger">Reject</button>
                </>
                )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default BookingDashboard;
