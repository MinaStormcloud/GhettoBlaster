package com.example.builds.ghettoblaster;

import android.os.Handler;
import android.view.MotionEvent;
import android.widget.Button;
import android.os.Bundle;
import android.widget.*;

import android.content.*;
import android.view.View;
import android.net.Uri;

import java.io.IOException;
import java.util.ArrayList;
import android.media.MediaPlayer;
import java.io.File;

public class MainActivity extends PermissionsManager implements MediaPlayer.OnPreparedListener, MediaController.MediaPlayerControl {

    private ListView listview;
    private ArrayAdapter adapter;
    private ArrayList list;
    int pos;
    boolean videoCompleted = false;

    private MediaPlayer mediaPlayer;
    private MediaController mCtrl;
    private Handler handler = new Handler();

    TextView textView;
    Button btnSelect, btnStop;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = new ArrayList<String>();
        listview = (ListView) findViewById(R.id.listView);
        textView = (TextView)findViewById(R.id.textView1);
        adapter = new  ArrayAdapter(this,android.R.layout.simple_list_item_1, list);

        btnSelect = (Button)findViewById(R.id.btnSelect);
        btnStop = (Button)findViewById(R.id.btnStop);

        mediaPlayer = new MediaPlayer();
        pos = 0;

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkPermission()) {
                    adapter.clear();
                    textView.setText("");
                    mediaPlayer.reset();
                    pos = 0;
                    Intent mediaIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    mediaIntent.setType("*/*");
                    String[] mimetypes = {"audio/*", "video/*"};
                    mediaIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                    mediaIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    startActivityForResult(Intent.createChooser(mediaIntent, "Choose a file") , 0);
                }
                else
                {
                    requestPermission();
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop();
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (mediaPlayer == null)
                {
                    mediaPlayer = new MediaPlayer();
                }
                if (isPlaying())
                {
                    stop();
                    mediaPlayer.reset();
                }

                if (!isPlaying())
                {
                    stop();
                    mediaPlayer.reset();
                }

                pos = position;
                playFile();
                btnStop.setEnabled(true);
                displayTrackInfo();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoCompleted = true;
                mediaPlayer.reset();
                playNextTrack(pos);
            }
        });
    }

    public void playFile()
    {
        try
        {
            mediaPlayer.setDataSource(MainActivity.this, Uri.parse(list.get(pos).toString()));
            mediaPlayer.setOnPreparedListener(this);
            mCtrl = new MediaController(MainActivity.this);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        try
        {
            mediaPlayer.prepare();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

        mediaPlayer.start();
    }

    public void displayTrackInfo()
    {
        textView.setText("");
        String selectedItem =(listview.getItemAtPosition(pos).toString());
        File file = new File(selectedItem);
        textView.setText(file.getName());
    }

    public void playNextTrack(int trackIndex){
        pos = trackIndex;

        if (pos < (list.size() - 1)) {
            pos = pos + 1;
            playFile();
            displayTrackInfo();
        }
        else
        {
            stop();
        }
    }

    public void stop()
    {
        btnStop.setEnabled(false);
        mediaPlayer.stop();
        mediaPlayer.reset();
        textView.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            if(null != data)
            {
                if(null != data.getClipData()) // multiple selection
                {
                    for(int i = 0; i < data.getClipData().getItemCount(); i++) {
                        uri = data.getClipData().getItemAt(i).getUri();
                        String s = FileInfo.getPath(getApplicationContext(), uri);
                        File file = new File(s);
                        list.add(file); //gets the whole uri without invalidating files containing illegal characters
                    }
                }
                else
                {
                    uri = data.getData(); // single file selection
                    String s = FileInfo.getPath(getApplicationContext(), uri);
                    File file = new File(s);
                    list.add(file);
                }
                listview.setAdapter(adapter);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try
        {
            if(uri!=null)
            {
                //the MediaController will be hidden after 3 seconds - tap the screen to make it appear again

                mCtrl.show();
            }
        }

        catch(Exception exc)
        {
            exc.printStackTrace();
        }

        if (uri != null &&(!isPlaying()))
        {
            mCtrl.hide();
            stop();
        }

        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mCtrl.setMediaPlayer(this);
        mCtrl.setAnchorView(findViewById(R.id.surfaceView));
        handler.post(new Runnable() {

            public void run() {
                mCtrl.setEnabled(true);
                mCtrl.show();
            }
        });
    }

    //--Pause, resume and end application----------------------------------------------

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    public void onBackPressed()
    {
        super.onBackPressed();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        this.mediaPlayer.stop();
        this.mediaPlayer.release();
        this.mediaPlayer = null;
        handler.removeCallbacksAndMessages(mCtrl);

        super.onDestroy();
    }

    //--MediaPlayerControl methods----------------------------------------------------
    public void start() {
        mediaPlayer.start();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void seekTo(int i) {
        mediaPlayer.seekTo(i);
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public int getBufferPercentage() {
        return 0;
    }

    public boolean canPause() {
        return true;
    }

    public boolean canSeekBackward() {
        return true;
    }

    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
