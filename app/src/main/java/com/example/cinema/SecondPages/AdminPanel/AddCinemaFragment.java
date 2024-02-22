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
import android.widget.TextView;
import android.widget.Toast;

import com.example.cinema.R;
import com.example.cinema.SecondPages.CinemaHelpClasses.CinemaDataClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AddCinemaFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    private StorageReference storageReference;
    private DatabaseReference cinemaDatabaseReference;

    private EditText nameEditText, venueEditText, dateEditText;
    private TextView freeSeats;
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
        freeSeats = view.findViewById(R.id.seats_add);

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

        // Проверка правильности введенной даты
        if (!isValidDate(dateEditText.getText().toString())) {
            Toast.makeText(requireContext(), "Неверный формат даты", Toast.LENGTH_SHORT).show();
            return;
        }

        // Создание уникального имени файла для изображения
        String fileName = FirebaseAuth.getInstance().getCurrentUser().getUid() + "_" + System.currentTimeMillis() + ".jpg";

        // Получение ссылки на место в Storage, где будет хранится файл
        StorageReference fileReference = storageReference.child(fileName);
        // Загрузка изображения в Storage
        int seats = 20;
        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Получение ссылки на загруженное изображение
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Создание объекта DiskDataClass с введенными данными и ссылкой на изображение
                        CinemaDataClass cinemaData = new CinemaDataClass(
                                nameEditText.getText().toString(),
                                venueEditText.getText().toString(),
                                seats,
                                dateEditText.getText().toString(),
                                uri.toString()
                        );

                        // Сохранение данныых в Realtime Database
                        String cinemaName = nameEditText.getText().toString();
                        cinemaDatabaseReference.child(cinemaName).setValue(cinemaData)
                                .addOnSuccessListener(aVoid -> {
                                    // Успешно сохранено
                                    Toast.makeText(requireContext(), "Данные сохранены", Toast.LENGTH_SHORT).show();
                                    // Очистка поля ввода
                                    clearInputFields();
                                })
                                .addOnFailureListener(e -> {
                                    // Ошибка при сохранении
                                    Toast.makeText(requireContext(), "Ошибка при сохранении данных", Toast.LENGTH_SHORT).show();
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    // Ошибка при загрузке изображения
                    Toast.makeText(requireContext(), "Ошибка при загрузке изображения", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isValidDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.mm.yyyy", Locale.getDefault());
        sdf.setLenient(false);

        try {
            // Пытаемся распарсить введенную дату
            Date date = sdf.parse(dateStr);
            return true; // Если успешно, дата введена верно
        } catch (ParseException e) {
            // Если происходит ParseException, значит дата введена неверно
            return false;
        }
    }

    private void clearInputFields() {
        nameEditText.setText("");
        venueEditText.setText("");
        dateEditText.setText("");
        imageUri = null;
    }
}