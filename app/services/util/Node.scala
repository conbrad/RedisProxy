package services.util

class Node(lookupValue: String) {
  var next: Node = null
  var prev: Node = null
  var value: String = lookupValue
}
