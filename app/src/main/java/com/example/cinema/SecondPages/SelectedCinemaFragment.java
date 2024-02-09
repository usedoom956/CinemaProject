package com.example.cinema.SecondPages;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cinema.R;
import com.squareup.picasso.Picasso;

public class SelectedCinemaFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_selected_cinema, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String name = bundle.getString("name");
            String placeAndCount = bundle.getString("placeAndCount");
            String date = bundle.getString("date");

            TextView nameAndDateTextView = view.findViewById(R.id.name_and_date_cinema_selected);
            TextView placeAndCountTextView = view.findViewById(R.id.seats);

            CinemaHallView cinemaHallView = view.findViewById(R.id.cinemaHallView);

            nameAndDateTextView.setText(name + "\n" + date);
            placeAndCountTextView.setText(placeAndCount);

        }

        return view;
    }

    private void onSeatClicked(int seatNumber) {
        // Обработайте событие выбора места
    }
}
