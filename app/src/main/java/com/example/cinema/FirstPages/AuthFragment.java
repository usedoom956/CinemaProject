package com.example.cinema.FirstPages;

import android.content.Context;
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
import com.google.firebase.auth.FirebaseAuth;

public class AuthFragment extends Fragment {

    public interface OnAuthorizationSuccessListener {
        void onAuthorizationSuccess();
    }

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private OnAuthorizationSuccessListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnAuthorizationSuccessListener) {
            listener = (OnAuthorizationSuccessListener) context;
        } else {
            throw new ClassCastException(context.toString() + " должен быть OnAuthorizationSuccessListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavController navController = Navigation.findNavController(view);

        // Находим кнопки и поля ввода по их идентификаторам
        Button enter = view.findViewById(R.id.enterAuth);
        Button registration = view.findViewById(R.id.regAuth);
        EditText login = view.findViewById(R.id.email_auth);
        EditText password = view.findViewById(R.id.password_auth);

        // Обработчик нажатия на кнопку входа
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLoginButtonClick(login, password);
            }
        });

        // Обработчик нажатия на кнопку регистрации
        registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_authFragment_to_regFragment);
            }
        });
    }

    private void handleLoginButtonClick(EditText login, EditText password) {
        // Если логин и пароль введены, то выполняются дальнейшие действия
        if (isInputValid(login, password)) {
            mAuth.signInWithEmailAndPassword(login.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Вызываем метод интерфейса, оповещая активити об успешной авторизации
                            listener.onAuthorizationSuccess();
                        } else {
                            Toast.makeText(getContext(), "Логин или пароль введены неверно", Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "Введите логин и пароль", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isInputValid(EditText login, EditText password) {
        return !login.getText().toString().isEmpty() && !password.getText().toString().isEmpty();
    }
}
