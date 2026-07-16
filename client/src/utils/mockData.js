export const mockUser = {
  id: '12345',
  name: 'Alex Developer',
  email: 'alex@example.com',
  avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Alex',
};

export const mockDeviceStatus = {
  model: 'Google Pixel 8 Pro',
  battery: 85,
  isCharging: true,
  storage: {
    used: 64,
    total: 128,
  },
  network: 'Wi-Fi (5G)',
  lastSync: new Date().toISOString(),
};

export const mockCommands = [
  { id: '1', action: 'OPEN_APP', target: 'YouTube', status: 'completed', time: '10:42 AM' },
  { id: '2', action: 'SYSTEM_TOGGLE', target: 'Flashlight', status: 'completed', time: '09:15 AM' },
  { id: '3', action: 'CALL', target: 'John Doe', status: 'failed', time: 'Yesterday' },
];

export const mockChatHistory = [
  { id: '1', role: 'user', content: 'Turn on the flashlight' },
  { id: '2', role: 'ai', content: 'Flashlight turned on.' },
  { id: '3', role: 'user', content: 'Open Chrome and go to github.com' },
  { id: '4', role: 'ai', content: 'Opening Chrome...' },
];

export const mockAISuggestions = [
  "Turn on Battery Saver",
  "Call Mom",
  "Open Spotify",
  "Set an alarm for 7 AM"
];
