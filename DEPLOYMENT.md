# Deploying Your Meditation Timer PWA

## Step 1: Merge to Main Branch

Your PWA changes are on the `claude/meditation-timer-app-011CUVFJxaatW6gQ9iYpPX7o` branch. To deploy, you need to merge to `main`:

### Quick Merge URL
Visit: https://github.com/cclawton/cctest/compare/main...claude/meditation-timer-app-011CUVFJxaatW6gQ9iYpPX7o?expand=1

Or:
1. Go to https://github.com/cclawton/cctest
2. Click "Pull requests" → "New pull request"
3. Set base: `main` ← compare: `claude/meditation-timer-app-011CUVFJxaatW6gQ9iYpPX7o`
4. Create and merge the PR

## Step 2: Enable GitHub Pages

1. Go to your repository: **https://github.com/cclawton/cctest**
2. Click **Settings** (in the top menu)
3. Scroll down to **Pages** (in the left sidebar under "Code and automation")
4. Under "Source":
   - Select **Deploy from a branch**
   - Branch: **main**
   - Folder: **/ (root)**
5. Click **Save**
6. Wait 1-2 minutes for deployment

## Step 3: Get Your App URL

After GitHub Pages is enabled, your URL will be:
```
https://cclawton.github.io/cctest/
```

Visit this page at the top of Settings → Pages to confirm.

## Step 4: Install on Your Phone

### On GrapheneOS Pixel 9 Pro:

1. **Open Vanadium browser** (or Chrome/Brave)

2. **Navigate to your app**:
   ```
   https://cclawton.github.io/cctest/
   ```

3. **Install the PWA**:
   - Tap the **menu (⋮)** in the top right
   - Select **"Add to Home screen"** or **"Install app"**
   - Confirm the installation
   - You may see a prompt like "Add Meditation Timer to Home screen?"

4. **Launch the app**:
   - The app icon will appear on your home screen
   - Tap it to open in fullscreen mode
   - No browser UI, just the app!

5. **Test offline mode**:
   - Close the app
   - Enable Airplane mode
   - Open the app again - it should work!

## Troubleshooting

### GitHub Pages not showing up
- Make sure you're on the Settings page for your repository (not your profile)
- Check that you have admin access to the repository
- Try waiting a few more minutes for DNS propagation

### "Add to Home screen" not available
- Ensure you're accessing via HTTPS (GitHub Pages provides this automatically)
- Make sure you're using a compatible browser (Vanadium, Chrome, Brave, Firefox)
- Interact with the page first (tap around) then check the menu again

### App not working offline
- Visit the app while online first
- Use it briefly (30 seconds) to let the service worker cache everything
- Check that cache isn't disabled in browser settings
- Try reinstalling the PWA

## Alternative: Deploy to Netlify (Easier)

If GitHub Pages doesn't work or you want faster deployment:

1. Go to **https://app.netlify.com/drop**
2. Drag your entire project folder (or just the files) onto the page
3. Netlify will give you an instant HTTPS URL like: `https://random-name-12345.netlify.app`
4. Install from that URL on your phone

No account required for Netlify Drop!

## Verification Checklist

✅ PR merged to main
✅ GitHub Pages enabled on main branch
✅ App accessible at https://cclawton.github.io/cctest/
✅ App loads correctly in phone browser
✅ "Add to Home screen" option available
✅ App icon appears on home screen
✅ App opens in fullscreen mode
✅ App works offline

## Your App is Ready!

Once deployed, you'll have:
- 🎯 Direct installation on your GrapheneOS phone
- 🔒 Complete privacy (no tracking, no external servers)
- 📱 Native app experience
- ✈️ Full offline functionality
- 🎵 Custom meditation bells included

Enjoy your meditation practice!
