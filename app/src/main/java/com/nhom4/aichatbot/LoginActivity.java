package com.nhom4.aichatbot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.zip.Inflater;

public class LoginActivity extends AppCompatActivity {
    TextView txt_title;
    EditText editTextUsername;
    EditText editTextPassword;
    EditText editTextPassword_confirm;
    CheckBox checkBox_save_login;
    Button button_confirm;
    Button button_toggle;
    int MODE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ints();
        btn_login();
        btn_toggle();
    }

    private void btn_toggle() {
        button_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MODE=MODE==1?2:1;
                mode();
            }
        });
    }

    private void btn_login() {
        button_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
    private void ints() {
        txt_title = findViewById(R.id.TextView_title);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPassword_confirm = findViewById(R.id.editTextPassword_confirm);
        checkBox_save_login = findViewById(R.id.checkBox_save_login);
        button_confirm = findViewById(R.id.button_confirm);
        button_toggle = findViewById(R.id.button_toggle);
        //1 DN,2 DK
        mode();
    }
    private void mode() {
        switch(MODE){
            case 1:
                txt_title.setText("Đăng nhập");
                editTextPassword_confirm.setVisibility(View.GONE);
                checkBox_save_login.setVisibility(View.VISIBLE);
                button_confirm.setText("Đăng nhập");
                button_toggle.setText("an vao de dk tai khoang");
                break;
            case 2:
                txt_title.setText("Đăng ky");
                editTextPassword_confirm.setVisibility(View.VISIBLE);
                checkBox_save_login.setVisibility(View.GONE);
                button_confirm.setText("Đăng ky");
                button_toggle.setText("da co tk an vao de dang nhap");
                break;
            default:
                MODE=1;
                this.mode();
                break;
        }
    }
}