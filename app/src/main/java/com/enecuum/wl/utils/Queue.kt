package com.enecuum.wl.utils

class Queue(list: MutableList<Any>) {

    var items: MutableList<Any> = list

    fun isEmpty(): Boolean = items.isEmpty()

    fun size(): Int = items.count()

    override fun toString() = items.toString()

    fun enqueue(element: Any) {
        items.add(element)
    }

    fun dequeue(): String {
        return try {
            if (items.isEmpty()) {
                ""
            } else {
                items.removeAt(0).toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun peek(): Any? {
        return items[0]
    }
}