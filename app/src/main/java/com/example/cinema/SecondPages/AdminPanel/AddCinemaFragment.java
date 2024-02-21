package com.example.cinema.SecondPages.AdminPanel;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cinema.R;
import com.example.cinema.SecondPages.CinemaHelpClasses.CinemaDataClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class AddCinemaFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    private StorageReference storageReference;
    private DatabaseReference cinemaDatabaseReference;

    private EditText nameEditText, venueEditText, dateEditText;
    private Button addImageButton, saveDataButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_cinema, container, false);

        // Инициализация Firebase Storage и Realtime Database
        storageReference = FirebaseStorage.getInstance().getReference("cinema_images");
        cinemaDatabaseReference = FirebaseDatabase.getInstance().getReference("cinema_data");

        // Инициализация всех EditText
        nameEditText = view.findViewById(R.id.name_add);
        venueEditText = view.findViewById(R.id.venue_add);
        dateEditText = view.findViewById(R.id.date_add);

        // Инициализация кнопок
        addImageButton = view.findViewById(R.id.add_image);
        addImageButton.setOnClickListener(v -> openFileChooser());

        saveDataButton = view.findViewById(R.id.save_data_button);
        saveDataButton.setOnClickListener(v -> saveDataToFirebase());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void openFileChooser() {
        // Реализация выбора изображения
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
        }
    }

    private void saveDataToFirebase() {
        // Проверка на заполненность обязательных полей
        if (nameEditText.getText().toString().isEmpty()
                || venueEditText.getText().toString().isEmpty()
                || dateEditText.getText().toString().isEmpty()
                || imageUri == null) {
            Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        // Создание уникального имени файла для изображения
        String fileName = FirebaseAuth.getInstance().getCurrentUser().getUid() + "_" + System.currentTimeMillis() + ".jpg";

        // Получение ссылки на место в Storage, где будет хранится файл
        StorageReference fileReference = storageReference.child(fileName);
        // Загрузка изображения в Storage

    }

    private void clearInputFields() {
        nameEditText.setText("");
        venueEditText.setText("");
        dateEditText.setText("");
        imageUri = null;
    }
}