package mx.gob.veracruz.tarjetas;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Tarjeta extends AppCompatActivity {
    private final Handler_sqlite base = new Handler_sqlite(this);

    RequestQueue queue;

    //Declaramos items del activity
    private Adaptador adaptador;
    private ListView list;
    private TextView tvContenido, tvTitulo, tvNumchats;
    private FloatingActionButton fab, btnChat;
    private boolean seleccionar = true;
    private String idEliminar, extra;
    private NotificationHandler notificationHandler;
    private timer timer;
    private Mensajes mensaje = new Mensajes();
    private String URL ="http://10.1.40.157:8081/Comparecencia/json.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setLogo(R.drawable.logo);
        setSupportActionBar(toolbar);
        timer  timer = null;

        fab = findViewById(R.id.fab);
        btnChat = findViewById(R.id.btnChat);
        list = (ListView) findViewById(R.id.lvLista);
        tvTitulo = findViewById(R.id.tvTitulo);
        tvContenido = findViewById(R.id.tvContenido);
        tvNumchats = findViewById(R.id.tvNumchats);
        tvNumchats.setVisibility(View.GONE);

        extra = "0";

        //Recibe la notificacion
        //notificationHandler = new NotificationHandler( this);

        //Abre la aplicación en la notificación
        extra = getIntent().getStringExtra("chat");
        if(extra != null){
            if(extra.equals("1")){
                Dialogochat dialogochat= new Dialogochat();
                dialogochat.show(getSupportFragmentManager(), null);
            }else{

            }
        }

        if(queue == null) {
            queue = Volley.newRequestQueue(getApplicationContext());
        }



        cargarLista();
        if(timer==null) {
           timer = new timer();
           timer.execute();
        }

        list.setClickable(true);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                idEliminar = GetArrayItems().get(position).getId();

                //Actualiza a seleccionada leer y a ya leidas cuando pierde el foco
                base.abrir();
                base.leer(idEliminar);
                base.leeidas(idEliminar);
                base.cerrar();
                webs(idEliminar);
                cargarLista();
            }
        });

        //Boton para elimiar los mensajes y cambiar el estado en internet
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionar = true;
                deshabilitar(idEliminar, 1);

            }
        });

        //Boton que abre el chat en un dialogo
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogochat dialogochat= new Dialogochat();
                dialogochat.show(getSupportFragmentManager(), null);
            }
        });

    }

    //Tiempo que se le asigna al temporizador para que se este ejecutando se encuentra en 3 segundos
    public class timer extends AsyncTask<Void, Integer, Boolean> {

        int tiempo = 1;
        @Override
        protected Boolean doInBackground(Void... voids) {

            for(int i = 1; i <= tiempo; i++)
            {
                hilo();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            ejecutar();
            cargarLista();
            setTvNumchats();
            webs(idEliminar);
        }
    }

    //Función que vuelve a ejecutar el temporizador
    public void ejecutar(){
        timer timer = new timer();
        timer.execute();
    }

    //Temporizado de 1 segundo
    public void hilo(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void cargarLista(){
        adaptador = new Adaptador(getApplicationContext(), GetArrayItems()); //adptador
        list.setAdapter(adaptador); //Listview
    }

    //Llena el array list para el adaptador si no encuetra ninguna nota pone en blanco los textos de la aplicación
    private ArrayList<Entidad> GetArrayItems(){
        ArrayList<Entidad> listItems = new ArrayList<>();
        base.abrir();
        Cursor cur= base.obtener();
        if(cur.getCount()>0) {
            cur.moveToFirst();
            if(seleccionar) {
                idEliminar = cur.getString(0);
                seleccionar= false;
                base.leer(idEliminar);
                base.leeidas(idEliminar);
            }
            cur = base.obtener();
            while (cur.moveToNext()) {

                int temaTamano = cur.getString(1).length();

                String tema;

                if(temaTamano > 49){
                    tema = cur.getString(1).substring(0, 50)+ "...";
                }else{
                    tema = cur.getString(1);
                }

                listItems.add(new Entidad(
                        cur.getString(0),
                        cur.getString(1),
                cur.getString(5) + " \n" + tema,
                        cur.getString(4), 0));//id
            }
            webs(idEliminar);
        }else{
            tvContenido.setText("");
            tvTitulo.setText("");
            idEliminar= "0";
        }
        cur.close();

        base.close();

        return listItems;
    }

    /////////////    Borra de la base las notas que se van eliminando y manda al servidor el estado de que ya fue borrado
    public void deshabilitar(final String idel, final int id){

        String chat;

        if(id==1){
            chat = "http://10.1.40.157:8081/Comparecencia/actu.php?id="+idel+"&selec=1";
        }else{
            chat = "http://10.1.40.157:8081/Comparecencia/actuchat.php?id="+idel;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, chat,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        response = response.trim();

                        //Despues de cambiar el estado en el servidor lo eliminamos de la base de datos
                        if(id == 1) {
                            base.abrir();
                            base.eliminar(response);
                            base.cerrar();
                            //Eliminamos de la vista el mensaje eliminado
                            cargarLista();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest);
    }

    /////// Esta función es la que está actualizando la informacion que se va modificando en tiempor real en el servidor

    public void webs(final String idSel ){
        System.gc();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {

                            if(!response.trim().equals("null")) {
                                databases(response);
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
                params.put("id", idSel);
                params.put("pass", "ser1m2n3c0s9w8r78u9k4u6y2te4i4");

                return params;
            }
        };

        queue.add(stringRequest);
    }

    public void databases(String json) throws JSONException {

        JSONArray jsonobject = new JSONArray(json);
        json = json.trim();
        if (!json.equals("")) {
            JSONObject pru = jsonobject.getJSONObject(0);
            tvTitulo.setText(pru.getString("TITULO") + "\n" + pru.getString("NOMBRE"));
            tvContenido.setText(pru.getString("CONTENIDO"));
        }

    }


    //////////////////// numero de mensajes en la pantalla ////////
    private void setTvNumchats(){

        base.abrir();
        Cursor cursor = base.numchats();

        if(cursor.getCount()>0){
            tvNumchats.setVisibility(View.VISIBLE);
            tvNumchats.setText(""+cursor.getCount());
        }else{
            tvNumchats.setVisibility(View.GONE);
            tvNumchats.setText("");
        }
        base.cerrar();
    }

    public void enviarMensaje(Mensajes newMensaje){
        mensaje = newMensaje;
    }
}
