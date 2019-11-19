package mx.gob.veracruz.tarjetas;
//2836075738
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.provider.BaseColumns._ID;

public class Handler_sqlite extends SQLiteOpenHelper {

	private String encuesta = "encuesta";
	private String dependencias = "dependencias";
	private String login = "login";
	private String chat = "chat";
	private String chatSub = "chatSub";

	public Handler_sqlite(Context ctx) {

		super(ctx, "mibase", null, 1);
	}


	@Override
	public void onCreate(SQLiteDatabase db)
	{

		String query = "CREATE TABLE "+ encuesta +"("+_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, ID text UNIQUE, TITULO text, " +
				"TEMA text,  PREGUNTA text, PERFIL text," +
				"FECHA TIMESTAMP default (datetime('now', 'localtime')), guardado INTEGER DEFAULT 1, estado INTEGER DEFAULT 0)";
		db.execSQL(query);

		String query2 = "CREATE TABLE "+ login +"("+_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"usuario text, idUsuario text, pass text, nombre text, paterno, materno text, fecha TIMESTAMP default (datetime('now', 'localtime')), " +
				" estado INTEGER DEFAULT 0, VISITA text DEFAULT '1');";
		db.execSQL(query2);

		String query3 = "CREATE TABLE "+ dependencias +"("+_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"dependencia text);";
		db.execSQL(query3);

		String query4 = "CREATE TABLE "+ chat +"("+_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, ID text, numchat text DEFAULT '1'," +
				"mensajes text, estatus text DEFAULT '1', destinatario text, FECHA DATETIME);";
		db.execSQL(query4);

		String query5 = "CREATE TABLE "+ chatSub +"("+_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"mensajes text, estatus text DEFAULT '0', FECHA TIMESTAMP default (datetime('now', 'localtime')));";
		db.execSQL(query5);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int version_ant, int version_nue)
	{
		db.execSQL("DROP TABLE IF EXISTS encuesta");
		onCreate(db);
	}

	public void insertaJson(String ID, String TEMA, String TITULO, String PREGUNTA, String PERFIL)
	{
		ContentValues valores = new ContentValues();
		valores.put("ID", ID);
		valores.put("TEMA", TEMA);
		valores.put("TITULO", TITULO);
		valores.put("PREGUNTA", PREGUNTA);
		valores.put("PERFIL", PERFIL);

		if ( (ID != null) && (!ID.equals("")) ) {
			this.getWritableDatabase().insert(encuesta , null, valores);
		}
	}

	public void insertaChat(String ID, String mensajes, String fecha, String destinatario)
	{
		String estatus = "1";

		if(destinatario.equals("1")){
			estatus = "0";
		}
		ContentValues valores = new ContentValues();
		valores.put("ID", ID);
		valores.put("mensajes", mensajes);
		valores.put("FECHA", fecha);
		valores.put("destinatario", destinatario);
		valores.put("estatus", estatus);



		if ( (ID != null) && (!ID.equals("")) ) {
			this.getWritableDatabase().insert(chat , null, valores);
		}
	}

	//<editor-fold desc="Description">
	public void insertaChat(String mensajes)
	{
		ContentValues valores = new ContentValues();
		valores.put("mensajes", mensajes);

		if ( (mensajes != null) && (!mensajes.equals("")) ) {
			this.getWritableDatabase().insert(chatSub , null, valores);
		}
	}
	//</editor-fold>

	public Cursor obtener(){
		String query = "select ID, TITULO, PREGUNTA, PERFIL, estado, TEMA from encuesta WHERE GUARDADO = 1;";
		Cursor c = this.getReadableDatabase().rawQuery(query, null);

		return c;
	}

	public void vistos(){
		ContentValues valores = new ContentValues();
		valores.put("numchat", "0");
		this.getWritableDatabase().update(chat, valores, null, null);
	}

	public Cursor numchats(){
		String query = "select numchat from chat WHERE numchat = 1;";
		Cursor c = this.getReadableDatabase().rawQuery(query, null);

		return c;
	}


	public Cursor chat(){
		String query = "select "+_ID+", mensajes, destinatario, time(FECHA), estatus from chat;";
		Cursor c = this.getReadableDatabase().rawQuery(query, null);

		return c;
	}

	public String ids(String ide){

		String result= "0";
		String columnas[] = {"ID"};
		String[] ident= {ide};
		Cursor c = this.getReadableDatabase().query(encuesta, columnas, "ID=?", ident, null, null, null, "1");
		c.moveToLast();
		if( c.getCount() > 0){
			result = "1";
		}else{
			result = "0";
		}
		return result;
	}

	public String idschat(String ide){

		String result= "0";
		String columnas[] = {"ID"};
		String[] ident= {ide};
		Cursor c = this.getReadableDatabase().query(chat, columnas, "ID=?", ident, null, null, null, "1");
		c.moveToLast();
		if( c.getCount() > 0){
			result = "1";
		}else{
			result = "0";
		}
		return result;
	}

	public int id(){
		int result=0;
		String columnas[] = {"ID"};
		try{
			Cursor c = this.getReadableDatabase().query(encuesta, columnas,  null, null, null, null, "id asc");
			c.moveToLast();
			if ( c.getCount() > 0) {
				result = c.getInt(0);
			}
		}catch(SQLiteException e){
			System.err.println("Exception @ rawQuery: " + e.getMessage());
			{
				result=0;
			}

		}
		return result;
	}

	public Cursor enviar(String ide){
		String columnas[] = {_ID, "mensajes"};
		String[] ident= {ide};															// group by Having order by, limit
		Cursor c = this.getReadableDatabase().query(chat, columnas, "estatus=?", ident, null, null, null, "0,30");
		return c;
	}

	public void salir(String id) {
		ContentValues valores = new ContentValues();
		valores.put("pass", "0");
		this.getWritableDatabase().update(login, valores, "pass=?", new String[] {id});
	}

	public void eliminarChat() {
		this.getWritableDatabase().delete(chat,null, null);
	}

	public void eliminar(String id) {
		this.getWritableDatabase().delete(encuesta,"ID=?", new String[] {id});
	}


	public void modifica(String id) {
		ContentValues valores = new ContentValues();
		valores.put("estatus", "1");
		this.getWritableDatabase().update(chat, valores, _ID+"=?", new String[] {id});
	}

	public void leer(String id) {
		ContentValues valores = new ContentValues();
		valores.put("estado", "1");
		this.getWritableDatabase().update(encuesta, valores, "ID=?", new String[] {id});
	}

	public void leeidas(String id) {
		ContentValues valores = new ContentValues();
		valores.put("estado", "2");
		this.getWritableDatabase().update(encuesta, valores, "ID!=? and estado=?", new String[] {id,"1"});
	}

	public String password(){
		String result="";
		String columnas[] = {"pass"};
		try{
			Cursor c = this.getReadableDatabase().query(login, columnas,  null, null, null, null, null);
			c.moveToLast();
			if ( c.getCount() > 0) {
				int iu;
				iu = c.getColumnIndex("pass");
				result = c.getString(iu);
			}
		}catch(SQLiteException e){
			System.err.println("Exception @ rawQuery: " + e.getMessage());
			{
				result="";
			}

		}
		return result;
	}

	public String fecha(String tabla, String fecha){
		String result="00-00-0000 00:00:00";
		String columnas[] = {fecha};
		try{
			Cursor c = this.getReadableDatabase().query(tabla, columnas,  null, null, null, null, null);
			//c.moveToFirst();
			c.moveToLast();
			if ( c.getCount() > 0) {
				int iu;
				iu = c.getColumnIndex(fecha);
				result = c.getString(iu);
			}
		}catch(SQLiteException e){
            System.err.println("Exception @ rawQuery: " + e.getMessage());
			{
				result="00-00-0000 00:00:00";
			}

		}
		return result;
	}

	public String iden(){
		String result="0";
		String columnas[] = {_ID};
		try{
			Cursor c = this.getReadableDatabase().query(encuesta, columnas,  null, null, null, null, null);
			//c.moveToFirst();
			c.moveToLast();
			if ( c.getCount() > 0) {
				int iu;
				iu = c.getColumnIndex(_ID);
				result = c.getString(iu);
			}
		}catch(SQLiteException e){
			System.err.println("Exception @ rawQuery: " + e.getMessage());
			{
				result="0";
			}

		}
		return result;
	}

	public String iden(String FOLIOENCUESTA){
		String result="0";
		String columnas[] = {_ID};
		String ide[] = {FOLIOENCUESTA};
		try{
			Cursor c = this.getReadableDatabase().query(encuesta, columnas,  "FOLIOENCUESTA=?", ide, null, null, null);
			//c.moveToFirst();
			c.moveToLast();
			if ( c.getCount() > 0) {
				int iu;
				iu = c.getColumnIndex(_ID);
				result = c.getString(iu);
			}
		}catch(SQLiteException e){
			System.err.println("Exception @ rawQuery: " + e.getMessage());
			{
				result="0";
			}

		}
		return result;
	}

	public void abrir() {
		// abrir base
		this.getWritableDatabase();
	}

	public void cerrar() {

		this.close();
	}
}
