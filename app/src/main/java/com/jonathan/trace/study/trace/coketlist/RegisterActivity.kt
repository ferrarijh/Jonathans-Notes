package com.jonathan.trace.study.trace.coketlist

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        /*
        btn_registerSubmit.setOnClickListener{
            if (registerIdEdit.text.isNullOrBlank() || registerPwEdit.text.isNullOrBlank()
                || registerNameEdit.text.isNullOrBlank() || registerAgeEdit.text.isNullOrBlank() ) {
                Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show()
            }else{
                val id = et_registerId.text.toString()
                val pw = et_registerPw.text.toString()
                val name = et_registerName.text.toString()
                val age = et_registerAge.text.toString().toInt()
                //TODO
            }

        }

         */
    }
}