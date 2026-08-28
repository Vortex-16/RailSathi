package com.example

import com.example.data.model.RegionalSnacksCatalog
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SnackCatalogTest {

  @Test
  fun `verify regional snacks catalog has real items with imagery and pricing`() {
    val items = RegionalSnacksCatalog.items
    assertFalse("Snacks catalog should not be empty", items.isEmpty())

    val chai = items.find { it.id == "masala_chai" }
    assertNotNull("Masala chai should be in catalog", chai)
    assertTrue("Typical price should be positive", chai!!.typicalPriceInr > 0)
    assertNotNull("Masala chai should have image url", chai.imageUrl)

    val jhalmuri = items.find { it.id == "jhalmuri_kol" }
    assertNotNull("Jhalmuri should be in catalog", jhalmuri)
    assertTrue("Jhalmuri should have dietary tags", jhalmuri!!.dietaryTags.isNotEmpty())
  }
}

