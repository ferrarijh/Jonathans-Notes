package com.jonathan.trace.study.trace.coketlist

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //for test!
        btn_login.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
        }
        tv_sign_up.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        //

        /* TODO below
        btn_register_submit.setOnClickListener{
            if(et_login_email.text.isNullORBlank() || et_login_pw.text.isNullOrBlank())
                Toast.makeText(this, "Please check ID and password.", Toast.LENGTH_SHORT).show()
            else{

            }
        }
        tv_register.setOnClickListener{
            startActivity(this, RegisterActivity::class.java))
        }

         */
    }
}