# Meditation Timer App

A simple, elegant meditation timer web application with customizable audio alerts and flexible interval settings.

## Features

- **Customizable Duration**: Set your meditation time in minutes and seconds
- **Periodic Intervals**: Configure interval timers to sound at regular intervals during your meditation
- **Custom Audio**: Upload your own audio files for start, interval, and end sounds
- **Visual Progress**: Watch your progress with a smooth progress bar
- **Pause/Resume**: Take breaks when needed without losing your progress
- **Responsive Design**: Works beautifully on desktop and mobile devices

## Usage

### Getting Started

1. Open `index.html` in your web browser
2. Configure your meditation session:
   - Set the total duration (e.g., 1 hour 15 minutes = 75 minutes, 0 seconds)
   - Set the periodic interval (e.g., 12 minutes, 0 seconds)
   - Upload audio files for start, interval, and end sounds

### Audio Files

- **Start Sound**: Plays when you begin your meditation
- **Interval Sound**: Plays at each periodic interval (e.g., every 12 minutes)
- **End Sound**: Plays when your meditation session is complete

You can test each audio file using the "Test" button after uploading.

### Example Setup

For a 1 hour 15 minute meditation with bells every 12 minutes:

1. Total Duration: **75** minutes, **0** seconds
2. Periodic Interval: **12** minutes, **0** seconds
3. Upload your preferred bell/chime sounds
4. Click "Start Meditation"

The timer will:
- Play the start sound immediately
- Ring the interval sound at 12, 24, 36, 48, 60, and 72 minutes
- Play the end sound at 75 minutes

### During Meditation

- **Time Remaining**: Shows how much time is left in your session
- **Progress Bar**: Visual representation of your progress
- **Next Interval**: Shows when the next interval sound will play
- **Intervals Completed**: Tracks how many intervals you've completed
- **Pause**: Pause and resume as needed
- **Stop**: End the meditation early

## Technical Details

- Pure HTML, CSS, and JavaScript
- No external dependencies or frameworks required
- Runs entirely in the browser
- Audio files are stored in memory (not uploaded to any server)

## Browser Compatibility

Works in all modern browsers that support:
- HTML5 Audio API
- FileReader API
- ES6 JavaScript

## Tips for Best Experience

1. Use high-quality, calming audio files (MP3, WAV, or OGG format)
2. Test your audio files before starting to ensure proper volume
3. Use headphones for a more immersive experience
4. Consider using different sounds for start, interval, and end to create variety

Enjoy your meditation practice!
