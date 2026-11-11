package com.backlogorder.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class InteractiveCropDialogTest {

    @Test
    fun performBitmapCrop_scaleOneNoOffset_cropsCenterSquare() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
        bitmap.setPixel(500, 500, Color.GREEN)

        val resultUriString = performBitmapCrop(
            context = context,
            bitmap = bitmap,
            scale = 1f,
            offsetX = 0f,
            offsetY = 0f,
            squareSizePx = 500f,
        )

        assertNotNull(resultUriString)
        val file = File(Uri.parse(resultUriString!!).path!!)
        val croppedBitmap = BitmapFactory.decodeFile(file.absolutePath)

        assertEquals(512, croppedBitmap.width)
        assertEquals(512, croppedBitmap.height)
    }

    @Test
    fun performBitmapCrop_scaledAndPanned_cropsCorrectRegion() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // 1000x1000 bitmap where left half is RED and right half is BLUE
        val bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
        for (x in 0 until 500) {
            for (y in 0 until 1000) {
                bitmap.setPixel(x, y, Color.RED)
            }
        }
        for (x in 500 until 1000) {
            for (y in 0 until 1000) {
                bitmap.setPixel(x, y, Color.BLUE)
            }
        }

        val squareSize = 500f
        val scale = 2f
        // maxOffset at 2x scale is (500 * (2 - 1)) / 2 = 250f
        // Dragging image right (offsetX = +250f) moves image to the right, showing the LEFT side of the bitmap (RED)
        val resultUriLeft = performBitmapCrop(
            context = context,
            bitmap = bitmap,
            scale = scale,
            offsetX = 250f,
            offsetY = 0f,
            squareSizePx = squareSize,
        )
        assertNotNull(resultUriLeft)
        val fileLeft = File(Uri.parse(resultUriLeft!!).path!!)
        val bitmapLeft = BitmapFactory.decodeFile(fileLeft.absolutePath)
        val pixelLeft = bitmapLeft.getPixel(256, 256)
        assertTrue("Expected RED component to be dominant in left region, got R=${Color.red(pixelLeft)} B=${Color.blue(pixelLeft)}", Color.red(pixelLeft) > 200 && Color.blue(pixelLeft) < 50)

        // Dragging image left (offsetX = -250f) moves image to the left, showing the RIGHT side of the bitmap (BLUE)
        val resultUriRight = performBitmapCrop(
            context = context,
            bitmap = bitmap,
            scale = scale,
            offsetX = -250f,
            offsetY = 0f,
            squareSizePx = squareSize,
        )
        assertNotNull(resultUriRight)
        val fileRight = File(Uri.parse(resultUriRight!!).path!!)
        val bitmapRight = BitmapFactory.decodeFile(fileRight.absolutePath)
        val pixelRight = bitmapRight.getPixel(256, 256)
        assertTrue("Expected BLUE component to be dominant in right region, got B=${Color.blue(pixelRight)} R=${Color.red(pixelRight)}", Color.blue(pixelRight) > 200 && Color.red(pixelRight) < 50)
    }

    @Test
    fun performBitmapCrop_landscapeImageScaleOne_pannedLeftAndRight_cropsOuterEdges() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // 2000x1000 bitmap: 0..1000 is RED, 1000..2000 is BLUE
        val bitmap = Bitmap.createBitmap(2000, 1000, Bitmap.Config.ARGB_8888)
        for (x in 0 until 1000) {
            for (y in 0 until 1000) {
                bitmap.setPixel(x, y, Color.RED)
            }
        }
        for (x in 1000 until 2000) {
            for (y in 0 until 1000) {
                bitmap.setPixel(x, y, Color.BLUE)
            }
        }

        val squareSize = 500f
        val scale = 1f
        // maxOffsetX for 2000x1000 at scale 1 = (500 / 2) * (2000 * 1 / 1000 - 1) = 250f

        // Panned right (offsetX = +250f) -> selects the LEFT region (RED)
        val resultLeft = performBitmapCrop(
            context = context,
            bitmap = bitmap,
            scale = scale,
            offsetX = 250f,
            offsetY = 0f,
            squareSizePx = squareSize,
        )
        assertNotNull(resultLeft)
        val fileLeft = File(Uri.parse(resultLeft!!).path!!)
        val bitmapLeft = BitmapFactory.decodeFile(fileLeft.absolutePath)
        val pixelLeft = bitmapLeft.getPixel(256, 256)
        assertTrue("Expected RED for left section of landscape image, got R=${Color.red(pixelLeft)}", Color.red(pixelLeft) > 200 && Color.blue(pixelLeft) < 50)

        // Panned left (offsetX = -250f) -> selects the RIGHT region (BLUE)
        val resultRight = performBitmapCrop(
            context = context,
            bitmap = bitmap,
            scale = scale,
            offsetX = -250f,
            offsetY = 0f,
            squareSizePx = squareSize,
        )
        assertNotNull(resultRight)
        val fileRight = File(Uri.parse(resultRight!!).path!!)
        val bitmapRight = BitmapFactory.decodeFile(fileRight.absolutePath)
        val pixelRight = bitmapRight.getPixel(256, 256)
        assertTrue("Expected BLUE for right section of landscape image, got B=${Color.blue(pixelRight)}", Color.blue(pixelRight) > 200 && Color.red(pixelRight) < 50)
    }

    @Test
    fun performBitmapCrop_portraitImageScaleOne_pannedTopAndBottom_cropsOuterEdges() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // 1000x2000 bitmap: y 0..1000 is GREEN, y 1000..2000 is YELLOW
        val bitmap = Bitmap.createBitmap(1000, 2000, Bitmap.Config.ARGB_8888)
        for (x in 0 until 1000) {
            for (y in 0 until 1000) {
                bitmap.setPixel(x, y, Color.GREEN)
            }
            for (y in 1000 until 2000) {
                bitmap.setPixel(x, y, Color.YELLOW)
            }
        }

        val squareSize = 500f
        val scale = 1f
        // maxOffsetY for 1000x2000 at scale 1 = (500 / 2) * (2000 * 1 / 1000 - 1) = 250f

        // Panned down (offsetY = +250f) -> selects TOP region (GREEN)
        val resultTop = performBitmapCrop(
            context = context,
            bitmap = bitmap,
            scale = scale,
            offsetX = 0f,
            offsetY = 250f,
            squareSizePx = squareSize,
        )
        assertNotNull(resultTop)
        val fileTop = File(Uri.parse(resultTop!!).path!!)
        val bitmapTop = BitmapFactory.decodeFile(fileTop.absolutePath)
        val pixelTop = bitmapTop.getPixel(256, 256)
        assertTrue("Expected GREEN for top section of portrait image, got G=${Color.green(pixelTop)}", Color.green(pixelTop) > 200 && Color.red(pixelTop) < 50)

        // Panned up (offsetY = -250f) -> selects BOTTOM region (YELLOW: RED > 200 & GREEN > 200)
        val resultBottom = performBitmapCrop(
            context = context,
            bitmap = bitmap,
            scale = scale,
            offsetX = 0f,
            offsetY = -250f,
            squareSizePx = squareSize,
        )
        assertNotNull(resultBottom)
        val fileBottom = File(Uri.parse(resultBottom!!).path!!)
        val bitmapBottom = BitmapFactory.decodeFile(fileBottom.absolutePath)
        val pixelBottom = bitmapBottom.getPixel(256, 256)
        assertTrue("Expected YELLOW for bottom section of portrait image, got R=${Color.red(pixelBottom)} G=${Color.green(pixelBottom)}", Color.red(pixelBottom) > 200 && Color.green(pixelBottom) > 200)
    }
}
