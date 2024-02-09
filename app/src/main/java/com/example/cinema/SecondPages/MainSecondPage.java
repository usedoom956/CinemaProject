package com.example.cinema.SecondPages;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.cinema.R;
import com.example.cinema.SecondPages.AdminPanel.AdminPanelFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainSecondPage extends AppCompatActivity {
    private DrawerLayout drawerLayout;

    private TextView userNameTextView;
    private TextView userEmailTextView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_second_page_activity);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        View headerView = ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);
        userNameTextView = headerView.findViewById(R.id.userNameTextView);
        userEmailTextView = headerView.findViewById(R.id.userEmailTextView);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Получаем информацию о текущем пользователе
            String userEmail = currentUser.getEmail();
            userEmailTextView.setText(userEmail);

            String userId = currentUser.getUid();

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user_data").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String userName = dataSnapshot.child("name").getValue(String.class);
                        if (userName != null && !userName.isEmpty()) {
                            userNameTextView.setText(userName);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Обработка ошибок при чтении из базы данных
                }
            });
        }

        checkUserPrivileges();

        navigationView.setNavigationItemSelectedListener(item -> {
            // Обрабатываем нажатие на элемент меню
            switch (item.getItemId()) {
                case R.id.nav_author:
                    replaceFragment(new AuthorFragment());
                    break;
                case R.id.nav_prog:
                    replaceFragment(new ProgramFragment());
                    break;
                case R.id.nav_git:
                    replaceFragment(new GitFragment());
                    break;
                case R.id.nav_home:
                    replaceFragment(new CinemaFragment());
                    break;
                case R.id.nav_instruction:
                    replaceFragment(new InstructionFragment());
                    break;
                case R.id.nav_admin:
                    replaceFragment(new AdminPanelFragment());
                    break;
                case R.id.nav_input:
                    finish();
                    break;
            }

            // Закрываем боковое меню после нажатия
            drawerLayout.closeDrawer(GravityCompat.START);

            return true;
        });
        replaceFragment(new CinemaFragment());
    }

    private void replaceFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
    private void checkUserPrivileges() {
        // Получаем UID текущего пользователя
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Строим путь к данным о привилегиях пользователя в базе данных
        DatabaseReference userRootRef = FirebaseDatabase.getInstance().getReference()
                .child("user_data")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean haveRoot = dataSnapshot.child("adminRoot").getValue(Boolean.class);

                if (haveRoot != null && haveRoot) {
                    // У пользователя есть привилегии, отображаем элемент "admin" в боковом меню
                    setAdminMenuItemVisibility(true);
                } else {
                    // У пользователя нет привилегий, скрываем элемент "admin" в боковом меню
                    setAdminMenuItemVisibility(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Обработка ошибок, если не удалось получить данные из базы данных
            }
        });
    }


    private void setAdminMenuItemVisibility(boolean isVisible) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        MenuItem adminMenuItem = navigationView.getMenu().findItem(R.id.nav_admin);

        adminMenuItem.setVisible(isVisible);
        adminMenuItem.setEnabled(isVisible);
    }

}

