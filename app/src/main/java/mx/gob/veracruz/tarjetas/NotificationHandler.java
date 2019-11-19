package mx.gob.veracruz.tarjetas;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;

public class NotificationHandler extends ContextWrapper {

    private NotificationManager manager;

    public static final String CHANNEL_HIGH_ID = "1";
    private static final String CHANNEL_HIGH_NAME = "HIGH CHANNEL";
    public static final String CHANNEL_LOW_ID = "2";
    private static final String CHANNEL_LOW_NAME = "LOW CHANNEL";
    private static final int SUMAR_GROUP_ID  = 1001;
    private static final String SUMAR_GROUP_NAME  = "GROUPING_NOTIFICATION";

    public NotificationHandler(Context context) {
        super(context);

        createChannels();
    }

    public NotificationManager getManager(){
        if(manager == null){
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }
    private void createChannels (){
        if(Build.VERSION.SDK_INT >= 26 ){
            NotificationChannel highChannel = new NotificationChannel(
                    CHANNEL_HIGH_ID, CHANNEL_HIGH_NAME, NotificationManager.IMPORTANCE_HIGH);

            highChannel.enableLights(true);
            highChannel.setLightColor(Color.YELLOW);

            NotificationChannel lowChannel = new NotificationChannel(
                    CHANNEL_LOW_ID, CHANNEL_LOW_NAME, NotificationManager.IMPORTANCE_LOW);

            lowChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            getManager().createNotificationChannel(highChannel);
            getManager().createNotificationChannel(lowChannel);
        }
    }

    public Notification.Builder createNotification (String titulo, String mensaje, boolean isHighImportance){

        if(Build.VERSION.SDK_INT >= 26){
            if(isHighImportance){
                return this.createNotificationWhithChannel(titulo, mensaje, CHANNEL_HIGH_ID);
            }else{
                return this.createNotificationWhithChannel(titulo, mensaje, CHANNEL_LOW_ID);
            }
        }else{
            return this.createNotificationWhithoutChannel(titulo, mensaje);
        }
    }
    private Notification.Builder createNotificationWhithChannel (String titulo, String mensaje, String channelId){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Intent intent = new Intent(this, Tarjeta.class);
            intent.putExtra("chat", "1");

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_CANCEL_CURRENT);

            //Esto va con el boton acción para agregar un boton
            Notification.Action action = new Notification.Action.Builder(
                    Icon.createWithResource(this, android.R.drawable.ic_menu_send),
                    "Ver Detalles",
                    pendingIntent).build();

            return new Notification.Builder(getApplicationContext(), channelId)
                    .setContentTitle(titulo)
                    .setContentText(mensaje)
                    //.addAction(action) //Para agregar botones y hagan una acción
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(android.R.drawable.ic_dialog_email)
                    .setGroup(SUMAR_GROUP_NAME)
                    .setAutoCancel(true);
        }

        return null;
    }

    private Notification.Builder createNotificationWhithoutChannel (String titulo, String mensaje){

        return new Notification.Builder(getApplicationContext())
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setSmallIcon(R.drawable.logo)
                .setAutoCancel(true);
    }

    public  void publishNotificationSummaryGroup(boolean isHighImportance ){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String channelId = (isHighImportance) ? CHANNEL_HIGH_ID: CHANNEL_LOW_ID;

            Notification summaryNotification = new Notification.Builder(getApplicationContext(), channelId)
                    .setSmallIcon(android.R.drawable.stat_notify_call_mute)
                    .setGroup(SUMAR_GROUP_NAME)
                    .setGroupSummary(true)
                    .build();
            getManager().notify(SUMAR_GROUP_ID, summaryNotification);
        }
    }
}
