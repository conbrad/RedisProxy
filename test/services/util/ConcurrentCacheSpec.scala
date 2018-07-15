package services.util

import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.scalatest.{MustMatchers, WordSpec}
import play.api.Configuration

class ConcurrentCacheSpec extends WordSpec with MustMatchers with MockitoSugar {

  class Fixture(val expiryTime: Long = 1000L) {
    val mockConfig: Configuration = mock[Configuration]
    when(mockConfig.get[Long]("cache.expiry_time")).thenReturn(expiryTime)
    when(mockConfig.get[Long]("cache.capacity")).thenReturn(10L)
  }

  "A Concurrent Cache" should {
    "initially have a size of zero and no nodes" in new Fixture {
      val concurrentCache = new ConcurrentCache(new LinkedList, mockConfig)
      concurrentCache.get("test") mustBe None
    }

    "retrieve a cached item" in new Fixture {
      val concurrentCache = new ConcurrentCache(new LinkedList, mockConfig)
      concurrentCache.add("testKey", "testValue")
      concurrentCache.get("testKey") mustBe Some("testValue")
    }

    "expire a cached item" in new Fixture {
      val concurrentCache = new ConcurrentCache(new LinkedList, mockConfig)
      concurrentCache.add("testKey", "testValue")
      // Sleep past expiry time
      Thread.sleep(mockConfig.get[Long]("cache.expiry_time") + 50L)
      concurrentCache.get("testKey") mustBe None
    }

    "evict LRU when adding an item at capacity" in new Fixture(600000L) {
      val concurrentCache = new ConcurrentCache(new LinkedList, mockConfig)
      for (i <- 1 to 10) {
        concurrentCache.add(s"key$i", s"val$i")
      }
      val tenthItem = concurrentCache.get("key10")
      tenthItem must not be None
      tenthItem.get mustBe "val10"

      // add an item that will exceed our capacity
      concurrentCache.add("key11", "val11")

      // initially added item that was never retrieved
      // is our LRU item, and should be gone
      concurrentCache.get("key1") mustBe None
    }

    "LRU sequence should move an item to the MRU position when it is accessed" in new Fixture(600000L) {
      val concurrentCache = new ConcurrentCache(new LinkedList, mockConfig)
      for (i <- 1 to 10) {
        concurrentCache.add(s"key$i", s"val$i")
      }
      concurrentCache.get("key1") must not be None
      concurrentCache.add("key11", "val11")
      concurrentCache.get("key2") mustBe None
      val firstVal = concurrentCache.get("key1")
      firstVal must not be None
      firstVal.get mustBe "val1"
    }
  }
}