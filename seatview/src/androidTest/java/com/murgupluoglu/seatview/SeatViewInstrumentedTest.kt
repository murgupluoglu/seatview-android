package com.murgupluoglu.seatview

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Parcelable
import android.view.View
import androidx.core.os.ParcelCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.customview.widget.ExploreByTouchHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

/**
 * On-device coverage for behavior that cannot be verified on the JVM: real `Canvas`
 * drawing, instance-state save/restore across a configuration change, and the
 * `ExploreByTouchHelper` accessibility node tree.
 */
@RunWith(AndroidJUnit4::class)
class SeatViewInstrumentedTest {

    private class Seat3(
        private val seatId: String,
        private val connected: Array<String> = emptyArray(),
        private val preSelected: Boolean = false,
    ) : Seat {
        override fun id(): String = seatId
        override fun name(): String = seatId
        override fun allConnectedSeatIds(): Array<String> = connected
        override fun isPreSelected(): Boolean = preSelected
    }

    private fun grid(cols: Int = 6, rows: Int = 4) =
        Array(rows) { y -> Array(cols) { x -> Seat3("$x-$y") } }

    /** Runs [block] on the main thread and returns its result. */
    private fun <T> onMain(block: () -> T): T {
        var result: T? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync { result = block() }
        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    private fun newView(): SeatView<Seat3> {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val view = SeatView<Seat3>(context)
        view.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, 1080, 1920)
        return view
    }

    @Test
    fun drawsOnRealCanvasWithoutCrashing() {
        onMain {
            val view = newView()
            view.initSeatView(grid(12, 20))
            val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            assertThat(view.columnCount).isEqualTo(12)
            assertThat(view.rowCount).isEqualTo(20)
        }
    }

    @Test
    fun instanceState_restoresSelectionWhenRestoredAfterInit() {
        onMain {
            val saved: Parcelable = newView().let { source ->
                source.initSeatView(grid())
                source.selectSeat("2-1")
                source.selectSeat("3-3")
                assertThat(source.selectedSeatIds).containsExactly("2-1", "3-3")
                source.onSaveInstanceState()!!
            }

            // Restore order A: grid first, then state (state must win immediately).
            val target = newView()
            target.initSeatView(grid())
            assertThat(target.selectedSeatIds).isEmpty()

            target.onRestoreInstanceState(saved)

            assertThat(target.selectedSeatIds).containsExactly("2-1", "3-3")
        }
    }

    @Test
    fun instanceState_restoresSelectionWhenRestoredBeforeInit() {
        onMain {
            val saved: Parcelable = newView().let { source ->
                source.initSeatView(grid())
                source.selectSeat("0-0")
                source.onSaveInstanceState()!!
            }

            // Restore order B: state arrives before the grid is supplied; the pending
            // selection must be applied once initSeatView() runs, overriding pre-selection.
            val target = newView()
            target.onRestoreInstanceState(saved)
            target.initSeatView(grid())

            assertThat(target.selectedSeatIds).containsExactly("0-0")
        }
    }

    /**
     * The framework marshals view state through a [android.os.Parcel] on a real
     * configuration change, which resolves `CREATOR` reflectively. Passing the
     * Parcelable straight from save to restore (as the tests above do) would not
     * exercise that, so this test does a genuine parcel round trip.
     */
    @Test
    fun instanceState_survivesRealParcelRoundTrip() {
        onMain {
            val saved: Parcelable = newView().let { source ->
                source.initSeatView(grid())
                source.selectSeat("2-1")
                source.selectSeat("4-2")
                source.onSaveInstanceState()!!
            }

            val parcel = android.os.Parcel.obtain()
            val restored: Parcelable = try {
                parcel.writeParcelable(saved, 0)
                parcel.setDataPosition(0)
                ParcelCompat.readParcelable(
                    parcel,
                    SeatView::class.java.classLoader,
                    Parcelable::class.java,
                )!!
            } finally {
                parcel.recycle()
            }

            val target = newView()
            target.initSeatView(grid())
            target.onRestoreInstanceState(restored)

            assertThat(target.selectedSeatIds).containsExactly("2-1", "4-2")
        }
    }

    @Test
    fun instanceState_restoredSelectionOverridesPreSelectedSeats() {
        onMain {
            val saved: Parcelable = newView().let { source ->
                source.initSeatView(grid())
                source.selectSeat("1-1")
                source.onSaveInstanceState()!!
            }

            // A grid whose "5-3" is pre-selected: after restore only the saved id survives.
            val preSelectedGrid = Array(4) { y ->
                Array(6) { x -> Seat3("$x-$y", preSelected = (x == 5 && y == 3)) }
            }
            val target = newView()
            target.onRestoreInstanceState(saved)
            target.initSeatView(preSelectedGrid)

            assertThat(target.selectedSeatIds).containsExactly("1-1")
        }
    }

    @Test
    fun accessibility_exposesOneVirtualNodePerVisibleSeat() {
        onMain {
            val view = newView()
            view.initSeatView(grid(cols = 5, rows = 3))

            val provider = ViewCompat.getAccessibilityDelegate(view)
                ?.getAccessibilityNodeProvider(view)
            assertThat(provider).isNotNull()

            val root = provider!!.createAccessibilityNodeInfo(ExploreByTouchHelper.HOST_ID)
            assertThat(root).isNotNull()
            assertThat(root!!.childCount).isEqualTo(15) // 5 columns x 3 rows, all visible
        }
    }

    @Test
    fun accessibility_nodeCarriesSeatNameAndCheckedState() {
        onMain {
            val view = newView()
            view.initSeatView(grid(cols = 4, rows = 2))
            val position = view.positionOf("2-1")!!
            val virtualId = view.virtualIdOf(position)

            val provider = ViewCompat.getAccessibilityDelegate(view)!!
                .getAccessibilityNodeProvider(view)!!

            val before = provider.createAccessibilityNodeInfo(virtualId)!!
            assertThat(before.contentDescription.toString()).isEqualTo("2-1")
            assertThat(before.isCheckable).isTrue()
            assertThat(before.isChecked).isFalse()

            view.selectSeat(position)

            val after = provider.createAccessibilityNodeInfo(virtualId)!!
            assertThat(after.isChecked).isTrue()
        }
    }

    @Test
    fun accessibility_clickActionTogglesSeat() {
        onMain {
            val view = newView()
            view.initSeatView(grid(cols = 4, rows = 2))
            val position = view.positionOf("1-0")!!
            val virtualId = view.virtualIdOf(position)

            val provider = ViewCompat.getAccessibilityDelegate(view)!!
                .getAccessibilityNodeProvider(view)!!

            // Click selects...
            assertThat(
                provider.performAction(virtualId, AccessibilityNodeInfoCompat.ACTION_CLICK, null)
            ).isTrue()
            assertThat(view.isSelected("1-0")).isTrue()

            // ...and clicking again releases.
            provider.performAction(virtualId, AccessibilityNodeInfoCompat.ACTION_CLICK, null)
            assertThat(view.isSelected("1-0")).isFalse()
        }
    }

    /**
     * Selecting one half of a multi-seat flips the partner's checked state too, so the
     * partner's virtual node must report it (its node is invalidated alongside).
     */
    @Test
    fun accessibility_connectedSeatNodeReflectsGroupSelection() {
        onMain {
            val couch = arrayOf("0-0", "1-0")
            val gridWithCouch = Array(2) { y ->
                Array(4) { x ->
                    if (y == 0 && x <= 1) Seat3("$x-$y", connected = couch) else Seat3("$x-$y")
                }
            }
            val view = newView()
            view.initSeatView(gridWithCouch)

            val partner = view.positionOf("1-0")!!
            val provider = ViewCompat.getAccessibilityDelegate(view)!!
                .getAccessibilityNodeProvider(view)!!
            val partnerId = view.virtualIdOf(partner)

            assertThat(provider.createAccessibilityNodeInfo(partnerId)!!.isChecked).isFalse()

            view.selectSeat("0-0") // selects the whole couch

            assertThat(view.isSelected("1-0")).isTrue()
            assertThat(provider.createAccessibilityNodeInfo(partnerId)!!.isChecked).isTrue()
        }
    }

    @Test
    fun virtualIdRoundTripsThroughPosition() {
        onMain {
            val view = newView()
            view.initSeatView(grid(cols = 7, rows = 5))

            for (row in 0 until view.rowCount) {
                for (column in 0 until view.columnCount) {
                    val position = SeatPosition(row, column)
                    assertThat(view.positionForVirtualId(view.virtualIdOf(position)))
                        .isEqualTo(position)
                }
            }
        }
    }
}
