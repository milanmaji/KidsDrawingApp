package com.sitapuramargram.kidsdrawingapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import com.sitapuramargram.kidsdrawingapp.databinding.ActivityMainBinding
import com.sitapuramargram.kidsdrawingapp.databinding.DialogBrushSizeBinding
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var activityMainBinding: ActivityMainBinding
    private var mImageButtonCurrentpaint: ImageButton? = null
    private var resultLauncher: ActivityResultLauncher<Intent>? = null
    private lateinit var customProgressDialog: CustomProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  setContentView(R.layout.activity_main)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        var view: View = activityMainBinding.root
        setContentView(view)

        activityMainBinding.drawingView.setSizeForBrush(5f)
        mImageButtonCurrentpaint = activityMainBinding.llColorPaint[1] as ImageButton
        mImageButtonCurrentpaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
        )


        activityMainBinding.ibBlack.setOnClickListener(this)
        activityMainBinding.ibBlue.setOnClickListener(this)
        activityMainBinding.ibGreen.setOnClickListener(this)
        activityMainBinding.ibLollipop.setOnClickListener(this)
        activityMainBinding.ibRandom.setOnClickListener(this)
        activityMainBinding.ibRed.setOnClickListener(this)
        activityMainBinding.ibSkin.setOnClickListener(this)
        activityMainBinding.ibWhite.setOnClickListener(this)
        activityMainBinding.ibYellow.setOnClickListener(this)
        activityMainBinding.ibGallery.setOnClickListener(this)
        activityMainBinding.ibUndo.setOnClickListener(this)
        activityMainBinding.ibBrush.setOnClickListener(this)
        activityMainBinding.ibSave.setOnClickListener(this)

        customProgressDialog = CustomProgressDialog(this)
        customProgressDialog.message = "Please Wait..."



        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                loadImage(data)
            }
        }


    }

    private fun loadImage(data: Intent?) {
        try {

            if (data!!.data != null) {
                activityMainBinding.ibBackground.visibility = View.VISIBLE
                activityMainBinding.ibBackground.setImageURI(data.data)
            } else {
                Toast.makeText(this, "Error in parsing the image or its corrupted.", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()

        }
    }

    private fun showBrushSizeChooserDialog() {

        val brushDialog = Dialog(this)
        val dialogBrushSizeBinding = DialogBrushSizeBinding.inflate(layoutInflater)
        brushDialog.setContentView(dialogBrushSizeBinding.root)
        brushDialog.setTitle("Brush size:")
        val smallBtn = dialogBrushSizeBinding.ibSmallBrush
        val mediumBtn = dialogBrushSizeBinding.ibMediumBrush
        val largeBtn = dialogBrushSizeBinding.ibLargeBrush
        smallBtn.setOnClickListener {
            activityMainBinding.drawingView.setSizeForBrush(5f)
            brushDialog.dismiss();
        }
        mediumBtn.setOnClickListener {
            activityMainBinding.drawingView.setSizeForBrush(10f)
            brushDialog.dismiss();
        }
        largeBtn.setOnClickListener {
            activityMainBinding.drawingView.setSizeForBrush(15f)
            brushDialog.dismiss();
        }

        brushDialog.show()

    }

    private fun paintClicked(view: View) {
        if (view != mImageButtonCurrentpaint) {
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            activityMainBinding.drawingView.setColor(colorTag)
            imageButton.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
            )
            mImageButtonCurrentpaint!!.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.pallet_normal)
            )
            mImageButtonCurrentpaint = view
        }

    }

    override fun onClick(view: View?) {

        when (view?.id) {

            R.id.ib_black,
            R.id.ib_blue,
            R.id.ib_green,
            R.id.ib_lollipop,
            R.id.ib_random,
            R.id.ib_red,
            R.id.ib_skin,
            R.id.ib_white,
            R.id.ib_yellow -> paintClicked(view)
            R.id.ib_brush -> showBrushSizeChooserDialog()
            R.id.ib_gallery -> {
                if (checkPermission()) {
                    //access storage
                    openSomeActivityForResult()
                } else {
                    requestPermission(STORAGE_READ_PERMISSION_CODE)
                }
            }
            R.id.ib_undo -> activityMainBinding.drawingView.onClickUndo()
            R.id.ib_save -> {
               saveImageButtonClicked()
            }

        }
    }

    private fun checkPermission(): Boolean {

        return (
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        &&
                        ContextCompat.checkSelfPermission(this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                )


    }

    private fun requestPermission(requestCode: Int) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())) {
            Toast.makeText(this, "Need permission to add a background", Toast.LENGTH_LONG).show()
        }
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), requestCode)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_READ_PERMISSION_CODE) {

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //access storage
                openSomeActivityForResult()
            } else {
                Toast.makeText(this,
                        "Oops you just denied the permission for Storage. You can also allow it from settings",
                        Toast.LENGTH_LONG
                ).show()
            }
        } else if (requestCode == STORAGE_WRITE_PERMISSION_CODE) {

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //access storage
                saveImageButtonClicked()
            } else {
                Toast.makeText(this,
                        "Oops you just denied the permission for Storage. You can also allow it from settings",
                        Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    private fun openSomeActivityForResult() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher!!.launch(intent)
    }

     private fun getBitmapFromView(view: View) : Bitmap{

        val returnBitmap = Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnBitmap)
        val bgDrawable = view.background
        if(bgDrawable != null){
            bgDrawable.draw(canvas)
        }else{
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)

        return returnBitmap


    }



    private  fun saveBitmap(mBitmap: Bitmap?): String {

        var savedImagePath = ""
        if(mBitmap !=null){

                val imageFileName = "KidsDrawingApp_"+System.currentTimeMillis()/1000+".png"
                val storageDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/KidsDrawingApp")
                var success = true
                if(!storageDir.exists()){
                    success = storageDir.mkdirs()
                }
                if(success){

                    val imageFile = File(storageDir,imageFileName)
                    savedImagePath = imageFile.absolutePath

                    try{
                        val fos = FileOutputStream(imageFile)
                        mBitmap.compress(Bitmap.CompressFormat.PNG,90,fos)
                        fos.close()
                    }catch (e : Exception){
                        savedImagePath = ""
                        e.printStackTrace()
                    }
                }

        }
        return  savedImagePath


    }

    private fun saveImageButtonClicked(){
        if(checkPermission()){
            //save image into storage
            customProgressDialog.show()
            lifecycleScope.launch {
                val result = async {
                    saveBitmap(getBitmapFromView(activityMainBinding.flDrawingViewContainer))
                }
                if(result.await().isNotEmpty()){
                    customProgressDialog.dismiss()
                    Toast.makeText(this@MainActivity,"File saved successfully: ${result.await()}", Toast.LENGTH_LONG).show()
                }
                else{
                    customProgressDialog.dismiss()
                    Toast.makeText(this@MainActivity,"Something went wrong while saving the file.", Toast.LENGTH_LONG).show()
                }
                MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result.await()),null){
                    path,uri -> val shareIntent = Intent()
                    shareIntent.action = Intent.ACTION_SEND
                    shareIntent.putExtra(Intent.EXTRA_STREAM,uri)
                    shareIntent.type = "image/png"
                    startActivity(Intent.createChooser(shareIntent,"Share"))
                }
            }


        }else{
            requestPermission(STORAGE_WRITE_PERMISSION_CODE)
        }
    }




    companion object {
        private const val STORAGE_READ_PERMISSION_CODE = 100
        private const val STORAGE_WRITE_PERMISSION_CODE = 200
    }
}