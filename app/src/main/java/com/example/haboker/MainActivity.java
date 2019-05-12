package com.example.haboker;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.haboker.XML.Segment;
import com.example.haboker.XML.Segments;
import com.example.haboker.XML.SimpleXmlRequest;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SegmentsAdapter segmentsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView rv = findViewById(R.id.rv);
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

    private static class SegmentsAdapter extends RecyclerView.Adapter<SegmentsAdapter.SegmentViewHolder> {
        public Segments segments;

        public void setSegments(Segments segments) {
            this.segments = segments;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public SegmentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View segmentView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.segment, viewGroup, false);
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

        public static class SegmentViewHolder extends RecyclerView.ViewHolder {
            public TextView title;

            public SegmentViewHolder(View segmentView) {
                super(segmentView);
                title = segmentView.findViewById(R.id.title);
            }
        }
    }
}
