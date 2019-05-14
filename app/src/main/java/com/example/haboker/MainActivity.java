package com.example.haboker;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.haboker.XML.Segment;
import com.example.haboker.XML.Segments;
import com.example.haboker.XML.SimpleXmlRequest;
import com.google.android.exoplayer2.Player;

public class MainActivity extends AppCompatActivity implements PlayerListener, SeekBar.OnSeekBarChangeListener {
    private RecyclerView rv;
    private SegmentsAdapter segmentsAdapter;
    private SegmentPlayer segmentPlayer;
    private ImageButton playPauseButton;
    private boolean isSeeking = false;
    private SeekBar seekBar;
    private TextView progressText;
    private TextView durationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        segmentPlayer = new SegmentPlayer(getApplicationContext(), this);
        playPauseButton = findViewById(R.id.playpuase);
        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        progressText = findViewById(R.id.progressText);
        durationText = findViewById(R.id.durationText);

        rv = findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        segmentsAdapter = new SegmentsAdapter();
        rv.setAdapter(segmentsAdapter);

        RequestQueue queue = Volley.newRequestQueue(this);
        SimpleXmlRequest<Segments> stringRequest = new SimpleXmlRequest<>(Request.Method.GET, "http://eco99fm.maariv.co.il/onair/talAndAviadXml.aspx", Segments.class,
                new Response.Listener<Segments>() {
                    @Override
                    public void onResponse(Segments response) {
                        segmentsAdapter.setSegments(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error.toString());
                    }
                }
        );
        queue.add(stringRequest);
    }

    private void onSegmentClicked(Segment segment) {
        System.out.println("Segment clicked!! " + segment.RecordedProgramsName);
        segmentPlayer.start(segment.RecordedProgramsDownloadFile);
    }

    public void playPauseButtonPressed(View v) {
        segmentPlayer.playPause();
    }

    public void jumpForwardPressed(View v) {
        segmentPlayer.jumpForward();
    }

    public void jumpBackwardPressed(View v) {
        segmentPlayer.jumpBackwards();
    }

    @Override
    public void onPlaying() {
        playPauseButton.setImageResource(R.drawable.exo_controls_pause);
    }

    @Override
    public void onPaused() {
        playPauseButton.setImageResource(R.drawable.exo_controls_play);
    }

    @Override
    public void onBuffering() {

    }

    @Override
    public void onEnded() {

    }

    @Override
    public void onTimeUpdate(long position, long buffered, long duration) {
        if (!isSeeking) {
            int progressPosition = (int) (((float) position / (float) duration) * 100);
            int bufferedPosition = (int) (((float) buffered / (float) duration) * 100);
            seekBar.setProgress(progressPosition);
            seekBar.setSecondaryProgress(bufferedPosition);
        }
        progressText.setText(getReadbleTime(position));
        durationText.setText(getReadbleTime(duration));
    }

    private String getReadbleTime(long millis) {
        String result = "";

        int h = (int) ((millis / 1000) / 3600);
        int m = (int) (((millis / 1000) / 60) % 60);
        int s = (int) ((millis / 1000) % 60);

        if (h > 0) {
            result += h + ":";
        }
        String mStr = "0" + m;
        String sStr = "0" + s;
        mStr = mStr.substring(mStr.length() - 2);
        sStr = sStr.substring(sStr.length() - 2);

        result += mStr + ":" + sStr;

        return result;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // Stub
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isSeeking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        float progress = seekBar.getProgress();
        System.out.println("seek! " + progress);
        segmentPlayer.seek(progress / 100);
        isSeeking = false;
    }

    private class SegmentsAdapter extends RecyclerView.Adapter<SegmentsAdapter.SegmentViewHolder> implements View.OnClickListener {
        public Segments segments;

        public void setSegments(Segments segments) {
            this.segments = segments;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public SegmentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View segmentView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.segment, viewGroup, false);
            segmentView.setOnClickListener(this);
            return new SegmentViewHolder(segmentView);
        }

        @Override
        public void onBindViewHolder(@NonNull SegmentViewHolder segmentViewHolder, int i) {
            Segment segment = segments.list.get(i);
            segmentViewHolder.title.setText(segment.RecordedProgramsName);
        }

        @Override
        public int getItemCount() {
            if (segments == null) {
                return 0;
            }
            return segments.list.size();
        }

        @Override
        public void onClick(View view) {
            int pos = rv.getChildLayoutPosition(view);
            onSegmentClicked(segments.list.get(pos));
        }


        class SegmentViewHolder extends RecyclerView.ViewHolder {
            TextView title;

            SegmentViewHolder(View segmentView) {
                super(segmentView);
                title = segmentView.findViewById(R.id.title);
            }
        }
    }
}
