package com.sarnava.mapassignment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegActivity extends AppCompatActivity {
    EditText username, phn, pass;
    Button btn;
    String password;
    int i;
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        username=(EditText) findViewById(R.id.user_name);
        phn=(EditText) findViewById(R.id.phn);
        pass=(EditText) findViewById(R.id.pass);
        btn= (Button) findViewById(R.id.btn);

        i=0;

        SharedPreferences sp = getSharedPreferences("Info", Context.MODE_PRIVATE);
        if(sp.contains("username")){
            i=1;
            username.setText(sp.getString("username", ""));
            username.setEnabled(false);
            phn.setText(sp.getString("phn", ""));
            phn.setEnabled(false);
            password= sp.getString("password", "");
            btn.setText("Log In");
        }

        boolean b= sp.getBoolean("isloggedin", false);
        if(b){
            startActivity(new Intent(getApplicationContext(),MapsActivity.class));
            finish();
        }

    }

    public void reg(View v){
        String u= username.getText().toString();
        String p=phn.getText().toString();
        String pa=pass.getText().toString();

        sp = getApplicationContext().getSharedPreferences("Info", Context.MODE_PRIVATE);
        editor= sp.edit();

        if(u.length()==0 || p.length()==0 || pa.length()==0){
            Toast.makeText(getApplicationContext(),"Please enter all fields",Toast.LENGTH_SHORT).show();
        }
        else if(i==0){  //register function
            editor.putString("username",u);
            editor.putString("phn",p);
            editor.putString("password",pa);
            editor.putBoolean("isloggedin",true);
            editor.apply();
            startActivity(new Intent(getApplicationContext(),MapsActivity.class));
            finish();
        }
        else{   //log in function
            if(pa.equals(password)){
                editor.putBoolean("isloggedin",true);
                editor.apply();
                startActivity(new Intent(getApplicationContext(),MapsActivity.class));
                finish();
            }
            else {
                Toast.makeText(getApplicationContext(),"Incorrect password",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
