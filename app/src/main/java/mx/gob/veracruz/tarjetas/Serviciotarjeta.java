package mx.gob.veracruz.tarjetas;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Serviciotarjeta extends Service {
    private final Handler_sqlite insertar = new Handler_sqlite(this);
    temporizador time;
    RequestQueue queue;
    NotificationHandler  notificationHandler;

    private String URL ="http://10.1.40.157:8081/Comparecencia/rest.php";
    private String chat = "http://10.1.40.157:8081/comparecencia/actu.php";

    @Override
    public void onCreate() {
        super.onCreate();
        notificationHandler = new NotificationHandler( this);
        if(time==null){
            ejecutar();
        }
        if(queue == null) {
            queue = Volley.newRequestQueue(getApplicationContext());
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }


    public class temporizador extends AsyncTask<Void, Integer, Boolean> {
        int tiempo = 1;

        @Override
        protected Boolean doInBackground(Void... voids) {

            for (int i = 1; i <= tiempo; i++) {
                hilo();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            ejecutar();
            web(3);
            web(2);
        }
    }

    public void ejecutar() {

        time = new temporizador();
        time.execute();
    }

    public void hilo() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void web(final int parametro){
        System.gc();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {
                            if(!response.trim().equals("null")) {
                                base(response, parametro);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }



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
                params.put("parametro", String.valueOf(parametro));
                params.put("pass", "ser1m2n3c0s9w8r78u9k4u6y2te4i4");

                return params;
            }
        };

        queue.add(stringRequest);
    }

    private void base(String response, int parametros) throws JSONException  {
        JSONArray jsonobject = new JSONArray(response);
        //Toast.makeText(getApplicationContext(), ""+response, Toast.LENGTH_SHORT).show();
        for (int i = 0; i < jsonobject.length(); i++) {
            JSONObject pru = jsonobject.getJSONObject(i);
            if (parametros == 2) {

                String id = pru.getString("ID");
                String tema = pru.getString("TEMA");
                String titulo = pru.getString("TITULO");
                String pregunta = pru.getString("CONTENIDO");
                String perfil = pru.getString("PERFIL");

                insertar.abrir();
                String ide = insertar.ids(id);
                insertar.cerrar();

                if (ide.equals("0")) {
                    insertar.abrir();
                    insertar.insertaJson(id, tema, titulo, pregunta, perfil);
                    insertar.cerrar();
                }
                deshabilitar(2, id);

            } else {

                String id = pru.getString("idChat");
                String mensajes = pru.getString("mensajes");
                String fecha = pru.getString("fecha");
                String destinatario = pru.getString("uid");

                //Verificamos si ya ingresaron el mensaje para evitar repetidos
                insertar.abrir();
                String ide = insertar.idschat(id);
                insertar.cerrar();
                //Toast.makeText(notificationHandler, "Ye", Toast.LENGTH_SHORT).show();
                if (ide.equals("0") && !destinatario.equals("1")) {

                    //Inserta en la base de datos los mensajes que van llegando
                    insertar.abrir();
                    insertar.insertaChat(id, mensajes, fecha, destinatario);
                    insertar.cerrar();

                    //Cuando llega un mensaje nuevo manda una notificación
                    Notification.Builder nb = notificationHandler.createNotification(fecha, mensajes, true);
                    notificationHandler.getManager().notify(1, nb.build());
                }
                deshabilitar(3, id);

            }
        }

    }

    // Modifica la base para evitar se reciban de nuevo los datos
    public void deshabilitar(final int sel, final String id){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, chat+"?id="+id+"&selec="+sel,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        //response = response.trim();
                       //Toast.makeText(getApplicationContext(), ""+sel, Toast.LENGTH_SHORT).show();
                       //Toast.makeText(getApplicationContext(), ""+response, Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest);
    }
}
