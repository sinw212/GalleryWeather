package com.ww.galleryweather

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ww.galleryweather.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val requestGalleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data // 선택한 이미지의 주소
                if (uri != null) {
                    // 사진 가져오기
                    var inputStream = contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    // 사진의 회전 정보 가져오기
                    var exif: ExifInterface? = ExifInterface(inputStream!!)
                    inputStream!!.close()
//                    var orientation =
//                        when (exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)) {
//                            ExifInterface.ORIENTATION_ROTATE_90 -> 90
//                            ExifInterface.ORIENTATION_ROTATE_180 -> 180
//                            ExifInterface.ORIENTATION_ROTATE_270 -> 270
//                            else -> 0
//                        }
//                    // 이미지 회전하기
//                    val newBitmap = getRotatedBitmap(bitmap, orientation.toFloat())
//                    // 회전된 이미지로 imaView 설정
//                    binding.imgGallery.setImageBitmap(newBitmap)
                    binding.imgGallery.setImageBitmap(bitmap)

                    val dateTime = exif?.getAttribute(ExifInterface.TAG_DATETIME)
                    binding.txtDate.setText(dateTime)

                    if (dateTime != null) {
                        Log.d("진입exifDate", dateTime)
                    }

                } else {
                    Log.d("진입GalleryLauncher", "Bitmap null")
                    Toast.makeText(this, "오류가 발생하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d("진입GalleryLauncher", "RESULT_OK 오류")
            }
        }

        binding.imgGallery.setOnClickListener {
            val writePermission =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val readPermission =
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

            //권한 확인
            if (writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED) {
                //권한 요청
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), 1
                )
            } else {
                //권한이 있는 경우 갤러리 실행
                val intent = Intent(Intent.ACTION_PICK)
                //intent의 data와 type을 동시에 설정하는 메서드
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                requestGalleryLauncher.launch(intent)
            }
        }
    }

    // 이미지 회전하기
    @Throws(Exception::class)
    private fun getRotatedBitmap(bitmap: Bitmap?, degrees: Float): Bitmap? {
        if (bitmap == null) return null
        if (degrees == 0F) return bitmap
        val m = Matrix()
        m.setRotate(degrees, bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    }
}