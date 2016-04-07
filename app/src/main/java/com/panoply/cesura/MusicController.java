package com.panoply.cesura;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MusicController extends MediaController {

    public static final String TAG = "MusicController";

    public MusicController(Context context) {
        super(context);
    }

    @Override
    public void hide() {}

    public void setAnchorView(View view, String songTitle, String artistName){
        super.setAnchorView(view);
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        TextView title = new TextView(getContext());
        title.setText(songTitle);
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setPadding(0, 0, 5, 0);
        linearLayout.addView(title);
        TextView artist = new TextView(getContext());
        artist.setText(" - " + artistName);
        artist.setTextColor(Color.WHITE);
        artist.setTextSize(12);
        linearLayout.addView(artist);
        addView(linearLayout);
    }

    public void setAnchorView(View view, Song song) {
        super.setAnchorView(view);
        TextView textView = new TextView(getContext());
        textView.setText(song.getTitle());
        textView.setTextColor(Color.WHITE);
        addView(textView);
    }

    @Override
    public void setAnchorView(View view) {
        super.setAnchorView(view);
    }


}
