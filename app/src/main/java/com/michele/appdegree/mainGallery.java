package com.michele.appdegree;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.michele.appdegree.adapters.JsonAdapterPhoto;
import com.michele.fragmentexample.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mattia on 24/12/15.
 */
public class mainGallery extends Fragment {

    String url = "http://esamiuniud.altervista.org/tesi/getPhotos.php?idU=";

    ListView list_images;

    ProgressDialog progressDialog;

    ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

    // segue l'interfaccia per lo scambio dei dati con la mainActivity
    ToolbarListener activityCallback;

    public interface ToolbarListener {
        public void onPhotoSelected(String idF);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            activityCallback = (ToolbarListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ToolbarListener");
        }
    }
    // fine interfaccia di collegamento con la main activity

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        globals idUtente = (globals) getActivity().getApplicationContext();
        String idU = idUtente.getId();

        url=url+idU;
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rowView = null;
        rowView = inflater.inflate(R.layout.main_notification, container, false);

        list_images = (ListView) rowView.findViewById(R.id.lista01);

        list.clear();

        new GetData().execute();

        return rowView;
    }

    public class GetData extends AsyncTask<String, Void, JSONArray> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Attendere...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();

        }

        @Override
        protected JSONArray doInBackground(String... params) {

            //initialize
            InputStream is = null;
            String result = "";
            JSONArray jArray = null;
            //http post
            try{
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(url);
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
            }
            catch(Exception e){
                Log.e("log_tag", "Error in http connection "+e.toString());
            }
            //convert response to string
            try{
                BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                result=sb.toString();
            }
            catch(Exception e){
                Log.e("log_tag", "Error converting result "+e.toString());
            }
            //try parse the string to a JSON object
            try{
                jArray = new JSONArray(result);
                Log.d("json arrivo",jArray.toString());
            }
            catch(Exception e){
                Log.e("log_tag", "Error parsing data "+e.toString());
            }
            return jArray;
        }

        @Override
        protected void onPostExecute(JSONArray result)
        {
            super.onPostExecute(result);

            progressDialog.dismiss();

            if(result != null){
                try{

                    for(int i=0;i<result.length();i++){
                        JSONObject c = result.getJSONObject(i);
                        String nome = c.getString("nome");
                        String data = c.getString("data");
                        String idF = c.getString("idF");

                        data = "Scattata il: "+data;

                        HashMap<String, String> map = new HashMap<String, String>();

                        map.put("nome", nome);
                        map.put("data", data);
                        map.put("idF", idF);
                        map.put("fragment", "gallery");

                        list.add(map);

                        ListAdapter adapter = new JsonAdapterPhoto(getActivity(), list,
                                R.layout.mylist,
                                new String[] { "nome", "data" }, new int[] {
                                R.id.item, R.id.testo1});

                        list_images.setAdapter(adapter);
                        list_images.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {
                                //Toast.makeText(getActivity(), "You Clicked at " + list.get(+position).get("nome"), Toast.LENGTH_SHORT).show();
                                activityCallback.onPhotoSelected(list.get(+position).get("idF"));
                            }
                        });
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onResume() {
        //getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        super.onResume();
    }

}
