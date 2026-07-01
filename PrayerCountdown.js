import React, { useState, useEffect } from 'react';
// Assuming praytimes library is available or imported
// import PrayTimes from 'praytimes'; 

const PrayerCountdown = ({ latitude, longitude, timezone }) => {
  const [times, setTimes] = useState({});
  const [nextPrayer, setNextPrayer] = useState({ name: '', time: '', remaining: '' });

  // Mock PrayTimes logic for demonstration if library is not installed
  const calculateTimes = () => {
    // In a real scenario, you'd use: 
    // const pt = new PrayTimes('MWL');
    // return pt.getTimes(new Date(), [latitude, longitude], timezone);
    
    // Mocking some times for the example
    return {
      fajr: "04:30",
      sunrise: "05:45",
      dhuhr: "12:15",
      asr: "15:45",
      maghrib: "18:30",
      isha: "20:00"
    };
  };

  const formatDiff = (diffMs) => {
    const hours = Math.floor(diffMs / 3600000);
    const minutes = Math.floor((diffMs % 3600000) / 60000);
    const seconds = Math.floor((diffMs % 60000) / 1000);
    return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
  };

  useEffect(() => {
    const interval = setInterval(() => {
      const now = new Date();
      const currentTimes = calculateTimes();
      setTimes(currentTimes);

      const prayerOrder = ['fajr', 'dhuhr', 'asr', 'maghrib', 'isha'];
      let found = false;

      for (let name of prayerOrder) {
        const [h, m] = currentTimes[name].split(':').map(Number);
        const pTime = new Date();
        pTime.setHours(h, m, 0);

        if (pTime > now) {
          const diff = pTime - now;
          setNextPrayer({
            name: name.charAt(0).toUpperCase() + name.slice(1),
            time: currentTimes[name],
            remaining: formatDiff(diff)
          });
          found = true;
          break;
        }
      }

      // Handle rollover to next day Fajr
      if (!found) {
        const [h, m] = currentTimes.fajr.split(':').map(Number);
        const pTime = new Date();
        pTime.setDate(pTime.getDate() + 1);
        pTime.setHours(h, m, 0);
        const diff = pTime - now;
        setNextPrayer({
          name: 'Fajr',
          time: currentTimes.fajr,
          remaining: formatDiff(diff)
        });
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [latitude, longitude, timezone]);

  return (
    <div style={{ 
      padding: '20px', 
      borderRadius: '12px', 
      background: '#1a1a1a', 
      color: '#fff', 
      textAlign: 'center',
      fontFamily: 'Arial, sans-serif'
    }}>
      <h3>Next Prayer: {nextPrayer.name}</h3>
      <div style={{ fontSize: '2.5rem', fontWeight: 'bold', margin: '10px 0' }}>
        {nextPrayer.remaining}
      </div>
      <p>Starts at: {nextPrayer.time}</p>
      
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px', marginTop: '20px' }}>
        {Object.entries(times).map(([name, time]) => (
          <div key={name} style={{ background: '#333', padding: '8px', borderRadius: '8px' }}>
            <div style={{ fontSize: '0.8rem', textTransform: 'capitalize' }}>{name}</div>
            <div style={{ fontWeight: 'bold' }}>{time}</div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default PrayerCountdown;
