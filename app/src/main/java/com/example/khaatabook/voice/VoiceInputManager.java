package com.example.khaatabook.voice;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.StorageService;

import java.io.IOException;

/**
 * Manages Vosk-based offline voice recognition.
 */
public class VoiceInputManager {
    private static final String TAG = "VoiceInputManager";
    private static final String MODEL_NAME = "vosk-model-small-hi-0.22";
    private static final float SAMPLE_RATE = 16000.0f;

    private Model voskModel;
    private SpeechService speechService;
    private VoiceParserCallback callback;
    private boolean isModelLoaded = false;
    private boolean isListening = false;

    public void setCallback(VoiceParserCallback callback) {
        this.callback = callback;
    }

    // ─────────────────────────────────────────────
    // INIT
    // ─────────────────────────────────────────────
    public void init(Context context) {
        if (isModelLoaded) return;

        LibVosk.setLogLevel(LogLevel.INFO);

        StorageService.unpack(context, MODEL_NAME, "model",
                (model) -> {
                    voskModel = model;
                    isModelLoaded = true;
                    Log.d(TAG, "✅ Vosk Model loaded successfully");
                },
                (exception) -> {
                    Log.e(TAG, "❌ Failed to load Vosk model", exception);
                    if (callback != null) callback.onError(exception);
                }
        );
    }

    // ─────────────────────────────────────────────
    // START LISTENING
    // ─────────────────────────────────────────────
    public void startListening(Context context) {
        if (!isModelLoaded || voskModel == null) {
            Log.e(TAG, "Model not loaded yet");
            if (callback != null)
                callback.onError(new IllegalStateException("Model not ready. Please wait."));
            return;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "No microphone permission");
            // ✅ FIX 3: notify callback about permission error
            if (callback != null)
                callback.onError(new SecurityException("Microphone permission not granted."));
            return;
        }

        if (isListening) {
            Log.w(TAG, "Already listening");
            return;
        }

        try {
            // ✅ FIX 1: only clean up if there's actually a leftover service
            if (speechService != null) {
                speechService.stop();
                speechService = null;
            }

            Recognizer recognizer = new Recognizer(voskModel, SAMPLE_RATE);
            speechService = new SpeechService(recognizer, SAMPLE_RATE);
            speechService.startListening(new RecognitionListener() {

                @Override
                public void onPartialResult(String hypothesis) {
                    Log.v(TAG, "Partial: " + hypothesis);
                    handleResult(hypothesis, true);
                }

                @Override
                public void onResult(String hypothesis) {
                    Log.d(TAG, "Result: " + hypothesis);
                    handleResult(hypothesis, false);
                }

                @Override
                public void onFinalResult(String hypothesis) {
                    Log.d(TAG, "Final: " + hypothesis);
                    handleResult(hypothesis, false);
                    isListening = false;
                    if (callback != null) callback.onListeningStopped();
                }

                @Override
                public void onError(Exception exception) {
                    Log.e(TAG, "Recognizer error", exception);
                    isListening = false;
                    if (callback != null) callback.onError(exception);
                }

                @Override
                public void onTimeout() {
                    Log.d(TAG, "Timeout");
                    // ✅ FIX 5: notify callback on timeout
                    isListening = false;
                    if (callback != null) callback.onListeningStopped();
                    stopListening();
                }
            });

            isListening = true;
            if (callback != null) callback.onListeningStarted();
            Log.d(TAG, "🎙️ Listening started");

        } catch (IOException e) {
            Log.e(TAG, "Failed to start recognizer", e);
            if (callback != null) callback.onError(e);
        }
    }

    // ─────────────────────────────────────────────
    // HANDLE RESULT
    // ─────────────────────────────────────────────
    private void handleResult(String hypothesis, boolean isPartial) {
        // ✅ Always log raw JSON so you can debug in Logcat
        Log.d(TAG, "RAW JSON: " + hypothesis);

        try {
            JSONObject json = new JSONObject(hypothesis);
            String key = isPartial ? "partial" : "text";
            String text = json.optString(key, "").trim();

            Log.d(TAG, "Extracted (" + key + "): '" + text + "'");

            if (!text.isEmpty()) {
                if (isPartial) {
                    if (callback != null) callback.onPartialTextReceived(text);
                } else {
                    if (callback != null) callback.onTextReceived(text);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON parse error", e);
        }
    }

    // ─────────────────────────────────────────────
    // STOP LISTENING
    // ─────────────────────────────────────────────
    public void stopListening() {
        if (speechService != null) {
            // ✅ FIX 4: only call stop() here, NOT shutdown()
            speechService.stop();
            speechService = null;

            if (isListening) {
                isListening = false;
                if (callback != null) callback.onListeningStopped();
            }
        }
    }

    // ─────────────────────────────────────────────
    // DESTROY — call in onDestroy()
    // ─────────────────────────────────────────────
    public void destroy() {
        stopListening();
        if (speechService != null) {
            // ✅ FIX 4: shutdown() only here in destroy
            speechService.shutdown();
            speechService = null;
        }
        if (voskModel != null) {
            voskModel.close();
            voskModel = null;
        }
        isModelLoaded = false;
        Log.d(TAG, "VoiceInputManager destroyed");
    }

    // ─────────────────────────────────────────────
    // GETTERS
    // ─────────────────────────────────────────────

    // ✅ FIX 2: added state getters
    public boolean isReady() {
        return isModelLoaded;
    }

    public boolean isListening() {
        return isListening;
    }
}