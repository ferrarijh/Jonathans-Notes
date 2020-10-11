package com.jonathan.trace.study.trace.coketlist

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GestureDetectorCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jonathan.trace.study.trace.coketlist.gesturelistener.MyGestureListener
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteViewModel
import com.jonathan.trace.study.trace.coketlist.viewmodel.FragmentStateViewModel
import kotlinx.android.synthetic.main.fragment_edit_note.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditNoteFragment: Fragment(){
    private var mNote: Note? = null
    private lateinit var mDialog: MyDialog
    private lateinit var mViewModel: NoteViewModel
    private lateinit var fViewModel: FragmentStateViewModel
    private val isPaletteOpen = MutableLiveData<Boolean>()
    private var justInitialized = true
    private lateinit var colorSelected: String

    private val fabSave: FloatingActionButton by lazy{ requireActivity().findViewById(R.id.fab_save) }
    private val fabColors: FloatingActionButton by lazy{ requireActivity().findViewById(R.id.fab_colors)}
    private val fabCoral: FloatingActionButton by lazy{ requireActivity().findViewById(R.id.fab_coral)}
    private val fabLemon: FloatingActionButton by lazy{ requireActivity().findViewById(R.id.fab_lemon)}
    private val fabMint: FloatingActionButton by lazy{ requireActivity().findViewById(R.id.fab_mint)}
    private val fabWhite: FloatingActionButton by lazy{ requireActivity().findViewById(R.id.fab_white)}

    private val expand: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.fab_expand)}
    private val shrink: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.fab_shrink)}
    private val up: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.fab_up)}
    private val down: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.fab_down)}
    private val init: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.fab_init)}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        @SuppressLint("ResourceType")
        colorSelected = resources.getString(R.color.white)
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
        setBackground()
        setFAB()
        setDialog()
        setAppBar()
        setDrawer()
    }

    private fun setBackground(){
        cv_edit_note.setCardBackgroundColor(Color.parseColor(colorSelected))
    }

    private fun setDrawer(){
        val drawer = requireActivity().findViewById<DrawerLayout>(R.id.layout_drawer)
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun setAppBar() {
        val parent = requireActivity() as AppCompatActivity
        val toolBar = parent.findViewById<Toolbar>(R.id.toolbar)
        val ivHome = toolBar.findViewById<ImageView>(R.id.iv_hamburger)
        ivHome.setImageResource(R.drawable.back)
        parent.apply {
            setSupportActionBar(toolBar)
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        }

        //ImageView used as button since navigation icon does not support long click
        ivHome.apply{
            setOnClickListener{
                if(!isEdited()){
                    goBack()
                }
                else
                    Toast.makeText(context, getString(R.string.long_click), Toast.LENGTH_SHORT).show()
            }
            setOnLongClickListener{
                saveNote()
                goBack()
                true
            }
        }
    }

    private fun isEdited(): Boolean{
        mNote?.let {
            val oldTitle = mNote!!.title
            val oldBody = mNote!!.body
            val newTitle = et_title.text.toString()
            val newBody = et_body.text.toString()
            val oldColor = mNote!!.color
            return newBody != oldBody || oldTitle != newTitle || oldColor != colorSelected
        }
        return !et_title.text.isNullOrBlank() || !et_body.text.isNullOrBlank()
    }

    private fun setOnBackPressed(){
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if(mNote != null){
                if(isEdited()) {
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
            goBack()
        }
    }

    private fun setNote(){
        arguments?.let{
            mNote = EditNoteFragmentArgs.fromBundle(it).note
            mNote?.let{ n ->
                et_title.setText(n.title)
                et_body.setText(n.body)
                colorSelected = n.color
            }
        }
    }

    @SuppressLint("ResourceType")
    private fun setFAB(){
        fabSave.show()
        fabColors.show()

        fViewModel.isPaletteOpen.value?.let{
            if(!it)
                fabColors.animation = init
            else
                openPalette()
        }

        fabSave.apply {
            setOnClickListener {
                saveNote()
            }
            setOnLongClickListener{
                saveNote()
                goBack()
                true
            }
        }

        fabColors.setOnClickListener{
            val isOpen = isPaletteOpen.value!!
            isPaletteOpen.value = !isOpen
        }

        isPaletteOpen.observe(viewLifecycleOwner){
            fViewModel.isPaletteOpen.postValue(it)
            if(justInitialized){
                justInitialized = false
                return@observe
            }
            if(!it)
                closePalette()
            else
                openPalette()
        }

        fabCoral.setOnClickListener{
            colorSelected = resources.getString(R.color.coral)
            cv_edit_note.setCardBackgroundColor(Color.parseColor(colorSelected))
        }
        fabLemon.setOnClickListener{
            colorSelected = resources.getString(R.color.lemon)
            cv_edit_note.setCardBackgroundColor(Color.parseColor(colorSelected))
        }
        fabMint.setOnClickListener{
            colorSelected = resources.getString(R.color.mint)
            cv_edit_note.setCardBackgroundColor(Color.parseColor(colorSelected))
        }
        fabWhite.setOnClickListener{
            colorSelected = resources.getString(R.color.white)
            cv_edit_note.setCardBackgroundColor(Color.parseColor(colorSelected))
        }
    }

    private fun openPalette(){
        fabColors.animation = expand
        fabCoral.show()
        fabCoral.animation = up
        fabLemon.show()
        fabLemon.animation = up
        fabMint.show()
        fabMint.animation = up
        fabWhite.show()
        fabWhite.animation = up
    }

    private fun closePalette(){
        fabColors.animation = shrink
        fabCoral.animation = down
        fabCoral.hide()
        fabLemon.animation = down
        fabLemon.hide()
        fabMint.animation = down
        fabMint.hide()
        fabWhite.animation = down
        fabWhite.hide()
    }

    @SuppressLint("ResourceType")
    private fun saveNote(){

        val title = et_title.text.toString().trim()
        val body = et_body.text.toString().trim()
        val dateTime = getDateTime()
        if(title.isBlank() || body.isBlank()){
            Toast.makeText(context, "Title and body can't be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if(mNote == null) {
            mNote = Note(0, title, body, dateTime, dateTime, 0, colorSelected)
            CoroutineScope(Dispatchers.IO).launch {
                mViewModel.addNote(mNote!!)
            }.invokeOnCompletion {
                CoroutineScope(Dispatchers.Main).launch{
                    Toast.makeText(context, getString(R.string.saved), Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            mNote!!.title = title
            mNote!!.body = body
            mNote!!.dateTimeModified = dateTime
            mNote!!.color = colorSelected
            CoroutineScope(Dispatchers.IO).launch {
                mViewModel.update(mNote!!)
            }.invokeOnCompletion {
                CoroutineScope(Dispatchers.Main).launch{
                    Toast.makeText(context, getString(R.string.saved), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setDialog(){
        mDialog = MyDialog(requireContext()){
            goBack()
            mDialog.dismiss()
        }
    }

    private fun setViewModel(){
        mViewModel = ViewModelProvider(requireActivity()).get(NoteViewModel::class.java)
        fViewModel = ViewModelProvider(this).get(FragmentStateViewModel::class.java)
        fViewModel.isPaletteOpen.value?.let{
            isPaletteOpen.value = it
        }
    }

    private fun getDateTime(): String{
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(date)
    }

    private fun goBack(){
        fabSave.hide()
        fabColors.hide()
        fabCoral.hide()
        fabLemon.hide()
        fabMint.hide()
        fabWhite.hide()
        val action = EditNoteFragmentDirections.actionEditNoteFragmentToHomeFragment()
        findNavController().navigate(action)
    }

}