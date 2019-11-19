package mx.gob.veracruz.tarjetas;

import android.app.Notification;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final Handler_sqlite insertar = new Handler_sqlite(this);

    private String url ="http://10.1.40.157:8081/Comparecencia/json.php";
    private String urlchat ="http://10.1.40.157:8081/Comparecencia/jsonchat.php";
    int ids;
    private times time;
    private Adaptador adaptador;
    private ListView list;
    private TextView tvContenido, tvTitulo, tvNumchats;
    private FloatingActionButton fab, btnChat;
    private String idEliminar = "0";
    private boolean seleccionar = true;
    private Mensajes mensaje = new Mensajes();
    private NotificationHandler notificationHandler;
    private int counter = 0;
    String extra;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Barra de herramientas donde se configura el logo
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setLogo(R.drawable.logo);
        setSupportActionBar(toolbar);

        Intent startServiceIntent = new Intent(getApplicationContext(), Serviciotarjeta.class);
        startService(startServiceIntent);

        Intent intent = new Intent( getApplicationContext(), Tarjeta.class );
        startActivity( intent );
        finish();

        //Asignamos a las variables los elelmentos
        fab = findViewById(R.id.fab);
        btnChat = findViewById(R.id.btnChat);
        list = (ListView) findViewById(R.id.lvLista);
        tvTitulo = findViewById(R.id.tvTitulo);
        tvContenido = findViewById(R.id.tvContenido);
        tvNumchats = findViewById(R.id.tvNumchats);
        tvNumchats.setVisibility(View.GONE);

        extra = "0";

        //Recibe la notificacion
        notificationHandler = new NotificationHandler( this);

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

        /*
        //Carga la lista y la muestra en pantalla
        cargarLista();

        // Al hacer click en un elemento de la lista cambia su estado a leido y al que esta seleccionado en ese momento
        list.setClickable(true);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                idEliminar = GetArrayItems().get(position).getId();

                //Actualiza a seleccionada leer y a ya leidas cuando pierde el foco
                insertar.abrir();
                insertar.leer(idEliminar);
                insertar.leeidas(idEliminar);
                insertar.cerrar();

                cargarLista();

                web( 1, idEliminar);
            }
        });

         */
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

    //Al iniciar actividad se inicia el hilo que se ejecutara para estar en conexión con la base de datos externa
    @Override
    protected void onStart() {
        super.onStart();

        //if(time==null) {
         //   time = new times();
         //   time.execute();
        //}
    }

    //Función que vuelve a ejecutar el temporizador
    public void ejecutar(){

        times time = new times();
        time.execute();
    }

    //Temporizado de 1 segundo
    public void hilo(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Tiempo que se le asigna al temporizador para que se este ejecutando se encuentra en 3 segundos
    public class times extends AsyncTask<Void, Integer, Boolean> {

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
            web( 1,  idEliminar);
            webchat();
            webDatos();
            setTvNumchats();
        }

    }

    ////////////// Actualiza los datos en tiempo real por si hay un cambio de texto en el servidor ///////
    public void web(final int parametro, final String idSel ){
        System.gc();

        ids = Integer.parseInt(idSel);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {
                            databases(response, parametro);
                            //Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();

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
                params.put("id", String.valueOf(ids));
                params.put("parametro", String.valueOf(parametro));
                params.put("id2", idEliminar);
                params.put("pass", "ser1m2n3c0s9w8r78u9k4u6y2te4i4");

                Log.d("", "getParams: "+ parametro +" id "+ ids +" idEliminar " + idEliminar);

                return params;
            }
        };

        queue.add(stringRequest);
    }

    //Conexión que obteniendo los datos del menú lateral ///////////////////////
    public void webDatos( ){
        System.gc();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonobject = new JSONArray(response);

                            JSONObject seleccion = jsonobject.getJSONObject(0);

                            if (seleccion.getString("sel").equals("2")) {
                                for (int i = 0; i < jsonobject.length(); i++) {
                                    JSONObject pru = jsonobject.getJSONObject(i);

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
                                        //Toast.makeText(notificationHandler, idEliminar, Toast.LENGTH_SHORT).show();
                                        adaptador = new Adaptador(getApplicationContext(), GetArrayItems()); //adptador
                                        list.setAdapter(adaptador); //Listview
                                    }
                                }
                            }
                            //Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                //Toast.makeText(MainActivity.this, "That didn't work!", Toast.LENGTH_SHORT).show();

            }
        });
        queue.add(stringRequest);
    }
    public void databases(String json, int parametro) throws JSONException {

            JSONArray jsonobject = new JSONArray(json);
                json = json.trim();
                if (!json.equals("")) {
                    JSONObject pru = jsonobject.getJSONObject(0);
                    tvTitulo.setText(pru.getString("TITULO") + "\n" + pru.getString("NOMBRE"));
                    tvContenido.setText(pru.getString("CONTENIDO"));
                }

    }

    private ArrayList<Entidad> GetArrayItems(){
        ArrayList<Entidad> listItems = new ArrayList<>();
        insertar.abrir();
        Cursor cur= insertar.obtener();
        //Log.d("DATABASE", cur.getString(1));
        //Toast.makeText(this, Integer.toString(cur.getCount()), Toast.LENGTH_SHORT).show();
        if(cur.getCount()>0) {
            cur.moveToFirst();
            if(seleccionar) {
                idEliminar = cur.getString(0);
                web(1, idEliminar);
                seleccionar= false;
                insertar.leer(idEliminar);
                insertar.leeidas(idEliminar);
            }
            cur = insertar.obtener();

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
                        cur.getString(5)
                        + " \n" + tema
                        ,
                        cur.getString(4), 0));//id
            }
        }else{
            idEliminar = "0";
            tvTitulo.setText("");
            tvContenido.setText("");

        }
        cur.close();

        insertar.close();

        return listItems;
    }

    public void enviarMensaje(Mensajes newMensaje){
        mensaje = newMensaje;
    }


    /////////// Obtiene los datos del chat del servidor y llama a la funcion datachat para que los guarde  ////////////////////////
    public void webchat(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlchat,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {
                            datachat(response);
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
                params.put("pass", "ser1m2n3c0s9w8r78u9k4u6y2te4i4");
                return params;
            }
        };

        queue.add(stringRequest);
    }

    /////////////Guarda en la base de datos los datos obtenidos del chat ////////////////////
    public void datachat(String json) throws JSONException {


        //Recibimos el JSON con los datos que llegan con los mensajes

            JSONArray jsonobject = new JSONArray(json);

            for (int i = 0; i < jsonobject.length(); i++) {
                JSONObject pru = jsonobject.getJSONObject(i);

                //Extraemos los datos y los asignamos a las variables correspondientes
                String id = pru.getString("idChat");
                String mensajes = pru.getString("mensajes");
                String fecha = pru.getString("fecha");
                String destinatario = pru.getString("destinatario");

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
                    deshabilitar(id, 2);

                    //Cuando llega un mensaje nuevo manda una notificación
                    Notification.Builder nb = notificationHandler.createNotification(fecha, mensajes, true);
                    notificationHandler.getManager().notify(1, nb.build());
                    //notificationHandler.publishNotificationSummaryGroup(true);

                    //Cuando llega un mensaje nuevo lo muestra en pantalla agregandolo al listview por medio del adaptador
                    cargarLista();
                    setTvNumchats();

                } else {
                    web(1, idEliminar);
                }
            }
    }

    private void setTvNumchats(){

        insertar.abrir();
        Cursor cursor = insertar.numchats();

        if(cursor.getCount()>0){
            tvNumchats.setVisibility(View.VISIBLE);
            tvNumchats.setText(""+cursor.getCount());
        }else{
            tvNumchats.setVisibility(View.GONE);
            tvNumchats.setText("");
        }

        insertar.cerrar();
    }

    //Conexión para cambiar el estado en el servidor de los que ya se eliminaron /////////////////////////////////////
    public void deshabilitar(final String idel, final int id){

        String chat;

        if(id==1){
            chat = "http://10.1.40.157:8081/Comparecencia/actu.php?id="+idel;
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
                            insertar.abrir();
                            insertar.eliminar(response);
                            insertar.cerrar();
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

    private void cargarLista(){
        adaptador = new Adaptador(getApplicationContext(), GetArrayItems()); //adptador
        list.setAdapter(adaptador); //Listview
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            insertar.abrir();
            insertar.eliminarChat();
            insertar.cerrar();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}