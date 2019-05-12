package com.example.haboker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.haboker.XML.Segments;
import com.example.haboker.XML.SimpleXmlRequest;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RequestQueue queue = Volley.newRequestQueue(this);
        SimpleXmlRequest<Segments> stringRequest = new SimpleXmlRequest<>(Request.Method.GET, "http://eco99fm.maariv.co.il/onair/talAndAviadXml.aspx", Segments.class,
                new Response.Listener<Segments>() {
                    @Override
                    public void onResponse(Segments response) {
                        System.out.println(response);
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
}
