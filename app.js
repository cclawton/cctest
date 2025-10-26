class MeditationTimer {
    constructor() {
        this.totalDuration = 0;
        this.intervalDuration = 0;
        this.remainingTime = 0;
        this.timeUntilNextInterval = 0;
        this.intervalsCompleted = 0;
        this.isRunning = false;
        this.isPaused = false;
        this.timerInterval = null;

        this.audioFiles = {
            start: null,
            interval: null,
            end: null
        };

        this.initializeElements();
        this.attachEventListeners();
        this.loadDefaultAudio();
    }

    initializeElements() {
        // Settings panel elements
        this.totalMinutesInput = document.getElementById('totalMinutes');
        this.totalSecondsInput = document.getElementById('totalSeconds');
        this.intervalMinutesInput = document.getElementById('intervalMinutes');
        this.intervalSecondsInput = document.getElementById('intervalSeconds');

        // Audio dropdowns
        this.startAudioSelect = document.getElementById('startAudioSelect');
        this.intervalAudioSelect = document.getElementById('intervalAudioSelect');
        this.endAudioSelect = document.getElementById('endAudioSelect');

        // Audio file inputs
        this.startAudioInput = document.getElementById('startAudio');
        this.intervalAudioInput = document.getElementById('intervalAudio');
        this.endAudioInput = document.getElementById('endAudio');

        // Panels
        this.settingsPanel = document.getElementById('settingsPanel');
        this.timerPanel = document.getElementById('timerPanel');

        // Timer display
        this.timeDisplay = document.getElementById('timeDisplay');
        this.nextIntervalDisplay = document.getElementById('nextInterval');
        this.intervalsCountDisplay = document.getElementById('intervalsCount');
        this.progressFill = document.getElementById('progressFill');

        // Buttons
        this.startBtn = document.getElementById('startBtn');
        this.pauseBtn = document.getElementById('pauseBtn');
        this.stopBtn = document.getElementById('stopBtn');

        // Test audio buttons
        this.testStartBtn = document.getElementById('testStartAudio');
        this.testIntervalBtn = document.getElementById('testIntervalAudio');
        this.testEndBtn = document.getElementById('testEndAudio');

        // File name displays
        this.startFileName = document.getElementById('startFileName');
        this.intervalFileName = document.getElementById('intervalFileName');
        this.endFileName = document.getElementById('endFileName');
    }

    attachEventListeners() {
        // Audio dropdown changes
        this.startAudioSelect.addEventListener('change', (e) => this.handleAudioSelect(e, 'start'));
        this.intervalAudioSelect.addEventListener('change', (e) => this.handleAudioSelect(e, 'interval'));
        this.endAudioSelect.addEventListener('change', (e) => this.handleAudioSelect(e, 'end'));

        // Audio file uploads
        this.startAudioInput.addEventListener('change', (e) => this.handleAudioUpload(e, 'start'));
        this.intervalAudioInput.addEventListener('change', (e) => this.handleAudioUpload(e, 'interval'));
        this.endAudioInput.addEventListener('change', (e) => this.handleAudioUpload(e, 'end'));

        // Test audio buttons
        this.testStartBtn.addEventListener('click', () => this.playAudio('start'));
        this.testIntervalBtn.addEventListener('click', () => this.playAudio('interval'));
        this.testEndBtn.addEventListener('click', () => this.playAudio('end'));

        // Control buttons
        this.startBtn.addEventListener('click', () => this.startMeditation());
        this.pauseBtn.addEventListener('click', () => this.togglePause());
        this.stopBtn.addEventListener('click', () => this.stopMeditation());
    }

    loadDefaultAudio() {
        // Load default audio files on page load
        this.loadAudioFromPath('audio/start-bell.wav', 'start');
        this.loadAudioFromPath('audio/interval-bell.mp3', 'interval');
        this.loadAudioFromPath('audio/end-bell.wav', 'end');
    }

    loadAudioFromPath(path, type) {
        this.audioFiles[type] = new Audio(path);
        this.audioFiles[type].addEventListener('error', (e) => {
            console.error(`Failed to load ${type} audio from ${path}`, e);
            alert(`Error loading ${type} audio. Please check that the audio file exists at: ${path}`);
        });
        this.audioFiles[type].addEventListener('canplaythrough', () => {
            console.log(`${type} audio loaded successfully from ${path}`);
        });
    }

    handleAudioSelect(event, type) {
        const select = event.target;
        const value = select.value;

        if (value === 'custom') {
            // Show file input for custom upload
            const fileInput = document.getElementById(`${type}Audio`);
            fileInput.style.display = 'inline-block';
            fileInput.click();
        } else {
            // Load selected preset audio
            this.loadAudioFromPath(value, type);
            const fileInput = document.getElementById(`${type}Audio`);
            fileInput.style.display = 'none';
            const fileNameElement = document.getElementById(`${type}FileName`);
            fileNameElement.textContent = '';
        }
    }

    handleAudioUpload(event, type) {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = (e) => {
                this.audioFiles[type] = new Audio(e.target.result);
                this.updateFileName(type, file.name);

                // Add custom file to dropdown
                const select = document.getElementById(`${type}AudioSelect`);

                // Remove any previous custom uploads from dropdown
                const customOptions = Array.from(select.options).filter(opt => opt.dataset.custom === 'true');
                customOptions.forEach(opt => opt.remove());

                // Add new custom option
                const customOption = document.createElement('option');
                customOption.value = 'custom-uploaded';
                customOption.textContent = file.name;
                customOption.dataset.custom = 'true';
                customOption.selected = true;

                // Insert before "Upload Custom Audio..." option
                const uploadOption = Array.from(select.options).find(opt => opt.value === 'custom');
                select.insertBefore(customOption, uploadOption);
            };
            reader.readAsDataURL(file);
        }
    }

    updateFileName(type, name) {
        const fileNameElement = document.getElementById(`${type}FileName`);
        fileNameElement.textContent = `(${name})`;
    }

    capitalize(str) {
        return str.charAt(0).toUpperCase() + str.slice(1);
    }

    playAudio(type) {
        if (this.audioFiles[type]) {
            this.audioFiles[type].currentTime = 0;
            const playPromise = this.audioFiles[type].play();

            if (playPromise !== undefined) {
                playPromise.then(() => {
                    console.log(`${type} audio playing`);
                }).catch(error => {
                    console.error(`Error playing ${type} audio:`, error);
                    // Show user-friendly error only if not during meditation
                    if (!this.isRunning) {
                        alert(`Unable to play ${type} audio. Error: ${error.message}\n\nTip: Try clicking the Test button again.`);
                    }
                });
            }
        } else {
            console.warn(`No audio loaded for ${type}`);
            if (!this.isRunning) {
                alert(`No audio file loaded for ${type} sound.`);
            }
        }
    }

    startMeditation() {
        // Get total duration
        const totalMinutes = parseInt(this.totalMinutesInput.value) || 0;
        const totalSeconds = parseInt(this.totalSecondsInput.value) || 0;
        this.totalDuration = (totalMinutes * 60) + totalSeconds;

        // Get interval duration
        const intervalMinutes = parseInt(this.intervalMinutesInput.value) || 0;
        const intervalSeconds = parseInt(this.intervalSecondsInput.value) || 0;
        this.intervalDuration = (intervalMinutes * 60) + intervalSeconds;

        if (this.totalDuration <= 0) {
            alert('Please set a valid total duration');
            return;
        }

        // Initialize timer state
        this.remainingTime = this.totalDuration;
        this.timeUntilNextInterval = this.intervalDuration > 0 ? this.intervalDuration : 0;
        this.intervalsCompleted = 0;
        this.isRunning = true;
        this.isPaused = false;

        // Switch to timer panel
        this.settingsPanel.style.display = 'none';
        this.timerPanel.style.display = 'block';

        // Play start audio
        this.playAudio('start');

        // Update display
        this.updateDisplay();

        // Start the timer
        this.timerInterval = setInterval(() => this.tick(), 1000);
    }

    tick() {
        if (this.isPaused) return;

        this.remainingTime--;

        // Check for interval
        if (this.intervalDuration > 0) {
            this.timeUntilNextInterval--;

            if (this.timeUntilNextInterval <= 0) {
                this.playAudio('interval');
                this.intervalsCompleted++;
                this.timeUntilNextInterval = this.intervalDuration;
            }
        }

        this.updateDisplay();

        // Check if meditation is complete
        if (this.remainingTime <= 0) {
            this.completeMeditation();
        }
    }

    updateDisplay() {
        // Update time remaining
        this.timeDisplay.textContent = this.formatTime(this.remainingTime);

        // Update next interval
        if (this.intervalDuration > 0) {
            this.nextIntervalDisplay.textContent = this.formatTime(this.timeUntilNextInterval);
        } else {
            this.nextIntervalDisplay.textContent = 'N/A';
        }

        // Update intervals count
        this.intervalsCountDisplay.textContent = this.intervalsCompleted;

        // Update progress bar
        const progress = ((this.totalDuration - this.remainingTime) / this.totalDuration) * 100;
        this.progressFill.style.width = `${progress}%`;
    }

    formatTime(seconds) {
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        const secs = seconds % 60;

        if (hours > 0) {
            return `${this.padZero(hours)}:${this.padZero(minutes)}:${this.padZero(secs)}`;
        } else {
            return `${this.padZero(minutes)}:${this.padZero(secs)}`;
        }
    }

    padZero(num) {
        return num.toString().padStart(2, '0');
    }

    togglePause() {
        this.isPaused = !this.isPaused;
        this.pauseBtn.textContent = this.isPaused ? 'Resume' : 'Pause';
    }

    stopMeditation() {
        if (confirm('Are you sure you want to stop the meditation?')) {
            this.cleanup();
            this.settingsPanel.style.display = 'block';
            this.timerPanel.style.display = 'none';
        }
    }

    completeMeditation() {
        this.playAudio('end');
        this.cleanup();

        setTimeout(() => {
            alert(`Meditation complete! You completed ${this.intervalsCompleted} intervals.`);
            this.settingsPanel.style.display = 'block';
            this.timerPanel.style.display = 'none';
        }, 500);
    }

    cleanup() {
        clearInterval(this.timerInterval);
        this.isRunning = false;
        this.isPaused = false;
        this.pauseBtn.textContent = 'Pause';
    }
}

// Initialize the app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new MeditationTimer();
});
