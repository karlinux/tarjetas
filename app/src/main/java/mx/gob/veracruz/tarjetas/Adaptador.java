package mx.gob.veracruz.tarjetas;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;


public class Adaptador extends BaseAdapter {
    private Context context;
    private ArrayList<Entidad> listItems;
    LinearLayout.LayoutParams lp;
    public Adaptador(Context context, ArrayList<Entidad> listItems) {
        this.context = context;
        this.listItems = listItems;
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        Entidad item = (Entidad) getItem(position);

        convertView = LayoutInflater.from(context).inflate(R.layout.listview_item, null);
        TextView tvId = (TextView) convertView.findViewById(R.id.tvId);
        TextView tvTitulo = (TextView) convertView.findViewById(R.id.tvTitulo);
        TextView tvFecha = (TextView) convertView.findViewById(R.id.tvFecha);
        TextView tvContenido = (TextView) convertView.findViewById(R.id.tvContenido);
        LinearLayout llFecha = (LinearLayout) convertView.findViewById(R.id.llFecha);
        ImageView ivClock = (ImageView) convertView.findViewById(R.id.ivClock);


        tvTitulo.setVisibility(View.GONE);
        tvId.setVisibility(View.GONE);
        tvFecha.setVisibility(View.GONE);
        ivClock.setVisibility(View.GONE);

        String color = item.getColor();
        int usuario = item.getUsuario();
        String estatus = item.getEstatus();

        if(color.equals("0")){
            tvContenido.setBackgroundColor(Color.parseColor("#AA983F"));
        }else if (color.equals("1")){
            tvContenido.setBackground(convertView.getResources().getDrawable(R.drawable.borde));
        }else if (color.equals("2")){
            tvContenido.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }else{
            tvContenido.setBackground(convertView.getResources().getDrawable(R.drawable.bordechat));
        }

        lp = (LinearLayout.LayoutParams) tvContenido.getLayoutParams();
        if(usuario > 1) {
        lp.setMargins(2,10,150,0);
            tvFecha.setVisibility(View.VISIBLE);
            //ivClock.setVisibility(View.VISIBLE);
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            tvContenido.setLayoutParams(lp);
            lp.setMargins(10,10,150,0);
            llFecha.setLayoutParams(lp);
            tvContenido.setBackground(convertView.getResources().getDrawable(R.drawable.bordechat));
            tvFecha.setText(item.getHora());

        }else if(usuario == 1 ){
            tvFecha.setVisibility(View.VISIBLE);
            ivClock.setVisibility(View.VISIBLE);
            lp.setMargins(150,10,2,0);
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            //tvContenido.setGravity(Gravity.RIGHT);
            lp.gravity = Gravity.RIGHT;
            tvContenido.setLayoutParams(lp);
            lp.setMargins(150,10,10,0);
            llFecha.setLayoutParams(lp);
            tvContenido.setBackground(convertView.getResources().getDrawable(R.drawable.bordeusuario));
            tvFecha.setText(item.getHora());
            if(estatus.equals("1")){
                ivClock.setImageResource(R.drawable.checkmark);
            }else{
                ivClock.setImageResource(R.drawable.clock);
            }
        }

        tvId.setText(item.getColor() +" " + item.getId());
        tvTitulo.setText(item.getTitulo());
        tvContenido.setText(item.getContenido());

        return convertView;
    }
}
