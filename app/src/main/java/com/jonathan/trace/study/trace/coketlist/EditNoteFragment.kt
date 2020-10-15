package com.jonathan.trace.study.trace.coketlist

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jonathan.trace.study.trace.coketlist.dialog.MyDialog
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
    private var isNew = false
    private lateinit var warnDialog: MyDialog
    private val nViewModel by lazy{ViewModelProvider(requireActivity()).get(NoteViewModel::class.java)}
    private val fViewModel by lazy{ViewModelProvider(this).get(FragmentStateViewModel::class.java)}
    private val isPaletteOpen = MutableLiveData<Boolean>()
    private var justInitialized = true

    private lateinit var colorSelected: MutableLiveData<String>

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_edit_note, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setNoteContent()
        setBackground()
        setFAB()
        setDialog()
        setAppBar()
        setDrawer()
        setOnBackPressed()
    }

    private fun setBackground(){
        colorSelected = fViewModel.colorSelected
        colorSelected.observe(viewLifecycleOwner){
            cv_edit_note.setCardBackgroundColor(Color.parseColor(it))
            Log.d("", "color changed to: $it")
        }

        if(isNew) {
            if (colorSelected.value == null)
                colorSelected.value = colorResToStr(R.color.white)
        }else{
            if(colorSelected.value == null)
                colorSelected.value = nViewModel.getNotePointed()!!.second.color
            else
                colorSelected.value = colorSelected.value
        }

        Log.d("", "colorSelected.value: ${colorSelected.value}")


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
            supportActionBar!!.show()
        }

        //ImageView used as button since navigation icon does not support long click
        ivHome.apply{
            setOnClickListener{
                if(!isEdited())
                    goBack()
                else
                    Toast.makeText(context, getString(R.string.long_click), Toast.LENGTH_SHORT).show()
            }
            setOnLongClickListener{
                if(isEdited() && !isEmpty())
                    saveNote()
                else if(isEmpty()) {
                    Toast.makeText(context, getString(R.string.cant_save_empty), Toast.LENGTH_SHORT).show()
                }else
                    Toast.makeText(context, getString(R.string.not_edited), Toast.LENGTH_SHORT).show()
                goBack()
                true
            }
        }
    }

    private fun isEdited(): Boolean{
        val curNote = nViewModel.getNotePointed()!!.second

        val oldTitle = curNote.title
        val oldBody = curNote.body
        val oldColor = curNote.color

        val newTitle = et_title.text.toString()
        val newBody = et_body.text.toString()

        return newBody != oldBody || oldTitle != newTitle || oldColor != colorSelected.value
    }

    private fun setOnBackPressed(){
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if(isEdited())
                warnDialog.show()
            else
                goBack()
        }
    }

    private fun setNoteContent(){
        isNew = EditNoteFragmentArgs.fromBundle(requireArguments()).isNew

        if(!isNew) {
            val note = nViewModel.getNotePointed()!!.second
            et_title.setText(note.title)
            et_body.setText(note.body)
        }else
            nViewModel.setNotePointed(-1,
                Note(0,
                    "",
                    "",
                    "",
                    "",
                    0,
                    colorResToStr(R.color.white),
                    null
                ))
    }

    private fun setFAB(){
        //palette
        fViewModel.isPaletteOpen.value?.let{
            isPaletteOpen.value = it
        }

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
                if(isEdited())
                    saveNote()
                else
                    Toast.makeText(context, getString(R.string.not_edited), Toast.LENGTH_SHORT).show()
            }
            setOnLongClickListener{
                if(isEdited() && !isEmpty())
                    saveNote()
                else if(isEmpty()) {
                    Toast.makeText(context, getString(R.string.cant_save_empty), Toast.LENGTH_SHORT).show()
                }else
                    Toast.makeText(context, getString(R.string.not_edited), Toast.LENGTH_SHORT).show()
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
            colorSelected.value = colorResToStr(R.color.coral)
        }
        fabLemon.setOnClickListener{
            colorSelected.value = colorResToStr(R.color.lemon)
        }
        fabMint.setOnClickListener{
            colorSelected.value = colorResToStr(R.color.mint)
        }
        fabWhite.setOnClickListener{
            colorSelected.value = colorResToStr(R.color.white)
        }
    }

    private fun openPalette(){
        fabColors.startAnimation(expand)
        fabCoral.show()
        fabCoral.startAnimation(up)
        fabLemon.show()
        fabLemon.startAnimation(up)
        fabMint.show()
        fabMint.startAnimation(up)
        fabWhite.show()
        fabWhite.startAnimation(up)
    }

    private fun closePalette(){
        fabColors.startAnimation(shrink)
        fabCoral.startAnimation(down)
        fabCoral.hide()
        fabLemon.startAnimation(down)
        fabLemon.hide()
        fabMint.startAnimation(down)
        fabMint.hide()
        fabWhite.startAnimation(down)
        fabWhite.hide()
    }

    private fun saveNote(){
        val title = et_title.text.toString().trim()
        val body = et_body.text.toString().trim()
        val dateTime = getDateTime()

        if(nViewModel.getNotePointed()!!.second.id == 0) {
            val curNote = Note(0, title, body, dateTime, dateTime, 0, colorSelected.value!!)
            Log.d("","saving note with id: ${nViewModel.getNotePointed()!!.second.id }")

            //fetch id immediately after saving new unsaved note
            var curIdLive: LiveData<Int>
            CoroutineScope(Dispatchers.IO).launch {
                nViewModel.addNote(curNote)
            }.invokeOnCompletion {
                curIdLive = nViewModel.getIdLastSaved()
                CoroutineScope(Dispatchers.Main).launch {
                    curIdLive.observe(viewLifecycleOwner) {
                        curNote.id = it
                        Log.d("","observed, notePointed id is now: ${nViewModel.getNotePointed()!!.second.id }")
                        curIdLive.removeObservers(viewLifecycleOwner)
                    }
                }
            }
            nViewModel.setNotePointed(-1, curNote)  //pos -1 ONLY when saving new note
            Toast.makeText(context, getString(R.string.saved), Toast.LENGTH_SHORT).show()
        }else{
            val curNote = nViewModel.getNotePointed()!!.second
            curNote.title = title
            curNote.body = body
            curNote.dateTimeModified = dateTime
            curNote.color = colorSelected.value!!
            CoroutineScope(Dispatchers.IO).launch {
                nViewModel.update(curNote)
            }
            Toast.makeText(context, getString(R.string.saved), Toast.LENGTH_SHORT).show()
        }
    }

    private fun isEmpty(): Boolean {
        val title = et_title.text.toString().trim()
        val body = et_body.text.toString().trim()
        return title.isBlank() && body.isBlank()
    }

    private fun setDialog(){
        warnDialog = MyDialog(requireContext(), R.layout.dialog, getString(R.string.warn_cancel_edit)){
            goBack()
            warnDialog.dismiss()
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


        arguments?.let{
            val fromSearch = EditNoteFragmentArgs.fromBundle(it).fromSearch
            if(fromSearch)
                findNavController().popBackStack(R.id.homeFragment, false)
        }
        findNavController().navigateUp()
    }

    private fun colorResToStr(colorRes: Int): String{
        return String.format("#%08x", ContextCompat.getColor(requireContext(), colorRes) and 0xffffffff.toInt())
    }
}