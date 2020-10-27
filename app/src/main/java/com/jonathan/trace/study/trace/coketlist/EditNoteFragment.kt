package com.jonathan.trace.study.trace.coketlist

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
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
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jonathan.trace.study.trace.coketlist.adapter.ViewPagerAdapter
import com.jonathan.trace.study.trace.coketlist.dialog.MyDialog
import com.jonathan.trace.study.trace.coketlist.dialog.fragment.ImageViewFragment
import com.jonathan.trace.study.trace.coketlist.room.Image
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteViewModel
import com.jonathan.trace.study.trace.coketlist.viewmodel.FragmentStateViewModel
import kotlinx.android.synthetic.main.fragment_edit_note.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class EditNoteFragment: Fragment(){

    companion object{
        const val REQUEST_CODE_FETCH = 1994
        const val IMAGE_PICK_CODE = 1995
        const val GO_BACK = 5
    }

    private var isNew = false
    private lateinit var warnDialog: MyDialog
    private val nViewModel by lazy{ViewModelProvider(requireActivity()).get(NoteViewModel::class.java)}
    private val fViewModel by lazy{ViewModelProvider(this).get(FragmentStateViewModel::class.java)}
    private val isPaletteOpen = MutableLiveData<Boolean>()
    private var justInitialized = true
    private val adapter by lazy {ViewPagerAdapter(imageViewer, this){deleteImageDialog.show()
        true} }
    private val imageViewer: ImageViewFragment by lazy{ImageViewFragment()}
    private lateinit var deleteImageDialog : MyDialog

    private lateinit var colorSelected: MutableLiveData<String>

    /** color palette **/
    private val fabSave: FloatingActionButton by lazy{ requireActivity().findViewById(R.id.fab_save) }
    private val fabColors: FloatingActionButton by lazy{ requireActivity().findViewById(R.id.fab_colors)}
    private val fabCoral: FloatingActionButton by lazy{ requireActivity().findViewById(R.id.fab_coral)}
    private val fabLemon: FloatingActionButton by lazy{ requireActivity().findViewById(R.id.fab_lemon)}
    private val fabMint: FloatingActionButton by lazy{ requireActivity().findViewById(R.id.fab_mint)}
    private val fabWhite: FloatingActionButton by lazy{ requireActivity().findViewById(R.id.fab_white)}

    /** animations **/
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

        hideKeyboard()
        setIsNew()
        setViewPager()
        setNote()
        setViewModel()
        setBackground()
        setFAB()
        setDialog()
        setAppBar()
        setDrawer()
        setOnBackPressed()
        setImageButton()

        //setFabTest()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ){
        if(requestCode == REQUEST_CODE_FETCH && grantResults.isNotEmpty()){
            if (grantResults[0]!= PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        context,
                        "I'm not permitted to attach image :(",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
            }
        attachImage()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE){
            Toast.makeText(context, getString(R.string.fetching_image), Toast.LENGTH_SHORT).show()
            data?.data.run{
                processImageUri(this)
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setViewPager(){
        vp_attached_images.adapter = adapter

        val marginPx = resources.getDimensionPixelOffset(R.dimen.pageMargin)
        val offsetPx = resources.getDimensionPixelOffset(R.dimen.peekOffset)
        vp_attached_images.setPageTransformer{ page, position ->
            page.translationX = position * -(offsetPx + marginPx)
        }

        fViewModel.pageIndicator.observe(viewLifecycleOwner){
            tv_page_count.text = it
        }
        vp_attached_images.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                setPageIndicator(adapter.itemCount)
                super.onPageSelected(position)
            }
        })
    }

    //TODO("save new image to db & adapter.submit(newList)")
    private fun processImageUri(uri: Uri?){
        CoroutineScope(Dispatchers.Default).launch {
            if (uri == null) {
                toastNullUri()
                this.cancel()
            }

            val scaledBitmap = parseScaledBitmap(uri!!)
            val fileName = saveToStorage(scaledBitmap)

            val isNewAndNotSaved = nViewModel.getNotePointed()!!.second.id == 0

            if (!isNewAndNotSaved) {
                val noteId = nViewModel.getNotePointed()!!.second.id
                val imgProfile = Image(fileName, noteId)
                fViewModel.addImages(listOf(imgProfile))
            } else {
                fViewModel.imagesForNewNote.value!!.add(Image(fileName, 0))
                withContext(Dispatchers.Main) {
                    fViewModel.imagesForNewNote.value = fViewModel.imagesForNewNote.value!!
                }
            }
        }
    }

    private fun saveToStorage(bitmap: Bitmap): String{
        val filesDir = requireActivity().filesDir
        val noteId = nViewModel.getNotePointed()!!.second.id
        val dir = File(filesDir.absolutePath + "/Pictures/$noteId")
        dir.mkdirs()

        val fileName = String.format("%d.jpeg", System.currentTimeMillis())
        val outputFile = File(dir, fileName)
        var oStream: FileOutputStream? = null
        try{
            oStream = FileOutputStream(outputFile)
        }catch (e: Exception){
            e.printStackTrace()
        }

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, oStream)

        return fileName
    }

    private fun parseScaledBitmap(uri: Uri): Bitmap{

        val contentResolver = requireActivity().contentResolver
        var bitmap = with(ImageDecoder.createSource(contentResolver, uri)) {
            ImageDecoder.decodeBitmap(this)
        }

        val w = bitmap.width
        val h = bitmap.height
        val max = if(w>h) w else h
        val scaleFactor = if(max > 1000){
            1000 / max.toFloat()
        }else 1f

        if (scaleFactor != 1f)
            bitmap = Bitmap.createScaledBitmap(
                bitmap, (w * scaleFactor).toInt(), (h * scaleFactor).toInt(), false
            )

        return bitmap
    }

    private fun setViewModel(){
        val isNewAndNotSaved = nViewModel.getNotePointed()!!.second.id == 0

        if(!isNewAndNotSaved) {
            val noteId = nViewModel.getNotePointed()!!.second.id
            CoroutineScope(Dispatchers.Main).launch {
                fViewModel.setImages(noteId)
                fViewModel.images!!.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    setPageIndicator(it.size)
                }
            }
        }else{
            adapter.submitList(fViewModel.imagesForNewNote.value)
        }
    }

    private fun setPageIndicator(size: Int){
        if(size == 0)
            fViewModel.pageIndicator.value = ""
        else {
            var indicator = ""
            val pos = vp_attached_images.currentItem
            for(i in 0 until size){
                indicator += if(i == pos) "●" else "○"
            }
            fViewModel.pageIndicator.value = indicator
        }
    }

    private fun setImageButton(){
        ib_attach_image.setOnClickListener{
            if(checkPermission())
                attachImage()
            else {
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, REQUEST_CODE_FETCH)
            }
        }
    }

    private fun attachImage(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun checkPermission(): Boolean{
        val parent = requireActivity()
        val denied = PackageManager.PERMISSION_DENIED
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (parent.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == denied
                || parent.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == denied)
                return false
        return true
    }

    private fun hideKeyboard() {
        requireActivity().currentFocus?.let{
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun setBackground(){
        colorSelected = fViewModel.colorSelected
        colorSelected.observe(viewLifecycleOwner){
            cv_edit_note.setCardBackgroundColor(Color.parseColor(it))
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
                    saveNote(GO_BACK)
                else {
                    if (isEmpty())
                        Toast.makeText(
                            context,
                            getString(R.string.cant_save_empty),
                            Toast.LENGTH_SHORT
                        ).show()
                    else
                        Toast.makeText(context, getString(R.string.not_edited), Toast.LENGTH_SHORT).show()
                    goBack()
                }
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

    private fun setIsNew(){
        isNew = EditNoteFragmentArgs.fromBundle(requireArguments()).isNew
    }

    private fun setNote(){
        setNoteText()
        setNoteImage()
    }

    private fun setNoteText(){

        if(!isNew) {
            val note = nViewModel.getNotePointed()!!.second
            et_title.setText(note.title)
            et_body.setText(note.body)
        }else
            nViewModel.setNotePointed(
                -1,
                Note(
                    0, //id == 0 if new note && not saved  //TODO("change to nullable on migration")
                    "",
                    "",
                    "",
                    "",
                    0,
                    colorResToStr(R.color.white),
                    null
                )
            )
    }

    private fun setNoteImage(){
        val isNewAndNotSaved = nViewModel.getNotePointed()!!.second.id == 0
        if(!isNewAndNotSaved){
            val noteId = nViewModel.getNotePointed()!!.second.id
            fViewModel.setImages(noteId)
            fViewModel.images!!.observe(viewLifecycleOwner){    //TODO("NPE - possibly?")
                submitToAdapter(it as MutableList<Image>)
            }
        }else{
            fViewModel.imagesForNewNote.observe(viewLifecycleOwner){
                submitToAdapter(it)
                setPageIndicator(it.size)
            }
        }
    }

    private fun submitToAdapter(images: MutableList<Image>){
        val newList = mutableListOf<Image>()
        images.forEach{
            newList.add(it)
        }
        adapter.submitList(newList)
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
                    saveNote(null)
                else
                    Toast.makeText(context, getString(R.string.not_edited), Toast.LENGTH_SHORT).show()
            }
            setOnLongClickListener{
                if(isEdited() && !isEmpty())
                    saveNote(GO_BACK)
                else {
                    if (isEmpty()) {
                        Toast.makeText(
                            context,
                            getString(R.string.cant_save_empty),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else
                        Toast.makeText(context, getString(R.string.not_edited), Toast.LENGTH_SHORT).show()
                    goBack()
                }
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
        fabLemon.show()
        fabMint.show()
        fabWhite.show()

        fabCoral.startAnimation(up)
        fabLemon.startAnimation(up)
        fabMint.startAnimation(up)
        fabWhite.startAnimation(up)
    }

    private fun closePalette(){
        fabColors.startAnimation(shrink)

        fabCoral.hide()
        fabLemon.hide()
        fabMint.hide()
        fabWhite.hide()

        fabCoral.startAnimation(down)
        fabLemon.startAnimation(down)
        fabMint.startAnimation(down)
        fabWhite.startAnimation(down)
    }

    private fun saveNote(action: Int?){
        val title = et_title.text.toString().trim()
        val body = et_body.text.toString().trim()
        val dateTime = getDateTime()

        val isNewAndNotSaved = nViewModel.getNotePointed()!!.second.id == 0

        if(isNewAndNotSaved) {
            val curNote = Note(0, title, body, dateTime, dateTime, 0, colorSelected.value!!)

            //fetch id immediately after saving new unsaved note
            var curIdLive: LiveData<Int>
            CoroutineScope(Dispatchers.IO).launch {
                nViewModel.addNote(curNote)
            }.invokeOnCompletion {
                curIdLive = nViewModel.getIdLastSaved()
                CoroutineScope(Dispatchers.Main).launch {
                    curIdLive.observe(viewLifecycleOwner) {//TODO("does NOT run when new note is LongClick saved")
                        curNote.id = it
                        saveNewImagesToDb(it)
                        if(action == GO_BACK)
                            goBack()
                    }
                }
            }
            nViewModel.setNotePointed(-1, curNote)  //pos == -1 ONLY when saving new note

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
            if(action == GO_BACK)
                goBack()
        }
    }

    /** call ONLY when saving unsaved new note**/
    private fun saveNewImagesToDb(noteId: Int){
        val newImages = fViewModel.imagesForNewNote.value!!
        if(newImages.isNotEmpty()){
            newImages.forEach{
                it.noteId = noteId
            }

            CoroutineScope(Dispatchers.IO).launch {
                fViewModel.addImages(newImages)
            }
            val path = requireActivity().filesDir.absolutePath+"/Pictures"
            val oldDir = File(path, "0")
            val newDir = File(path, noteId.toString())
            if(oldDir.renameTo(newDir))
                Log.d("", "SUCCESS: change dir from 0 to $noteId")
            else
                throw Exception("FAILED: change dir name from 0 to $noteId")

            CoroutineScope(Dispatchers.Main).launch {
                fViewModel.setImages(noteId)
                fViewModel.images!!.observe(viewLifecycleOwner){
                    submitToAdapter(it as MutableList<Image>)
                }
            }
            fViewModel.imagesForNewNote.removeObservers(viewLifecycleOwner)
        }
    }

    private fun isEmpty(): Boolean {
        val title = et_title.text.toString().trim()
        val body = et_body.text.toString().trim()
        return title.isBlank() && body.isBlank()
    }

    private fun setDialog(){
        warnDialog = MyDialog(
            requireContext(),
            R.layout.dialog,
            getString(R.string.warn_cancel_edit)
        ){
            goBack()
            warnDialog.dismiss()
        }

        deleteImageDialog = MyDialog(requireContext(), R.layout.dialog, "delete?"){
            deleteImageCallback()
            deleteImageDialog.dismiss()
        }
    }

    private fun deleteImageCallback(){
        val imagePointed = fViewModel.imagePointed!!
        val isNewAndNotSaved = imagePointed.noteId == 0

        if(isNewAndNotSaved){
            val newImagesLive = fViewModel.imagesForNewNote
            newImagesLive.value!!.remove(imagePointed)
            newImagesLive.value = newImagesLive.value!!
            setPageIndicator(newImagesLive.value!!.size)
        }else {
            fViewModel.deleteImage(imagePointed)
        }
        val fullDir = requireActivity().filesDir.absolutePath + "/Pictures/${imagePointed.noteId}"
        val file = File(fullDir, imagePointed.path)

        if(file.exists()){
            if(file.delete())
                Log.d("", "file deleted: ${imagePointed.path}")
            else
                Log.d("", "deletion failed: ${imagePointed.path}")
        }
    }

    private fun deleteNewImages(){
        val fullPath = requireActivity().filesDir.absolutePath+"/Pictures/0"
        fViewModel.imagesForNewNote.value!!.forEach{
            val fileName = it.path
            val file = File(fullPath, fileName)
            if(file.exists()){
                if(file.delete())
                    Log.d("", "file deleted: $fileName")
                else
                    Log.d("", "deletion failed: $fileName")
            }
        }
    }

    private fun getDateTime(): String{
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(date)
    }

    private fun goBack(){
        if(nViewModel.getNotePointed()!!.second.id == 0)
            deleteNewImages()

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

    private suspend fun toastNullUri(){
        withContext(Dispatchers.IO) {
            Toast.makeText(context, "Something went wrong - null uri :(", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun colorResToStr(colorRes: Int): String{
        return String.format(
            "#%08x",
            ContextCompat.getColor(requireContext(), colorRes) and 0xffffffff.toInt()
        )
    }

    //for debugging
//    private fun setFabTest(){
//        fab_test.setOnClickListener{
//            Log.d("", "note id: ${nViewModel.getNotePointed()!!.second.id}")
//            Log.d("", "fViewModel.images.value.size: ${fViewModel.images?.value?.size}")
//            Log.d(
//                "",
//                "fViewModel.imagesForNewNote.value.size: ${fViewModel.imagesForNewNote.value?.size}"
//            )
//            Log.d("", "adapter size: ${adapter.itemCount}")
//            Log.d("", "page selected: ${vp_attached_images.currentItem}")
//        }
//    }
}