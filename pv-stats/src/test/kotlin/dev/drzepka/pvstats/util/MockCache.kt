package dev.drzepka.pvstats.util

import javax.cache.Cache
import javax.cache.CacheManager
import javax.cache.configuration.CacheEntryListenerConfiguration
import javax.cache.configuration.Configuration
import javax.cache.integration.CompletionListener
import javax.cache.processor.EntryProcessor
import javax.cache.processor.EntryProcessorResult

class MockCache : Cache<Any, Any> {

    private val map = HashMap<Any, Any>()

    override fun clear() {
        map.clear()
    }

    override fun getName(): String = ""

    override fun getAll(keys: MutableSet<out Any>?): MutableMap<Any, Any> = map

    override fun getCacheManager(): CacheManager = throw NotImplementedException()

    override fun putAll(map: MutableMap<out Any, out Any>?) = this.map.putAll(map!!.toMap())

    override fun <T : Any?> unwrap(clazz: Class<T>?): T = throw NotImplementedException()

    override fun putIfAbsent(key: Any?, value: Any?): Boolean {
        return if (!map.contains(key)) {
            map[key!!] = value!!
            true
        } else false
    }

    override fun removeAll(keys: MutableSet<out Any>?) {
        keys?.forEach { map.remove(it) }
    }

    override fun removeAll() = clear()

    override fun <C : Configuration<Any, Any>?> getConfiguration(clazz: Class<C>?): C = throw NotImplementedException()

    override fun replace(key: Any?, oldValue: Any?, newValue: Any?): Boolean = throw NotImplementedException()

    override fun replace(key: Any?, value: Any?): Boolean = throw NotImplementedException()

    override fun iterator(): MutableIterator<Cache.Entry<Any, Any>> = throw NotImplementedException()

    override fun get(key: Any?): Any? = map[key!!]

    override fun containsKey(key: Any?): Boolean = map.containsKey(key!!)

    override fun close() = Unit

    override fun isClosed(): Boolean = false

    override fun <T : Any?> invoke(key: Any?, entryProcessor: EntryProcessor<Any, Any, T>?, vararg arguments: Any?): T = throw NotImplementedException()

    override fun put(key: Any?, value: Any?) {
        map[key!!] = value!!
    }

    override fun getAndRemove(key: Any?): Any? = throw NotImplementedException()

    override fun remove(key: Any?): Boolean {
        return map.remove(key!!) != null
    }

    override fun remove(key: Any?, oldValue: Any?): Boolean = throw NotImplementedException()

    override fun deregisterCacheEntryListener(cacheEntryListenerConfiguration: CacheEntryListenerConfiguration<Any, Any>?) = Unit

    override fun getAndReplace(key: Any?, value: Any?): Any? = throw NotImplementedException()

    override fun getAndPut(key: Any?, value: Any?): Any? = throw NotImplementedException()

    override fun loadAll(keys: MutableSet<out Any>?, replaceExistingValues: Boolean, completionListener: CompletionListener?) = Unit

    override fun registerCacheEntryListener(cacheEntryListenerConfiguration: CacheEntryListenerConfiguration<Any, Any>?) = Unit

    override fun <T : Any?> invokeAll(keys: MutableSet<out Any>?, entryProcessor: EntryProcessor<Any, Any, T>?, vararg arguments: Any?): MutableMap<Any, EntryProcessorResult<T>> = throw NotImplementedException()

    private class NotImplementedException : Exception("Not implemented")
}