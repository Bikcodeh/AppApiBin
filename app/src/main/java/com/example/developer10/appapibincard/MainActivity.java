package com.example.developer10.appapibincard;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.developer10.appapibincard.Servicios.AppStatus;
import com.example.developer10.appapibincard.Servicios.Request;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    Button botonConsultar;
    String data;
    EditText cajaNumero, cajaBanco, cajaScheme, cajaType, cajaBrand;

    String type = "";
    String scheme = "";
    String brand = "";
    String bank = "";
    String numberBin;

    private ProgressDialog proceso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        botonConsultar = findViewById(R.id.botonConsulta);
        botonConsultar.setOnClickListener(onButtonClick);

        cajaNumero = findViewById(R.id.cajaBinn);
        cajaBanco = findViewById(R.id.cajaBanco);
        cajaScheme = findViewById(R.id.cajaScheme);
        cajaBrand = findViewById(R.id.cajaBrand);
        cajaType = findViewById(R.id.cajaType);

        proceso = new ProgressDialog(this);
        proceso.setTitle("Por favor espere");
        proceso.setMessage("Validando informacion");
    }


    private View.OnClickListener  onButtonClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.botonConsulta) {
                start();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Cerrar: ", "ME HE CERRADO HOSTIA");
    }

    public void start()
    {
        if(AppStatus.getInstance(getApplicationContext()).isOnline()) {
            /**
             * Internet is available, Toast It!
             */
            data = cajaNumero.getText().toString();
            new Peticiones(data, null).execute();
            //Toast.makeText(getApplicationContext(), "WiFi/Mobile Networks Connected!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Ooops! No hay conexion a internet!", Toast.LENGTH_SHORT).show();
        }

    }

    private class Peticiones extends AsyncTask<String, String, String>
    {

        private String numero;
        private JSONObject params;

        public Peticiones(String number, JSONObject params)
        {
            this.numero = number;
            this.params = params;
        }

        @Override
        protected void onPreExecute() {
            if(params == null){
                clearEditText();
                proceso.show();
            }
        }

        @Override
        protected String doInBackground(String... data) {
            JSONObject parametros = new JSONObject();
            String resp = null;
            if(numero != null)
            {
                resp = Request.makeRequest("GET", "https://lookup.binlist.net/"+ this.numero, null, 1);
                Log.d("Datos GET: ", numero);
                return resp;
            }
            else if(params != null)
            {
                resp = Request.makeRequest("POST", "http://192.168.1.124/ApiBin/", this.params, 2);
                Log.d("Datos POST: ", params.toString());
            }
            return resp;
        }

        @Override
        protected void onProgressUpdate(String... values) {

        }

        @Override
        protected void onPostExecute(String s) {
            proceso.dismiss();

            if(IsJsonString(s)){
                showData(s);
            }
            else if(s.toUpperCase().equals("SUCCESS")){
                Toast.makeText(MainActivity.this, "Almacenado exitosamente.", Toast.LENGTH_SHORT).show();
            }
            else if(s.toUpperCase().equals("FAIL")){
                Toast.makeText(MainActivity.this, "Ha ocurrido un error al guardar.", Toast.LENGTH_SHORT).show();
            }
            else{
                Log.d("RESPUESTA: ", s);
            }
        }
    }

    private  boolean IsJsonString(String str) {
        JSONObject params = new JSONObject();

        try {
            params = new JSONObject(str);
        } catch (JSONException e) {
            return false;
        }
        return true;
    }

    protected JSONObject fillParams(String numberB, String scheme, String bank, String type, String brand)
    {
        JSONObject params = new JSONObject();
        try {
            params.put("scheme", scheme);
            params.put("bank", bank);
            params.put("typeCard", type);
            params.put("brand", brand);
            params.put("binNumber", numberB);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return params;
    }

    private void showData(String respServer)
    {
        JSONObject resp = null;
        try
        {
            resp = new JSONObject(respServer);
        }
        catch(JSONException e)
        {
            Log.d("Error parseo: ", e.getMessage());
        }

        try
        {
            if(resp != null)
            {
                if(resp.has("STATUS") && resp.getString("STATUS").equals("0"))
                {
                    Toast.makeText(this, "No se encontro informacion.", Toast.LENGTH_SHORT).show();
                    clearEditText();
                }
                else if(resp.has("STATUS")  && resp.getString("STATUS").equals("1")){
                    Toast.makeText(this, "No hay conexion a la base de datos.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    numberBin = cajaNumero.getText().toString();

                    if(resp.has("type")){
                        type = resp.getString("type").toUpperCase();
                        cajaType.setText(type);
                    }

                    if(resp.has("scheme")){
                        scheme = resp.getString("scheme").toUpperCase();
                        cajaScheme.setText(scheme);
                    }

                    if(resp.has("bank") && resp.getJSONObject("bank").has("name")){
                        bank = resp.getJSONObject("bank").getString("name").toUpperCase();
                        cajaBanco.setText(bank);
                    }

                    if(resp.has("brand")){
                        brand = resp.getString("brand").toUpperCase();
                        cajaBrand.setText(brand);
                    }
                    new Peticiones(null, fillParams(numberBin, scheme,bank, type, brand)).execute();
                }
            }
            else
            {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                clearEditText();
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void clearEditText()
    {
        cajaBrand.setText("");
        cajaScheme.setText("");
        cajaBanco.setText("");
        cajaType.setText("");
    }

}
