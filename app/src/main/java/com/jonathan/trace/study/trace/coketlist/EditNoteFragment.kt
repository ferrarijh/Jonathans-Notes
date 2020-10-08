package com.jonathan.trace.study.trace.coketlist

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_edit_note.*
import kotlinx.android.synthetic.main.toolbar_edit_note.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditNoteFragment: Fragment(){
    private var mNote: Note? = null
    private lateinit var mDialog: MyDialog
    private lateinit var mViewModel: NoteViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setOnBackPressed()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_edit_note, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViewModel()
        setNote()
        setFAB()
        setDialog()
        setAppBar()
        setDrawer()
    }

    private fun setDrawer(){
        val drawer = requireActivity().findViewById<DrawerLayout>(R.id.layout_drawer)
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun setAppBar() {
        val parent = requireActivity() as AppCompatActivity
        if (parent.supportActionBar!!.isShowing) {
            parent.supportActionBar!!.hide()
            Log.d("", "supportActionbar.hide() called.")
        }
    }

    private fun setOnBackPressed(){
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if(mNote != null){
                val oldTitle = mNote!!.title
                val oldBody = mNote!!.body
                val newTitle = et_title.text.toString()
                val newBody = et_body.text.toString()
                if (newBody != oldBody || oldTitle != newTitle) {
                    mDialog.show()
                    mDialog.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.warn_cancel_edit)  //find dialog view after showing it.
                    return@addCallback
                }
            }else{
                if(et_title.text.isNotBlank() || et_body.text.isNotBlank()){
                    mDialog.show()
                    mDialog.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.warn_cancel_edit)
                    return@addCallback
                }
            }
            findNavController().navigate(EditNoteFragmentDirections.actionEditNoteFragmentToHomeFragment())
        }
    }

    private fun setNote(){
        arguments?.let{
            mNote = EditNoteFragmentArgs.fromBundle(it).note
            mNote?.let{ n ->
                et_title.setText(n.title)
                et_body.setText(n.body)
            }
        }
    }

    private fun setFAB(){
        val fabSave = requireActivity().findViewById<FloatingActionButton>(R.id.fab_save)
        val fabAdd = requireActivity().findViewById<FloatingActionButton>(R.id.fab_add)

        fabAdd.hide()
        fabSave.show()

        fabSave.setOnClickListener{
            val title = et_title.text.toString().trim()
            val body = et_body.text.toString().trim()
            val dateTime = getDateTime()
            if(title.isBlank() || body.isBlank()){
                Toast.makeText(context, "Title and body can't be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var newNote: Note? = null
            CoroutineScope(Dispatchers.IO).launch{
                if(mNote == null) {
                    newNote = Note(0, title, body, dateTime, dateTime)
                    mViewModel.addNote(newNote!!)
                }else{
                    //newNote = Note(mNote!!.id, title, body, mNote!!.dateTimeCreated, dateTime)
                    //mViewModel.update(newNote!!)
                    mNote!!.title = title
                    mNote!!.body = body
                    mNote!!.dateTimeModified = dateTime
                    mViewModel.update(mNote!!)
                }
            }.invokeOnCompletion {
                CoroutineScope(Dispatchers.Main).launch{
                    Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
                    mNote = newNote
                }
            }
        }
    }

    private fun setDialog(){
        mDialog = MyDialog(requireContext()){
            val action = EditNoteFragmentDirections.actionEditNoteFragmentToHomeFragment()
            findNavController().navigate(action)
            mDialog.dismiss()
        }
    }

    private fun setViewModel(){
        mViewModel = ViewModelProvider(requireActivity()).get(NoteViewModel::class.java)
    }

    private fun getDateTime(): String{
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(date)
    }

}