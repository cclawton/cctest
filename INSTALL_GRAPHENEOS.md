# Installing Meditation Timer on GrapheneOS

This guide will help you install the Meditation Timer as a Progressive Web App (PWA) on your GrapheneOS Google Pixel 9 Pro.

## What is a PWA?

A Progressive Web App works like a native app but:
- No Google Play Store required
- No app store permissions needed
- Respects your privacy
- Works offline once installed
- Can be installed directly from your browser

## Installation Methods

### Method 1: Install via Vanadium Browser (Recommended for GrapheneOS)

Vanadium is the hardened Chromium-based browser that comes with GrapheneOS.

1. **Host the app** (choose one option):
   - **Option A: GitHub Pages** (Easiest)
     - Push this repository to GitHub
     - Go to repository Settings → Pages
     - Enable Pages from main branch
     - Your app will be available at: `https://yourusername.github.io/meditation-timer/`

   - **Option B: Local Server** (For testing)
     - On your computer, run: `python3 -m http.server 8000` in the app directory
     - Find your computer's local IP address (e.g., 192.168.1.100)
     - Access from phone: `http://192.168.1.100:8000`

   - **Option C: Deploy to Netlify/Vercel** (Free hosting)
     - Drop the folder on Netlify Drop
     - Get instant HTTPS URL

2. **Open the app in Vanadium**:
   - Open Vanadium browser on your Pixel 9 Pro
   - Navigate to your hosted URL
   - The app should load with the meditation timer interface

3. **Install the PWA**:
   - Tap the **three dots menu** (⋮) in the top right corner
   - Select **"Add to Home screen"** or **"Install app"**
   - Confirm the installation
   - The app icon will appear on your home screen

4. **Launch the app**:
   - Tap the app icon from your home screen
   - It will open in fullscreen mode like a native app
   - Works offline once cached!

### Method 2: Install via Chromium/Brave Browser

If you have Chromium or Brave installed on GrapheneOS:

1. Open the app URL in the browser
2. Tap the menu (⋮)
3. Select "Install app" or "Add to Home screen"
4. Confirm installation

## Using GitHub Pages (Detailed Steps)

1. **Push to GitHub**:
   ```bash
   git push origin claude/meditation-timer-app-011CUVFJxaatW6gQ9iYpPX7o
   ```

2. **Enable GitHub Pages**:
   - Go to: `https://github.com/yourusername/yourrepo/settings/pages`
   - Source: Deploy from branch
   - Branch: Select your branch → `/root` → Save
   - Wait 1-2 minutes for deployment

3. **Access on phone**:
   - URL will be: `https://yourusername.github.io/yourrepo/`
   - Open this in Vanadium browser
   - Install as described above

## Verifying the Installation

Once installed, you should see:
- App icon on your home screen
- No browser UI (address bar, etc.) when opened
- Works in airplane mode (offline capability)
- Notification bar matches app theme color (purple)

## Permissions

The app requires minimal permissions:
- **Storage**: To cache the app for offline use
- **Audio**: To play meditation bell sounds (only when you select audio files)

No network permissions are required after initial installation.

## Troubleshooting

### "Add to Home screen" option not showing
- Make sure you're using HTTPS (not HTTP)
- Try visiting the site, using it briefly, then check the menu again
- Some browsers require you to interact with the page first

### App not working offline
- Open the app while online first
- Use it for a few seconds to let it cache
- Check browser settings to ensure cache isn't disabled
- Try reinstalling the PWA

### Audio not playing
- Grant microphone/audio permissions if prompted
- Check that Do Not Disturb mode isn't blocking sounds
- Test the audio files using the "Test" buttons
- Ensure audio files are in supported formats (MP3, WAV, OGG)

### App looks like a website
- Make sure you installed via "Add to Home screen"
- Reinstall the app if needed
- Check that the manifest.json file loaded correctly

## Privacy & Security Notes

This PWA:
- Runs entirely in your browser sandbox
- No data is sent to external servers
- Audio files stay on your device
- No tracking or analytics
- No external dependencies
- Works completely offline after first load

Perfect for privacy-focused GrapheneOS!

## Updating the App

To update the app:
1. Open the PWA
2. Pull down to refresh
3. The service worker will automatically update the cached files
4. Restart the app to see updates

## Uninstalling

To remove the app:
1. Long-press the app icon
2. Select "App info" or drag to "Uninstall"
3. Confirm removal

Or from Settings:
1. Settings → Apps
2. Find "Meditation Timer"
3. Select → Uninstall

## Additional Tips

- **Use with Do Not Disturb**: Perfect for distraction-free meditation
- **Airplane mode**: Works great offline for extra focus
- **Battery optimization**: Disable battery optimization for this app if timer stops when screen is off
- **Keep screen on**: You may want to adjust your screen timeout settings during meditation

Enjoy your meditation practice!
