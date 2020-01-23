package com.projecteugene.inmagine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private val images = arrayOf(R.drawable.image1, R.drawable.image2, R.drawable.image3,
        R.drawable.image4, R.drawable.image5, R.drawable.image6, R.drawable.image7, R.drawable.image8)

    private val filterList: MutableList<GPUImageFilter> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        iv_canvas.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        iv_canvas.setImageBitmap(canvasFx())

        btn_build.setOnClickListener {
            val amountInRow = et_amount_x.text.toString().toIntOrNull()
            val amountInColumn = et_amount_y.text.toString().toIntOrNull()
            iv_canvas.setImageBitmap(canvasFx(amountInRow, amountInColumn))
        }

        btn_draw.setOnClickListener {
            val amountInRow = et_amount_x.text.toString().toIntOrNull()
            val amountInColumn = et_amount_y.text.toString().toIntOrNull()
            filterList.clear()
            cg_filters.checkedChipIds.forEach { id -> addFilter(filterList, id) }
            iv_canvas.setImageBitmap(canvasFx(amountInRow, amountInColumn, true, filterList))
        }
    }

    /**
     * Function that will draw a square grid based on the amount, images if possible and apply filters to images.
     * and returns as a bitmap
     *
     * @param amountX        Amount of squares in a row
     * @param amountY        Amount of squares in a column
     * @param drawImages    Boolean to draw images if true
     * @param list          Filters to apply to images
     */
    private fun canvasFx(amountX: Int? = 2, amountY: Int? = 2, drawImages: Boolean = false, list: MutableList<GPUImageFilter> = ArrayList()): Bitmap {
        val actualAmountX = amountX ?: 2
        val actualAmountY = amountY ?: 2
        val fillPaint = PaintUtils.fillPaint(Color.WHITE)
        val strokePaint = PaintUtils.strokePaint(Color.BLACK)
        val imagePaint = PaintUtils.imagePaint()

        val canvasSize = 800
        val margin = 50

        val bitmap = Bitmap.createBitmap(canvasSize, canvasSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawRGB(218, 218, 218)

        if (actualAmountX > 0 && actualAmountY > 0) {
            // Square size is canvas minus all the margins (amount + 1) divided by the amount
            val squareSizeX = (canvasSize - ((actualAmountX + 1) * margin)) / actualAmountX
            val squareSizeY = (canvasSize - ((actualAmountY + 1) * margin)) / actualAmountY
            if (squareSizeX > 0 && squareSizeY > 0) {
                for (x in 0 until actualAmountX) {          // x
                    for (y in 0 until actualAmountY) {      // y
                        canvas.save()
                        val totalMarginSizeX = margin * (x + 1)
                        val totalMarginSizeY = margin * (y + 1)
                        val top = totalMarginSizeY.toFloat() + (squareSizeY * y)
                        val left = totalMarginSizeX.toFloat() + (squareSizeX * x)
                        val origin = 0f
                        canvas.translate(left, top)
                        canvas.drawRect(origin, origin, squareSizeX.toFloat(), squareSizeY.toFloat(), fillPaint)
                        canvas.drawRect(origin, origin, squareSizeX.toFloat(), squareSizeY.toFloat(), strokePaint)

                        if (drawImages) {
                            val position = (y + (actualAmountY * x)) % images.size
                            val imageCropped = createThumbnail(images[position], squareSizeX, squareSizeY, list)
                            canvas.drawBitmap(imageCropped, origin, origin, imagePaint)
                        }
                        canvas.restore()
                    }
                }
            } else {
                Toast.makeText(this, "Unable to draw that amount of squares in a row.", Toast.LENGTH_SHORT).show()
            }

        }
        return bitmap
    }

    /**
     * Function that creates thumbnail of an image given the resource and apply filters to the image
     * and returns as a bitmap
     *
     * @param res           Image resource id
     * @param list          Filters to apply to images
     */
    private fun createThumbnail(res: Int, sizeX: Int, sizeY: Int, list: MutableList<GPUImageFilter>): Bitmap {
        val image = BitmapUtils.decodeSampledBitmapFromResource(resources, res, sizeX, sizeY)
        val scale: Float =
            if (image.width < image.height) {
                sizeX / image.width.toFloat()
            } else {
                sizeY / image.height.toFloat()
            }

        val scaledWidth = (image.width * scale).roundToInt()
        val scaledHeight = (image.height * scale).roundToInt()

        val scaled = Bitmap.createScaledBitmap(image, scaledWidth, scaledHeight, true)
        val croppedX = (scaledWidth - sizeX) / 2
        val croppedY = (scaledHeight - sizeY) / 2
        val cropped = Bitmap.createBitmap(scaled, croppedX, croppedY, sizeX, sizeY)

        return applyFilter(cropped, list)
    }

    /**
     * Function that apply the filters to a bitmap and returns the bitmap
     *
     * @param bitmap        Bitmap to apply filter too
     * @param list          Filters to apply
     */

    private fun applyFilter(bitmap: Bitmap, list: MutableList<GPUImageFilter>): Bitmap {
        if (list.isEmpty()) return bitmap
        val gpuImage = GPUImage(this)
        gpuImage.setFilter(GPUImageFilterGroup(list))
        gpuImage.setImage(bitmap)
        return gpuImage.bitmapWithFilterApplied
    }

    /**
     * Function that add filters to the list based on the id of the checked view
     *
     * @param list          List of filters
     * @param checkedId     Id of the view that is checked
     */

    private fun addFilter(list: MutableList<GPUImageFilter>, checkedId: Int) {
        when(checkedId) {
            R.id.chip_monochrome -> list.add(GPUImageMonochromeFilter())
            R.id.chip_pixellate -> list.add(GPUImagePixelationFilter().apply { setPixel(10.0f) })
            R.id.chip_haze -> list.add(GPUImageHazeFilter().apply { setSlope(0.3f) })
            R.id.chip_sepia -> list.add(GPUImageSepiaToneFilter())
            R.id.chip_crosshatch -> list.add(GPUImageCrosshatchFilter())
            R.id.chip_inversion -> list.add(GPUImageColorInvertFilter())
            R.id.chip_sketch -> list.add(GPUImageSketchFilter())
            R.id.chip_toon -> list.add(GPUImageToonFilter())
        }
    }

}
