package services.util

import javax.inject.Singleton

@Singleton
class LinkedList {
  private var head = new Node("")
  private var tail = head
  private var size = 0L
  head.prev = tail

  def add(value: String) = {
    val node = new Node(value)
    node.next = tail
    tail.prev = node
    tail = node
    size += 1
    node
  }

  // Splices out the node in O(1)
  def remove(node: Node): Unit = {
    if (node == head) {
      head = head.prev
      head.next = null
    } else if (node == tail) {
      tail = node.next
      tail.prev = null
    } else {
      val prev = node.prev
      val next = node.next
      prev.next = next
      next.prev = prev
    }
    size -= 1
  }

  def leastRecentlyUsed(): Node = {
    val lru = head
    head = head.prev
    head.next = null
    size -= 1
    lru
  }
}
