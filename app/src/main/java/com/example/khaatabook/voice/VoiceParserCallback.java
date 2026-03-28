package com.example.khaatabook.voice;

public interface VoiceParserCallback {

    /** Called when mic starts listening */
    default void onListeningStarted() {}

    /** Called when a partial result comes in — good for live UI feedback */
    default void onPartialTextReceived(String partialText) {}

    /** Called when Vosk returns a final transcription */
    void onTextReceived(String text);

    /** Called when mic stops */
    default void onListeningStopped() {}

    /** Called on any recognition or init error */
    void onError(Exception e);
}