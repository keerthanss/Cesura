package com.panoply.cesura;

import android.content.Context;
import android.widget.MediaController;

public class MusicController extends MediaController {
    public MusicController(Context context) {
        super(context);
    }

    @Override
    public void hide() {}
}
