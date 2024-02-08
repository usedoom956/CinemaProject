package com.example.cinema.SecondPages.CinemaHelpClasses;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cinema.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CinemaAdapter extends ArrayAdapter<CinemaDataClass> {
    private Context context;
    private int resource;

    public CinemaAdapter(@NonNull Context context, int resource, @NonNull List<CinemaDataClass> cinemaList) {
        super(context, resource, cinemaList);
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(resource, null);
        }
        CinemaDataClass cinema = getItem(position);

        ImageView cinemaImageView = view.findViewById(R.id.imageViewCinema);
        TextView nameTextView = view.findViewById(R.id.name_cinema);
        TextView dateTextView = view.findViewById(R.id.date);
        TextView placeAndCountTextView = view.findViewById(R.id.placeAndCount);

        if (cinema != null) {
            nameTextView.setText(cinema.getName());
            dateTextView.setText("Время: " + cinema.getDate());
            placeAndCountTextView.setText("Место: " + cinema.getVenue() + "\nКоличество свободных мест: " + cinema.getFreeSeats());

            if (cinema.getImagePath() != null && !cinema.getImagePath().isEmpty()) {

                Picasso.get().load(cinema.getImagePath()).into(cinemaImageView);
            } else {

                cinemaImageView.setImageResource(R.drawable.baseline_mms_24);
            }
        }

        return view;
    }
}

