package com.nhom4.aichatbot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {
    TextView txt_title;
    EditText editTextUsername, editTextPassword, editTextPassword_confirm;
    CheckBox checkBox_save_login;
    Button button_confirm;
    Button button_toggle;
    int MODE=1;
    FirebaseAuthHelper authHelper = new FirebaseAuthHelper();

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
        Test test = new Test();
        ints();
        btn_confirm();
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

    private void btn_confirm() {
        button_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (MODE){
                    case 1:
                        loginUser();
                        break;
                    case 2:
                        registerUser();
                        break;
                    default:
                        MODE=1;
                        break;
                }
            }
        });
    }

    private void loginUser() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tất cả các trường", Toast.LENGTH_SHORT).show();
            return;
        }
        authHelper.signInUser(username, password, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                if(checkBox_save_login.isChecked()){
                    //todo: save login
                    movetomain();
                }else{
                    movetomain();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextPassword_confirm.getText().toString().trim();
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tất cả các trường", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!authHelper.isValidEmail(username)){
            Toast.makeText(this, "email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        authHelper.createUser(username, password, task -> {
            if (task.isSuccessful()) {
                MODE=1;
                mode();
                Toast.makeText(LoginActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, "Đăng ký thất bại: " +
                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                button_toggle.setText("Đăng kí tài khoảng");
                break;
            case 2:
                txt_title.setText("Đăng ký");
                editTextPassword_confirm.setVisibility(View.VISIBLE);
                checkBox_save_login.setVisibility(View.GONE);
                button_confirm.setText("Đăng ky");
                button_toggle.setText("Đăng nhập tài khoảng");
                break;
            default:
                MODE=1;
                this.mode();
                break;
        }
    }
    private void movetomain(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}