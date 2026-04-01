package com.example.cultivation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.cultivation.data.DatabaseClient;
import com.example.cultivation.data.ScanLog;

public class ScanFragment extends Fragment {

    private PreviewView viewFinder;
    private ImageButton btnCapture;
    private CardView cvResult;
    private TextView tvDisease, tvSeverity, tvMaturity, tvConfidence;
    private Button btnCloseResult;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private Interpreter tflite;

    private static final int PERMISSION_REQUEST_CODE = 10;
    private final String[] CLASSES = { "Healthy", "Mosaic", "RedRot", "Rust", "Yellow" };
    private int imageSize = 224;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        viewFinder = view.findViewById(R.id.viewFinder);
        btnCapture = view.findViewById(R.id.btnCapture);
        cvResult = view.findViewById(R.id.cvResult);
        tvDisease = view.findViewById(R.id.tvDisease);
        tvSeverity = view.findViewById(R.id.tvSeverity);
        tvMaturity = view.findViewById(R.id.tvMaturity);
        tvConfidence = view.findViewById(R.id.tvConfidence);
        btnCloseResult = view.findViewById(R.id.btnCloseResult);

        cameraExecutor = Executors.newSingleThreadExecutor();

        // Load TFLite Model
        try {
            tflite = new Interpreter(loadModelFile("SugarcaneDiseaseClassifier.tflite"));
        } catch (IOException e) {
            Log.e("ScanFragment", "Error loading model", e);
            Toast.makeText(requireContext(), "Model failed to load", Toast.LENGTH_SHORT).show();
        }

        // Check Permissions
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissions(new String[] { Manifest.permission.CAMERA }, PERMISSION_REQUEST_CODE);
        }

        btnCapture.setOnClickListener(v -> takePhoto());
        btnCloseResult.setOnClickListener(v -> {
            cvResult.setVisibility(View.GONE);
            btnCapture.setVisibility(View.VISIBLE);
        });

        return view;
    }

    private void takePhoto() {
        if (imageCapture == null)
            return;

        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        processImage(image);
                        image.close();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("ScanFragment", "Photo capture failed: " + exception.getMessage(), exception);
                    }
                });
    }

    private Bitmap currentBitmap; // Add member

    private void processImage(ImageProxy imageProxy) {
        // Convert ImageProxy to Bitmap
        ByteBuffer buffer = imageProxy.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        // Rotate if needed (simplified, assuming portrait)
        Matrix matrix = new Matrix();
        matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        // Resize for Model
        currentBitmap = Bitmap.createScaledBitmap(rotatedBitmap, imageSize, imageSize, false); // Store it

        classifyImage(currentBitmap);
    }

    // Fallback for YUV format (ImageAnalysis) or JPEG (ImageCapture)
    // The above processImage assumes JPEG (which takePicture provides if not
    // configuring otherwise)

    private void classifyImage(Bitmap image) {
        if (tflite == null)
            return;

        try {
            // Prepare Input
            ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            inputBuffer.order(ByteOrder.nativeOrder());
            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++];
                    inputBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    inputBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    inputBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            // Prepare Output
            float[][] outputValues = new float[1][CLASSES.length];
            tflite.run(inputBuffer, outputValues);

            // Process Output
            float[] confidences = outputValues[0];
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            final String diseaseName = CLASSES[maxPos];
            final float confidenceScore = maxConfidence;

            // Run UI updates on Main Thread
            requireActivity().runOnUiThread(() -> {
                showResult(diseaseName, confidenceScore);
            });

        } catch (Exception e) {
            Log.e("ScanFragment", "Inference error", e);
        }
    }

    private void showResult(String disease, float confidence) {
        tvDisease.setText(disease);
        tvConfidence.setText(String.format("Confidence: %.1f%%", confidence * 100));

        // Mock Severity
        String severity = "Low";
        if (confidence > 0.90)
            severity = "High";
        else if (confidence > 0.70)
            severity = "Medium";
        tvSeverity.setText("Severity: " + severity);

        // Mock Maturity
        String maturity = "Early Stage";
        if (disease.equals("Healthy"))
            maturity = "Ready for Harvest";
        else if (confidence > 0.85)
            maturity = "Late Stage";
        tvMaturity.setText("Maturity: " + maturity);

        // Recommendation
        String rec = "";
        switch (disease) {
            case "Healthy":
                rec = "Keep monitoring water levels and ensure proper fertilization.";
                performHealthyAnalysis();
                break;
            case "Mosaic":
                rec = "Remove infected plants immediately. Control aphids to prevent spread.";
                getView().findViewById(R.id.layoutHealthyAnalysis).setVisibility(View.GONE);
                break;
            case "RedRot":
                rec = "Use disease-free setts for planting. Improve field drainage.";
                getView().findViewById(R.id.layoutHealthyAnalysis).setVisibility(View.GONE);
                break;
            case "Rust":
                rec = "Apply recommended fungicides. Avoid water stress.";
                getView().findViewById(R.id.layoutHealthyAnalysis).setVisibility(View.GONE);
                break;
            case "Yellow":
                rec = "Check for Iron deficiency. Apply Ferrous Sulphate.";
                getView().findViewById(R.id.layoutHealthyAnalysis).setVisibility(View.GONE);
                break;
            default:
                rec = "Consult an expert for detailed analysis.";
                getView().findViewById(R.id.layoutHealthyAnalysis).setVisibility(View.GONE);
                break;
        }
        TextView tvRecommendation = getView().findViewById(R.id.tvRecommendation);
        tvRecommendation.setText("Tip: " + rec);

        // Save to History
        saveScanToHistory(disease, confidence);

        cvResult.setVisibility(View.VISIBLE);
        btnCapture.setVisibility(View.GONE);
    }

    private void performHealthyAnalysis() {
        if (currentBitmap == null)
            return;

        long r = 0, g = 0, b = 0;
        int width = currentBitmap.getWidth();
        int height = currentBitmap.getHeight();
        int[] pixels = new int[width * height];
        currentBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int pixel : pixels) {
            r += (pixel >> 16) & 0xFF;
            g += (pixel >> 8) & 0xFF;
            b += pixel & 0xFF;
        }

        int count = width * height;
        if (count > 0) {
            r /= count;
            g /= count;
            b /= count;
        }

        TextView tvR = getView().findViewById(R.id.tvR);
        TextView tvG = getView().findViewById(R.id.tvG);
        TextView tvB = getView().findViewById(R.id.tvB);
        View layout = getView().findViewById(R.id.layoutHealthyAnalysis);

        tvR.setText(String.valueOf(r));
        tvG.setText(String.valueOf(g));
        tvB.setText(String.valueOf(b));
        layout.setVisibility(View.VISIBLE);

        // Simple maturity logic: Greenness ratio
        // If Green is dominant, it's growing. If Yellowing (Red+Green mix), might be
        // maturing or sick (but here it's "Healthy")
    }

    private void saveScanToHistory(String disease, float confidence) {
        cameraExecutor.execute(() -> {
            // Timestamp as ID
            String date = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());

            ScanLog log = new ScanLog(disease, confidence, date, "");

            // 1. Local Save
            DatabaseClient.getInstance(requireContext()).getAppDatabase().scanLogDao().insert(log);

            // 2. Cloud Sync (Firestore)
            com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance()
                    .getCurrentUser();
            if (user != null) {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.getUid())
                        .collection("scans")
                        .add(log)
                        .addOnSuccessListener(docRef -> Log.d("Firestore", "Log added: " + docRef.getId()))
                        .addOnFailureListener(e -> Log.e("Firestore", "Error adding log", e));
            }
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider
                .getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("ScanFragment", "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private ByteBuffer loadModelFile(String filename) throws IOException {
        AssetFileDescriptor fileDescriptor = requireActivity().getAssets().openFd(filename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}