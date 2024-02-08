package com.example.cinema.FirstPages;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cinema.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegFormFragment extends Fragment {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mUsersRef = mDatabase.getReference("user_data");

    private EditText nameEditText;
    private EditText phoneNumberEditText;
    private EditText cityEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reg_form, container, false);

        nameEditText = view.findViewById(R.id.name_user);
        phoneNumberEditText = view.findViewById(R.id.number);
        cityEditText = view.findViewById(R.id.city);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavController navController = Navigation.findNavController(view);
        // Получение аргументов из RegFragment
        Bundle bundle = getArguments();
        if (bundle != null) {
            String login = bundle.getString("login");
            String password = bundle.getString("password");

            // Добавление кода для записи данных в Firebase при нажатии кнопки (примерно так)
            Button registrationButton = view.findViewById(R.id.registration);
            registrationButton.setOnClickListener(v -> {
                String name = nameEditText.getText().toString();
                String phoneNumber = phoneNumberEditText.getText().toString();
                String city = cityEditText.getText().toString();

                // Вызов метода для завершения регистрации
                registerUser(navController, login, password, name, phoneNumber, city);
            });
        }
    }


    private void registerUser(NavController navController, String login, String password, String name, String phoneNumber, String city) {
        mAuth.createUserWithEmailAndPassword(login, password)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                        DatabaseReference userRef = mUsersRef.child(userId);

                        // Запись данных в Firebase
                        userRef.child("login").setValue(login);
                        userRef.child("password").setValue(password);
                        userRef.child("name").setValue(name);
                        userRef.child("phoneNumber").setValue(phoneNumber);
                        userRef.child("city").setValue(city);

                        Toast.makeText(getContext(), "Вы успешно зарегистрировались", Toast.LENGTH_LONG).show();
                        navController.navigate(R.id.action_regFormFragment_to_authFragment);
                    } else {
                        handleRegistrationFailure(navController, task);
                    }
                });
    }

    private void handleRegistrationFailure(NavController navController, Task<AuthResult> task) {
        Exception exception = task.getException();
        if (exception instanceof FirebaseAuthUserCollisionException) {
            Toast.makeText(getContext(), "Данная почта уже занята", Toast.LENGTH_LONG).show();
            navController.navigate(R.id.action_regFormFragment_to_regFragment);
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(getContext(), "Вы неправильно ввели почту", Toast.LENGTH_LONG).show();
        }
    }
}
