package mx.gob.veracruz.tarjetas;

public class Entidad {

    private String id;
    private String titulo;
    private String contenido;
    private String color;
    private String hora;
    private String estatus;
    private int usuario;

    public Entidad(String id, String titulo, String contenido, String color, int usuario) {
        this.id = id;
        this.titulo = titulo;
        this.contenido = contenido;
        this.color = color;
        this.color = color;
    }

    public Entidad(String id, String titulo, String contenido, String color, int usuario, String hora, String estatus) {
        this.id = id;
        this.titulo = titulo;
        this.contenido = contenido;
        this.color = color;
        this.usuario = usuario;
        this.hora = hora;
        this.estatus = estatus;
    }

    public String getId(){
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getContenido() {
        return contenido;
    }

    public String getColor() {
        return color;
    }
    public String getHora() {
        return hora;
    }
    public int getUsuario() {
        return usuario;
    }
    public String getEstatus() {
        return estatus;
    }
}
