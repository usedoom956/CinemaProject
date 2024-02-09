package com.example.cinema.SecondPages;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.cinema.SecondPages.CinemaHelpClasses.CinemaAdapter;
import com.example.cinema.SecondPages.CinemaHelpClasses.CinemaDataClass;
import com.example.cinema.SecondPages.CinemaHelpClasses.CinemaDatabaseHelper;
import com.example.cinema.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CinemaFragment extends Fragment {

    private List<CinemaDataClass> cinemaList;
    private List<CinemaDataClass> originalCinemaList;

    private CinemaDatabaseHelper dbHelper;

    private CinemaAdapter adapter;
    private ListView cinemaListView;
    private boolean up = false;
    private boolean down = false;

    private ImageView upArrow;
    private ImageView downArrow;

    private SearchView search;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference cinemaRef = database.getReference("cinema_data");

    private SQLiteDatabase db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cinema, container, false);

        dbHelper = new CinemaDatabaseHelper(requireContext());

        try {
            db = dbHelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        cinemaList = dbHelper.getAllCinema();
        originalCinemaList = new ArrayList<>(cinemaList);
        getCinema();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        upArrow = view.findViewById(R.id.upForTime);
        downArrow = view.findViewById(R.id.downForTime);
        CardView time = view.findViewById(R.id.time);
        search = view.findViewById(R.id.search);
        cinemaListView = view.findViewById(R.id.cinemaList);

        search.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_VARIATION_URI);
        time.setOnClickListener(view1 -> filterByTime());
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                filterByName(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                filterByName(newText);
                return true;
            }
        });

        cinemaListView.setOnItemClickListener((parent, listView, position, id) -> {
            // Получение выбранного элемента из адаптера
            CinemaDataClass selectedCinema = adapter.getItem(position);

            // Здесь можно выполнить действия в зависимости от выбранного элемента
            if (selectedCinema != null) {
                // Создаем новый фрагмент, передавая ему данные (например, ID выбранного кинотеатра)
                SelectedCinemaFragment selectedCinemaFragment = new SelectedCinemaFragment();
                Bundle bundle = new Bundle();
                bundle.putString("name", selectedCinema.getName());
                bundle.putString("date", selectedCinema.getDate());
                bundle.putInt("place", selectedCinema.getFreeSeats());
                bundle.putString("cinemaId", selectedCinema.getVenue()); // Необходимые данные
                selectedCinemaFragment.setArguments(bundle);

                // Заменяем текущий фрагмент на новый
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, selectedCinemaFragment); // R.id.fragment_container - это ID вашего контейнера фрагментов
                transaction.addToBackStack(null); // Добавляем транзакцию в стек возврата
                transaction.commit();
            }
        });


        filterUniqueCinemas();
    }

    public void getCinema() {
        cinemaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<CinemaDataClass> cinema = new ArrayList<>();

                for (DataSnapshot cinemaSnapshot : dataSnapshot.getChildren()) {
                    CinemaDataClass cinemaDataClass = cinemaSnapshot.getValue(CinemaDataClass.class);
                    if (cinemaDataClass != null) {
                        cinema.add(cinemaDataClass);
                    }
                }

                dbHelper.loadCinemaFromFirebase(cinema);
                addCinema();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void addCinema() {
        if (getContext() == null) {
            return;
        }
        CinemaDataClass cinema1 = new CinemaDataClass("Кинотеатр 1", "Красная площадь", 20, "2022-02-14", "https://firebasestorage.googleapis.com/v0/b/cinema-a7a8c.appspot.com/o/cinema_images%2Fimage_1.jfif?alt=media&token=f64abbd7-521d-45eb-a4d8-6730d150e37f");
        CinemaDataClass cinema2 = new CinemaDataClass("Кинотеатр 2", "Октябрьская", 20, "2022-02-15", "https://example.com/cinema2.jpg");

        if (dbHelper.isCinemaUnique(cinema1.getName())) {
            cinemaRef.child(cinema1.getName().replace(".", "_")).setValue(cinema1);
        }

        if (dbHelper.isCinemaUnique(cinema2.getName())) {
            cinemaRef.child(cinema2.getName().replace(".", "_")).setValue(cinema2);
        }


        dbHelper.deleteDuplicateCinemas();
        cinemaList.clear();
        cinemaList.addAll(dbHelper.getAllCinema());
        adapter = new CinemaAdapter(requireContext(), R.layout.list_item_cinema, cinemaList);

        cinemaListView.setAdapter(adapter);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    protected void filterUniqueCinemas() {
        Set<CinemaDataClass> uniqueCinemas = new HashSet<>(cinemaList);

        cinemaList.clear();
        cinemaList.addAll(uniqueCinemas);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    protected void filterByTime() {
        Comparator<CinemaDataClass> timeComparator = new Comparator<CinemaDataClass>() {
            @Override
            public int compare(CinemaDataClass cinema1, CinemaDataClass cinema2) {
                // Предположим, у вас есть поле date в формате строки (String)
                String time1 = cinema1.getDate();
                String time2 = cinema2.getDate();

                // Сравнение по времени
                return time1.compareTo(time2);
            }
        };

        if (down) {
            Collections.sort(cinemaList, timeComparator);
            up = true;
            down = false;
            upArrow.setVisibility(View.VISIBLE);
            downArrow.setVisibility(View.INVISIBLE);
        } else {
            Collections.sort(cinemaList, Collections.reverseOrder(timeComparator));
            up = false;
            down = true;
            upArrow.setVisibility(View.INVISIBLE);
            downArrow.setVisibility(View.VISIBLE);
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }



    protected void filterByName(String query) {
        List<CinemaDataClass> filteredList = new ArrayList<>();

        for (CinemaDataClass cinema : originalCinemaList) {
            // Приведение имени человека и введенного запрос к нижнему регистру и проверка, содержится ли запрос в имени диска
            if (cinema.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(cinema);
            }
        }

        // Обновление списка людей с отфильтрованным списком
        cinemaList.clear();
        cinemaList.addAll(filteredList);

        // Обновление адаптера для отображения изменений
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
