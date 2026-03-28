package com.example.khaatabook;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.khaatabook.voice.VoiceInputManager;
import com.example.khaatabook.voice.VoiceParserCallback;

import java.util.List;

public class VoiceFragment extends Fragment implements VoiceParserCallback {

    private static final String TAG = "VoiceFragment";
    private Button btnMic;
    private TextView tvStatus, tvHeard, tvUnderstood;
    private View pulse1, pulse2, pulse3;
    private LinearLayout layoutUnderstood;

    private boolean isListening = false;
    private String voiceText = "";
    private VoiceParser.ParseResult parsedResult = null;
    private String status = "idle";

    private Handler handler = new Handler();
    private Runnable pulseRunnable;
    private VoiceInputManager voiceInputManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_voice, container, false);

        if (getActivity() instanceof MainActivity) {
            voiceInputManager = ((MainActivity) getActivity()).getVoiceInputManager();
            if (voiceInputManager != null) {
                voiceInputManager.setCallback(this);
            }
        }

        initializeViews(view);
        setupMicButton();

        return view;
    }

    private void initializeViews(View view) {
        btnMic = view.findViewById(R.id.btn_mic);
        tvStatus = view.findViewById(R.id.tv_status);
        tvHeard = view.findViewById(R.id.tv_heard);
        tvUnderstood = view.findViewById(R.id.tv_understood);
        pulse1 = view.findViewById(R.id.pulse_1);
        pulse2 = view.findViewById(R.id.pulse_2);
        pulse3 = view.findViewById(R.id.pulse_3);
        layoutUnderstood = view.findViewById(R.id.layout_understood);

        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnConfirm = view.findViewById(R.id.btn_confirm);

        btnCancel.setOnClickListener(v -> cancelAction());
        btnConfirm.setOnClickListener(v -> confirmAction());
    }

    private void setupMicButton() {
        btnMic.setOnClickListener(v -> {
            if (voiceInputManager == null) return;

            if (isListening) {
                stopVoiceInput();
            } else {
                startVoiceInput();
            }
        });
    }

    private void startVoiceInput() {
        if (voiceInputManager == null) return;
        
        voiceText = "";
        parsedResult = null;
        status = "listening";
        updateUI();
        
        try {
            voiceInputManager.startListening(getContext());
        } catch (Exception e) {
            Log.e(TAG, "Error starting voice input", e);
            onError(e);
        }
    }

    private void stopVoiceInput() {
        if (voiceInputManager != null) {
            voiceInputManager.stopListening();
        }
    }

    // VoiceParserCallback implementation
    @Override
    public void onListeningStarted() {
        handler.post(() -> {
            isListening = true;
            status = "listening";
            startPulseAnimation();
            updateUI();
        });
    }

    @Override
    public void onPartialTextReceived(String partialText) {
        handler.post(() -> {
            voiceText = partialText;
            updateUI();
        });
    }

    @Override
    public void onTextReceived(String text) {
        handler.post(() -> {
            voiceText = text;
            status = "processing";
            updateUI();
            
            // Artificial delay for "processing" feel
            handler.postDelayed(() -> {
                parsedResult = VoiceParser.parse(text, getCustomerList());
                status = "done";
                updateUI();
            }, 500);
        });
    }

    @Override
    public void onListeningStopped() {
        handler.post(() -> {
            isListening = false;
            if (!"done".equals(status)) {
                status = "idle";
            }
            stopPulseAnimation();
            updateUI();
        });
    }

    @Override
    public void onError(Exception e) {
        handler.post(() -> {
            isListening = false;
            status = "idle";
            stopPulseAnimation();
            updateUI();
            Toast.makeText(getContext(), "Voice Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void startPulseAnimation() {
        if (pulseRunnable != null) return;
        pulseRunnable = new Runnable() {
            @Override
            public void run() {
                animatePulse(pulse1, 0);
                animatePulse(pulse2, 200);
                animatePulse(pulse3, 400);
            }
        };
        handler.post(pulseRunnable);
    }

    private void stopPulseAnimation() {
        pulseRunnable = null;
        pulse1.setVisibility(View.GONE);
        pulse2.setVisibility(View.GONE);
        pulse3.setVisibility(View.GONE);
        pulse1.clearAnimation();
        pulse2.clearAnimation();
        pulse3.clearAnimation();
    }

    private void animatePulse(View view, long delay) {
        view.setVisibility(View.VISIBLE);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.4f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.4f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0.6f, 0f);

        scaleX.setDuration(1000);
        scaleY.setDuration(1000);
        alpha.setDuration(1000);

        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        alpha.setRepeatCount(ValueAnimator.INFINITE);

        scaleX.setStartDelay(delay);
        scaleY.setStartDelay(delay);
        alpha.setStartDelay(delay);

        scaleX.start();
        scaleY.start();
        alpha.start();
    }

    private void updateUI() {
        tvStatus.setText(getStatusText());

        if (!voiceText.isEmpty()) {
            tvHeard.setVisibility(View.VISIBLE);
            tvHeard.setText("HEARD: \"" + voiceText + "\"");
        } else {
            tvHeard.setVisibility(View.GONE);
        }

        if (parsedResult != null && "done".equals(status)) {
            layoutUnderstood.setVisibility(View.VISIBLE);
            tvUnderstood.setText(getUnderstoodText());
        } else {
            layoutUnderstood.setVisibility(View.GONE);
        }

        btnMic.setText(isListening ? "⏹️" : "🎙️");
        btnMic.setBackgroundColor(getResources().getColor(
            isListening ? R.color.red_error : R.color.action_voice));
    }

    private String getStatusText() {
        switch (status) {
            case "listening": return "Listening...";
            case "processing": return "Processing...";
            case "done": return "Got it! Review below";
            default: return "Tap mic to speak";
        }
    }

    private String getUnderstoodText() {
        if (parsedResult == null) return "";

        StringBuilder sb = new StringBuilder("UNDERSTOOD:\n");
        if ("lend".equals(parsedResult.type)) {
            sb.append("👤 Customer: ").append(parsedResult.customerName != null ? parsedResult.customerName : "Unknown").append("\n");
            sb.append("🛒 Item: ").append(parsedResult.item != null ? parsedResult.item : "?").append(" × ")
              .append(parsedResult.qty != null ? parsedResult.qty : "?").append(" ")
              .append(parsedResult.unit != null ? parsedResult.unit : "").append("\n");
            sb.append("💰 Rate: ₹").append(parsedResult.price != null ? parsedResult.price : "?")
              .append(" → Total: ₹").append((parsedResult.qty != null && parsedResult.price != null) ?
              (parsedResult.qty * parsedResult.price) : "?");
        } else if ("payment".equals(parsedResult.type)) {
            sb.append("👤 Customer: ").append(parsedResult.customerName != null ? parsedResult.customerName : "Unknown").append("\n");
            sb.append("💵 Payment: ₹").append(parsedResult.amount != null ? parsedResult.amount : "?");
        } else if ("new_customer".equals(parsedResult.type)) {
            sb.append("👤 New Customer: ").append(parsedResult.customerName != null ? parsedResult.customerName : "?").append("\n");
            sb.append("📞 Phone: ").append(parsedResult.phone != null ? parsedResult.phone : "N/A");
        }
        return sb.toString();
    }

    private List<Customer> getCustomerList() {
        if (getActivity() instanceof MainActivity) {
            return ((MainActivity) getActivity()).getCustomerList();
        }
        return new java.util.ArrayList<>();
    }

    public void confirmAction() {
        if (parsedResult == null || !"done".equals(status)) return;

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).processVoiceResult(parsedResult);
        }

        // Reset
        status = "idle";
        voiceText = "";
        parsedResult = null;
        updateUI();
    }

    public void cancelAction() {
        status = "idle";
        voiceText = "";
        parsedResult = null;
        updateUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (voiceInputManager != null) {
            voiceInputManager.stopListening();
            voiceInputManager.setCallback(null);
        }
    }
}
