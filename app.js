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
    }

    initializeElements() {
        // Settings panel elements
        this.totalMinutesInput = document.getElementById('totalMinutes');
        this.totalSecondsInput = document.getElementById('totalSeconds');
        this.intervalMinutesInput = document.getElementById('intervalMinutes');
        this.intervalSecondsInput = document.getElementById('intervalSeconds');

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

    handleAudioUpload(event, type) {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = (e) => {
                this.audioFiles[type] = new Audio(e.target.result);
                this.updateFileName(type, file.name);
                this.enableTestButton(type);
            };
            reader.readAsDataURL(file);
        }
    }

    updateFileName(type, name) {
        const fileNameElement = document.getElementById(`${type}FileName`);
        fileNameElement.textContent = name;
    }

    enableTestButton(type) {
        const testBtn = document.getElementById(`test${this.capitalize(type)}Audio`);
        testBtn.disabled = false;
    }

    capitalize(str) {
        return str.charAt(0).toUpperCase() + str.slice(1);
    }

    playAudio(type) {
        if (this.audioFiles[type]) {
            this.audioFiles[type].currentTime = 0;
            this.audioFiles[type].play();
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
