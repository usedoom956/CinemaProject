package com.example.cinema.SecondPages;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cinema.R;
import com.example.cinema.SecondPages.CinemaHall.CinemaHallView;
import com.example.cinema.SecondPages.CinemaHelpClasses.CinemaDataClass;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SelectedCinemaFragment extends Fragment {
    private CinemaDataClass cinemaData;
    private CinemaHallView cinemaHallView;
    private int numRows = 4;  // Количество рядов
    private int numSeatsPerRow = 5;  // Количество мест в ряде
    private int selectedRow = -1;
    private int selectedSeat = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_selected_cinema, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String cinemaName = bundle.getString("name");
            String placeAndCount = bundle.getString("placeAndCount");
            String date = bundle.getString("date");

            TextView nameAndDateTextView = view.findViewById(R.id.name_and_date_cinema_selected);
            TextView placeAndCountTextView = view.findViewById(R.id.seats);
            cinemaHallView = view.findViewById(R.id.cinemaHallView);

            DatabaseReference cinemaRef = FirebaseDatabase.getInstance().getReference("cinema_data").child(cinemaName);
            cinemaRef.child("isReserved").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        // Если массива нет, инициализируем и сохраняем в Firebase
                        initializeSeats(cinemaName);
                    } else {
                        // Массив уже существует, загружаем данные и отображаем их
                        List<Boolean> isReserved = dataSnapshot.getValue(new GenericTypeIndicator<List<Boolean>>() {});
                        // Применение данных к CinemaHallView
                        getCinemaDataFromFirebase(cinemaName, isReserved);
                        CinemaDataClass cinemaData2 = new CinemaDataClass();
                        cinemaData2.setIsReserved(isReserved);
                        cinemaHallView.setCinemaData(cinemaData2);


                        // Установка слушателя на выбор места
                        cinemaHallView.setOnSeatClickListener((row, seat) -> onSeatClicked(cinemaName, row, seat));

                        Button reserveButton = view.findViewById(R.id.materialButton);
                        reserveButton.setOnClickListener(v -> onReserveButtonClicked(cinemaName, isReserved));

                        nameAndDateTextView.setText(cinemaName + "\n" + date);
                        placeAndCountTextView.setText(placeAndCount);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Обработка ошибок при чтении из базы данных
                }
            });

        }

        return view;
    }

    private void onReserveButtonClicked(String cinemaName, List<Boolean> isReserved) {
        // Проверяем, свободно ли выбранное место
        if (isSeatAvailable(isReserved, selectedRow, selectedSeat)) {
            // Если место свободно, обновляем статус
            updateSeatStatus(cinemaName, selectedRow, selectedSeat);

            // Обновляем данные в CinemaHallView после успешного бронирования
            getCinemaDataFromFirebase(cinemaName, isReserved);

            // Переход на фрагмент CinemaFragment
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.popBackStack(); // Убедимся, что стек фрагментов очищен перед переходом

            // Создание нового фрагмента CinemaFragment
            CinemaFragment cinemaFragment = new CinemaFragment();
            Bundle args = new Bundle();
            args.putString("cinemaName", cinemaName);
            cinemaFragment.setArguments(args);

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, cinemaFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

            // Вывод сообщения об успешной блокировке
            Toast.makeText(requireContext(), "Место успешно забронировано!", Toast.LENGTH_SHORT).show();
        } else {
            // Место уже занято, выполните необходимые действия (например, показать сообщение)
            Toast.makeText(requireContext(), "Выбранное место уже занято!", Toast.LENGTH_SHORT).show();
        }
    }


    private boolean isSeatAvailable(List<Boolean> occupiedSeats, int row, int seat) {
        if (occupiedSeats != null && row >= 0 && row < numRows && seat >= 0 && seat < numSeatsPerRow) {
            int seatNumber = calculateSeatNumber(row, seat);
            return !occupiedSeats.get(seatNumber); // Возвращаем true, если место свободно
        }
        return false; // Возвращаем false по умолчанию, если передан некорректный массив или координаты места
    }

    private CinemaDataClass getCinemaDataFromFirebase(String cinemaName, List<Boolean> isReserved) {
        DatabaseReference cinemaRef = FirebaseDatabase.getInstance().getReference("cinema_data").child(cinemaName);

        // Пример использования ValueEventListener для получения данных
        cinemaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    cinemaData = dataSnapshot.getValue(CinemaDataClass.class);
                    cinemaData.setIsReserved(isReserved);
                    // Обновите отображение, например:
                    setCinemaData(cinemaData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок при чтении из базы данных
            }
        });

        return new CinemaDataClass();  // Заглушка, пока данные не загружены
    }


    private void setCinemaData(CinemaDataClass cinemaData) {
        this.cinemaData = cinemaData;
    }

    private void onSeatClicked(String cinemaName, int row, int seat) {
        selectedRow = row;
        selectedSeat = seat;

        cinemaHallView.setSelectedSeat(row, seat);
    }

    private void initializeSeats(String cinemaName) {
        DatabaseReference cinemaRef = FirebaseDatabase.getInstance().getReference("cinema_data").child(cinemaName);

        // Инициализация массива мест
        List<Boolean> initialSeats = new ArrayList<>();
        for (int i = 0; i < numRows * numSeatsPerRow; i++) {
            initialSeats.add(false);
        }

        // Сохранение в Firebase
        cinemaRef.child("isReserved").setValue(initialSeats);
    }

    private void updateSeatStatus(String cinemaName, int row, int seat) {
        DatabaseReference cinemaRef = FirebaseDatabase.getInstance().getReference("cinema_data").child(cinemaName);

        cinemaRef.child("isReserved").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Boolean> isReservedList = dataSnapshot.getValue(new GenericTypeIndicator<List<Boolean>>() {});
                    if (isReservedList != null && row >= 0 && row < numRows && seat >= 0 && seat < numSeatsPerRow) {
                        int seatNumber = calculateSeatNumber(row, seat);

                        // Проверка, что место не забронировано
                        if (!isReservedList.get(seatNumber)) {
                            // Место не забронировано, бронируем его
                            isReservedList.set(seatNumber, true);
                            cinemaRef.child("isReserved").setValue(isReservedList);
                        } else {
                            // Место уже забронировано, выполните необходимые действия
                            Toast.makeText(requireContext(), "Место уже забронировано", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обработка ошибок при чтении из базы данных
            }
        });
    }

    private int calculateSeatNumber(int row, int seat) {
        return row * numSeatsPerRow + seat;
    }
}
