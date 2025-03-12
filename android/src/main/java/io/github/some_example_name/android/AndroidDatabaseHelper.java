package io.github.some_example_name.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import io.github.some_example_name.DatabaseInterface;

public class AndroidDatabaseHelper extends SQLiteOpenHelper implements DatabaseInterface {

    private static final String DATABASE_NAME = "partidas.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_PARTIDAS = "partidas";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_MAPA = "mapa";
    private static final String COLUMN_FECHA = "fecha";
    private static final String COLUMN_RESULTADO = "resultado";

    public AndroidDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_PARTIDAS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_MAPA + " TEXT, " +
            COLUMN_FECHA + " TEXT, " +
            COLUMN_RESULTADO + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARTIDAS);
        onCreate(db);
    }

    @Override
    public void insertarPartida(String mapa, String fecha, String resultado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MAPA, mapa);
        values.put(COLUMN_FECHA, fecha);
        values.put(COLUMN_RESULTADO, resultado);
        db.insert(TABLE_PARTIDAS, null, values);
        db.close();
    }

    @Override
    public String obtenerUltimasPartidas() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PARTIDAS + " ORDER BY " + COLUMN_ID + " DESC LIMIT 10", null);

        StringBuilder resultado = new StringBuilder();
        if (cursor.moveToFirst()) {
            do {
                resultado.append("Mapa: ").append(cursor.getString(1))
                    .append(" | Fecha: ").append(cursor.getString(2))
                    .append(" | Resultado: ").append(cursor.getString(3))
                    .append("\n");
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return resultado.toString();
    }
}
