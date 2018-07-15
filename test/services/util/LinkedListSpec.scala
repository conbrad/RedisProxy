package services.util

import org.scalatest.{MustMatchers, WordSpec}


class LinkedListSpec extends WordSpec with MustMatchers {

  "A Linked List" should {
    "initially have a size of zero and no nodes" in {
      val linkedList = new LinkedList
      linkedList.size mustBe 0L
      linkedList.tail mustEqual linkedList.head
    }
    "produce None when leastRecentlyUsed is invoked on an empty list" in {
      val linkedList = new LinkedList
      linkedList.leastRecentlyUsed() mustBe None
    }
    "successfully add nodes" in {
      val linkedList = new LinkedList
      linkedList.addFirst("key1", "test1")
      linkedList.size mustBe 1L
      linkedList.allNodes mustBe Seq("test1")
      linkedList.addFirst("key2", "test2")
      linkedList.size mustBe 2L
      linkedList.allNodes mustBe Seq("test2", "test1")
      linkedList.addFirst("key3", "test3")
      linkedList.size mustBe 3L
      linkedList.allNodes mustBe Seq("test3", "test2", "test1")
    }

    "remove LRU node" in {
      val linkedList = new LinkedList
      linkedList.addFirst("key1", "test1")
      linkedList.addFirst("key2", "test2")
      linkedList.addFirst("key3", "test3")
      linkedList.leastRecentlyUsed()
      linkedList.allNodes mustBe Seq("test3", "test2")
    }

    "remove specified node" in {
      val linkedList = new LinkedList
      linkedList.addFirst("key1", "test1")
      val toRemove = linkedList.addFirst("key2", "test2")
      linkedList.addFirst("key3", "test3")
      linkedList.remove(toRemove)
      linkedList.allNodes mustBe Seq("test3", "test1")
    }

    "remove the initially added node via leastRecentlyUsed" in {
      val linkedList = new LinkedList
      linkedList.addFirst("key1", "test1")
      val node = linkedList.leastRecentlyUsed()
      node must not be None
      node.get.value mustBe "test1"
    }

    "remove the initially added node via remove" in {
      val linkedList = new LinkedList
      val node = linkedList.addFirst("key1", "test1")
      linkedList.size mustBe 1L
      linkedList.remove(node)
      linkedList.size mustBe 0L
      linkedList.leastRecentlyUsed() mustBe None
    }
  }

}
