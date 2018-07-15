package services.util

import javax.inject.Singleton

import scala.collection.mutable.ArrayBuffer

@Singleton
class LinkedList {
  var head: Node = null
  var tail: Node = null
  private var count = 0L

  def addFirst(key: String, value: String): Node = {
    val node = new Node(key, value)
    if (head == null) {
      head = node
      tail = head
    } else {
      node.next = tail
      tail.prev = node
      tail = node
    }
    count += 1
    node
  }

  // Splices out the node in O(1)
  def remove(node: Node): Unit = {
    if (node == head) {
      if(count == 1L) {
        head = null
        tail = null
      } else {
        head = head.prev
        head.next = null
      }
    } else if (node == tail) {
      tail = node.next
      tail.prev = null
    } else {
      val prev = node.prev
      val next = node.next
      prev.next = next
      next.prev = prev
    }
    count -= 1
  }

  def leastRecentlyUsed(): Option[Node] = {
    if (count == 0L) {
      None
    } else if (count == 1L) {
      val lru = head
      head = null
      tail = null
      count -= 1
      Some(lru)
    } else {
      val lru = head
      head = head.prev
      head.next = null
      count -= 1
      Some(lru)
    }
  }

  def allNodes: Seq[String] = {
    var current = tail
    var nodes = ArrayBuffer[String]()
    while (current != null) {
      nodes += current.value
      current = current.next
    }
    nodes
  }

  def size: Long = count

}
