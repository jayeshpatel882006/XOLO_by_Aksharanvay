package com.aksharanvay.xolo.data

class InMemoryStore(
    private val strings: MutableMap<String, String> = mutableMapOf(),
    private val booleans: MutableMap<String, Boolean> = mutableMapOf(),
    private val ints: MutableMap<String, Int> = mutableMapOf()
) : KeyValueStore {
    override fun getString(key: String, defaultValue: String?): String? =
        strings[key] ?: defaultValue

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        booleans[key] ?: defaultValue

    override fun getInt(key: String, defaultValue: Int): Int = ints[key] ?: defaultValue

    override fun putString(key: String, value: String) {
        strings[key] = value
    }

    override fun putBoolean(key: String, value: Boolean) {
        booleans[key] = value
    }

    override fun putInt(key: String, value: Int) {
        ints[key] = value
    }
}
