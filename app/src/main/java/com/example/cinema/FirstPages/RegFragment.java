package com.example.cinema.FirstPages;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegFragment extends Fragment {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static boolean password_check = false;
    private static boolean password_confirmed = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reg, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final NavController navController = Navigation.findNavController(view);

        Button enter = view.findViewById(R.id.enterReg);
        Button registration = view.findViewById(R.id.registrationEmail);

        EditText login = view.findViewById(R.id.email_reg);
        EditText password = view.findViewById(R.id.password_reg);
        EditText passwordConfirmed = view.findViewById(R.id.password_confirmed_reg);

        enter.setOnClickListener(v -> navController.navigate(R.id.action_regFragment_to_authFragment));

        registration.setOnClickListener(v -> handleRegistrationButtonClick(navController, login, password, passwordConfirmed));

    }

    private void handleRegistrationButtonClick(NavController navController, EditText login, EditText password, EditText passwordConfirmed) {
        if (isPasswordValid(password.getText().toString())) {
            password_check = true;
        } else {
            password_check = false;
        }

        if (isConfirmedPassword(password.getText().toString(), passwordConfirmed.getText().toString())) {
            password_confirmed = true;
        } else {
            password_confirmed = false;
        }

        if (password_check && password_confirmed) {
            registerUser(navController, login, password);
        } else if (!password_check) {
            Toast.makeText(getContext(), "Вы написали некорректный пароль, следуйте правилам", Toast.LENGTH_LONG).show();
        } else if (!password_confirmed) {
            Toast.makeText(getContext(), "Пароли не совпадают", Toast.LENGTH_LONG).show();
        }
    }

    private void registerUser(NavController navController, EditText login, EditText password) {
        // Передаем логин и пароль в аргументы RegFormFragment
        Bundle bundle = new Bundle();
        bundle.putString("login", login.getText().toString());
        bundle.putString("password", password.getText().toString());
        // Навигация на RegFormFragment с передачей аргументов
        navController.navigate(R.id.action_regFragment_to_regFormFragment, bundle);
    }


    // Метод для проверки сложности пароля
    private boolean isPasswordValid(String password) {
        Pattern pattern = Pattern.compile("^(?=.*[0-9]).{8,}$");
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    // Метод для проверки совпадения пароля и подтверждения пароля
    private boolean isConfirmedPassword(String password, String password_confirmed) {
        return Objects.equals(password, password_confirmed);
    }
}
