package mx.gob.veracruz.tarjetas;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Dialogochat extends DialogFragment {

    private Handler_sqlite insertar;
    private timerchat timerchat;
    private Adaptador adaptador;
    private int lastitem = 0;
    RequestQueue queue;
    ListView lvChat;
    boolean nochat;
    int Scroll;
    String fecha;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        insertar = new Handler_sqlite(getActivity());
        View dialogView = inflater.inflate(R.layout.activity_chat, null);

        final EditText etChat = dialogView.findViewById(R.id.etChat);
        final Button btnCerrar = dialogView.findViewById(R.id.btnCerrar);
        final ImageButton btnChat = dialogView.findViewById(R.id.btnChat);
        lvChat = dialogView.findViewById(R.id.lvChat);
        etChat.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        lvChat.setDivider(null);

        if(queue == null) {
            queue = Volley.newRequestQueue(getContext());
        }

        insertar.abrir();
        insertar.vistos();
        insertar.cerrar();

        adaptador = new Adaptador(getActivity(), GetArrayItems());
        lvChat.setAdapter(adaptador);
        lvChat.setSelection(lastitem);


        builder.setView(dialogView)
                .setMessage("Mensajería");

        //////// Botón para cerrar el chat //////////////////////////////////////////////

        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss();
                insertar.abrir();
                insertar.vistos();
                insertar.cerrar();
            }
        });

        //////// Botón para enviar mensajes de chat //////////////////////////////////////////////

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Mensajes newMensaje = new Mensajes();
                newMensaje.setMensaje(etChat.getText().toString());

                fecha = getFecha();
                insertar.abrir();
                insertar.insertaChat("0", etChat.getText().toString(), fecha, "1");
                insertar.cerrar();
                enviarChat();
                adaptador = new Adaptador(getActivity(), GetArrayItems());
                lvChat.setAdapter(adaptador);
                lvChat.setSelection(lastitem);
                etChat.setText("");

                //dismiss();
            }
        });
        setCancelable(false);
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        nochat= true;
        if(timerchat==null && nochat) {
            timerchat = new timerchat();
            timerchat.execute();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        nochat =false;
    }

    private ArrayList<Entidad> GetArrayItems(){
        ArrayList<Entidad> listItems = new ArrayList<>();

        insertar.abrir();
        Cursor cur= insertar.chat();
        Log.d("Base", "G" + cur.getCount());

        if(cur.getCount()>0) {
            lastitem = cur.getCount();
            listItems.add(new Entidad("","","","0",0, "", ""));//id
            while (cur.moveToNext()) {

                listItems.add(new Entidad(
                        cur.getString(0),
                        cur.getString(0),
                        cur.getString(1)
                                + " \n",
                        "1",
                        Integer.parseInt(cur.getString(2)), cur.getString(3), cur.getString(4)));//id

            }
        }
        insertar.cerrar();
        return listItems;
    }

    public void sendChat( final String id , final String mensaje ){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constantes.API_RUTA_BASE_URL + "sincronizar.php",
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                            insertar.abrir();
                            insertar.modifica(response);
                            insertar.cerrar();
                        //Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                        Log.d("wovebowu04", "getParams: "+ response);
                        adaptador = new Adaptador(getActivity(), GetArrayItems());
                        lvChat.setAdapter(adaptador);
                        lvChat.setSelection(lastitem);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                //Toast.makeText(MainActivity.this, "That didn't work!", Toast.LENGTH_SHORT).show();

            }
        })
        {
            //Parametros post

            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("tabla", "chat");
                params.put("foto", "0");

                params.put("id", String.valueOf(id));
                params.put("mensajes", mensaje);
                params.put("uid", "1");
                params.put("estatus", "2");
                params.put("pass", "ser1m2n3c0s9w8r78u9k4u6y2te4i4");

                return params;
            }
        };

        queue.add(stringRequest);
    }

    public void ejecuta(){

        timerchat timerchat = new timerchat();
        timerchat.execute();
    }

    public class timerchat extends AsyncTask<Void, Integer, Boolean> {

        int tiem = 1;
        @Override
        protected Boolean doInBackground(Void... voids) {

            for(int i = 1; i <= tiem; i++)
            {
                hilos();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(nochat) {
                ejecuta();
            }
            enviarChat();
            insertar.abrir();
            Cursor curnum = insertar.chat();
            if(Scroll<curnum.getCount()){
                adaptador = new Adaptador(getActivity(), GetArrayItems());
                lvChat.setAdapter(adaptador);
                lvChat.setSelection(lastitem);
                Scroll=curnum.getCount();
            }
            insertar.cerrar();

        }

    }

    public void hilos(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Le da formato a la fecha del chat en que se envió
    public String getFecha() {

        Date fechaActual = new Date();
        SimpleDateFormat a = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String FECHA = a.format(fechaActual);
        return FECHA;
    }

    //Envia los chats que se van ingresando los toma de la base y se los manda a la función sendChat()
    private void enviarChat(){
        insertar.abrir();
        Cursor enviarchat = insertar.enviar("0");
        //Toast.makeText(mainActivity, Integer.toString(enviarchat.getCount()), Toast.LENGTH_SHORT).show();

        if(enviarchat.getCount()>0){
            while(enviarchat.moveToNext()) {
                sendChat(enviarchat.getString(0), enviarchat.getString(1));
            }
        }
        enviarchat.close();
        insertar.cerrar();
    }

}
